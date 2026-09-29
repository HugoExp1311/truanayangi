package com.example.foodgacha.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodgacha.ui.detail.FullscreenImageViewer
import com.example.foodgacha.ui.detail.ItemDetailScreen
import com.example.foodgacha.ui.edit.ItemEditScreen
import com.example.foodgacha.ui.home.HomeScreen
import com.example.foodgacha.ui.library.LibraryScreen
import com.example.foodgacha.ui.roll.RollScreen
import com.example.foodgacha.ui.tags.TagsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Roll : Screen("roll")
    object Tags : Screen("tags")
    object ItemDetail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Long) = "detail/$itemId"
    }
    object ItemEdit : Screen("edit/{itemId}") {
        fun createRoute(itemId: Long) = "edit/$itemId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                onNavigateToRoll = { navController.navigate(Screen.Roll.route) },
                onNavigateToTags = { navController.navigate(Screen.Tags.route) }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onNavigateToEdit = { itemId ->
                    navController.navigate(Screen.ItemEdit.createRoute(itemId))
                }
            )
        }

        composable(Screen.Roll.route) {
            RollScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                }
            )
        }

        composable(Screen.Tags.route) {
            TagsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            ItemDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.ItemEdit.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.ItemEdit.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            ItemEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}
