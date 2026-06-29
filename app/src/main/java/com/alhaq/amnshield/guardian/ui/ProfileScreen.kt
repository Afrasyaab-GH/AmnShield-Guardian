package com.alhaq.amnshield.guardian.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alhaq.amnshield.guardian.auth.viewmodel.AuthViewModel
import com.alhaq.amnshield.guardian.auth.model.AuthState
import com.alhaq.amnshield.guardian.auth.model.IdentityMode
import com.alhaq.amnshield.guardian.viewmodel.PremiumViewModel

/**
 * Profile Screen - User account and identity management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentAccount by authViewModel.currentAccount.collectAsState()
    val premiumManager = premiumViewModel.premiumManager
    val isPremium = premiumManager.isPremium()
    val compassionateDetails = premiumManager.getCompassionateAccessDetails()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        ProfileHeader(authState, currentAccount?.username)
        
        // Account Options
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        when (authState) {
            is AuthState.Authenticated -> {
                AuthenticatedOptions(
                    username = currentAccount?.username ?: "User",
                    identityMode = (authState as AuthState.Authenticated).identityMode,
                    onSignOut = { /* TODO: Implement sign out */ }
                )
            }
            else -> {
                UnauthenticatedOptions(
                    onIdentityModeSelect = { navController.navigate("identityMode") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Premium Section
        Text(
            text = "Premium",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        PremiumCard(
            isPremium = isPremium,
            compassionateDetails = compassionateDetails,
            onPremiumClick = { navController.navigate("premium") },
            onCompassionateAccess = { navController.navigate("compassionateAccess") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Guardian Integration
        Text(
            text = "Guardian Hub",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        GuardianHubCard()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // About & Support
        Text(
            text = "Support",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        SupportCard()
    }
}

@Composable
private fun ProfileHeader(authState: AuthState, username: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = username ?: "Guest User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val statusText = when (authState) {
                is AuthState.Authenticated -> {
                    when (authState.identityMode) {
                        IdentityMode.EMAIL_ACCOUNT -> "Email Account"
                        IdentityMode.LOCAL_ACCOUNT -> "Local Account"
                        IdentityMode.DEVICE_ID -> "Device ID"
                        IdentityMode.NO_IDENTITY -> "Anonymous"
                    }
                }
                is AuthState.Loading -> "Loading..."
                else -> "Not signed in"
            }
            
            AssistChip(
                onClick = {},
                label = { Text(statusText) },
                leadingIcon = {
                    Icon(
                        imageVector = if (authState is AuthState.Authenticated) 
                            Icons.Default.CheckCircle 
                        else 
                            Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedOptions(
    username: String,
    identityMode: IdentityMode,
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Username",
                subtitle = username,
                onClick = {}
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.Security,
                title = "Identity Mode",
                subtitle = identityMode.name.replace("_", " "),
                onClick = {}
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.Logout,
                title = "Sign Out",
                subtitle = "Sign out of your account",
                onClick = onSignOut,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun UnauthenticatedOptions(onIdentityModeSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create an account to unlock full features",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onIdentityModeSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose Identity Mode")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumCard(
    isPremium: Boolean,
    compassionateDetails: com.alhaq.amnshield.guardian.premium.CompassionateAccessDetails?,
    onPremiumClick: () -> Unit,
    onCompassionateAccess: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium && compassionateDetails != null)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = if (!isPremium) onPremiumClick else { {} }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Show compassionate access details if granted
            if (isPremium && compassionateDetails != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "âœ¨ Compassionate Access Active",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "${compassionateDetails.getDaysRemaining()} days remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "App ID: ${compassionateDetails.appId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
                
                if (compassionateDetails.isExpiringSoon()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "âš ï¸ Your access expires soon. You can re-apply when needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
                        Text(
                            text = "Premium Features",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Unlock advanced protection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            if (!isPremium) {
                Spacer(modifier = Modifier.height(12.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Compassionate Access Link
                TextButton(
                    onClick = onCompassionateAccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "I can't afford to pay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun GuardianHubCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DeenShield Ecosystem",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Connect with DeenShield App and NetBlock for unified protection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Connect DeenShield App */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("DeenShield App", style = MaterialTheme.typography.labelSmall)
                }
                
                OutlinedButton(
                    onClick = { /* Connect NetBlock */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("NetBlock", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SupportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileMenuItem(
                icon = Icons.Default.Help,
                title = "Help & FAQ",
                subtitle = "Get help using the app",
                onClick = {}
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.Email,
                title = "Contact Support",
                subtitle = "support@alhaq-initiative.org",
                onClick = {}
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "DeenShield Guardian v2.0.0",
                onClick = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = tint
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tint.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tint.copy(alpha = 0.5f)
            )
        }
    }
}

