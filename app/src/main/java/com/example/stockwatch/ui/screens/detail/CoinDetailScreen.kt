package com.example.stockwatch.ui.screens.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.stockwatch.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    coinId: String,
    navController: NavHostController,
    viewModel: CoinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(coinId) {
        viewModel.loadCoinDetail(coinId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    if (uiState is CoinDetailUiState.Success) {
                        val state = uiState as CoinDetailUiState.Success
                        IconButton(onClick = { viewModel.toggleWatchlist(state.coin, state.isWatchlisted) }) {
                            Icon(
                                imageVector = if (state.isWatchlisted) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is CoinDetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is CoinDetailUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = state.coin.image.large,
                            contentDescription = state.coin.name,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = state.coin.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = state.coin.symbol.uppercase(), style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        // Chart Section
                        state.marketChart?.let { chart ->
                            SimpleLineChart(
                                prices = chart.prices.map { it[1] },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val currencySymbol = CurrencyUtils.getSymbol(state.currency)
                        val currentPrice = state.coin.marketData.currentPrice[state.currency.lowercase()] ?: 0.0
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cena (${state.currency.uppercase()}):", fontWeight = FontWeight.Bold)
                            Text("$currencySymbol${String.format("%.2f", currentPrice)}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Zmiana 24h:", fontWeight = FontWeight.Bold)
                            val priceChange = state.coin.marketData.priceChangePercentage24h
                            val color = if (priceChange >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                            Text(
                                text = "${String.format("%.2f", priceChange)}%",
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Opis",
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.coin.description.en.replace(Regex("<.*?>"), ""), // Remove HTML tags
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                is CoinDetailUiState.Error -> Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    prices: List<Double>,
    modifier: Modifier = Modifier
) {
    if (prices.isEmpty()) return

    val maxPrice = prices.maxOrNull() ?: 0.0
    val minPrice = prices.minOrNull() ?: 0.0
    val range = maxPrice - minPrice

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = Path()

        prices.forEachIndexed { index, price ->
            val x = index * (width / (prices.size - 1))
            val y = if (range == 0.0) height / 2 else height - ((price - minPrice) / range * height).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = Color(0xFF4CAF50),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
