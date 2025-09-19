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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.platform.LocalContext
import com.deenshield.blocker.data.BlockRepository
import com.deenshield.blocker.viewmodel.BlockViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
    remember(Unit) { vm.initRepository(BlockRepository.get(ctx)) }
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
            // Segmented tabs row under the app bar, matching the screenshot style
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                val selectedColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                )
                FilterChip(
                    selected = currentRoute == "blocks",
                    onClick = { nav.navigate("blocks") },
                    label = { Text("Blocks") },
                    leadingIcon = { Icon(painterResource(android.R.drawable.checkbox_on_background), contentDescription = null) },
                    colors = if (currentRoute == "blocks") selectedColors else FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = currentRoute == "usage",
                    onClick = { nav.navigate("usage") },
                    label = { Text("Usage") },
                    leadingIcon = { Icon(painterResource(android.R.drawable.ic_menu_recent_history), contentDescription = null) },
                    colors = if (currentRoute == "usage") selectedColors else FilterChipDefaults.filterChipColors()
                )
                FilterChip(
                    selected = currentRoute == "reports",
                    onClick = { nav.navigate("reports") },
                    label = { Text("Reports") },
                    leadingIcon = { Icon(painterResource(android.R.drawable.ic_menu_info_details), contentDescription = null) },
                    colors = if (currentRoute == "reports") selectedColors else FilterChipDefaults.filterChipColors()
                )
            }

            androidx.compose.foundation.layout.Box(Modifier.weight(1f, fill = true)) {
                NavHost(navController = nav, startDestination = "blocks") {
                    composable("blocks") { BlocksScreen(vm) }
                    composable("usage") { Text("Usage") }
                    composable("reports") { Text("Reports") }
                    composable("add") { AddBlockScreen(vm) }
                    composable("settings") { com.deenshield.blocker.ui.SettingsScreen(vm) }
                }
            }
        }
    }
}
