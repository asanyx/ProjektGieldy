package com.example.stockwatch.data.repository

import com.example.stockwatch.data.remote.api.CoinGeckoApi
import com.example.stockwatch.domain.model.Coin
import com.example.stockwatch.domain.model.toDomain
import javax.inject.Inject

class CoinRepository @Inject constructor(
    private val api: CoinGeckoApi
) {
    private var cachedCoins: List<Coin> = emptyList()
    private var cacheTimestamp = 0L
    private var cachedCurrency: String = ""
    private val CACHE_DURATION = 60_000L // 60 sekund

    /** Wynik opakowany w Result do obsługi stanów loading/error. */
    suspend fun getMarkets(currency: String): Result<List<Coin>> {
        val now = System.currentTimeMillis()
        if (cachedCoins.isNotEmpty() && currency == cachedCurrency && now - cacheTimestamp < CACHE_DURATION) {
            return Result.success(cachedCoins)
        }
        return runCatching {
            api.getMarkets(currency = currency).map { it.toDomain() }
        }.onSuccess {
            cachedCoins = it
            cacheTimestamp = now
            cachedCurrency = currency
        }
    }

    suspend fun getCoinDetail(id: String) = runCatching {
        api.getCoinDetail(id)
    }

    suspend fun getCoinMarketChart(id: String, currency: String, days: String) = runCatching {
        api.getCoinMarketChart(id, currency, days)
    }
}
