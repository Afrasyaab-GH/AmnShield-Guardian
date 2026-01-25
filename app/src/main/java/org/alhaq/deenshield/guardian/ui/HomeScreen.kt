package org.alhaq.deenshield.guardian.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.alhaq.deenshield.guardian.service.BlockingVpnService
import org.alhaq.deenshield.guardian.viewmodel.BlockViewModel

/**
 * Home Screen - Main dashboard for DeenShield Guardian
 * Styled consistently with DeenShield app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: BlockViewModel,
    navController: NavController
) {
    val ctx = LocalContext.current
    var vpnActive by remember { mutableStateOf(false) }
    var accessibilityActive by remember { mutableStateOf(false) }
    
    // Check service statuses periodically
    LaunchedEffect(Unit) {
        while (true) {
            vpnActive = isVpnServiceActive(ctx)
            accessibilityActive = isAccessibilityServiceEnabled(ctx)
            delay(2000)
        }
    }
    
    val vpnConsentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            ctx.startService(Intent(ctx, BlockingVpnService::class.java).apply {
                action = BlockingVpnService.ACTION_START
            })
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        WelcomeCard()
        
        // Service Status Section
        Text(
            text = "Protection Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        ServiceStatusCard(
            title = "VPN Protection",
            description = "Network-level content filtering",
            isActive = vpnActive,
            icon = Icons.Default.Shield,
            onToggle = {
                if (!vpnActive) {
                    val prepare = VpnService.prepare(ctx)
                    if (prepare != null) {
                        vpnConsentLauncher.launch(prepare)
                    } else {
                        ctx.startService(Intent(ctx, BlockingVpnService::class.java).apply {
                            action = BlockingVpnService.ACTION_START
                        })
                    }
                } else {
                    ctx.startService(Intent(ctx, BlockingVpnService::class.java).apply {
                        action = BlockingVpnService.ACTION_STOP
                    })
                }
            }
        )
        
        ServiceStatusCard(
            title = "Accessibility Service",
            description = "App-level blocking and monitoring",
            isActive = accessibilityActive,
            icon = Icons.Default.Accessibility,
            onToggle = {
                ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Quick Actions Section
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Feature Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Content Blocks",
                description = "Website & keyword filters",
                icon = Icons.Default.Block,
                count = vm.blocks.size,
                isActive = vm.blocks.isNotEmpty(),
                onClick = { navController.navigate("blocks") }
            )
            
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "App Control",
                description = "Per-app network access",
                icon = Icons.Default.Apps,
                count = vm.appRules.count { it.isBlocked() },
                isActive = vm.appRules.any { it.isBlocked() },
                onClick = { navController.navigate("appBlocks") }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Focus Mode",
                description = "Block distractions",
                icon = Icons.Default.Timer,
                count = 0,
                isActive = false,
                onClick = { navController.navigate("focusMode") }
            )
            
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Schedules",
                description = "Time-based blocking",
                icon = Icons.Default.Schedule,
                count = vm.blocks.count { it.weeklySchedule.isNotEmpty() },
                isActive = vm.blocks.any { it.weeklySchedule.isNotEmpty() },
                onClick = { navController.navigate("schedules") }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Global Settings Section
        Text(
            text = "Global Filters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        GlobalFilterCard(vm)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Guardian Integration Card
        IntegrationCard(navController)
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Welcome to DeenShield Guardian",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Centralized protection for your digital wellbeing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ServiceStatusCard(
    title: String,
    description: String,
    isActive: Boolean,
    icon: ImageVector,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                Color(0xFF1B5E20).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { 
                        Text(
                            if (isActive) "Active" else "Inactive",
                            fontSize = 11.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isActive) 
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                )
                
                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                
                AssistChip(
                    onClick = {},
                    label = { 
                        Text(
                            if (isActive) "On" else "Off",
                            fontSize = 10.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isActive) 
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                if (count > 0) {
                    Text(
                        text = "$count configured",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalFilterCard(vm: BlockViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlobalFilterToggle(
                title = "Block Harmful Keywords",
                description = "Filter adult and harmful content keywords",
                isChecked = vm.blockHarmfulKeywords,
                onCheckedChange = { vm.updateBlockHarmfulKeywords(it) }
            )
            
            HorizontalDivider()
            
            GlobalFilterToggle(
                title = "Block Harmful Websites",
                description = "Block known harmful and adult websites",
                isChecked = vm.blockHarmfulWebsites,
                onCheckedChange = { vm.updateBlockHarmfulWebsites(it) }
            )
            
            HorizontalDivider()
            
            GlobalFilterToggle(
                title = "Block Social Media",
                description = "Block popular social media platforms",
                isChecked = vm.blockSocialMedia,
                onCheckedChange = { vm.updateBlockSocialMedia(it) }
            )
        }
    }
}

@Composable
private fun GlobalFilterToggle(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntegrationCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = { navController.navigate("profile") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Guardian Integration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Connect with DeenShield ecosystem for centralized control",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
