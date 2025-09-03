package com.deenshield.blocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenshield.blocker.ui.BlocksScreen
import com.deenshield.blocker.ui.AddBlockScreen
import androidx.compose.ui.platform.LocalContext
import com.deenshield.blocker.data.BlockRepository
import com.deenshield.blocker.viewmodel.BlockViewModel

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
    // Init repository once
    val ctx = LocalContext.current
    remember(Unit) { vm.initRepository(BlockRepository.get(ctx)) }
    var tab by remember { mutableStateOf(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocker") },
                actions = {
                    IconButton(onClick = { /* TODO open settings */ }) {
                        Icon(painterResource(android.R.drawable.ic_menu_preferences), contentDescription = "Settings")
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            Row {
                OutlinedButton(onClick = { tab = 0 }) { Text("Blocks") }
                OutlinedButton(onClick = { tab = 1 }) { Text("Usage") }
                OutlinedButton(onClick = { tab = 2 }) { Text("Reports") }
                OutlinedButton(onClick = { tab = 3 }) { Text("Archived") }
                OutlinedButton(onClick = { tab = 4 }) { Text("Add") }
            }
            when (tab) {
                0 -> BlocksScreen(vm)
                1 -> UsageScreen()
                2 -> ReportsScreen()
                3 -> ArchivedScreen()
                4 -> AddBlockScreen(vm)
            }
        }
    }
}

@Composable fun UsageScreen() { Text("Usage") }
@Composable fun ReportsScreen() { Text("Reports") }
@Composable fun ArchivedScreen() { Text("Archived") }
