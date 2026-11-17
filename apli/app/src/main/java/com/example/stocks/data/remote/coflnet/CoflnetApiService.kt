package com.example.stocks.data.remote.coflnet

import com.example.stocks.data.remote.coflnet.model.PriceHistoryEntry
import retrofit2.http.GET
import retrofit2.http.Path

interface CoflnetApiService {
    /**
     * 7-day price history for a Bazaar item.
     * e.g. itemTag = "GLOSSY_GEMSTONE"
     */
    @GET("api/bazaar/{itemTag}/history/week")
    suspend fun getWeekHistory(
        @Path("itemTag") itemTag: String,
    ): List<PriceHistoryEntry>

    /**
     * 24-hour intraday price history.
     */
    @GET("api/bazaar/{itemTag}/history/day")
    suspend fun getDayHistory(
        @Path("itemTag") itemTag: String,
    ): List<PriceHistoryEntry>

    /**
     * 30-day price history for a Bazaar item.
     */
    @GET("api/bazaar/{itemTag}/history/month")
    suspend fun getMonthHistory(
        @Path("itemTag") itemTag: String,
    ): List<PriceHistoryEntry>

    /**
     * 3-month price history for a Bazaar item.
     */
    @GET("api/bazaar/{itemTag}/history/3month")
    suspend fun get3MonthHistory(
        @Path("itemTag") itemTag: String,
    ): List<PriceHistoryEntry>

    /**
     * 1-year price history for a Bazaar item.
     */
    @GET("api/bazaar/{itemTag}/history/year")
    suspend fun getYearHistory(
        @Path("itemTag") itemTag: String,
    ): List<PriceHistoryEntry>
}
