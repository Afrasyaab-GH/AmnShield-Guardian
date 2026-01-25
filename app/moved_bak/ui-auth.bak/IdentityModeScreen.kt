package com.deenshield.blocker.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.deenshield.blocker.auth.IdentityMode
import com.deenshield.blocker.auth.viewmodel.AuthViewModel

/**
 * Identity Mode Selection Screen
 * 
 * First screen in authentication flow. User selects their preferred identity mode:
 * - EMAIL: Sign up with email address (optional cloud sync)
 * - LOCAL: Local account with username/password (offline only)
 * - DEVICE_ID: Auto-generated device UUID (no registration)
 * - NO_IDENTITY: Continue without account (anonymous mode)
 * 
 * **Islamic Principle: Rida (Consent)**
 * - User explicitly chooses identity method
 * - Clear description of each option
 * - No hidden tracking or forced registration
 * - "Continue without account" always available
 * 
 * @param viewModel AuthViewModel for state management
 * @param onIdentitySelected Callback when identity mode is selected
 * @param onContinueWithoutAccount Callback for NO_IDENTITY mode
 */
@Composable
fun IdentityModeScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onIdentitySelected: (IdentityMode) -> Unit = {},
    onContinueWithoutAccount: () -> Unit = {}
) {
    val selectedMode by viewModel.selectedIdentityMode.collectAsState()
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Choose Your Identity Mode") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Select how you want to identify yourself in DeenShield Guardian",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Identity Mode Options
            IdentityModeCard(
                icon = Icons.Default.Email,
                title = "Email Account",
                description = "Sign up with email address. Enables optional cloud sync and account recovery.",
                isSelected = selectedMode == IdentityMode.EMAIL,
                onClick = {
                    viewModel.selectIdentityMode(IdentityMode.EMAIL)
                    onIdentitySelected(IdentityMode.EMAIL)
                }
            )
            
            IdentityModeCard(
                icon = Icons.Default.Person,
                title = "Local Account",
                description = "Create local account with username and password. Works completely offline.",
                isSelected = selectedMode == IdentityMode.LOCAL,
                onClick = {
                    viewModel.selectIdentityMode(IdentityMode.LOCAL)
                    onIdentitySelected(IdentityMode.LOCAL)
                }
            )
            
            IdentityModeCard(
                icon = Icons.Default.Smartphone,
                title = "Device ID",
                description = "Auto-generated unique device identifier. No registration required.",
                isSelected = selectedMode == IdentityMode.DEVICE_ID,
                onClick = {
                    viewModel.selectIdentityMode(IdentityMode.DEVICE_ID)
                    onIdentitySelected(IdentityMode.DEVICE_ID)
                }
            )
            
            IdentityModeCard(
                icon = Icons.Default.VisibilityOff,
                title = "Continue Without Account",
                description = "Anonymous mode. No account, no tracking. Limited features.",
                isSelected = selectedMode == IdentityMode.NO_IDENTITY,
                onClick = {
                    viewModel.selectIdentityMode(IdentityMode.NO_IDENTITY)
                    onContinueWithoutAccount()
                },
                isNoIdentityMode = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Islamic Principle Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Your Choice, Your Privacy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All authentication is local by default. We never force you to create an account or track you without consent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Identity Mode Selection Card
 * 
 * Displays icon, title, description, and selection state.
 * 
 * @param icon Material icon for this mode
 * @param title Mode title
 * @param description Mode description
 * @param isSelected Whether this mode is currently selected
 * @param onClick Callback when card is clicked
 * @param isNoIdentityMode Special styling for NO_IDENTITY mode
 */
@Composable
private fun IdentityModeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isNoIdentityMode: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isNoIdentityMode -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isNoIdentityMode -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isNoIdentityMode -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
            
            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Selection Indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Preview for IdentityModeScreen
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun IdentityModeScreenPreview() {
    MaterialTheme {
        // Preview without ViewModel injection
        // Use actual ViewModel in production
    }
}
