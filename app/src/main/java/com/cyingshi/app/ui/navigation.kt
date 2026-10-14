package com.cyingshi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cyingshi.app.ui.screens.BlockManageScreen
import com.cyingshi.app.ui.screens.DetailScreen
import com.cyingshi.app.ui.screens.FavoriteScreen
import com.cyingshi.app.ui.screens.HistoryScreen
import com.cyingshi.app.ui.screens.HomeScreen
import com.cyingshi.app.ui.screens.PlayerScreen
import com.cyingshi.app.ui.screens.SettingsScreen
import com.cyingshi.app.ui.screens.SourceManageScreen
import java.net.URLDecoder
import java.net.URLEncoder

fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")

fun dec(s: String): String = try {
    URLDecoder.decode(s, "UTF-8")
} catch (_: Exception) {
    s
}

/**
 * App 导航。MainViewModel 在此提升到 Activity 级，
 * 所有页面共享同一实例（换源/收藏/历史/屏蔽状态互通）。
 */
@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = route == "home" || route == "settings"

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") { HomeScreen(nav, vm) }
            composable("history") { HistoryScreen(nav, vm) }
            composable("favorites") { FavoriteScreen(nav, vm) }
            composable("settings") { SettingsScreen(nav, vm) }
            composable("blocks") { BlockManageScreen(nav, vm) }
            composable("sources") { SourceManageScreen(nav, vm) }
            composable(
                route = "detail?apiUrl={apiUrl}&vodId={vodId}",
                arguments = listOf(
                    navArgument("apiUrl") { type = NavType.StringType },
                    navArgument("vodId") { type = NavType.StringType }
                )
            ) { entry ->
                DetailScreen(
                    nav = nav,
                    vm = vm,
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
        }
        if (showBar) {
            CYBottomBar(
                nav = nav,
                current = route,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun navToTab(nav: NavController, route: String) {
    nav.navigate(route) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun CYBottomBar(nav: NavController, current: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarItem(Icons.Filled.Movie, current == "home") { navToTab(nav, "home") }
        BarItem(Icons.Filled.Settings, current == "settings") { navToTab(nav, "settings") }
    }
}

@Composable
private fun BarItem(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Color.White.copy(alpha = 0.55f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) GreenDeep else Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}
