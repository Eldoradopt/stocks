package com.example.stocks.data.remote.shiiyu.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShiiyuProfileResponse(
    @Json(name = "data") val data: ShiiyuData? = null,
    @Json(name = "profile") val profile: ShiiyuProfileInfo? = null,
)

@JsonClass(generateAdapter = true)
data class ShiiyuData(
    @Json(name = "stats") val stats: ShiiyuStats? = null,
    @Json(name = "skills") val skills: Map<String, ShiiyuSkill>? = null,
    @Json(name = "inventory") val inventory: ShiiyuInventory? = null,
)

@JsonClass(generateAdapter = true)
data class ShiiyuStats(
    @Json(name = "purse") val purse: Double? = 0.0,
    @Json(name = "bank") val bank: Double? = 0.0,
)

@JsonClass(generateAdapter = true)
data class ShiiyuSkill(
    @Json(name = "level") val level: Double? = 0.0,
)

@JsonClass(generateAdapter = true)
data class ShiiyuInventory(
    @Json(name = "sacks") val sacks: Map<String, Long>? = emptyMap(),
)

@JsonClass(generateAdapter = true)
data class ShiiyuProfileInfo(
    @Json(name = "cute_name") val cuteName: String? = null,
    @Json(name = "profile_id") val profileId: String? = null,
)
