package com.deenshield.blocker.model

import java.time.Instant

data class Block(
    val id: String,
    val name: String,
    val createdAt: Instant = Instant.now(),
    val appIds: List<String> = emptyList(),
    val websites: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val fullBlock: Boolean = false,
    val weeklySchedule: Map<Int, List<IntRange>> = emptyMap(),
    val dailyLimitMinutes: Int = 0,
    val hourlyLimitMinutes: Int = 0
)
