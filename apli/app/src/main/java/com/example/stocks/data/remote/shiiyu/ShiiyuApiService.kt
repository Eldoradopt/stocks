package com.example.stocks.data.remote.shiiyu

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface ShiiyuApiService {
    /**
     * Fetch player profile data from Shiiyu
     * Returns HTML with embedded JSON-LD
     */
    @GET
    suspend fun getProfile(@Url url: String): String

    /**
     * Try to fetch SkyCrypt API data (JSON format)
     * Example: https://sky.shiiyu.moe/api/Blazecraft_edu/Papaya
     */
    @GET
    suspend fun getSkyCryptData(@Url url: String): String
}
