package com.example.stocks.data.repository

import com.example.stocks.data.local.datastore.UserPreferencesRepository
import com.example.stocks.data.local.datastore.UserSettings
import com.example.stocks.data.remote.hypixel.HypixelApiService
import com.example.stocks.data.remote.mojang.MojangApiService
import com.example.stocks.data.remote.shiiyu.ShiiyuApiService
import com.example.stocks.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class AccountRepository(
    private val mojangApi: MojangApiService,
    private val hypixelApi: HypixelApiService,
    private val shiiyuApi: ShiiyuApiService,
    private val userPrefs: UserPreferencesRepository,
    private val bazaarRepo: BazaarRepository,
) {
    val userSettings: Flow<UserSettings> = userPrefs.userSettingsFlow

    private val _coopProfiles = MutableStateFlow<List<CoopProfile>>(emptyList())
    val coopProfiles = _coopProfiles.asStateFlow()

    private val _currentSacksBreakdown = MutableStateFlow<SacksBreakdown?>(null)
    val currentSacksBreakdown = _currentSacksBreakdown.asStateFlow()

    suspend fun linkAccount(username: String, apiKey: String = "", shiiyuUrl: String = ""): Result<Unit> = runCatching {
        // If Shiiyu URL is provided, use it instead of Hypixel API
        if (shiiyuUrl.isNotBlank() && shiiyuUrl.contains("shiiyu.moe")) {
            // Add https:// prefix if missing
            val fullUrl = if (shiiyuUrl.startsWith("http")) {
                shiiyuUrl
            } else {
                "https://$shiiyuUrl"
            }
            fetchFromShiiyu(fullUrl)
            // Extract profile name from URL for display
            val urlParts = fullUrl.trim().split("/")
            val profileName = urlParts.getOrNull(5) ?: urlParts.getOrNull(4) ?: username
            userPrefs.saveLinkedAccount(profileName, "", "", fullUrl)
            Result.success(Unit)
        } else {
            // Original Hypixel flow
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
    }

    suspend fun refreshLinkedAccount(): Result<Unit> = runCatching {
        val settings = userPrefs.userSettingsFlow.first()
        
        // If Shiiyu URL is saved, use that
        if (settings.shiiyuUrl.isNotBlank()) {
            fetchFromShiiyu(settings.shiiyuUrl)
            return@runCatching
        }
        
        // Otherwise use Hypixel
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

    private suspend fun fetchFromShiiyu(shiiyuUrl: String) {
        // Try SkyCrypt API first (JSON format with detailed data)
        val apiUrl = shiiyuUrl.replace("https://sky.shiiyu.moe/stats/", "https://sky.shiiyu.moe/api/")
        
        val jsonResponse = try {
            shiiyuApi.getSkyCryptData(apiUrl)
        } catch (e: Exception) {
            // Fallback to HTML parsing if API fails
            fetchFromShiiyuHtml(shiiyuUrl)
            return
        }

        // Parse SkyCrypt JSON if successful
        try {
            val purseRegex = Regex(""""purse"\s*:\s*([0-9.]+)""")
            val bankRegex = Regex(""""bank"\s*:\s*([0-9.]+)""")
            val netWorthRegex = Regex(""""networth"\s*:\s*([0-9.]+)""")
            
            val purse = purseRegex.find(jsonResponse)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val bank = bankRegex.find(jsonResponse)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val netWorth = netWorthRegex.find(jsonResponse)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            val urlParts = shiiyuUrl.trim().split("/")
            val profileName = urlParts.getOrNull(5) ?: urlParts.getOrNull(4) ?: "Unknown"
            val profileId = "shiiyu-profile"

            val coopProfile = CoopProfile(
                profileId = profileId,
                cuteName = profileName,
                isSelected = true,
                coopBankCoins = bank,
                members = listOf(
                    CoopMember(
                        uuid = "shiiyu-$profileName",
                        purseCoins = purse,
                        sacksCounts = emptyMap(),
                        sacksValue = 0.0,
                        slayerValue = 0.0,
                        isCurrentPlayer = true
                    )
                ),
                totalCoopCoins = netWorth
            )

            _coopProfiles.value = listOf(coopProfile)
            userPrefs.setSelectedProfileId(profileId)
            userPrefs.updateCachedBalances(purse, bank, 0.0, netWorth)
        } catch (e: Exception) {
            // Fallback to HTML parsing if JSON parsing fails
            fetchFromShiiyuHtml(shiiyuUrl)
        }
    }

    private suspend fun fetchFromShiiyuHtml(shiiyuUrl: String) {
        val htmlResponse = try {
            shiiyuApi.getProfile(shiiyuUrl)
        } catch (e: Exception) {
            throw Exception("Failed to fetch Shiiyu profile. Please check the URL.\n\nError: ${e.message}")
        }

        // Extract username (IGN) from HTML
        val nameRegex = Regex("""alternateName":"([^"]+)"""")
        val nameMatch = nameRegex.find(htmlResponse)
        val username = nameMatch?.groupValues?.get(1) ?: "Unknown"

        // Extract profile name from URL or HTML
        val urlParts = shiiyuUrl.trim().split("/")
        val profileName = urlParts.getOrNull(5) ?: try {
            val profileRegex = Regex(""""profile":"\{[^}]*"cute_name":"([^"]+)"""")
            val profileMatch = profileRegex.find(htmlResponse)
            profileMatch?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        } ?: username

        // Try multiple patterns to extract net worth
        val netWorth = try {
            val descRegex = Regex("""description:"[^"]+ Networth: ([^,]+)""")
            val descMatch = descRegex.find(htmlResponse)
            descMatch?.groupValues?.get(1)?.let { parseNetWorth(it) }
        } catch (e: Exception) {
            null
        } ?: try {
            val netWorthRegex = Regex("""Networth:\s*([0-9.,]+[BKM])""")
            val nwMatch = netWorthRegex.find(htmlResponse)
            nwMatch?.groupValues?.get(1)?.let { parseNetWorth(it) }
        } catch (e: Exception) {
            null
        } ?: 0.0

        val purse = 0.0
        val bank = 0.0
        val sacks = emptyMap<String, Long>()
        val profileId = "shiiyu-profile"

        val coopProfile = CoopProfile(
            profileId = profileId,
            cuteName = profileName,
            isSelected = true,
            coopBankCoins = bank,
            members = listOf(
                CoopMember(
                    uuid = "shiiyu-$username",
                    purseCoins = purse,
                    sacksCounts = sacks,
                    sacksValue = 0.0,
                    slayerValue = 0.0,
                    isCurrentPlayer = true
                )
            ),
            totalCoopCoins = netWorth
        )

        _coopProfiles.value = listOf(coopProfile)
        userPrefs.setSelectedProfileId(profileId)
        userPrefs.updateCachedBalances(purse, bank, 0.0, netWorth)
    }

    private fun parseNetWorth(text: String): Double {
        // Parse formats like "117.63M", "5.2K", "1.2B", "500"
        val cleanText = text.trim().replace(",", "") // Remove commas
        val multiplier = when {
            cleanText.endsWith("B", ignoreCase = true) -> 1_000_000_000.0
            cleanText.endsWith("M", ignoreCase = true) -> 1_000_000.0
            cleanText.endsWith("K", ignoreCase = true) -> 1_000.0
            else -> 1.0
        }
        val numberStr = cleanText.dropLastWhile { it.isLetter() }.trim()
        return numberStr.toDoubleOrNull()?.times(multiplier) ?: 0.0
    }

    private suspend fun fetchAndSyncProfiles(uuid: String, apiKey: String?) {
        val response = try {
            hypixelApi.getProfiles(uuid = uuid, apiKey = apiKey)
        } catch (e: Exception) {
            val errorMsg = if (apiKey != null) {
                "Failed to fetch Hypixel profiles. API key might be invalid or expired.\n\nError: ${e.message}\n\nTry generating a new key with /api new on Hypixel."
            } else {
                "Failed to fetch Hypixel profiles. An API key is required to fetch profile data.\n\nError: ${e.message}\n\nGet your API key by typing /api new on Hypixel.net"
            }
            throw Exception(errorMsg)
        }

        if (!response.success) {
            val cause = response.cause ?: "Unknown error"
            throw Exception("Hypixel API error: $cause")
        }

        val rawProfiles = response.profiles ?: emptyList()
        if (rawProfiles.isEmpty()) {
            throw Exception("No SkyBlock profiles found for this player. Make sure you've played SkyBlock at least once.")
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
