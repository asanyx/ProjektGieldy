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

    fun loadCoinDetail(coinId: String) {
        viewModelScope.launch {
            _uiState.value = CoinDetailUiState.Loading
            
            combine(
                settingsDataStore.selectedCurrency,
                watchlistRepository.isWatchlisted(coinId)
            ) { currency, isWatchlisted ->
                currency to isWatchlisted
            }.collectLatest { (currency, isWatchlisted) ->
                val detailResult = coinRepository.getCoinDetail(coinId)
                val chartResult = coinRepository.getCoinMarketChart(coinId, currency, "7")
                
                detailResult
                    .onSuccess { dto ->
                        _uiState.value = CoinDetailUiState.Success(
                            coin = dto,
                            isWatchlisted = isWatchlisted,
                            currency = currency,
                            marketChart = chartResult.getOrNull()
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = CoinDetailUiState.Error(error.message ?: "Błąd pobierania danych")
                    }
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
