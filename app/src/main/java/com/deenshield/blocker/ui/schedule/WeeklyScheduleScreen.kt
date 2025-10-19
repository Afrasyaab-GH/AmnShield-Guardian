package com.deenshield.blocker.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.launch

/**
 * WeeklyScheduleScreen
 *
 * Inputs:
 * - initial: Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>
 * - onSave: callback invoked with updated map when user taps Save
 *
 * Behavior:
 * - Presets (School/Work, Bedtime, Weekend)
 * - Weekly preview with tappable bars to jump to day sections
 * - Day cards with interval chips and time-range add dialog
 * - Shortcuts per-day to apply/copy intervals
 */
@Composable
fun WeeklyScheduleScreen(
    initial: Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>,
    onSave: (Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>) -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Internal mutable state
    val daysOrder = remember { listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    ) }

    var schedule by remember {
        mutableStateOf(
            daysOrder.associateWith { day ->
                initial[day]?.map { it.first to it.second }?.toMutableList() ?: mutableListOf()
            }.toMutableMap()
        )
    }

    val listState = rememberLazyListState()

    Scaffold(
        bottomBar = {
            BottomAppBar(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        // Return immutable copy
                        val out = schedule.mapValues { it.value.toList() }
                        onSave(out)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .semantics { contentDescription = "Save Schedule" }
                ) {
                    Icon(Icons.Filled.Done, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Save Schedule")
                }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item { ScreenHeader() }
            item { PresetsRow(onApply = { type -> applyPreset(type, schedule) { schedule = it } }) }
            item { WeeklyPreview(daysOrder, schedule, listState) }
            items(daysOrder) { day ->
                DayCard(
                    day = day,
                    intervals = schedule[day] ?: mutableListOf(),
                    onIntervalsChange = { 
                        schedule = schedule.toMutableMap().apply { put(day, it.toMutableList()) }
                    },
                    onApplyWeekdays = {
                        // apply this day's intervals to Mon-Fri
                        val src = schedule[day]?.toList() ?: emptyList()
                        schedule = schedule.toMutableMap().apply {
                            DayOfWeek.MONDAY..DayOfWeek.FRIDAY
                            listOf(
                                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                            ).forEach { put(it, src.toMutableList()) }
                        }
                    },
                    onCopyToAll = {
                        val src = schedule[day]?.toList() ?: emptyList()
                        schedule = schedule.toMutableMap().apply {
                            DayOfWeek.entries.forEach { put(it, src.toMutableList()) }
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun ScreenHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Weekly Schedule",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Configure when blocking is active. Use presets or customize per day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private enum class Preset { SCHOOL_WORK, BEDTIME, WEEKEND }

@Composable
private fun PresetsRow(onApply: (Preset) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = { onApply(Preset.SCHOOL_WORK) }) { Text("School/Work Mode") }
        FilledTonalButton(onClick = { onApply(Preset.BEDTIME) }) { Text("Bedtime Block") }
        FilledTonalButton(onClick = { onApply(Preset.WEEKEND) }) { Text("Weekend Lockdown") }
    }
}

private fun applyPreset(
    preset: Preset,
    current: Map<DayOfWeek, MutableList<Pair<LocalTime, LocalTime>>>,
    update: (MutableMap<DayOfWeek, MutableList<Pair<LocalTime, LocalTime>>>) -> Unit
) {
    val copy = current.mapValues { it.value.toMutableList() }.toMutableMap()
    fun add(day: DayOfWeek, start: LocalTime, end: LocalTime) {
        // Handle cross-midnight by splitting
        if (end.isAfter(start)) {
            copy[day] = (copy[day] ?: mutableListOf()).apply { add(start to end) }
        } else {
            // e.g. 22:00 -> 07:00 => [22:00-23:59], [00:00-07:00]
            copy[day] = (copy[day] ?: mutableListOf()).apply { add(start to LocalTime.of(23, 59)) }
            val next = nextDay(day)
            copy[next] = (copy[next] ?: mutableListOf()).apply { add(LocalTime.MIDNIGHT to end) }
        }
    }
    when (preset) {
        Preset.SCHOOL_WORK -> {
            val rng = LocalTime.of(8, 0) to LocalTime.of(16, 0)
            listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            ).forEach { copy[it] = mutableListOf(rng) }
        }
        Preset.BEDTIME -> {
            val start = LocalTime.of(22, 0)
            val end = LocalTime.of(7, 0)
            DayOfWeek.entries.forEach { add(it, start, end) }
        }
        Preset.WEEKEND -> {
            val rng = LocalTime.of(10, 0) to LocalTime.of(22, 0)
            listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).forEach { copy[it] = mutableListOf(rng) }
        }
    }
    update(copy)
}

private fun nextDay(d: DayOfWeek): DayOfWeek = when (d) {
    DayOfWeek.MONDAY -> DayOfWeek.TUESDAY
    DayOfWeek.TUESDAY -> DayOfWeek.WEDNESDAY
    DayOfWeek.WEDNESDAY -> DayOfWeek.THURSDAY
    DayOfWeek.THURSDAY -> DayOfWeek.FRIDAY
    DayOfWeek.FRIDAY -> DayOfWeek.SATURDAY
    DayOfWeek.SATURDAY -> DayOfWeek.SUNDAY
    DayOfWeek.SUNDAY -> DayOfWeek.MONDAY
}

@Composable
private fun WeeklyPreview(
    daysOrder: List<DayOfWeek>,
    schedule: Map<DayOfWeek, List<Pair<LocalTime, LocalTime>>>,
    listState: LazyListState
) {
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        daysOrder.forEachIndexed { index, day ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(dayLabel(day), modifier = Modifier.width(56.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                DayTimelineBar(
                    intervals = schedule[day] ?: emptyList(),
                    onClick = { scope.launch { listState.animateScrollToItem(index + 3) } }, // offset after header+presets+preview
                )
            }
        }
    }
}

@Composable
private fun DayTimelineBar(
    intervals: List<Pair<LocalTime, LocalTime>>,
    onClick: () -> Unit,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.primary
    val radius = 6.dp
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clickable(onClick = onClick)
    ) {
        // Draw background track as rounded rect
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
            size = Size(size.width, size.height)
        )
        fun minutesOf(t: LocalTime) = t.hour * 60 + t.minute
        val flat = intervals.flatMap { (s, e) ->
            if (e.isAfter(s)) listOf(s to e)
            else listOf(s to LocalTime.of(23, 59), LocalTime.MIDNIGHT to e)
        }.sortedBy { minutesOf(it.first) }
        val total = 24f * 60f
        flat.forEach { (start, end) ->
            val startPx = (minutesOf(start) / total) * size.width
            val endPx = (minutesOf(end) / total) * size.width
            val left = startPx.coerceAtLeast(0f)
            val right = endPx.coerceAtLeast(left)
            val segWidth = (right - left)
            if (segWidth > 0f) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(left, 0f),
                    size = Size(segWidth, size.height),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                )
            }
        }
    }
}

