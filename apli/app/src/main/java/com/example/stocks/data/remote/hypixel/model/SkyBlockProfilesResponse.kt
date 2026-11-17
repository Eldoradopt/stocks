package com.example.stocks.data.remote.hypixel.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkyBlockProfilesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "cause") val cause: String? = null,
    @Json(name = "profiles") val profiles: List<SkyBlockProfileData>?,
)

@JsonClass(generateAdapter = true)
data class SkyBlockProfileData(
    @Json(name = "profile_id") val profileId: String,
    @Json(name = "cute_name") val cuteName: String?,
    @Json(name = "selected") val selected: Boolean? = false,
    @Json(name = "banking") val banking: BankingData? = null,
    @Json(name = "members") val members: Map<String, ProfileMemberData>? = null,
)

@JsonClass(generateAdapter = true)
data class BankingData(
    @Json(name = "balance") val balance: Double? = 0.0,
)

@JsonClass(generateAdapter = true)
data class ProfileMemberData(
    @Json(name = "currencies") val currencies: CurrenciesData? = null,
    @Json(name = "inventory") val inventory: MemberInventoryData? = null,
)

@JsonClass(generateAdapter = true)
data class CurrenciesData(
    @Json(name = "coin_purse") val coinPurse: Double? = 0.0,
)

@JsonClass(generateAdapter = true)
data class MemberInventoryData(
    @Json(name = "sacks_counts") val sacksCounts: Map<String, Long>? = null,
)
