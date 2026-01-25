package com.deenshield.blocker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ReportsScreen - Displays detailed reports and analytics
 * Shows daily, weekly, and monthly reports with export options
 */
@Composable
fun ReportsScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily", "Weekly", "Monthly")
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Reports & Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            // Tab Selection
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = Color(0xFF60A5FA)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = index == selectedTab,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title,
                                fontWeight = if (index == selectedTab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Content based on selected tab
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> DailyReportContent()
                1 -> WeeklyReportContent()
                2 -> MonthlyReportContent()
            }
        }
    }
}

@Composable
private fun DailyReportContent() {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Date Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendar",
                    tint = Color(0xFF60A5FA)
                )
                Column {
                    Text(
                        text = "Today's Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = today.format(formatter),
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        // Daily Stats
        ReportStatsGrid()

        // App Blocker Report
        ReportSection(
            title = "App Blocker Report",
            items = listOf(
                ReportItem("Instagram", 12, "Blocked", Color(0xFFF87171)),
                ReportItem("YouTube", 8, "Blocked", Color(0xFFF87171)),
                ReportItem("TikTok", 5, "Blocked", Color(0xFFF87171)),
                ReportItem("Twitter", 3, "Blocked", Color(0xFFF87171))
            )
        )

        // Keyword Blocker Report
        ReportSection(
            title = "Keyword Blocker Report",
            items = listOf(
                ReportItem("Adult Content", 7, "Blocked", Color(0xFFF87171)),
                ReportItem("Harmful Content", 4, "Blocked", Color(0xFFF87171)),
                ReportItem("Distracting Sites", 2, "Blocked", Color(0xFFF87171))
            )
        )

        // Export Button
        ExportButton()
    }
}

@Composable
private fun WeeklyReportContent() {
    val weekStartDate = LocalDate.now().minusDays(7)
    val formatter = DateTimeFormatter.ofPattern("MMM dd")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Week Header
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
                    text = "Weekly Report",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "${weekStartDate.format(formatter)} - ${LocalDate.now().format(formatter)}",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        // Weekly Summary
        WeeklyReportStats()

        // Daily Breakdown
        ReportSection(
            title = "Daily Breakdown",
            items = listOf(
                ReportItem("Monday", 45, "blocks", Color(0xFF60A5FA)),
                ReportItem("Tuesday", 38, "blocks", Color(0xFF60A5FA)),
                ReportItem("Wednesday", 52, "blocks", Color(0xFF60A5FA)),
                ReportItem("Thursday", 41, "blocks", Color(0xFF60A5FA)),
                ReportItem("Friday", 39, "blocks", Color(0xFF60A5FA)),
                ReportItem("Saturday", 48, "blocks", Color(0xFF60A5FA)),
                ReportItem("Sunday", 55, "blocks", Color(0xFF60A5FA))
            )
        )

        // Trends
        TrendsCard()

        ExportButton()
    }
}

@Composable
private fun MonthlyReportContent() {
    val monthStart = LocalDate.now().minusMonths(1)
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Month Header
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
                    text = "Monthly Report",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = monthStart.format(formatter),
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        // Monthly Stats
        MonthlyStatsCard()

        // Top Apps Blocked
        ReportSection(
            title = "Most Blocked Apps",
            items = listOf(
                ReportItem("Instagram", 342, "blocks", Color(0xFFA78BFA)),
                ReportItem("YouTube", 287, "blocks", Color(0xFFA78BFA)),
                ReportItem("TikTok", 198, "blocks", Color(0xFFA78BFA)),
                ReportItem("Twitter", 145, "blocks", Color(0xFFA78BFA)),
                ReportItem("Reddit", 92, "blocks", Color(0xFFA78BFA))
            )
        )

        // Achievements
        AchievementsCard()

        ExportButton()
    }
}

@Composable
private fun ReportStatsGrid() {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(label = "App Blocks", value = "38", color = Color(0xFFF87171), modifier = Modifier.weight(1f))
                StatCard(label = "Keyword Blocks", value = "12", color = Color(0xFFA78BFA), modifier = Modifier.weight(1f))
                StatCard(label = "View Blocks", value = "5", color = Color(0xFFFFB86C), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF6B7280))
    }
}

data class ReportItem(val name: String, val value: Int, val unit: String, val color: Color)

@Composable
private fun ReportSection(title: String, items: List<ReportItem>) {
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
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(item.color.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxSize()
                                .background(item.color, shape = RoundedCornerShape(2.dp))
                        )
                    }
                    Text(
                        text = "${item.value} ${item.unit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = item.color,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(70.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyReportStats() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(label = "Total Blocks", value = "318", color = Color(0xFF60A5FA), modifier = Modifier.weight(1f))
                StatCard(label = "Avg Daily", value = "45", color = Color(0xFF60A5FA), modifier = Modifier.weight(1f))
                StatCard(label = "Peak Day", value = "Saturday", color = Color(0xFF60A5FA), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TrendsCard() {
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
                text = "Trends",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = "📈 Blocking activity increased by 12% this week",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "📱 Most blocked on mobile apps (Social Media category)",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "⏰ Peak blocking time: 2-4 PM daily",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun MonthlyStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(label = "Total Blocks", value = "1,247", color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
                StatCard(label = "Avg Daily", value = "41", color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
                StatCard(label = "Unique Apps", value = "24", color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎉 Achievements",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF16A34A)
            )
            Text(
                text = "✓ Focused Guardian - 1000+ blocking actions",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "✓ Consistent Protector - 30 days of active protection",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
            Text(
                text = "✓ Productivity Master - 50+ hours in focus mode",
                fontSize = 14.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun ExportButton() {
    Button(
        onClick = { /* Export functionality */ },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA))
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download",
            modifier = Modifier.padding(end = 8.dp),
            tint = Color.White
        )
        Text(text = "Export Report")
    }
}


