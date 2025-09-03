package com.deenshield.blocker.service

import java.time.DayOfWeek
import java.time.LocalDateTime

class Scheduler {
    // weeklySchedule: map weekday 1..7 (Mon..Sun) to list of IntRange minutes from midnight
    fun isActive(weeklySchedule: Map<Int, List<IntRange>>, now: LocalDateTime = LocalDateTime.now()): Boolean {
        val weekday = (now.dayOfWeek.value) // 1..7
        val minutes = now.hour * 60 + now.minute
        val ranges = weeklySchedule[weekday] ?: return false
        return ranges.any { r -> minutes in r }
    }

    // Optional hook to update a boolean flag in a host (e.g., ViewModel) about active schedule
    fun updateActiveFlag(
        weeklySchedule: Map<Int, List<IntRange>>, now: LocalDateTime,
        setActive: (Boolean) -> Unit
    ) {
        setActive(isActive(weeklySchedule, now))
    }
}
