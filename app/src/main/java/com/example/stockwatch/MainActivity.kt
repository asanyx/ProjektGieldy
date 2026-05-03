package com.example.stockwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.stockwatch.ui.navigation.AppNavHost
import com.example.stockwatch.ui.navigation.BottomNavBar
import com.example.stockwatch.ui.navigation.Screen
import com.example.stockwatch.ui.theme.StockWatchTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stockwatch.ui.screens.settings.SettingsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

            StockWatchTheme(darkTheme = isDarkMode) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Ekrany na których NIE pokazujemy drawera i TopAppBar
    val fullscreenRoutes = listOf(Screen.Splash.route, Screen.Login.route, Screen.Register.route)
    val showDrawer = currentRoute !in fullscreenRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            if (showDrawer) {
                ModalDrawerSheet {
                    // Nagłówek
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
                            Text(
                                "StockWatch",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Elementy nawigacji
                    val drawerItems = listOf(
                        Triple("Home", Icons.Default.Home, Screen.Home.route),
                        Triple("Watchlist", Icons.Default.Bookmarks, Screen.Watchlist.route),
                        Triple("Ustawienia", Icons.Default.Settings, Screen.Settings.route),
                        Triple("Kontakt", Icons.Default.ContactPage, Screen.Contact.route),
                    )

                    drawerItems.forEach { (label, icon, route) ->
                        NavigationDrawerItem(
                            label = { Text(label) },
                            icon = { Icon(icon, contentDescription = label) },
                            selected = currentRoute == route,
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
                    HorizontalDivider()

                    // Wyloguj
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
        }
    ) {
        Scaffold(
            topBar = {
                if (showDrawer && currentRoute != Screen.Detail.route) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { 
                            val title = when(currentRoute) {
                                Screen.Home.route -> "StockWatch"
                                Screen.Watchlist.route -> "Obserwowane"
                                Screen.Settings.route -> "Ustawienia"
                                Screen.Contact.route -> "Kontakt"
                                else -> "StockWatch"
                            }
                            Text(title)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            if (currentRoute == Screen.Home.route) {
                                // Tutaj można by dodać akcje specyficzne dla Home, jeśli potrzebne
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showDrawer && currentRoute != Screen.Detail.route) {
                    BottomNavBar(navController = navController)
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
