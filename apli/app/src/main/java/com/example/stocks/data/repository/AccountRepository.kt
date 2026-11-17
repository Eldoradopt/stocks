package com.example.stocks.data.repository

import com.example.stocks.data.local.datastore.UserPreferencesRepository
import com.example.stocks.data.local.datastore.UserSettings
import com.example.stocks.data.remote.hypixel.HypixelApiService
import com.example.stocks.data.remote.mojang.MojangApiService
import kotlinx.coroutines.flow.Flow

data class PlayerAccountDetails(
    val ign: String,
    val uuid: String,
    val activeProfileName: String,
    val purseCoins: Double,
    val bankCoins: Double,
    val totalCoins: Double,
    val sackItems: Map<String, Long>,
)

class AccountRepository(
    private val mojangApi: MojangApiService,
    private val hypixelApi: HypixelApiService,
    private val userPrefs: UserPreferencesRepository,
    private val bazaarRepo: BazaarRepository,
) {
    val userSettings: Flow<UserSettings> = userPrefs.userSettingsFlow

    suspend fun linkAccount(username: String, apiKey: String = ""): Result<PlayerAccountDetails> = runCatching {
        // 1. Resolve username to UUID
        val mojangProfile = try {
            mojangApi.getProfile(username.trim())
        } catch (e: Exception) {
            throw Exception("Player '$username' not found. Please check the username spelling.")
        }
        val uuid = mojangProfile.id
        val properName = mojangProfile.name

        // 2. Fetch profiles
        val key = apiKey.trim().ifBlank { null }
        val response = try {
            hypixelApi.getProfiles(uuid = uuid, apiKey = key)
        } catch (e: Exception) {
            val errorMsg = if (key != null) {
                "Failed to fetch Hypixel profiles. Please check your API key."
            } else {
                "Failed to fetch Hypixel profiles. Try adding an API key from /api new on Hypixel."
            }
            throw Exception(errorMsg)
        }
        
        val profiles = response.profiles ?: emptyList()
        
        if (profiles.isEmpty()) {
            throw Exception("No SkyBlock profiles found for this player. Make sure you have played SkyBlock at least once.")
        }
        
        val activeProfile = profiles.firstOrNull { it.selected == true } ?: profiles.firstOrNull()
            ?: throw Exception("No active SkyBlock profile found.")

        val bankCoins = activeProfile?.banking?.balance ?: 0.0
        val memberData = activeProfile?.members?.get(uuid)
        
        if (memberData == null) {
            throw Exception("Player data not found in the selected profile. Try adding an API key.")
        }
        
        val purseCoins = memberData.currencies?.coinPurse ?: 0.0
        val sacks = memberData.inventory?.sacksCounts ?: emptyMap()

        // 3. Persist to DataStore
        userPrefs.saveLinkedAccount(properName, uuid, key ?: "")
        userPrefs.updateCachedBalances(purseCoins, bankCoins)

        PlayerAccountDetails(
            ign = properName,
            uuid = uuid,
            activeProfileName = activeProfile?.cuteName ?: "Default",
            purseCoins = purseCoins,
            bankCoins = bankCoins,
            totalCoins = purseCoins + bankCoins,
            sackItems = sacks,
        )
    }

    suspend fun refreshLinkedAccount(): Result<Unit> = runCatching {
        // fetch current settings
        var currentSettings: UserSettings? = null
        userPrefs.userSettingsFlow.collect {
            currentSettings = it
            return@collect
        }
        val settings = currentSettings ?: return@runCatching
        if (settings.linkedUuid.isBlank()) return@runCatching

        val key = settings.hypixelApiKey.ifBlank { null }
        val response = hypixelApi.getProfiles(uuid = settings.linkedUuid, apiKey = key)
        val profiles = response.profiles ?: emptyList()
        val activeProfile = profiles.firstOrNull { it.selected == true } ?: profiles.firstOrNull()

        val bankCoins = activeProfile?.banking?.balance ?: 0.0
        val memberData = activeProfile?.members?.get(settings.linkedUuid)
        val purseCoins = memberData?.currencies?.coinPurse ?: 0.0

        userPrefs.updateCachedBalances(purseCoins, bankCoins)
    }

    suspend fun unlinkAccount() {
        userPrefs.clearLinkedAccount()
    }

    suspend fun toggleHideBalance(hide: Boolean) {
        userPrefs.setBalanceHidden(hide)
    }
}
