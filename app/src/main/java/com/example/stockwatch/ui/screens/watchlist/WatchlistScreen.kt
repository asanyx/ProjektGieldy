package com.example.stockwatch.ui.screens.watchlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.stockwatch.data.local.entity.WatchlistEntity
import com.example.stockwatch.ui.navigation.Screen
import com.example.stockwatch.ui.theme.PriceDown
import com.example.stockwatch.ui.theme.PriceUp
import com.example.stockwatch.util.CurrencyUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    navController: NavHostController,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val watchlist by viewModel.watchlistItems.collectAsState()
    var editingItem by remember { mutableStateOf<WatchlistEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (watchlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Twoja lista jest pusta")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(watchlist) { item ->
                    WatchlistItemRow(
                        item = item,
                        onDelete = { viewModel.removeFromWatchlist(item.entity) },
                        onClick = { navController.navigate(Screen.Detail.createRoute(item.entity.coinId)) },
                        onLongClick = { editingItem = item.entity }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (editingItem != null) {
            var notesText by remember { mutableStateOf(editingItem!!.notes) }
            
            AlertDialog(
                onDismissRequest = { editingItem = null },
                title = { Text("Edytuj notatkę — ${editingItem!!.name}") },
                text = {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notatka") },
                        placeholder = { Text("np. Kupić gdy spadnie poniżej $1000") },
                        singleLine = false,
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateNotes(editingItem!!.coinId, notesText)
                        editingItem = null
                    }) { Text("Zapisz") }
                },
                dismissButton = {
                    TextButton(onClick = { editingItem = null }) { Text("Anuluj") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchlistItemRow(
    item: WatchlistItemUi,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
            
            if (item.entity.notes.isNotBlank()) {
                Text(
                    text = item.entity.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val symbol = CurrencyUtils.getSymbol(item.currency)
            Text(
                text = item.currentPrice?.let { "$symbol$it" } ?: "--",
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
