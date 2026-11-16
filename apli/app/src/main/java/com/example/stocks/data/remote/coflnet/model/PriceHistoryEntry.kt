package com.example.stocks.data.remote.coflnet.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * One entry from Coflnet's /api/bazaar/{itemTag}/history/week response.
 * Coflnet returns a JSON array of these objects.
 */
@JsonClass(generateAdapter = true)
data class PriceHistoryEntry(
    @Json(name = "timestamp") val timestamp: String,  // ISO-8601, e.g. "2024-01-01T12:00:00"
    @Json(name = "buy")  val buy: Double,
    @Json(name = "sell") val sell: Double,
)
