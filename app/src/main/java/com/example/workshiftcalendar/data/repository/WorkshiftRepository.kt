package com.example.workshiftcalendar.data.repository

import com.example.workshiftcalendar.data.local.WorkshiftLocalDataSource
import com.example.workshiftcalendar.data.mapper.*
import com.example.workshiftcalendar.data.model.AppDataDto
import com.example.workshiftcalendar.domain.model.*
import com.example.workshiftcalendar.ui.theme.AppStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

/**
 * Репозиторий для работы с данными смен
 */
class WorkshiftRepository(private val localDataSource: WorkshiftLocalDataSource) {

    /**
     * Поток данных приложения
     */
    val appDataFlow: Flow<AppDataDto> = localDataSource.appDataFlow

    /**
     * Поток смен
     */
    val assignmentsFlow: Flow<Map<LocalDate, ShiftDetails>> = appDataFlow.map { dto ->
        dto.assignments.toDomain()
    }

    /**
     * Поток шаблонов (встроенные + пользовательские)
     */
    val templatesFlow: Flow<List<ShiftTemplate>> = appDataFlow.map { dto ->
        builtInTemplates() + dto.customTemplates.mapNotNull { it.toDomain() }
    }

    /**
     * Поток ставок смен
     */
    val shiftRatesFlow: Flow<Map<ShiftKind, String>> = appDataFlow.map { dto ->
        dto.shiftRates.toShiftKindMap()
    }

    /**
     * Поток расходов
     */
    val expensesFlow: Flow<List<ExpenseEntry>> = appDataFlow.map { dto ->
        dto.expenses.mapNotNull { it.toDomain() }
    }

    /**
     * Поток стиля приложения
     */
    val appStyleFlow: Flow<AppStyle> = appDataFlow.map { dto ->
        try {
            AppStyle.valueOf(dto.appStyle)
        } catch (e: Exception) {
            AppStyle.MODERN_BLUE
        }
    }

    /**
     * Поток настроек уведомлений
     */
    val notificationSettingsFlow: Flow<Pair<Boolean, String>> = appDataFlow.map { dto ->
        dto.notificationsEnabled to (dto.notificationTime.takeIf { it.isNotBlank() } ?: "20:00")
    }

    /**
     * Поток кастомных цветов смен
     */
    val customShiftColorsFlow: Flow<Map<ShiftKind, Long>> = appDataFlow.map { dto ->
        dto.customShiftColors.mapNotNull { (kindName, color) ->
            ShiftKind.fromStringOrNull(kindName) to color
        }.toMap()
    }

    /**
     * Поток профилей
     */
    val profilesFlow: Flow<List<UserProfile>> = appDataFlow.map { dto ->
        dto.profiles.toDomain()
    }

    /**
     * Поток текущего профиля
     */
    val currentProfileIdFlow: Flow<String?> = appDataFlow.map { dto ->
        dto.currentProfileId
    }

    /**
     * Поток периодов отпуска
     */
    val vacationsFlow: Flow<List<VacationPeriod>> = appDataFlow.map { dto ->
        dto.vacations.toDomain()
    }

    // ═══════════════════════════════════════════════
    // Assignments Operations
    // ═══════════════════════════════════════════════

    suspend fun getAssignments(): Map<LocalDate, ShiftDetails> =
        localDataSource.getAssignments()

    suspend fun getShift(date: LocalDate): ShiftDetails? =
        localDataSource.getShift(date)

    suspend fun saveShift(date: LocalDate, shift: ShiftDetails) {
        val current = getAssignments()
        localDataSource.updateAssignments(current + (date to shift))
    }

    suspend fun deleteShift(date: LocalDate) {
        val current = getAssignments()
        localDataSource.updateAssignments(current - date)
    }

    suspend fun clearAssignments() {
        localDataSource.updateAssignments(emptyMap())
    }

    suspend fun copyAssignmentsFromMonth(sourceMonth: YearMonth, targetMonth: YearMonth) {
        val current = getAssignments()
        val sourceAssignments = current.filter { entry ->
            YearMonth.from(entry.key) == sourceMonth
        }

        val newAssignments = sourceAssignments.mapNotNull { (date, shift) ->
            try {
                val newDay = date.dayOfMonth
                val newDate = targetMonth.atDay(newDay)
                newDate to shift
            } catch (e: Exception) {
                null // День не существует в целевом месяце (например, 31 февраля)
            }
        }.toMap()

        localDataSource.updateAssignments(current + newAssignments)
    }

    // ═══════════════════════════════════════════════
    // Templates Operations
    // ═══════════════════════════════════════════════

    suspend fun saveTemplate(template: ShiftTemplate) {
        val currentTemplates = templatesFlow.first()
            .filter { !it.isBuiltIn }
        localDataSource.updateTemplates(currentTemplates + template)
    }

    suspend fun deleteTemplate(templateId: String) {
        val currentTemplates = templatesFlow.first()
            .filter { !it.isBuiltIn && it.id != templateId }
        localDataSource.updateTemplates(currentTemplates)
    }

