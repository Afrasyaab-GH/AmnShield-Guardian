package com.deenshield.blocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenshield.blocker.ui.BlocksScreen
import com.deenshield.blocker.ui.AddBlockScreen
import com.deenshield.blocker.ui.schedule.WeeklyScheduleScreen
import androidx.compose.ui.platform.LocalContext
import com.deenshield.blocker.data.BlockRepository
import com.deenshield.blocker.viewmodel.BlockViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.deenshield.blocker.ui.theme.BlockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlockerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BlockerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockerApp(vm: BlockViewModel = viewModel()) {
    val ctx = LocalContext.current
    remember(Unit) { 
        vm.initRepository(BlockRepository.get(ctx))
        vm.setContext(ctx) // Pass context for VPN service updates
    }
    val nav: NavHostController = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Blocker") },
                navigationIcon = {
                    IconButton(onClick = { /* optional: navigate home */ nav.navigate("blocks") }) {
                        // Using a generic view icon as a home glyph to avoid adding new icon deps
                        Icon(painterResource(android.R.drawable.ic_menu_view), contentDescription = "Home")
                    }
                },
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(painterResource(android.R.drawable.ic_menu_preferences), contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentRoute == "blocks") {
                FloatingActionButton(
                    onClick = { nav.navigate("add") },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(painterResource(android.R.drawable.ic_input_add), contentDescription = "Add")
                }
            }
        }
    ) { inner ->
        androidx.compose.foundation.layout.Column(Modifier.padding(inner)) {
            androidx.compose.foundation.layout.Box(Modifier.weight(1f).fillMaxSize()) {
                NavHost(navController = nav, startDestination = "blocks") {
                    composable("blocks") { BlocksScreen(vm) }
                    composable("usage") { Text("Usage") }
                    composable("reports") { Text("Reports") }
                    composable("add") { AddBlockScreen(vm, nav) }
                    composable("weeklySchedule") {
                        // Read initial schedule from previous entry (set by AddBlockScreen)
                        val prev = nav.previousBackStackEntry
                        val initialEncoded = prev?.savedStateHandle?.get<String>("weekly_initial")
                        val initial = com.deenshield.blocker.ui.decodeWeeklyFromEncoded(initialEncoded)
                        WeeklyScheduleScreen(
                            initial = initial,
                            onSave = { updated ->
                                // Return encoded result to previous entry and pop
                                val encoded = com.deenshield.blocker.ui.encodeWeeklyToEncoded(updated)
                                prev?.savedStateHandle?.set("weekly_result", encoded)
                                nav.popBackStack()
                            }
                        )
                    }
                    composable("settings") { com.deenshield.blocker.ui.SettingsScreen(vm) }
                }
            }
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "blocks",
                    onClick = { nav.navigate("blocks") },
                    icon = { Icon(painterResource(android.R.drawable.checkbox_on_background), contentDescription = null) },
                    label = { Text("Blocks") }
                )
                NavigationBarItem(
                    selected = currentRoute == "usage",
                    onClick = { nav.navigate("usage") },
                    icon = { Icon(painterResource(android.R.drawable.ic_menu_recent_history), contentDescription = null) },
                    label = { Text("Usage") }
                )
                NavigationBarItem(
                    selected = currentRoute == "reports",
                    onClick = { nav.navigate("reports") },
                    icon = { Icon(painterResource(android.R.drawable.ic_menu_info_details), contentDescription = null) },
                    label = { Text("Reports") }
                )
            }
        }
    }
}
