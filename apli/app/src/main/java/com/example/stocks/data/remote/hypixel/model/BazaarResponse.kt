package com.example.stocks.data.remote.hypixel.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BazaarResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "products") val products: Map<String, BazaarProduct>,
)

@JsonClass(generateAdapter = true)
data class BazaarProduct(
    @Json(name = "product_id") val productId: String,
    @Json(name = "quick_status") val quickStatus: QuickStatus,
)

@JsonClass(generateAdapter = true)
data class QuickStatus(
    @Json(name = "productId") val productId: String,
    @Json(name = "sellPrice") val sellPrice: Double,
    @Json(name = "sellVolume") val sellVolume: Long,
    @Json(name = "sellMovingWeek") val sellMovingWeek: Long,
    @Json(name = "buyPrice") val buyPrice: Double,
    @Json(name = "buyVolume") val buyVolume: Long,
    @Json(name = "buyMovingWeek") val buyMovingWeek: Long,
)
