package com.cyingshi.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cyingshi.app.ui.screens.DetailScreen
import com.cyingshi.app.ui.screens.HomeScreen
import com.cyingshi.app.ui.screens.PlayerScreen
import com.cyingshi.app.ui.screens.SourceManageScreen
import java.net.URLDecoder
import java.net.URLEncoder

fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

fun dec(s: String): String = try {
    URLDecoder.decode(s, "UTF-8")
} catch (_: Exception) {
    s
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(nav)
        }
        composable(
            route = "detail?apiUrl={apiUrl}&vodId={vodId}",
            arguments = listOf(
                navArgument("apiUrl") { type = NavType.StringType },
                navArgument("vodId") { type = NavType.StringType }
            )
        ) { entry ->
            DetailScreen(
                nav = nav,
                apiUrl = dec(entry.arguments?.getString("apiUrl") ?: ""),
                vodId = entry.arguments?.getString("vodId") ?: ""
            )
        }
        composable(
            route = "player?url={url}&title={title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { entry ->
            PlayerScreen(
                url = dec(entry.arguments?.getString("url") ?: ""),
                title = dec(entry.arguments?.getString("title") ?: "播放")
            )
        }
        composable("sources") {
            SourceManageScreen(nav)
        }
    }
}
