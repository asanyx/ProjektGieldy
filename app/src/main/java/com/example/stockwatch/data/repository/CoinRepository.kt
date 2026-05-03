package com.example.stockwatch.data.repository

import com.example.stockwatch.data.remote.api.CoinGeckoApi
import com.example.stockwatch.data.remote.dto.CoinDetailDto
import com.example.stockwatch.domain.model.Coin
import com.example.stockwatch.domain.model.toDomain
import javax.inject.Inject

/**
 * Repozytorium odpowiedzialne za pobieranie danych rynkowych z CoinGecko API.
 * Implementuje strategię prefetch oraz cache w pamięci.
 */
class CoinRepository @Inject constructor(
    private val api: CoinGeckoApi
) {
    companion object {
        // Cache list monet — współdzielony między instancjami
        private var marketsCache: Map<String, List<Coin>> = emptyMap()
        private var marketsCacheTime: Long = 0L

        // Cache szczegółów i wykresów per coinId
        private val detailCache = mutableMapOf<String, Pair<Long, CoinDetailDto>>()
        private val chartCache = mutableMapOf<String, Pair<Long, List<Pair<Long, Double>>>>()

        private const val CACHE_DURATION = 120_000L // 2 minuty
    }

    // Pobierz listę monet — zwraca z cache jeśli świeże
    suspend fun getMarkets(currency: String = "usd"): Result<List<Coin>> {
        val now = System.currentTimeMillis()
        val cached = marketsCache[currency]
        if (cached != null && now - marketsCacheTime < CACHE_DURATION) {
            return Result.success(cached)
        }
        return runCatching {
            api.getMarkets(currency = currency).map { it.toDomain() }
        }.onSuccess { coins ->
            marketsCache = marketsCache.toMutableMap().also { it[currency] = coins }
            marketsCacheTime = System.currentTimeMillis()
        }
    }

    // Szczegóły monety — po pierwszym pobraniu zawsze z cache
    suspend fun getCoinDetail(id: String): Result<CoinDetailDto> {
        val now = System.currentTimeMillis()
        val cached = detailCache[id]
        if (cached != null && now - cached.first < CACHE_DURATION) {
            return Result.success(cached.second)
        }
        return runCatching {
            api.getCoinDetail(id)
        }.onSuccess {
            detailCache[id] = System.currentTimeMillis() to it
        }.onFailure {
            // Przy błędzie zwróć stare dane jeśli są
            val stale = detailCache[id]
            if (stale != null) return Result.success(stale.second)
        }
    }

    // Wykres — po pierwszym pobraniu zawsze z cache
    suspend fun getMarketChart(coinId: String, currency: String = "usd", days: Int = 7): Result<List<Pair<Long, Double>>> {
        val cacheKey = "${coinId}_${currency}_$days"
        val now = System.currentTimeMillis()
        val cached = chartCache[cacheKey]
        if (cached != null && now - cached.first < CACHE_DURATION) {
            return Result.success(cached.second)
        }
        return runCatching {
            api.getMarketChart(coinId, currency = currency, days = days).prices.map { it[0].toLong() to it[1] }
        }.onSuccess {
            chartCache[cacheKey] = System.currentTimeMillis() to it
        }.onFailure {
            val stale = chartCache[cacheKey]
            if (stale != null) return Result.success(stale.second)
        }
    }

    // Wymuś odświeżenie listy (pomija cache)
    suspend fun forceRefreshMarkets(currency: String = "usd"): Result<List<Coin>> {
        marketsCacheTime = 0L // unieważnij cache
        return getMarkets(currency)
    }
}
