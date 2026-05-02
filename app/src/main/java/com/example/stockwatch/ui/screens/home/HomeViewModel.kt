package com.example.stockwatch.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockwatch.data.local.datastore.SettingsDataStore
import com.example.stockwatch.data.repository.CoinRepository
import com.example.stockwatch.domain.model.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

    private var currentCurrency: String = "usd"
    private var lastFetchTime = 0L
    private val MIN_FETCH_INTERVAL = 10_000L // 10 sekund

    init {
        viewModelScope.launch {
            settingsDataStore.selectedCurrency.collectLatest { currency ->
                currentCurrency = currency
                loadMarkets(force = true) // Wymuś przy zmianie waluty
            }
        }
    }

    fun loadMarkets(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastFetchTime < MIN_FETCH_INTERVAL) return
        lastFetchTime = now

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            coinRepository.getMarkets(currentCurrency)
                .onSuccess { 
                    _uiState.value = HomeUiState.Success(it, currentCurrency) 
                }
                .onFailure { error ->
                    val message = if ((error as? HttpException)?.code() == 429) {
                        "Zbyt wiele zapytań, poczekaj chwilę"
                    } else {
                        error.message ?: "Błąd sieci"
                    }
                    _uiState.value = HomeUiState.Error(message)
                }
        }
    }
}
