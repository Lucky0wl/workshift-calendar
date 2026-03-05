package com.example.workshiftcalendar.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Тесты для ShiftDetails
 */
class ShiftDetailsTest {

    @Test
    fun `calculateTotalHours returns 0 for OFF shift`() {
        val shift = ShiftDetails(kind = ShiftKind.OFF)
        assertEquals(0.0, shift.calculateTotalHours(), 0.0)
    }

    @Test
    fun `calculateTotalHours returns custom hours when set`() {
        val shift = ShiftDetails(
            kind = ShiftKind.MORNING,
            customHours = "10.5"
        )
        assertEquals(10.5, shift.calculateTotalHours(), 0.0)
    }

    @Test
    fun `calculateTotalHours calculates from start and end time`() {
        val shift = ShiftDetails(
            kind = ShiftKind.MORNING,
            startTime = "08:00",
            endTime = "17:00"
        )
        assertEquals(9.0, shift.calculateTotalHours(), 0.0)
    }

    @Test
    fun `calculateTotalHours handles overnight shifts`() {
        val shift = ShiftDetails(
            kind = ShiftKind.NIGHT,
            startTime = "20:00",
            endTime = "08:00"
        )
        assertEquals(12.0, shift.calculateTotalHours(), 0.0)
    }

    @Test
    fun `calculateTotalHours returns default hours for shift kind`() {
        val morningShift = ShiftDetails(kind = ShiftKind.MORNING)
        assertEquals(8.0, morningShift.calculateTotalHours(), 0.0)

        val nightShift = ShiftDetails(kind = ShiftKind.NIGHT)
        assertEquals(12.0, nightShift.calculateTotalHours(), 0.0)
    }

    @Test
    fun `calculateTotalHours prefers custom hours over time range`() {
        val shift = ShiftDetails(
            kind = ShiftKind.MORNING,
            startTime = "08:00",
            endTime = "17:00",
            customHours = "5.0"
        )
        assertEquals(5.0, shift.calculateTotalHours(), 0.0)
    }

    @Test
    fun `calculateTotalHours handles comma as decimal separator`() {
        val shift = ShiftDetails(
            kind = ShiftKind.MORNING,
            customHours = "8,5"
        )
        assertEquals(8.5, shift.calculateTotalHours(), 0.0)
    }
}
