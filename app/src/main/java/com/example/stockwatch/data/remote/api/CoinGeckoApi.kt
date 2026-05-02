package com.example.stockwatch.data.remote.api

import com.example.stockwatch.data.remote.dto.CoinDetailDto
import com.example.stockwatch.data.remote.dto.CoinDto
import com.example.stockwatch.data.remote.dto.MarketChartDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfejs Retrofit do komunikacji z CoinGecko API v3.
 * Dokumentacja: https://www.coingecko.com/en/api/documentation
 * Brak klucza API — darmowe, publiczne endpointy.
 */
interface CoinGeckoApi {

    /** Zwraca listę monet posortowaną wg market cap. */
    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") currency: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinDto>

    /** Szczegóły pojedynczej monety. */
    @GET("coins/{id}")
    suspend fun getCoinDetail(
        @Path("id") id: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false
    ): CoinDetailDto

    /** Dane historyczne do wykresu. */
    @GET("coins/{id}/market_chart")
    suspend fun getCoinMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") currency: String,
        @Query("days") days: String
    ): MarketChartDto
}
