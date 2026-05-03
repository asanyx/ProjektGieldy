package com.example.stockwatch.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.stockwatch.ui.navigation.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val currencies = listOf("usd", "eur", "pln", "gbp")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Opcje aplikacji", style = MaterialTheme.typography.titleMedium)
        
        ListItem(
            headlineContent = { Text("Waluta") },
            supportingContent = { Text(selectedCurrency.uppercase()) },
            trailingContent = { 
                TextButton(onClick = { showCurrencyDialog = true }) {
                    Text("Zmień")
                }
            }
        )
        
        ListItem(
            headlineContent = { Text("Tryb ciemny") },
            supportingContent = { Text(if (isDarkMode) "Włączony" else "Wyłączony") },
            trailingContent = { 
                Switch(
                    checked = isDarkMode, 
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                ) 
            }
        )
        
        HorizontalDivider()

        if (showCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showCurrencyDialog = false },
                title = { Text("Wybierz walutę") },
                text = {
                    Column {
                        currencies.forEach { currency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setCurrency(currency)
                                        showCurrencyDialog = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currency == selectedCurrency,
                                    onClick = {
                                        viewModel.setCurrency(currency)
                                        showCurrencyDialog = false
                                    }
                                )
                                Text(text = currency.uppercase(), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCurrencyDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        Button(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Wyloguj się")
        }
        
        Text(text = "Wersja 1.0.0", style = MaterialTheme.typography.bodySmall)
    }
}
