package com.alhaq.amnshield.guardian.ui.auth

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
import com.alhaq.amnshield.guardian.auth.viewmodel.AuthViewModel
import com.alhaq.amnshield.guardian.auth.model.IdentityMode

/**
 * Identity Mode Selection Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityModeScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onIdentitySelected: (IdentityMode) -> Unit = {},
    onContinueWithoutAccount: () -> Unit = {}
) {
    val selectedMode by viewModel.selectedIdentityMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Identity Mode") },
                colors = TopAppBarDefaults.topAppBarColors(
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
            Text(
                text = "Select how you want to identify yourself in DeenShield Guardian",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            IdentityModeCard(
                icon = Icons.Default.Email,
                title = "Email Account",
                description = "Sign up with email address. Enables optional cloud sync and account recovery.",
                isSelected = selectedMode == IdentityMode.EMAIL_ACCOUNT,
                onClick = {
                    viewModel.selectIdentityMode(IdentityMode.EMAIL_ACCOUNT)
                    onIdentitySelected(IdentityMode.EMAIL_ACCOUNT)
                }
            )

            IdentityModeCard(
                icon = Icons.Default.Person,
                title = "Local Account",
                description = "Create local account with username and password. Works completely offline.",
                isSelected = selectedMode == IdentityMode.LOCAL_ACCOUNT,
                onClick = {
                    viewModel.selectIdentityMode(IdentityMode.LOCAL_ACCOUNT)
                    onIdentitySelected(IdentityMode.LOCAL_ACCOUNT)
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
