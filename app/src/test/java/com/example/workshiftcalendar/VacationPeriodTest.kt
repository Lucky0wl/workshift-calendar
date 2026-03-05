package com.example.workshiftcalendar.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Тесты для VacationPeriod
 */
class VacationPeriodTest {

    @Test
    fun `containsDate returns true for date within range`() {
        val vacation = VacationPeriod(
            id = "1",
            startDate = LocalDate.of(2024, 6, 1),
            endDate = LocalDate.of(2024, 6, 14),
            name = "Отпуск"
        )

        assertTrue(vacation.containsDate(LocalDate.of(2024, 6, 1)))
        assertTrue(vacation.containsDate(LocalDate.of(2024, 6, 7)))
        assertTrue(vacation.containsDate(LocalDate.of(2024, 6, 14)))
    }

    @Test
    fun `containsDate returns false for date outside range`() {
        val vacation = VacationPeriod(
            id = "1",
            startDate = LocalDate.of(2024, 6, 1),
            endDate = LocalDate.of(2024, 6, 14),
            name = "Отпуск"
        )

        assertFalse(vacation.containsDate(LocalDate.of(2024, 5, 31)))
        assertFalse(vacation.containsDate(LocalDate.of(2024, 6, 15)))
    }

    @Test
    fun `durationDays returns correct number of days`() {
        val vacation = VacationPeriod(
            id = "1",
            startDate = LocalDate.of(2024, 6, 1),
            endDate = LocalDate.of(2024, 6, 14),
            name = "Отпуск"
        )

        assertEquals(14, vacation.durationDays())
    }

    @Test
    fun `durationDays for single day vacation`() {
        val vacation = VacationPeriod(
            id = "1",
            startDate = LocalDate.of(2024, 6, 1),
            endDate = LocalDate.of(2024, 6, 1),
            name = "Отпуск"
        )

        assertEquals(1, vacation.durationDays())
    }
}
