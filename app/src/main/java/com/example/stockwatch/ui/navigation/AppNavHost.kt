package com.example.stockwatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.stockwatch.ui.screens.auth.LoginScreen
import com.example.stockwatch.ui.screens.auth.RegisterScreen
import com.example.stockwatch.ui.screens.contact.ContactScreen
import com.example.stockwatch.ui.screens.detail.CoinDetailScreen
import com.example.stockwatch.ui.screens.home.HomeScreen
import com.example.stockwatch.ui.screens.settings.SettingsScreen
import com.example.stockwatch.ui.screens.splash.SplashScreen
import com.example.stockwatch.ui.screens.watchlist.WatchlistScreen

sealed class Screen(val route: String) {
    data object Splash   : Screen("splash")
    data object Login    : Screen("login")
    data object Register : Screen("register")
    data object Home     : Screen("home")
    data object Watchlist: Screen("watchlist")
    data object Settings : Screen("settings")
    data object Contact  : Screen("contact")
    data object Detail   : Screen("detail/{coinId}") {
        fun createRoute(coinId: String) = "detail/$coinId"
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route)    { SplashScreen(navController) }
        composable(Screen.Login.route)     { LoginScreen(navController) }
        composable(Screen.Register.route)  { RegisterScreen(navController) }
        composable(Screen.Home.route)      { HomeScreen(navController) }
        composable(Screen.Watchlist.route) { WatchlistScreen(navController) }
        composable(Screen.Settings.route)  { SettingsScreen(navController) }
        composable(Screen.Contact.route)   { ContactScreen() }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStack ->
            val coinId = backStack.arguments?.getString("coinId") ?: return@composable
            CoinDetailScreen(coinId = coinId, navController = navController)
        }
    }
}
