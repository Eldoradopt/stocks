package com.example.stocks.data.remote.mojang

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class MojangProfileResponse(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
)

interface MojangApiService {
    @GET("users/profiles/minecraft/{username}")
    suspend fun getProfile(
        @Path("username") username: String,
    ): MojangProfileResponse
}
