package com.example.workshiftcalendar.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Тесты для ShiftKind
 */
class ShiftKindTest {

    @Test
    fun `ShiftKind has correct hoursPerShift`() {
        assertEquals(8, ShiftKind.MORNING.hoursPerShift)
        assertEquals(8, ShiftKind.EVENING.hoursPerShift)
        assertEquals(12, ShiftKind.NIGHT.hoursPerShift)
        assertEquals(0, ShiftKind.OFF.hoursPerShift)
    }

    @Test
    fun `ShiftKind fromStringOrNull returns correct enum`() {
        assertEquals(ShiftKind.MORNING, ShiftKind.fromStringOrNull("MORNING"))
        assertEquals(ShiftKind.EVENING, ShiftKind.fromStringOrNull("EVENING"))
        assertEquals(ShiftKind.NIGHT, ShiftKind.fromStringOrNull("NIGHT"))
        assertEquals(ShiftKind.OFF, ShiftKind.fromStringOrNull("OFF"))
    }

    @Test
    fun `ShiftKind fromStringOrNull returns null for invalid string`() {
        assertNull(ShiftKind.fromStringOrNull("INVALID"))
        assertNull(ShiftKind.fromStringOrNull(""))
    }

    @Test
    fun `ShiftKind has correct emoji`() {
        assertEquals("☀️", ShiftKind.MORNING.emoji)
        assertEquals("🌙", ShiftKind.EVENING.emoji)
        assertEquals("🌌", ShiftKind.NIGHT.emoji)
        assertEquals("🏠", ShiftKind.OFF.emoji)
    }
}
