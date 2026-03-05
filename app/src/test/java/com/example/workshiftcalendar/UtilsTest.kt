package com.example.workshiftcalendar

import com.example.workshiftcalendar.domain.model.ShiftKind
import org.junit.Assert.*
import org.junit.Test

/**
 * Тесты для утилит форматирования
 */
class UtilsTest {

    @Test
    fun `formatHours formats whole hours correctly`() {
        assertEquals("8", 8.0.formatHours())
        assertEquals("12", 12.0.formatHours())
        assertEquals("0", 0.0.formatHours())
    }

    @Test
    fun `formatHours formats decimal hours correctly`() {
        assertEquals("8.5", 8.5.formatHours())
        assertEquals("10.5", 10.5.formatHours())
        assertEquals("7.5", 7.5.formatHours())
    }

    @Test
    fun `ShiftKind displayName is not empty`() {
        ShiftKind.entries.forEach { kind ->
            assertTrue("${kind.name} has empty displayName", kind.displayName.isNotEmpty())
        }
    }

    @Test
    fun `ShiftKind shortName is single character`() {
        assertEquals("У", ShiftKind.MORNING.shortName)
        assertEquals("В", ShiftKind.EVENING.shortName)
        assertEquals("Н", ShiftKind.NIGHT.shortName)
        assertEquals("О", ShiftKind.OFF.shortName)
    }
}

/**
 * Extension function for formatting hours
 */
fun Double.formatHours(): String {
    val i = this.toInt()
    return if (this == i.toDouble()) i.toString() else String.format(java.util.Locale.US, "%.1f", this)
}