    // ═══════════════════════════════════════════════
    // Shift Rates Operations
    // ═══════════════════════════════════════════════

    suspend fun updateShiftRates(rates: Map<ShiftKind, String>) {
        localDataSource.updateShiftRates(rates)
    }

    // ═══════════════════════════════════════════════
    // Expenses Operations
    // ═══════════════════════════════════════════════

    suspend fun getExpenses(): List<ExpenseEntry> =
        localDataSource.expensesFlow.first()

    suspend fun saveExpense(expense: ExpenseEntry) {
        val current = expensesFlow.first()
        localDataSource.updateExpenses(current + expense)
    }

    suspend fun deleteExpense(expenseId: String) {
        val current = expensesFlow.first()
        localDataSource.updateExpenses(current.filter { it.id != expenseId })
    }

    // ═══════════════════════════════════════════════
    // App Style Operations
    // ═══════════════════════════════════════════════

    suspend fun updateAppStyle(style: AppStyle) {
        localDataSource.updateAppStyle(style.name)
    }

    // ═══════════════════════════════════════════════
    // Notification Settings Operations
    // ═══════════════════════════════════════════════

    suspend fun updateNotificationSettings(enabled: Boolean, time: String) {
        localDataSource.updateNotificationSettings(enabled, time)
    }

    // ═══════════════════════════════════════════════
    // Custom Shift Colors Operations
    // ═══════════════════════════════════════════════

    suspend fun updateCustomShiftColor(kind: ShiftKind, color: Long?) {
        val current = customShiftColorsFlow.first()
        val updated = if (color != null) {
            current + (kind to color)
        } else {
            current - kind
        }
        localDataSource.updateCustomShiftColors(
            updated.mapKeys { it.key.name }
        )
    }

    // ═══════════════════════════════════════════════
    // Profile Operations
    // ═══════════════════════════════════════════════

    suspend fun createProfile(name: String): UserProfile {
        val profile = UserProfile(id = UUID.randomUUID().toString(), name = name)
        val current = profilesFlow.first()
        localDataSource.updateProfiles(current + profile, profile.id)
        return profile
    }

    suspend fun switchProfile(profileId: String) {
        val current = profilesFlow.first()
        localDataSource.updateProfiles(current, profileId)
    }

    suspend fun deleteProfile(profileId: String) {
        val current = profilesFlow.first()
        val updated = current.filter { it.id != profileId }
        val newCurrentId = if (updated.isNotEmpty()) updated.first().id else null
        localDataSource.updateProfiles(updated, newCurrentId)
    }

    // ═══════════════════════════════════════════════
    // Vacation Operations
    // ═══════════════════════════════════════════════

    suspend fun addVacation(vacation: VacationPeriod) {
        val current = vacationsFlow.first()
        localDataSource.updateVacations(current + vacation)
    }

    suspend fun deleteVacation(vacationId: String) {
        val current = vacationsFlow.first()
        localDataSource.updateVacations(current.filter { it.id != vacationId })
    }

    suspend fun isDateOnVacation(date: LocalDate): Boolean {
        val vacations = vacationsFlow.first()
        return vacations.any { it.containsDate(date) }
    }

    // ═══════════════════════════════════════════════
    // Export/Import Operations
    // ═══════════════════════════════════════════════

    suspend fun exportToJson(): String =
        localDataSource.exportToJson()

    suspend fun importFromJson(json: String): Result<Unit> =
        localDataSource.importFromJson(json)
}

/**
 * Встроенные шаблоны графиков
 */
fun builtInTemplates(): List<ShiftTemplate> = listOf(
    ShiftTemplate("2_2", "2 через 2", "Два рабочих, два выходных", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("3_3", "3 через 3", "Три рабочих, три выходных", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("4_2", "4 через 2", "Четыре рабочих, два выходных", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("5_2_m", "5/2 Утренние", "Пн–Пт утро, выходные", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("5_2_e", "5/2 Вечерние", "Пн–Пт вечер, выходные", listOf(ShiftKind.EVENING, ShiftKind.EVENING, ShiftKind.EVENING, ShiftKind.EVENING, ShiftKind.EVENING, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("6_1", "6 через 1", "Шесть рабочих, один выходной", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.OFF), true),
    ShiftTemplate("1_3", "Сутки через трое", "Ночная смена, три выходных", listOf(ShiftKind.NIGHT, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("2_2_4", "2/2/4 Вахта", "Две дневных, две ночных, 4 выходных", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.NIGHT, ShiftKind.NIGHT, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("dn_2", "День/Ночь через 2", "Дневная, ночная, два выходных", listOf(ShiftKind.MORNING, ShiftKind.NIGHT, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("eve_2", "2 Вечерних / 2", "Два вечерних, два выходных", listOf(ShiftKind.EVENING, ShiftKind.EVENING, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("night_3", "Ночная / 3", "Одна ночь, три выходных", listOf(ShiftKind.NIGHT, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF), true),
    ShiftTemplate("mixed", "Смешанная неделя", "3 дня, ночь, 3 выходных", listOf(ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.MORNING, ShiftKind.NIGHT, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF), true)
)
