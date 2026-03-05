package com.example.workshiftcalendar.data.mapper

import com.example.workshiftcalendar.domain.model.ExpenseCategory
import com.example.workshiftcalendar.domain.model.ExpenseEntry
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Тесты для мапперов расходов
 */
class ExpenseMapperTest {

    @Test
    fun `ExpenseEntry toDto and back preserves data`() {
        val original = ExpenseEntry(
            id = "123",
            date = LocalDate.of(2024, 6, 15),
            amount = 1500,
            category = ExpenseCategory.FOOD,
            note = "Продукты на неделю",
            createdAt = 1234567890L
        )

        val dto = original.toDto()
        val restored = dto.toDomain()

        assertNotNull(restored)
        assertEquals(original.id, restored?.id)
        assertEquals(original.date, restored?.date)
        assertEquals(original.amount, restored?.amount)
        assertEquals(original.category, restored?.category)
        assertEquals(original.note, restored?.note)
    }

    @Test
    fun `ExpenseEntryDto toDomain handles missing createdAt`() {
        val dto = ExpenseEntryDto(
            id = "1234567890",
            date = "2024-06-15",
            amount = 1000,
            category = "TRANSPORT",
            note = ""
        )

        val restored = dto.toDomain()

        assertNotNull(restored)
        assertEquals(1234567890L, restored?.createdAt)
    }

    @Test
    fun `ExpenseEntryDto toDomain returns null for invalid category`() {
        val dto = ExpenseEntryDto(
            id = "123",
            date = "2024-06-15",
            amount = 1000,
            category = "INVALID_CATEGORY",
            note = ""
        )

        val restored = dto.toDomain()

        assertNull(restored)
    }

    @Test
    fun `ExpenseEntryDto toDomain returns null for invalid date`() {
        val dto = ExpenseEntryDto(
            id = "123",
            date = "invalid-date",
            amount = 1000,
            category = "FOOD",
            note = ""
        )

        val restored = dto.toDomain()

        assertNull(restored)
    }
}
