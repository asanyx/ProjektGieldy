package com.example.stockwatch.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.stockwatch.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val items = listOf(
        Triple("Home", Icons.Default.Home, Screen.Home.route),
        Triple("Watchlist", Icons.Default.Bookmarks, Screen.Watchlist.route),
        Triple("Ustawienia", Icons.Default.Settings, Screen.Settings.route),
        Triple("Kontakt", Icons.Default.ContactPage, Screen.Contact.route),
    )

    Column(modifier = Modifier.fillMaxHeight()) {
        // Nagłówek drawera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("StockWatch", color = Color.White,
                    style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Elementy nawigacji
        items.forEach { (label, icon, route) ->
            NavigationDrawerItem(
                label = { Text(label) },
                icon = { Icon(icon, contentDescription = label) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Przycisk wylogowania na dole
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Wyloguj") },
            icon = { Icon(Icons.Default.Logout, contentDescription = "Wyloguj") },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}
