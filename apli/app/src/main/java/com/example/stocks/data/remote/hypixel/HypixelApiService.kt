package com.example.stocks.data.remote.hypixel

import com.example.stocks.data.remote.hypixel.model.BazaarResponse
import com.example.stocks.data.remote.hypixel.model.SkyBlockProfilesResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface HypixelApiService {
    /**
     * Returns live buy/sell prices for every Bazaar item.
     * No API key required. Full response ~2 MB — cache locally.
     */
    @GET("v2/skyblock/bazaar")
    suspend fun getBazaar(): BazaarResponse

    /**
     * Returns player SkyBlock profiles including purse, bank, and sacks.
     */
    @GET("v2/skyblock/profiles")
    suspend fun getProfiles(
        @Query("uuid") uuid: String,
        @Header("API-Key") apiKey: String? = null,
    ): SkyBlockProfilesResponse
}
