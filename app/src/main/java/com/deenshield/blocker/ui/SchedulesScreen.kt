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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
 * SchedulesScreen - Manage time-based blocking schedules
 * Configure daily schedules, exceptions, and automatic blocking times
 */
@Composable
fun SchedulesScreen(modifier: Modifier = Modifier) {
    var showAddSchedule by remember { mutableStateOf(false) }
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedules",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            // Quick Schedule Templates
            QuickScheduleTemplatesCard()

            // Active Schedules
            ActiveSchedulesCard()

            // Weekly Schedule
            WeeklyScheduleCard()

            // Create Custom Schedule Info
            Text(
                text = "Create custom schedules to automatically block apps and content during specific times.",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddSchedule = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF60A5FA),
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule")
        }
    }
}

@Composable
private fun QuickScheduleTemplatesCard() {
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
                ScheduleTemplate("School Hours", "9:00 AM - 3:00 PM", "📚", listOf("Mon", "Tue", "Wed", "Thu", "Fri")),
                ScheduleTemplate("Sleep Time", "10:00 PM - 6:00 AM", "😴", listOf("Every Day")),
                ScheduleTemplate("Work Hours", "9:00 AM - 5:00 PM", "💼", listOf("Mon", "Tue", "Wed", "Thu", "Fri")),
                ScheduleTemplate("Family Time", "6:00 PM - 8:00 PM", "👨‍👩‍👧", listOf("Every Day"))
            ).forEach { template ->
                ScheduleTemplateButton(template)
            }
        }
    }
}

data class ScheduleTemplate(
    val name: String,
    val time: String,
    val emoji: String,
    val days: List<String>
)

@Composable
private fun ScheduleTemplateButton(template: ScheduleTemplate) {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF3F4F6),
            contentColor = Color(0xFF1F2937)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(template.emoji, fontSize = 24.sp)
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        template.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        template.time,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            Text(
                "→",
                fontSize = 20.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
private fun ActiveSchedulesCard() {
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
                text = "Active Schedules",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            listOf(
                ActiveSchedule("School Hours", "9:00 AM - 3:00 PM", "Mon - Fri", Color(0xFF60A5FA), true),
                ActiveSchedule("Sleep Time", "10:00 PM - 6:00 AM", "Every Day", Color(0xFFA78BFA), true),
                ActiveSchedule("Break Time", "12:00 PM - 1:00 PM", "Mon, Wed, Fri", Color(0xFF34D399), false)
            ).forEach { schedule ->
                ScheduleItem(schedule)
            }
        }
    }
}

data class ActiveSchedule(
    val name: String,
    val time: String,
    val days: String,
    val color: Color,
    val isActive: Boolean
)

@Composable
private fun ScheduleItem(schedule: ActiveSchedule) {
    var isActive by remember { mutableStateOf(schedule.isActive) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(schedule.color, shape = RoundedCornerShape(2.dp))
                )
                Text(
                    text = schedule.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
            }
            Text(
                text = "${schedule.time} • ${schedule.days}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
        Switch(
            checked = isActive,
            onCheckedChange = { isActive = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = schedule.color,
                checkedTrackColor = schedule.color.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun WeeklyScheduleCard() {
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
                text = "Weekly View",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            // Days header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    DayScheduleView(day, day in listOf("Mon", "Wed", "Fri", "Sat", "Sun"), Modifier.weight(1f))
                }
            }

            // Schedule grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    DayScheduleBlock("School", "9:00 AM - 3:00 PM"),
                    DayScheduleBlock("Focus Time", "6:00 PM - 8:00 PM"),
                    DayScheduleBlock("Sleep", "10:00 PM - 6:00 AM")
                ).forEach { block ->
                    Text(
                        text = block.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..7).forEach { day ->
                            val isScheduled = (day % 2 == 0) || (day == 5)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(
                                        color = if (isScheduled) Color(0xFF60A5FA) else Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isScheduled) {
                                    Text("✓", fontSize = 16.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayScheduleView(day: String, isActive: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = if (isActive) Color(0xFFDEF7FF) else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Color(0xFF0369A1) else Color(0xFF6B7280)
        )
    }
}

data class DayScheduleBlock(val name: String, val time: String)


