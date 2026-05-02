package com.example.stockwatch.data.repository

import com.example.stockwatch.data.remote.api.CoinGeckoApi
import com.example.stockwatch.domain.model.Coin
import com.example.stockwatch.domain.model.toDomain
import javax.inject.Inject

class CoinRepository @Inject constructor(
    private val api: CoinGeckoApi
) {
    /** Wynik opakowany w Result do obsługi stanów loading/error. */
    suspend fun getMarkets(currency: String): Result<List<Coin>> = runCatching {
        api.getMarkets(currency = currency).map { it.toDomain() }
    }

    suspend fun getCoinDetail(id: String) = runCatching {
        api.getCoinDetail(id)
    }

    suspend fun getCoinMarketChart(id: String, currency: String, days: String) = runCatching {
        api.getCoinMarketChart(id, currency, days)
    }
}
