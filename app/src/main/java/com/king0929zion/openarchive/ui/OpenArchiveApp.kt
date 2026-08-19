package com.king0929zion.openarchive.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.king0929zion.openarchive.ArchiveViewModel
import com.king0929zion.openarchive.ui.screens.AchiScreen
import com.king0929zion.openarchive.ui.screens.AlbumScreen
import com.king0929zion.openarchive.ui.screens.ComposeScreen
import com.king0929zion.openarchive.ui.screens.DetailScreen
import com.king0929zion.openarchive.ui.screens.HomeScreen
import com.king0929zion.openarchive.ui.screens.ProviderEditorScreen
import com.king0929zion.openarchive.ui.screens.ProvidersScreen
import com.king0929zion.openarchive.ui.screens.SearchScreen
import com.king0929zion.openarchive.ui.screens.SettingsScreen
import com.king0929zion.openarchive.ui.screens.StatsScreen

@Composable
fun OpenArchiveApp(viewModel: ArchiveViewModel) {
    val nav = rememberNavController()
    Box(Modifier.fillMaxSize().background(Color.White).safeDrawingPadding()) {
        NavHost(
            navController = nav,
            startDestination = "home",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Fast,
                        delayMillis = 20,
                        easing = ArchiveMotion.Easing,
                    )
                ) + slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Screen,
                        easing = ArchiveMotion.Easing,
                    ),
                    initialOffsetX = { it / 12 },
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Quick,
                        easing = ArchiveMotion.Easing,
                    )
                ) + slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Fast,
                        easing = ArchiveMotion.Easing,
                    ),
                    targetOffsetX = { -it / 28 },
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Fast,
                        easing = ArchiveMotion.Easing,
                    )
                ) + slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Standard,
                        easing = ArchiveMotion.Easing,
                    ),
                    initialOffsetX = { -it / 16 },
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Quick,
                        easing = ArchiveMotion.Easing,
                    )
                ) + slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = ArchiveMotion.Standard,
                        easing = ArchiveMotion.Easing,
                    ),
                    targetOffsetX = { it / 10 },
                )
            },
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onCompose = { nav.navigate("compose") },
                    onEntry = { nav.navigate("detail/$it") },
                    onNavigate = { nav.navigate(it) },
                )
            }
            composable("compose") {
                ComposeScreen(
                    viewModel = viewModel,
                    onClose = { nav.popBackStack() },
                    onPublished = { nav.popBackStack("home", inclusive = false) },
                )
            }
            composable(
                route = "detail/{entryId}",
                arguments = listOf(navArgument("entryId") { type = NavType.StringType }),
            ) { backStack ->
                DetailScreen(
                    viewModel = viewModel,
                    entryId = backStack.arguments?.getString("entryId").orEmpty(),
                    onBack = { nav.popBackStack() },
                )
            }
            composable("search") {
                SearchScreen(viewModel, onBack = { nav.popBackStack() }, onEntry = { nav.navigate("detail/$it") })
            }
            composable("album") {
                AlbumScreen(viewModel, onBack = { nav.popBackStack() }, onEntry = { nav.navigate("detail/$it") })
            }
            composable("stats") {
                StatsScreen(viewModel, onBack = { nav.popBackStack() })
            }
            composable("achi") {
                AchiScreen(
                    viewModel,
                    onBack = { nav.popBackStack() },
                    onSettings = { nav.navigate("providers") },
                )
            }
            composable("settings") {
                SettingsScreen(viewModel, onBack = { nav.popBackStack() }, onProviders = { nav.navigate("providers") })
            }
            composable("providers") {
                ProvidersScreen(
                    viewModel,
                    onBack = { nav.popBackStack() },
                    onEdit = { id -> nav.navigate("provider/${id ?: "new"}") },
                )
            }
            composable(
                route = "provider/{providerId}",
                arguments = listOf(navArgument("providerId") { type = NavType.StringType }),
            ) { backStack ->
                val id = backStack.arguments?.getString("providerId").orEmpty().takeUnless { it == "new" }
                ProviderEditorScreen(
                    viewModel = viewModel,
                    providerId = id,
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.popBackStack("providers", inclusive = false) },
                )
            }
        }
    }
}
