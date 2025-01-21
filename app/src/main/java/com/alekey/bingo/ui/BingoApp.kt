package com.alekey.bingo.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alekey.bingo.navigation.NavigationItem
import com.alekey.bingo.ui.screens.PlayScreen
import com.alekey.bingo.ui.screens.SettingsScreen
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import com.alekey.bingo.SettingsViewModel


@Composable
fun BingoApp(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    val items = listOf(
        NavigationItem("settings", "Ajustes", Icons.Filled.Settings),
        NavigationItem("play", "Jugar", Icons.Filled.VideogameAsset)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "settings",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("settings") {
                SettingsScreen(settingsViewModel = settingsViewModel)
            }
            composable("play") {
                PlayScreen(settingsViewModel = settingsViewModel)
            }
        }
    }
}