@Composable
private fun DayCard(
    day: DayOfWeek,
    intervals: List<Pair<LocalTime, LocalTime>>,
    onIntervalsChange: (List<Pair<LocalTime, LocalTime>>) -> Unit,
    onApplyWeekdays: () -> Unit,
    onCopyToAll: () -> Unit
) {
    val ctx = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Shortcuts above the card
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onApplyWeekdays) {
                Icon(Icons.Filled.Schedule, contentDescription = "Apply to weekdays")
                Spacer(Modifier.size(6.dp))
                Text("Apply to weekdays")
            }
            OutlinedButton(onClick = onCopyToAll) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy to all days")
                Spacer(Modifier.size(6.dp))
                Text("Copy to all days")
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(dayLabel(day), style = MaterialTheme.typography.titleLarge)
                if (intervals.isEmpty()) {
                    Text(
                        "No intervals yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    FlowChips(
                        items = intervals.map { it to labelOf(it) },
                        onRemove = { toRemove ->
                            onIntervalsChange(intervals.filterNot { it == toRemove })
                        }
                    )
                }
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.semantics { contentDescription = "Add interval for ${dayLabel(day)}" }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Add Interval")
                }
            }
        }
    }

    if (showDialog) {
        TimeRangeDialog(
            onDismiss = { showDialog = false },
            onConfirm = { start, end ->
                val newList = intervals.toMutableList()
                // Only add valid ranges; allow same-hour different minutes
                if (end != start) {
                    newList.add(start to end)
                    onIntervalsChange(normalizeRanges(newList))
                }
                showDialog = false
            }
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowChips(
    items: List<Pair<Pair<LocalTime, LocalTime>, String>>,
    onRemove: (Pair<LocalTime, LocalTime>) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (range, label) ->
            AssistChip(
                onClick = { /* no-op */ },
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove interval $label"
                    )
                },
                modifier = Modifier.clickable { onRemove(range) }
            )
        }
    }
}

@Composable
private fun TimeRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime) -> Unit
) {
    val ctx = LocalContext.current
    var start by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var end by remember { mutableStateOf(LocalTime.of(17, 0)) }

    fun showPicker(isStart: Boolean) {
        val base = if (isStart) start else end
        TimePickerDialog(
            ctx,
            { _, h, m -> if (isStart) start = LocalTime.of(h, m) else end = LocalTime.of(h, m) },
            base.hour, base.minute, true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add time range") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showPicker(true) }) { Text("Start: ${formatTime(start)}") }
                    OutlinedButton(onClick = { showPicker(false) }) { Text("End: ${formatTime(end)}") }
                }
                Text(
                    "Tip: For overnight ranges, pick an end earlier than start (e.g., 22:00 to 07:00).",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(start, end) }) { Text("Add") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun normalizeRanges(list: List<Pair<LocalTime, LocalTime>>): List<Pair<LocalTime, LocalTime>> {
    // Sort and remove exact duplicates; no merge for cross-midnight here
    return list.distinct().sortedBy { it.first }
}

private fun labelOf(range: Pair<LocalTime, LocalTime>): String =
    "${formatTime(range.first)} – ${formatTime(range.second)}"

private fun dayLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
}

private fun formatTime(t: LocalTime): String = "%02d:%02d".format(t.hour, t.minute)
