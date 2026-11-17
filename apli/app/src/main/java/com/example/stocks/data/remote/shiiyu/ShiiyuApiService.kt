package com.example.stocks.data.remote.shiiyu

import retrofit2.http.GET
import retrofit2.http.Url

interface ShiiyuApiService {
    /**
     * Fetch player profile data from Shiiyu
     * Returns HTML with embedded JSON-LD
     */
    @GET
    suspend fun getProfile(@Url url: String): String
}
