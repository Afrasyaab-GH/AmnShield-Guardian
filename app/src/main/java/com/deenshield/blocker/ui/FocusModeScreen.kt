package com.deenshield.blocker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * FocusModeScreen - Manage focus sessions for productivity
 * Configure allowed apps, set duration, and track sessions
 */
@Composable
fun FocusModeScreen(modifier: Modifier = Modifier) {
    var isSessionActive by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Focus Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            if (isSessionActive) {
                ActiveSessionCard(onStop = { isSessionActive = false })
            } else {
                CreateSessionCard(
                    onStart = { isSessionActive = true }
                )
            }

            // Quick Templates
            QuickTemplatesCard()

            // Session History
            SessionHistoryCard()

            // Tips
            FocusModeTipsCard()
        }
    }
}

@Composable
private fun ActiveSessionCard(onStop: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯 Focus Session Active",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A)
            )

            // Timer Display
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(140.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "1h 23m",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                    Text(
                        text = "Remaining",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Progress
            LinearProgressIndicator(
                progress = 0.65f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF16A34A),
                trackColor = Color(0xFFDCFCE7)
            )

            // Allowed Apps
            Text(
                text = "Allowed Apps: Messages, Phone, Maps",
                fontSize = 13.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center
            )

            // Stop Button
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Stop Focus Session")
            }
        }
    }
}

@Composable
private fun CreateSessionCard(onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create New Focus Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            // Session Type
            SessionConfigItem(
                label = "Session Type",
                value = "Deep Work (90 min)",
                icon = "⏱️"
            )

            // Allowed Apps
            SessionConfigItem(
                label = "Allowed Apps",
                value = "Messages, Phone, Maps",
                icon = "✓"
            )

            // Blocked Apps
            SessionConfigItem(
                label = "Blocked Apps",
                value = "24 apps selected",
                icon = "🚫"
            )

            // Breaks
            SessionConfigItem(
                label = "Break Duration",
                value = "5 minutes every 30 min",
                icon = "☕"
            )

            // Start Button
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Start Focus Session")
            }
        }
    }
}

@Composable
private fun SessionConfigItem(label: String, value: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
        }
        Text(
            text = "→",
            fontSize = 18.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun QuickTemplatesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Templates",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            listOf(
                FocusTemplate("Deep Work", "90 min", "📚"),
                FocusTemplate("Quick Focus", "25 min", "⚡"),
                FocusTemplate("Study Mode", "60 min", "🎓"),
                FocusTemplate("Break Time", "10 min", "☕")
            ).forEach { template ->
                TemplateButton(template)
            }
        }
    }
}

data class FocusTemplate(val name: String, val duration: String, val emoji: String)

@Composable
private fun TemplateButton(template: FocusTemplate) {
    Button(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF3F4F6),
            contentColor = Color(0xFF1F2937)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(template.emoji, fontSize = 20.sp)
                Column {
                    Text(
                        template.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                template.duration,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun SessionHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recent Sessions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            listOf(
                SessionHistoryItem("Today", "Deep Work", "90 min", "✓ Completed", Color(0xFF16A34A)),
                SessionHistoryItem("Yesterday", "Study Mode", "60 min", "✓ Completed", Color(0xFF16A34A)),
                SessionHistoryItem("2 days ago", "Quick Focus", "25 min", "⊗ Interrupted", Color(0xFFEAB308))
            ).forEach { item ->
                SessionHistoryItemView(item)
            }
        }
    }
}

data class SessionHistoryItem(
    val date: String,
    val name: String,
    val duration: String,
    val status: String,
    val statusColor: Color
)

@Composable
private fun SessionHistoryItemView(item: SessionHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = Color(0xFFD1D5DB)
                )
                Text(
                    text = item.duration,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
        Text(
            text = item.status,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = item.statusColor
        )
    }
}

@Composable
private fun FocusModeTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💡 Focus Tips",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E40AF)
            )

            Text(
                text = "• Start with 25-minute sessions (Pomodoro technique)",
                fontSize = 13.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "• Take 5-minute breaks between sessions",
                fontSize = 13.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "• Close notifications during focus mode",
                fontSize = 13.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "• Track your progress in the Reports tab",
                fontSize = 13.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}


