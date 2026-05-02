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

    init {
        viewModelScope.launch {
            settingsDataStore.selectedCurrency.collectLatest { currency ->
                currentCurrency = currency
                loadMarkets()
            }
        }
    }

    fun loadMarkets() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            coinRepository.getMarkets(currentCurrency)
                .onSuccess { _uiState.value = HomeUiState.Success(it, currentCurrency) }
                .onFailure { _uiState.value = HomeUiState.Error(it.message ?: "Błąd sieci") }
        }
    }
}
