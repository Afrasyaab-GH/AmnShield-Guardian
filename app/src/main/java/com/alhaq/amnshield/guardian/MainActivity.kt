package com.alhaq.amnshield.guardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.alhaq.amnshield.guardian.data.BlockRepository
import com.alhaq.amnshield.guardian.ui.*
import com.alhaq.amnshield.guardian.ui.schedule.WeeklyScheduleScreen
import com.alhaq.amnshield.guardian.ui.theme.BlockerTheme
import com.alhaq.amnshield.guardian.viewmodel.BlockViewModel
import com.alhaq.amnshield.guardian.ui.auth.IdentityModeScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlockerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    GuardianApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianApp(vm: BlockViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    remember(Unit) { 
        vm.initRepository(BlockRepository.get(ctx))
        vm.setContext(ctx)
    }
    
    val nav: NavHostController = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    
    // Define main routes that show bottom nav
    val mainRoutes = listOf("home", "blocks", "appBlocks", "usage", "reports")
    val showBottomNav = currentRoute in mainRoutes
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                DrawerContent(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        nav.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "AmnShield Guardian",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { nav.navigate("profile") }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                if (currentRoute == "blocks") {
                    FloatingActionButton(
                        onClick = { nav.navigate("add") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Block")
                    }
                }
            },
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "blocks",
                            onClick = { nav.navigate("blocks") },
                            icon = { Icon(Icons.Default.Block, contentDescription = null) },
                            label = { Text("Blocks") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "appBlocks",
                            onClick = { nav.navigate("appBlocks") },
                            icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                            label = { Text("Apps") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "usage",
                            onClick = { nav.navigate("usage") },
                            icon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                            label = { Text("Usage") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "reports",
                            onClick = { nav.navigate("reports") },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                            label = { Text("Reports") }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = nav, 
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                // Main screens
                composable("home") { HomeScreen(vm, nav) }
                composable("blocks") { BlocksScreen(vm) }
                composable("appBlocks") { AppBlocksScreen(vm) }
                composable("usage") { UsageScreen() }
                composable("reports") { ReportsScreen() }
                
                // Feature screens
                composable("add") { AddBlockScreen(vm, nav) }
                composable("weeklySchedule") {
                    val prev = nav.previousBackStackEntry
                    val initialEncoded = prev?.savedStateHandle?.get<String>("weekly_initial")
                    val initial = decodeWeeklyFromEncoded(initialEncoded)
                    WeeklyScheduleScreen(
                        initial = initial,
                        onSave = { updated ->
                            val encoded = encodeWeeklyToEncoded(updated)
                            prev?.savedStateHandle?.set("weekly_result", encoded)
                            nav.popBackStack()
                        }
                    )
                }
                composable("focusMode") { FocusModeScreen() }
                composable("schedules") { SchedulesScreen() }
                
                // Settings & Profile
                composable("settings") { SettingsScreen(vm) }
                composable("profile") { ProfileScreen(nav) }
                composable("identityMode") { 
                    IdentityModeScreen(
                        onIdentitySelected = { nav.popBackStack() },
                        onContinueWithoutAccount = { nav.popBackStack() }
                    )
                }
                
                // Premium & Compassionate Access
                composable("premium") { PremiumScreen(nav) }
                composable("compassionateAccess") { CompassionateAccessScreen(nav) }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AmnShield Guardian",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your Protection Hub",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Navigation Items
        DrawerItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        
        DrawerItem(
            icon = Icons.Default.Block,
            label = "Content Blocks",
            selected = currentRoute == "blocks",
            onClick = { onNavigate("blocks") }
        )
        
        DrawerItem(
            icon = Icons.Default.Apps,
            label = "App Control",
            selected = currentRoute == "appBlocks",
            onClick = { onNavigate("appBlocks") }
        )
        
        DrawerItem(
            icon = Icons.Default.Timer,
            label = "Focus Mode",
            selected = currentRoute == "focusMode",
            onClick = { onNavigate("focusMode") }
        )
        
        DrawerItem(
            icon = Icons.Default.Schedule,
            label = "Schedules",
            selected = currentRoute == "schedules",
            onClick = { onNavigate("schedules") }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        DrawerItem(
            icon = Icons.Default.Timeline,
            label = "Usage Stats",
            selected = currentRoute == "usage",
            onClick = { onNavigate("usage") }
        )
        
        DrawerItem(
            icon = Icons.Default.Assessment,
            label = "Reports",
            selected = currentRoute == "reports",
            onClick = { onNavigate("reports") }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        DrawerItem(
            icon = Icons.Default.Star,
            label = "Premium",
            selected = currentRoute == "premium",
            onClick = { onNavigate("premium") }
        )
        
        DrawerItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") }
        )
        
        DrawerItem(
            icon = Icons.Default.Person,
            label = "Profile",
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        Text(
            text = "AmnShield Guardian v2.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

// Placeholder screens for unimplemented features
@Composable
fun UsageScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Usage Statistics", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Coming soon - Track your app usage and digital habits",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reports", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Coming soon - View detailed blocking reports",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FocusModeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Focus Mode", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Coming soon - Block distractions for focused work",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SchedulesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Schedules", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Coming soon - Schedule time-based blocking rules",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PremiumScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Premium Features",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Unlock Full Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Get access to all premium features across the AmnShield ecosystem.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Pricing options
        PricingCard("Lifetime", "Â£13.50", "One-time purchase", true)
        PricingCard("Yearly", "Â£2.50/month", "Billed annually Â£30", false)
        PricingCard("Monthly", "Â£3.50/month", "Cancel anytime", false)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Compassionate access link
        TextButton(
            onClick = { navController.navigate("compassionateAccess") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "I can't afford to pay",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PricingCard(
    title: String,
    price: String,
    subtitle: String,
    recommended: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recommended) 
                MaterialTheme.colorScheme.secondaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = { /* TODO: Handle purchase */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (recommended) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Best Value", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                price,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

