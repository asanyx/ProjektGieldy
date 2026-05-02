package com.example.stockwatch.ui.screens.watchlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.stockwatch.ui.navigation.Screen
import com.example.stockwatch.ui.theme.PriceDown
import com.example.stockwatch.ui.theme.PriceUp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    navController: NavHostController,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val watchlist by viewModel.watchlistItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Obserwowane") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                }
            )
        }
    ) { padding ->
        if (watchlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Twoja lista jest pusta")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(watchlist) { item ->
                    WatchlistItemRow(
                        item = item,
                        onDelete = { viewModel.removeFromWatchlist(item.entity) },
                        onClick = { navController.navigate(Screen.Detail.createRoute(item.entity.coinId)) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun WatchlistItemRow(
    item: WatchlistItemUi,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.entity.imageUrl,
            contentDescription = item.entity.name,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.entity.name, fontWeight = FontWeight.Bold)
            Text(text = item.entity.symbol.uppercase(), style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.currentPrice?.let { "$$it" } ?: "--",
                fontWeight = FontWeight.Bold
            )
            item.priceChange24h?.let { change ->
                val color = if (change >= 0) PriceUp else PriceDown
                Text(
                    text = "${String.format(Locale.US, "%.2f", change)}%",
                    color = color,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
        }
    }
}
