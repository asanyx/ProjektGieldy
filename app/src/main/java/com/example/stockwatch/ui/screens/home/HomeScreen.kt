package com.example.stockwatch.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.stockwatch.domain.model.Coin
import com.example.stockwatch.ui.navigation.Screen

import com.example.stockwatch.ui.theme.PriceDown
import com.example.stockwatch.ui.theme.PriceUp
import com.example.stockwatch.util.CurrencyUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StockWatch") },
                actions = {
                    IconButton(onClick = { viewModel.loadMarkets() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is HomeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is HomeUiState.Success -> CoinList(state.coins, state.currency) { coinId ->
                    navController.navigate(Screen.Detail.createRoute(coinId))
                }
                is HomeUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun CoinList(coins: List<Coin>, currency: String, onCoinClick: (String) -> Unit) {
    LazyColumn {
        items(coins) { coin ->
            CoinItem(coin = coin, currency = currency, onClick = { onCoinClick(coin.id) })
            HorizontalDivider()
        }
    }
}

@Composable
fun CoinItem(coin: Coin, currency: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = coin.name,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = coin.name, fontWeight = FontWeight.Bold)
            Text(text = coin.symbol.uppercase(), style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            val symbol = CurrencyUtils.getSymbol(currency)
            Text(text = "$symbol${coin.currentPrice}", fontWeight = FontWeight.Bold)
            val color = if (coin.priceChange24h >= 0) PriceUp else PriceDown
            Text(
                text = "${String.format(Locale.US, "%.2f", coin.priceChange24h)}%",
                color = color,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
