package com.example.stockwatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @SerializedName("market_cap") val marketCap: Long?,
    @SerializedName("total_volume") val totalVolume: Long?
)

data class CoinDetailDto(
    val id: String,
    val symbol: String,
    val name: String,
    val description: DescriptionDto,
    val image: ImageDto,
    @SerializedName("market_data") val marketData: MarketDataDto
)

data class DescriptionDto(
    val en: String
)

data class ImageDto(
    val large: String
)

data class MarketDataDto(
    @SerializedName("current_price") val currentPrice: Map<String, Double>,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double,
    @SerializedName("market_cap") val marketCap: Map<String, Long>,
    @SerializedName("total_volume") val totalVolume: Map<String, Long>
)

data class MarketChartDto(
    val prices: List<List<Double>>
)
