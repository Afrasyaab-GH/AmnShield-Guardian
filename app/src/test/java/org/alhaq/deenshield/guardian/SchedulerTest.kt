package org.alhaq.deenshield.guardian

import org.alhaq.deenshield.guardian.service.Scheduler
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class SchedulerTest {
    @Test
    fun schedule_ActiveWithinRange() {
        val s = Scheduler()
        // Monday 9:00-17:00
        val weekly = mapOf(
            1 to listOf(IntRange(9*60, 17*60))
        )
        val within = LocalDateTime.of(2024, 7, 1, 10, 0) // Monday
        val outside = LocalDateTime.of(2024, 7, 1, 18, 0)
        assertTrue(s.isActive(weekly, within))
        assertFalse(s.isActive(weekly, outside))
    }

    @Test
    fun schedule_UpdateFlag() {
        val s = Scheduler()
        val weekly = mapOf(1 to listOf(IntRange(9*60, 17*60)))
        var active = false
        s.updateActiveFlag(weekly, LocalDateTime.of(2024, 7, 1, 10, 0)) { active = it }
        assertTrue(active)
    }
}
