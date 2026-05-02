package com.example.stockwatch.domain.model

import com.example.stockwatch.data.remote.dto.CoinDto

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: Double,
    val priceChange24h: Double,
    val marketCap: Long,
    val volume: Long
)

// Mapper DTO -> Domain
fun CoinDto.toDomain() = Coin(
    id = id,
    symbol = symbol.uppercase(),
    name = name,
    imageUrl = image,
    currentPrice = currentPrice,
    priceChange24h = priceChangePercentage24h ?: 0.0,
    marketCap = marketCap ?: 0L,
    volume = totalVolume ?: 0L
)
