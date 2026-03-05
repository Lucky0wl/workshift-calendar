package com.example.workshiftcalendar.data.mapper

import com.example.workshiftcalendar.data.model.*
import com.example.workshiftcalendar.domain.model.*
import java.time.LocalDate

// ═══════════════════════════════════════════════
// ShiftDetails Mapper
// ═══════════════════════════════════════════════

fun ShiftDetails.toDto() = ShiftDetailsDto(
    kind = kind.name,
    note = note,
    location = location,
    customSalary = customSalary,
    customHours = customHours,
    startTime = startTime,
    endTime = endTime,
    customColor = customColor
)

fun ShiftDetailsDto.toDomain(): ShiftDetails? = try {
    ShiftKind.fromStringOrNull(kind)?.let {
        ShiftDetails(
            kind = it,
            note = note,
            location = location,
            customSalary = customSalary,
            customHours = customHours,
            startTime = startTime,
            endTime = endTime,
            customColor = customColor
        )
    }
} catch (e: Exception) {
    null
}

// ═══════════════════════════════════════════════
// ShiftTemplate Mapper
// ═══════════════════════════════════════════════

fun ShiftTemplate.toDto() = ShiftTemplateDto(
    id = id,
    name = name,
    description = description,
    pattern = pattern.map { it.name }
)

fun ShiftTemplateDto.toDomain(): ShiftTemplate? = try {
    ShiftTemplate(
        id = id,
        name = name,
        description = description,
        pattern = pattern.mapNotNull { ShiftKind.fromStringOrNull(it) },
        isBuiltIn = false
    )
} catch (e: Exception) {
    null
}

// ═══════════════════════════════════════════════
// ExpenseEntry Mapper
// ═══════════════════════════════════════════════

fun ExpenseEntry.toDto() = ExpenseEntryDto(
    id = id,
    date = date.toString(),
    amount = amount,
    category = category.name,
    note = note,
    createdAt = createdAt
)

fun ExpenseEntryDto.toDomain(): ExpenseEntry? = try {
    ExpenseCategory.fromStringOrNull(category)?.let {
        ExpenseEntry(
            id = id,
            date = LocalDate.parse(date),
            amount = amount,
            category = it,
            note = note,
            createdAt = if (createdAt > 0L) createdAt else (id.toLongOrNull() ?: System.currentTimeMillis())
        )
    }
} catch (e: Exception) {
    null
}

// ═══════════════════════════════════════════════
// VacationPeriod Mapper
// ═══════════════════════════════════════════════

fun VacationPeriod.toDto() = VacationPeriodDto(
    id = id,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    name = name
)

fun VacationPeriodDto.toDomain(): VacationPeriod? = try {
    VacationPeriod(
        id = id,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        name = name.ifBlank { "Отпуск" }
    )
} catch (e: Exception) {
    null
}

// ═══════════════════════════════════════════════
// UserProfile Mapper
// ═══════════════════════════════════════════════

fun UserProfile.toDto() = UserProfileDto(
    id = id,
    name = name,
    createdAt = createdAt
)

fun UserProfileDto.toDomain(): UserProfile? = try {
    UserProfile(
        id = id,
        name = name,
        createdAt = if (createdAt > 0L) createdAt else System.currentTimeMillis()
    )
} catch (e: Exception) {
    null
}

// ═══════════════════════════════════════════════
// AppDataDto Extensions
// ═══════════════════════════════════════════════

fun Map<LocalDate, ShiftDetails>.toDto() = entries.associate { (date, details) ->
    date.toString() to details.toDto()
}

fun Map<String, ShiftDetailsDto>.toDomain(): Map<LocalDate, ShiftDetails> =
    entries.mapNotNull { (dateStr, dto) ->
        try {
            LocalDate.parse(dateStr) to dto.toDomain()
        } catch (e: Exception) {
            null
        }
    }.filter { it.second != null }.associate { it.first to it.second!! }

fun Map<ShiftKind, String>.toStringMap() = entries.associate { (kind, rate) ->
    kind.name to rate
}

fun Map<String, String>.toShiftKindMap(): Map<ShiftKind, String> =
    entries.mapNotNull { (kindName, rate) ->
        try {
            ShiftKind.fromStringOrNull(kindName) to rate
        } catch (e: Exception) {
            null
        }
    }.filter { it.first != null }.associate { it.first!! to it.second }

@JvmName("vacationPeriodToDto")
fun List<VacationPeriod>.toDto() = map { it.toDto() }

@JvmName("vacationPeriodDtoToDomain")
fun List<VacationPeriodDto>.toDomain(): List<VacationPeriod> = mapNotNull { it.toDomain() }

@JvmName("userProfileToDto")
fun List<UserProfile>.toDto() = map { it.toDto() }

@JvmName("userProfileDtoToDomain")
fun List<UserProfileDto>.toDomain(): List<UserProfile> = mapNotNull { it.toDomain() }
