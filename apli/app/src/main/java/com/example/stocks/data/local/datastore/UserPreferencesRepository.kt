package com.example.stocks.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val isBalanceHidden: Boolean = false,
    val linkedIgn: String = "",
    val linkedUuid: String = "",
    val hypixelApiKey: String = "",
    val shiiyuUrl: String = "",
    val selectedProfileId: String = "",
    val cachedPurseCoins: Double = 0.0,
    val cachedBankCoins: Double = 0.0,
    val cachedSacksCoins: Double = 0.0,
    val cachedNetWorth: Double = 0.0,
    val lastSyncTimestamp: Long = 0L,
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_BALANCE_HIDDEN = booleanPreferencesKey("is_balance_hidden")
        val LINKED_IGN = stringPreferencesKey("linked_ign")
        val LINKED_UUID = stringPreferencesKey("linked_uuid")
        val HYPIXEL_API_KEY = stringPreferencesKey("hypixel_api_key")
        val SHIIYU_URL = stringPreferencesKey("shiiyu_url")
        val SELECTED_PROFILE_ID = stringPreferencesKey("selected_profile_id")
        val CACHED_PURSE_COINS = doublePreferencesKey("cached_purse_coins")
        val CACHED_BANK_COINS = doublePreferencesKey("cached_bank_coins")
        val CACHED_SACKS_COINS = doublePreferencesKey("cached_sacks_coins")
        val CACHED_NET_WORTH = doublePreferencesKey("cached_net_worth")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            isBalanceHidden = preferences[PreferencesKeys.IS_BALANCE_HIDDEN] ?: false,
            linkedIgn = preferences[PreferencesKeys.LINKED_IGN] ?: "",
            linkedUuid = preferences[PreferencesKeys.LINKED_UUID] ?: "",
            hypixelApiKey = preferences[PreferencesKeys.HYPIXEL_API_KEY] ?: "",
            shiiyuUrl = preferences[PreferencesKeys.SHIIYU_URL] ?: "",
            selectedProfileId = preferences[PreferencesKeys.SELECTED_PROFILE_ID] ?: "",
            cachedPurseCoins = preferences[PreferencesKeys.CACHED_PURSE_COINS] ?: 0.0,
            cachedBankCoins = preferences[PreferencesKeys.CACHED_BANK_COINS] ?: 0.0,
            cachedSacksCoins = preferences[PreferencesKeys.CACHED_SACKS_COINS] ?: 0.0,
            cachedNetWorth = preferences[PreferencesKeys.CACHED_NET_WORTH] ?: 0.0,
            lastSyncTimestamp = preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] ?: 0L,
        )
    }

    suspend fun setSelectedProfileId(profileId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PROFILE_ID] = profileId
        }
    }

    suspend fun setBalanceHidden(hidden: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BALANCE_HIDDEN] = hidden
        }
    }

    suspend fun saveLinkedAccount(ign: String, uuid: String, apiKey: String = "", shiiyuUrl: String = "") {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LINKED_IGN] = ign
            preferences[PreferencesKeys.LINKED_UUID] = uuid
            if (apiKey.isNotBlank()) {
                preferences[PreferencesKeys.HYPIXEL_API_KEY] = apiKey
            }
            if (shiiyuUrl.isNotBlank()) {
                preferences[PreferencesKeys.SHIIYU_URL] = shiiyuUrl
            }
        }
    }

    suspend fun clearLinkedAccount() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LINKED_IGN)
            preferences.remove(PreferencesKeys.LINKED_UUID)
            preferences.remove(PreferencesKeys.HYPIXEL_API_KEY)
            preferences.remove(PreferencesKeys.SHIIYU_URL)
            preferences.remove(PreferencesKeys.SELECTED_PROFILE_ID)
            preferences[PreferencesKeys.CACHED_PURSE_COINS] = 0.0
            preferences[PreferencesKeys.CACHED_BANK_COINS] = 0.0
            preferences[PreferencesKeys.CACHED_SACKS_COINS] = 0.0
            preferences[PreferencesKeys.CACHED_NET_WORTH] = 0.0
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = 0L
        }
    }

    suspend fun updateCachedBalances(purse: Double, bank: Double, sacks: Double = 0.0, netWorth: Double = 0.0) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_PURSE_COINS] = purse
            preferences[PreferencesKeys.CACHED_BANK_COINS] = bank
            preferences[PreferencesKeys.CACHED_SACKS_COINS] = sacks
            preferences[PreferencesKeys.CACHED_NET_WORTH] = netWorth
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }
}
