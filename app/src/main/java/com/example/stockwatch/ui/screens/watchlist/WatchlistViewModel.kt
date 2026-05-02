package com.example.stockwatch.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockwatch.data.local.datastore.SettingsDataStore
import com.example.stockwatch.data.local.entity.WatchlistEntity
import com.example.stockwatch.data.repository.CoinRepository
import com.example.stockwatch.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistItemUi(
    val entity: WatchlistEntity,
    val currentPrice: Double? = null,
    val priceChange24h: Double? = null,
    val currency: String = "usd"
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository,
    private val coinRepository: CoinRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    val watchlistItems: StateFlow<List<WatchlistItemUi>> = combine(
        settingsDataStore.selectedCurrency,
        repository.getAll(),
        _refreshTrigger
    ) { currency, entities, _ ->
        val marketsResult = coinRepository.getMarkets(currency)
        val markets = marketsResult.getOrNull() ?: emptyList()

        entities.map { entity ->
            val coin = markets.find { it.id == entity.coinId }
            WatchlistItemUi(
                entity = entity,
                currentPrice = coin?.currentPrice,
                priceChange24h = coin?.priceChange24h,
                currency = currency
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun refresh() {
        _refreshTrigger.value += 1
    }

    fun removeFromWatchlist(entity: WatchlistEntity) {
        viewModelScope.launch {
            repository.remove(entity)
        }
    }
}
