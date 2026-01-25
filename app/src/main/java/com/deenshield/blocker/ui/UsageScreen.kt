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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * UsageScreen - Displays app usage statistics and screen time analytics
 * Shows daily usage breakdown, app-wise statistics, and time tracking
 */
@Composable
fun UsageScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Usage Statistics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        // Today's Date
        Text(
            text = "Today: ${today.format(formatter)}",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )

        // Daily Summary Card
        UsageSummaryCard(
            screenTime = "4h 32m",
            appOpens = 142,
            blockedAttempts = 38,
            focusTime = "2h 15m"
        )

        // Usage by Category
        UsageCategoryCard(
            title = "App Usage Breakdown",
            categories = listOf(
                CategoryUsage("Social Media", 65, Color(0xFF60A5FA)),
                CategoryUsage("Entertainment", 45, Color(0xFFA78BFA)),
                CategoryUsage("Productivity", 22, Color(0xFF34D399)),
                CategoryUsage("Others", 10, Color(0xFFFCD34D))
            )
        )

        // Most Used Apps
        MostUsedAppsCard(
            apps = listOf(
                AppUsageItem("Instagram", "1h 23m", 42),
                AppUsageItem("YouTube", "58m", 35),
                AppUsageItem("TikTok", "45m", 28),
                AppUsageItem("WhatsApp", "32m", 18),
                AppUsageItem("Chrome", "28m", 15)
            )
        )

        // Blocked Content Summary
        BlockedContentCard(
            appBlocks = 38,
            keywordBlocks = 12,
            viewBlocks = 5,
            totalBlocked = 55
        )

        // Weekly Summary
        WeeklyTrendCard()

        Text(
            text = "Detailed analytics and trends are available in the Reports tab.",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun UsageSummaryCard(
    screenTime: String,
    appOpens: Int,
    blockedAttempts: Int,
    focusTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Screen Time",
                    value = screenTime,
                    color = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "App Opens",
                    value = appOpens.toString(),
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Blocked",
                    value = blockedAttempts.toString(),
                    color = Color(0xFFF87171),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Focus Time",
                    value = focusTime,
                    color = Color(0xFF34D399),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}

data class CategoryUsage(val name: String, val percentage: Int, val color: Color)

@Composable
private fun UsageCategoryCard(
    title: String,
    categories: List<CategoryUsage>
) {
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

            categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = category.name,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(8.dp)
                            .background(
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(category.percentage / 100f)
                                .fillMaxSize()
                                .background(
                                    color = category.color,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Text(
                        text = "${category.percentage}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = category.color,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

data class AppUsageItem(val name: String, val duration: String, val percentage: Int)

@Composable
private fun MostUsedAppsCard(apps: List<AppUsageItem>) {
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
                text = "Most Used Apps",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            apps.forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = app.duration,
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    Text(
                        text = "${app.percentage}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA)
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedContentCard(
    appBlocks: Int,
    keywordBlocks: Int,
    viewBlocks: Int,
    totalBlocked: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF2F2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Blocked Content",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFDC2626)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlockStat(label = "Apps", value = appBlocks, modifier = Modifier.weight(1f))
                BlockStat(label = "Keywords", value = keywordBlocks, modifier = Modifier.weight(1f))
                BlockStat(label = "Views", value = viewBlocks, modifier = Modifier.weight(1f))
            }

            Text(
                text = "Total Blocked: $totalBlocked attempts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFDC2626),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BlockStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDC2626)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun WeeklyTrendCard() {
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
                text = "Weekly Trend",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(45, 62, 38, 71, 54, 68, 72).forEachIndexed { index, height ->
                    DayBar(
                        day = "SMTWRFS"[index].toString(),
                        height = height,
                        isToday = index == 6,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Trend: Increasing usage over the week",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun DayBar(day: String, height: Int, isToday: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((height * 2).dp)
                .background(
                    color = if (isToday) Color(0xFF60A5FA) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Text(
            text = day,
            fontSize = 11.sp,
            color = if (isToday) Color(0xFF60A5FA) else Color(0xFF9CA3AF),
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}


