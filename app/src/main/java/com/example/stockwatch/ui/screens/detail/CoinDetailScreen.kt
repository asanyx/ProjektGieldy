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
import androidx.compose.ui.graphics.StrokeCap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.stockwatch.util.CurrencyUtils

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    coinId: String,
    navController: NavHostController,
    viewModel: CoinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                    if (isLandscape) {
                        CoinDetailLandscape(state, chartData)
                    } else {
                        CoinDetailPortrait(state, chartData)
                    }
                }
                is CoinDetailUiState.Error -> Text(
                    text = state.message,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CoinDetailPortrait(state: CoinDetailUiState.Success, chartData: List<Pair<Long, Double>>) {
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

        ChartSection(chartData)

        Spacer(modifier = Modifier.height(24.dp))
        
        PriceInfoSection(state)

        Spacer(modifier = Modifier.height(16.dp))
        
        DescriptionSection(state.coin.description.en)
    }
}

@Composable
fun CoinDetailLandscape(state: CoinDetailUiState.Success, chartData: List<Pair<Long, Double>>) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Left Column: Identity and Price
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = state.coin.image.large,
                contentDescription = state.coin.name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.coin.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = state.coin.symbol.uppercase(), style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            PriceInfoSection(state)
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Right Column: Chart and Description
        Column(
            modifier = Modifier
                .weight(1.5f)
                .verticalScroll(rememberScrollState())
        ) {
            ChartSection(chartData)
            Spacer(modifier = Modifier.height(16.dp))
            DescriptionSection(state.coin.description.en)
        }
    }
}

@Composable
fun ChartSection(chartData: List<Pair<Long, Double>>) {
    if (chartData.isNotEmpty()) {
        val prices = chartData.map { it.second.toFloat() }
        val timestamps = chartData.map { it.first }
        val minPrice = prices.min()
        val maxPrice = prices.max()
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01f)

        var selectedIndex by remember { mutableStateOf<Int?>(null) }

        // Kolor linii zależny od trendu
        val lineColor = if (prices.last() >= prices.first())
            Color(0xFF4CAF50) else Color(0xFFEF5350)

        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

        val yLabelCount = 4 // liczba etykiet na osi Y
        val yStep = priceRange / (yLabelCount - 1)

        // Etykiety osi Y
        val yLabels = (0 until yLabelCount).map { i ->
            val value = minPrice + i * yStep
            if (value >= 1000) "$${String.format("%.0f", value)}"
            else if (value >= 1) "$${String.format("%.2f", value)}"
            else "$${String.format("%.4f", value)}"
        }

        // Etykiety osi X
        val firstDate = java.text.SimpleDateFormat("dd.MM", java.util.Locale.getDefault())
            .format(java.util.Date(timestamps.first()))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cena — ostatnie 7 dni",
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceVariant
                )
                
                if (selectedIndex != null && selectedIndex!! < prices.size) {
                    val time = java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(timestamps[selectedIndex!!]))
                    val price = prices[selectedIndex!!]
                    Text(
                        text = "$time: $${String.format("%.2f", price)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .height(160.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yLabels.reversed().forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .pointerInput(prices) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val stepX = size.width / (prices.size - 1).coerceAtLeast(1)
                                    selectedIndex = (offset.x / stepX).roundToInt().coerceIn(0, prices.size - 1)
                                    try {
                                        awaitRelease()
                                    } finally {
                                        selectedIndex = null
                                    }
                                }
                            )
                        }
                        .pointerInput(prices) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val stepX = size.width / (prices.size - 1).coerceAtLeast(1)
                                    selectedIndex = (offset.x / stepX).roundToInt().coerceIn(0, prices.size - 1)
                                },
                                onDrag = { change, _ ->
                                    val stepX = size.width / (prices.size - 1).coerceAtLeast(1)
                                    selectedIndex = (change.position.x / stepX).roundToInt().coerceIn(0, prices.size - 1)
                                },
                                onDragEnd = { selectedIndex = null },
                                onDragCancel = { selectedIndex = null }
                            )
                        }
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val stepX = chartWidth / (prices.size - 1).coerceAtLeast(1)

                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    for (i in 0 until yLabelCount) {
                        val y = chartHeight - (i.toFloat() / (yLabelCount - 1)) * chartHeight
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val path = Path()
                    prices.forEachIndexed { index, price ->
                        val x = index * stepX
                        val y = chartHeight - ((price - minPrice) / priceRange) * chartHeight
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Rysowanie zaznaczonego punktu
                    selectedIndex?.let { index ->
                        val x = index * stepX
                        val y = chartHeight - ((prices[index] - minPrice) / priceRange) * chartHeight
                        
                        // Linia pionowa
                        drawLine(
                            color = primaryColor.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, chartHeight),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        
                        // Kółko na linii
                        drawCircle(
                            color = primaryColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    } ?: run {
                        // Domyślny kółko na końcu, gdy nic nie jest zaznaczone
                        val lastX = (prices.size - 1) * stepX
                        val lastY = chartHeight - ((prices.last() - minPrice) / priceRange) * chartHeight
                        drawCircle(color = lineColor, radius = 5.dp.toPx(),
                            center = Offset(lastX, lastY))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(firstDate, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                Text("Dziś", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ładowanie danych wykresu...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PriceInfoSection(state: CoinDetailUiState.Success) {
    val currencySymbol = CurrencyUtils.getSymbol(state.currency)
    val currentPrice = state.coin.marketData.currentPrice[state.currency.lowercase()] ?: 0.0
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Cena:", fontWeight = FontWeight.Bold)
            Text("$currencySymbol${String.format("%.2f", currentPrice)}")
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Zmiana 24h:", fontWeight = FontWeight.Bold)
            val priceChange = state.coin.marketData.priceChangePercentage24h
            val color = if (priceChange >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
            Text(
                text = "${String.format("%.2f", priceChange)}%",
                color = color
            )
        }
    }
}

@Composable
fun DescriptionSection(description: String) {
    Text(
        text = "Opis",
        modifier = Modifier.fillMaxWidth(),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = description.replace(Regex("<.*?>"), ""),
        style = MaterialTheme.typography.bodySmall
    )
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
