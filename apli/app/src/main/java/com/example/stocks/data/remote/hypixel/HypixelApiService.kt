package com.example.stocks.data.remote.hypixel

import com.example.stocks.data.remote.hypixel.model.BazaarResponse
import retrofit2.http.GET

interface HypixelApiService {
    /**
     * Returns live buy/sell prices for every Bazaar item.
     * No API key required. Full response ~2 MB — cache locally.
     */
    @GET("v2/skyblock/bazaar")
    suspend fun getBazaar(): BazaarResponse
}
