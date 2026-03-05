package com.example.workshiftcalendar.data.model

import com.example.workshiftcalendar.domain.model.ExpenseCategory
import com.example.workshiftcalendar.domain.model.ShiftKind

/**
 * DTO для сериализации ShiftDetails
 */
data class ShiftDetailsDto(
    val kind: String = "",
    val note: String = "",
    val location: String = "",
    val customSalary: String = "",
    val customHours: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val customColor: Long? = null
)

/**
 * DTO для сериализации ShiftTemplate
 */
data class ShiftTemplateDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val pattern: List<String> = emptyList()
)

/**
 * DTO для сериализации ExpenseEntry
 */
data class ExpenseEntryDto(
    val id: String = "",
    val date: String = "",
    val amount: Int = 0,
    val category: String = "",
    val note: String = "",
    val createdAt: Long = 0L
)

/**
 * DTO для сериализации VacationPeriod
 */
data class VacationPeriodDto(
    val id: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val name: String = ""
)

/**
 * DTO для сериализации UserProfile
 */
data class UserProfileDto(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = 0L
)

/**
 * Основное DTO для всех данных приложения
 */
data class AppDataDto(
    val assignments: Map<String, ShiftDetailsDto> = emptyMap(),
    val customTemplates: List<ShiftTemplateDto> = emptyList(),
    val shiftRates: Map<String, String> = emptyMap(),
    val appStyle: String = "MODERN_BLUE",
    val expenses: List<ExpenseEntryDto> = emptyList(),
    val notificationsEnabled: Boolean = false,
    val notificationTime: String = "20:00",
    val customShiftColors: Map<String, Long> = emptyMap(),
    val profiles: List<UserProfileDto> = emptyList(),
    val currentProfileId: String? = null,
    val vacations: List<VacationPeriodDto> = emptyList()
)
