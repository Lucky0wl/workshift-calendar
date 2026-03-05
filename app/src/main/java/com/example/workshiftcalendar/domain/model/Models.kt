package com.example.workshiftcalendar.domain.model

import java.time.LocalDate

/**
 * Запись о расходе
 */
data class ExpenseEntry(
    val id: String,
    val date: LocalDate,
    val amount: Int,
    val category: ExpenseCategory,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Шаблон графика смен
 */
data class ShiftTemplate(
    val id: String,
    val name: String,
    val description: String,
    val pattern: List<ShiftKind>,
    val isBuiltIn: Boolean = false
)

/**
 * Профиль пользователя (для поддержки нескольких профилей)
 */
data class UserProfile(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Период отпуска
 */
data class VacationPeriod(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val name: String = "Отпуск"
) {
    fun containsDate(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)

    fun durationDays(): Long =
        java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
}
