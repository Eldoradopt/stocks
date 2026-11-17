package com.example.stocks.data.repository

import com.example.stocks.data.local.datastore.UserPreferencesRepository
import com.example.stocks.data.local.datastore.UserSettings
import com.example.stocks.data.remote.hypixel.HypixelApiService
import com.example.stocks.data.remote.mojang.MojangApiService
import com.example.stocks.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountRepository(
    private val mojangApi: MojangApiService,
    private val hypixelApi: HypixelApiService,
    private val userPrefs: UserPreferencesRepository,
    private val bazaarRepo: BazaarRepository,
) {
    val userSettings: Flow<UserSettings> = userPrefs.userSettingsFlow

    private val _coopProfiles = MutableStateFlow<List<CoopProfile>>(emptyList())
    val coopProfiles = _coopProfiles.asStateFlow()

    private val _currentSacksBreakdown = MutableStateFlow<SacksBreakdown?>(null)
    val currentSacksBreakdown = _currentSacksBreakdown.asStateFlow()

    suspend fun linkAccount(username: String, apiKey: String = ""): Result<Unit> = runCatching {
        // 1. Resolve username to UUID
        val mojangProfile = try {
            mojangApi.getProfile(username.trim())
        } catch (e: Exception) {
            throw Exception("Player '$username' not found. Please check the username spelling.")
        }
        val uuid = mojangProfile.id
        val properName = mojangProfile.name
        val key = apiKey.trim().ifBlank { null }

        userPrefs.saveLinkedAccount(properName, uuid, key ?: "")
        fetchAndSyncProfiles(uuid, key)
    }

    suspend fun refreshLinkedAccount(): Result<Unit> = runCatching {
        var currentSettings: UserSettings? = null
        userPrefs.userSettingsFlow.collect {
            currentSettings = it
            return@collect
        }
        val settings = currentSettings ?: return@runCatching
        if (settings.linkedUuid.isBlank()) return@runCatching

        fetchAndSyncProfiles(settings.linkedUuid, settings.hypixelApiKey.ifBlank { null })
    }

    suspend fun switchProfile(profileId: String) {
        userPrefs.setSelectedProfileId(profileId)
        val selected = _coopProfiles.value.firstOrNull { it.profileId == profileId } ?: return
        val currentPlayer = selected.members.firstOrNull { it.isCurrentPlayer }

        val purse = currentPlayer?.purseCoins ?: 0.0
        val bank = selected.coopBankCoins
        val sacks = currentPlayer?.sacksValue ?: 0.0

        userPrefs.updateCachedBalances(purse, bank, sacks)
        evaluateCurrentMemberSacks(currentPlayer?.sacksCounts ?: emptyMap())
    }

    private suspend fun fetchAndSyncProfiles(uuid: String, apiKey: String?) {
        val response = try {
            hypixelApi.getProfiles(uuid = uuid, apiKey = apiKey)
        } catch (e: Exception) {
            val errorMsg = if (apiKey != null) {
                "Failed to fetch Hypixel profiles. Please check your API key."
            } else {
                "Failed to fetch Hypixel profiles. Try adding an API key from /api new on Hypixel."
            }
            throw Exception(errorMsg)
        }

        val rawProfiles = response.profiles ?: emptyList()
        if (rawProfiles.isEmpty()) {
            throw Exception("No SkyBlock profiles found for this player.")
        }

        val market = bazaarRepo.getAllItems()

        val parsedProfiles = rawProfiles.map { raw ->
            val bankCoins = raw.banking?.balance ?: 0.0
            val members = raw.members?.map { (memberUuid, data) ->
                val purse = data.currencies?.coinPurse ?: 0.0
                val sacks = data.inventory?.sacksCounts ?: emptyMap()

                var totalSackCoins = 0.0
                var slayerCoins = 0.0

                sacks.forEach { (tag, count) ->
                    val unitPrice = market[tag]?.sellPrice ?: 0.0
                    val valuated = unitPrice * count
                    totalSackCoins += valuated
                    if (SlayerCatalog.ALL_SLAYER_TAGS.contains(tag)) {
                        slayerCoins += valuated
                    }
                }

                CoopMember(
                    uuid = memberUuid,
                    purseCoins = purse,
                    sacksCounts = sacks,
                    sacksValue = totalSackCoins,
                    slayerValue = slayerCoins,
                    isCurrentPlayer = memberUuid.replace("-", "").equals(uuid.replace("-", ""), ignoreCase = true)
                )
            } ?: emptyList()

            val totalCoopPurse = members.sumOf { it.purseCoins }
            val totalCoopSacks = members.sumOf { it.sacksValue }

            CoopProfile(
                profileId = raw.profileId,
                cuteName = raw.cuteName ?: "Profile",
                isSelected = raw.selected == true,
                coopBankCoins = bankCoins,
                members = members,
                totalCoopCoins = bankCoins + totalCoopPurse + totalCoopSacks
            )
        }

        _coopProfiles.value = parsedProfiles

        // Select the active profile
        val active = parsedProfiles.firstOrNull { it.isSelected } ?: parsedProfiles.firstOrNull()
        if (active != null) {
            val currentPlayer = active.members.firstOrNull { it.isCurrentPlayer }
            val purse = currentPlayer?.purseCoins ?: 0.0
            val bank = active.coopBankCoins
            val sacks = currentPlayer?.sacksValue ?: 0.0

            userPrefs.setSelectedProfileId(active.profileId)
            userPrefs.updateCachedBalances(purse, bank, sacks)
            evaluateCurrentMemberSacks(currentPlayer?.sacksCounts ?: emptyMap())
        }
    }

    private suspend fun evaluateCurrentMemberSacks(sacks: Map<String, Long>) {
        val market = bazaarRepo.getAllItems()
        var total = 0.0
        var slayer = 0.0
        var gems = 0.0
        var farming = 0.0

        val topItems = mutableListOf<SlayerDropItem>()

        sacks.forEach { (tag, qty) ->
            if (qty > 0) {
                val price = market[tag]?.sellPrice ?: 0.0
                val itemVal = price * qty
                total += itemVal

                val cat = SlayerCatalog.getCategory(tag)
                if (SlayerCatalog.ALL_SLAYER_TAGS.contains(tag)) slayer += itemVal
                if (cat == "Gemstones") gems += itemVal
                if (cat == "Farming & Sacks") farming += itemVal

                if (itemVal > 1_000) {
                    topItems.add(
                        SlayerDropItem(
                            tag = tag,
                            displayName = market[tag]?.displayName ?: tag.replace('_', ' '),
                            bossCategory = cat,
                            quantity = qty,
                            unitPrice = price,
                            totalValue = itemVal
                        )
                    )
                }
            }
        }

        topItems.sortByDescending { it.totalValue }

        _currentSacksBreakdown.value = SacksBreakdown(
            totalSacksValue = total,
            slayerDropsValue = slayer,
            gemstonesValue = gems,
            farmingValue = farming,
            topValuedItems = topItems.take(20)
        )
    }

    suspend fun unlinkAccount() {
        userPrefs.clearLinkedAccount()
        _coopProfiles.value = emptyList()
        _currentSacksBreakdown.value = null
    }

    suspend fun toggleHideBalance(hide: Boolean) {
        userPrefs.setBalanceHidden(hide)
    }
}
