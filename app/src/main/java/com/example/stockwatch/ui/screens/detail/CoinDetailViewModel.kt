package com.example.stockwatch.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockwatch.data.local.datastore.SettingsDataStore
import com.example.stockwatch.data.local.entity.WatchlistEntity
import com.example.stockwatch.data.remote.dto.CoinDetailDto
import com.example.stockwatch.data.remote.dto.MarketChartDto
import com.example.stockwatch.data.repository.CoinRepository
import com.example.stockwatch.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CoinDetailUiState {
    data object Loading : CoinDetailUiState
    data class Success(
        val coin: CoinDetailDto,
        val isWatchlisted: Boolean,
        val currency: String,
        val marketChart: MarketChartDto? = null
    ) : CoinDetailUiState
    data class Error(val message: String) : CoinDetailUiState
}

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val watchlistRepository: WatchlistRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoinDetailUiState>(CoinDetailUiState.Loading)
    val uiState: StateFlow<CoinDetailUiState> = _uiState.asStateFlow()

    private val _chartData = MutableStateFlow<List<Pair<Long, Double>>>(emptyList())
    val chartData: StateFlow<List<Pair<Long, Double>>> = _chartData.asStateFlow()

    companion object {
        private val detailCache = mutableMapOf<String, Pair<Long, CoinDetailDto>>()
        private val chartCache = mutableMapOf<String, Pair<Long, List<Pair<Long, Double>>>>()
        private const val CACHE_DURATION = 120_000L // 2 minuty
    }

    fun loadCoinDetail(coinId: String) {
        val now = System.currentTimeMillis()

        val cached = detailCache[coinId]
        if (cached != null && now - cached.first < CACHE_DURATION) {
            viewModelScope.launch {
                val isWatchlisted = watchlistRepository.isWatchlisted(coinId).first()
                val currency = settingsDataStore.selectedCurrency.first()
                _uiState.value = CoinDetailUiState.Success(
                    coin = cached.second,
                    isWatchlisted = isWatchlisted,
                    currency = currency
                )
            }
            loadChartCached(coinId)
            return
        }

        viewModelScope.launch {
            _uiState.value = CoinDetailUiState.Loading
            coinRepository.getCoinDetail(coinId)
                .onSuccess { coin ->
                    detailCache[coinId] = System.currentTimeMillis() to coin
                    val isWatchlisted = watchlistRepository.isWatchlisted(coinId).first()
                    val currency = settingsDataStore.selectedCurrency.first()
                    _uiState.value = CoinDetailUiState.Success(
                        coin = coin,
                        isWatchlisted = isWatchlisted,
                        currency = currency
                    )
                    loadChart(coinId, currency)
                }
                .onFailure { error ->
                    val httpCode = (error as? retrofit2.HttpException)?.code()
                    if (httpCode == 429) {
                        val stale = detailCache[coinId]
                        if (stale != null) {
                            val isWatchlisted = watchlistRepository.isWatchlisted(coinId).first()
                            val currency = settingsDataStore.selectedCurrency.first()
                            _uiState.value = CoinDetailUiState.Success(
                                coin = stale.second,
                                isWatchlisted = isWatchlisted,
                                currency = currency
                            )
                        } else {
                            _uiState.value = CoinDetailUiState.Error(
                                "Zbyt wiele zapytań — poczekaj chwilę i spróbuj ponownie"
                            )
                        }
                    } else {
                        _uiState.value = CoinDetailUiState.Error(
                            error.message ?: "Błąd sieci"
                        )
                    }
                }
        }
    }

    private fun loadChartCached(coinId: String) {
        val now = System.currentTimeMillis()
        val cached = chartCache[coinId]
        if (cached != null && now - cached.first < CACHE_DURATION) {
            _chartData.value = cached.second
            return
        }
        viewModelScope.launch {
            val currency = settingsDataStore.selectedCurrency.first()
            loadChart(coinId, currency)
        }
    }

    private fun loadChart(coinId: String, currency: String = "usd") {
        viewModelScope.launch {
            delay(3_000L) // poczekaj aż request szczegółów się zakończy
            coinRepository.getMarketChart(coinId, currency)
                .onSuccess { data ->
                    chartCache[coinId] = System.currentTimeMillis() to data
                    _chartData.value = data
                }
                .onFailure {
                    // 429 na wykresie — ignoruj cicho, zostaw pusty wykres
                    // użytkownik widzi dane monety, tylko wykres nie załadowany
                }
        }
    }

    fun toggleWatchlist(coin: CoinDetailDto, isWatchlisted: Boolean) {
        viewModelScope.launch {
            val entity = WatchlistEntity(
                coinId = coin.id,
                name = coin.name,
                symbol = coin.symbol,
                imageUrl = coin.image.large
            )
            if (isWatchlisted) {
                watchlistRepository.remove(entity)
            } else {
                watchlistRepository.add(entity)
            }
        }
    }
}
