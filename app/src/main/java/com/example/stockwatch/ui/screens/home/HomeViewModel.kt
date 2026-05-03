package com.example.stockwatch.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockwatch.data.local.datastore.SettingsDataStore
import com.example.stockwatch.data.repository.CoinRepository
import com.example.stockwatch.domain.model.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Stan UI dla ekranu Home. */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val coins: List<Coin>, val currency: String) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCoins: StateFlow<List<Coin>> = combine(
        _uiState, _searchQuery
    ) { state, query ->
        if (state is HomeUiState.Success) {
            if (query.isBlank()) state.coins
            else state.coins.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.symbol.contains(query, ignoreCase = true)
            }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Załaduj dane przy starcie i reaguj na zmiany waluty
        viewModelScope.launch {
            settingsDataStore.selectedCurrency.collectLatest { currency ->
                loadMarkets(currency)
            }
        }
        
        // Automatyczne odświeżanie co 2 minuty w tle
        viewModelScope.launch {
            while (true) {
                delay(120_000L)
                refreshInBackground()
            }
        }
    }

    private fun loadMarkets(currency: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            coinRepository.getMarkets(currency)
                .onSuccess { _uiState.value = HomeUiState.Success(it, currency) }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Błąd sieci") }
        }
    }

    // Odświeżanie wymuszone przez użytkownika (przycisk)
    fun forceRefresh() {
        viewModelScope.launch {
            val currency = settingsDataStore.selectedCurrency.first()
            // Pokaż loading tylko jeśli nie ma danych
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }
            coinRepository.forceRefreshMarkets(currency)
                .onSuccess { _uiState.value = HomeUiState.Success(it, currency) }
                .onFailure {
                    // Przy błędzie zostaw stare dane jeśli są
                    if (_uiState.value !is HomeUiState.Success) {
                        _uiState.value = HomeUiState.Error(it.message ?: "Błąd sieci")
                    }
                }
        }
    }

    // Odświeżanie w tle — nie pokazuje loading, nie przeszkadza użytkownikowi
    private suspend fun refreshInBackground() {
        val currency = settingsDataStore.selectedCurrency.first()
        coinRepository.forceRefreshMarkets(currency)
            .onSuccess { coins ->
                // Aktualizuj tylko jeśli jesteśmy w stanie Success
                if (_uiState.value is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Success(coins, currency)
                }
            }
        // Błąd w tle ignoruj — nie pokazuj użytkownikowi
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
