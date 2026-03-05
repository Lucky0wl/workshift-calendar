package com.example.workshiftcalendar.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workshiftcalendar.data.local.WorkshiftLocalDataSource
import com.example.workshiftcalendar.data.repository.WorkshiftRepository
import com.example.workshiftcalendar.domain.model.*
import com.example.workshiftcalendar.ui.theme.AppStyle
import com.example.workshiftcalendar.NotificationScheduler
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

/**
 * UI State для приложения
 */
data class WorkshiftUiState(
    val assignments: Map<LocalDate, ShiftDetails> = emptyMap(),
    val templates: List<ShiftTemplate> = emptyList(),
    val shiftRates: Map<ShiftKind, String> = emptyMap(),
    val expenses: List<ExpenseEntry> = emptyList(),
    val appStyle: AppStyle = AppStyle.MODERN_BLUE,
    val notificationsEnabled: Boolean = false,
    val notificationTime: String = "20:00",
    val customShiftColors: Map<ShiftKind, Long> = emptyMap(),
    val profiles: List<UserProfile> = emptyList(),
    val currentProfileId: String? = null,
    val vacations: List<VacationPeriod> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel для управления состоянием приложения
 */
class WorkshiftViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkshiftRepository
    private val _uiState = MutableStateFlow(WorkshiftUiState())
    val uiState: StateFlow<WorkshiftUiState> = _uiState.asStateFlow()

    // Потоки для отдельных частей состояния
    val assignmentsFlow: StateFlow<Map<LocalDate, ShiftDetails>> = _uiState
        .map { it.assignments }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val templatesFlow: StateFlow<List<ShiftTemplate>> = _uiState
        .map { it.templates }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val expensesFlow: StateFlow<List<ExpenseEntry>> = _uiState
        .map { it.expenses }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val vacationsFlow: StateFlow<List<VacationPeriod>> = _uiState
        .map { it.vacations }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        val context = application.applicationContext
        val gson = Gson()
        val localDataSource = WorkshiftLocalDataSource(context, gson)
        repository = WorkshiftRepository(localDataSource)

        // Загрузка данных при инициализации
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                combine(
                    repository.assignmentsFlow,
                    repository.templatesFlow,
                    repository.shiftRatesFlow,
                    repository.expensesFlow,
                    repository.appStyleFlow,
                    repository.notificationSettingsFlow,
                    repository.customShiftColorsFlow,
                    repository.profilesFlow,
                    repository.currentProfileIdFlow,
                    repository.vacationsFlow
                ) { assignments, templates, shiftRates, expenses, appStyle, notificationSettings, customColors, profiles, currentProfileId, vacations ->
                    WorkshiftUiState(
                        assignments = assignments,
                        templates = templates,
                        shiftRates = shiftRates,
                        expenses = expenses,
                        appStyle = appStyle,
                        notificationsEnabled = notificationSettings.first,
                        notificationTime = notificationSettings.second,
                        customShiftColors = customColors,
                        profiles = profiles,
                        currentProfileId = currentProfileId,
                        vacations = vacations,
                        isLoading = false,
                        error = null
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Shift Operations
    // ═══════════════════════════════════════════════

    fun saveShift(date: LocalDate, shift: ShiftDetails) {
        viewModelScope.launch {
            try {
                repository.saveShift(date, shift)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteShift(date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.deleteShift(date)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearAllAssignments() {
        viewModelScope.launch {
            try {
                repository.clearAssignments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun copyAssignmentsFromMonth(sourceMonth: YearMonth, targetMonth: YearMonth) {
        viewModelScope.launch {
            try {
                repository.copyAssignmentsFromMonth(sourceMonth, targetMonth)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Template Operations
    // ═══════════════════════════════════════════════

    fun saveTemplate(template: ShiftTemplate) {
        viewModelScope.launch {
            try {
                repository.saveTemplate(template)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTemplate(templateId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Shift Rates Operations
    // ═══════════════════════════════════════════════

    fun updateShiftRates(rates: Map<ShiftKind, String>) {
        viewModelScope.launch {
            try {
                repository.updateShiftRates(rates)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Expense Operations
    // ═══════════════════════════════════════════════

    fun saveExpense(expense: ExpenseEntry) {
        viewModelScope.launch {
            try {
                repository.saveExpense(expense)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expenseId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // App Style Operations
    // ═══════════════════════════════════════════════

    fun updateAppStyle(style: AppStyle) {
        viewModelScope.launch {
            try {
                repository.updateAppStyle(style)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Notification Operations
    // ═══════════════════════════════════════════════

    fun updateNotificationSettings(enabled: Boolean, time: String) {
        viewModelScope.launch {
            try {
                repository.updateNotificationSettings(enabled, time)
                
                // Обновляем будильник
                val context = getApplication<Application>().applicationContext
                if (enabled) {
                    val timeParts = time.split(":")
                    if (timeParts.size == 2) {
                        NotificationScheduler.scheduleNotification(
                            context,
                            timeParts[0].toInt(),
                            timeParts[1].toInt()
                        )
                    }
                } else {
                    NotificationScheduler.cancelNotification(context)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Custom Shift Colors Operations
    // ═══════════════════════════════════════════════

    fun updateCustomShiftColor(kind: ShiftKind, color: Long?) {
        viewModelScope.launch {
            try {
                repository.updateCustomShiftColor(kind, color)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Profile Operations
    // ═══════════════════════════════════════════════

    fun createProfile(name: String) {
        viewModelScope.launch {
            try {
                repository.createProfile(name)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            try {
                repository.switchProfile(profileId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProfile(profileId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Vacation Operations
    // ═══════════════════════════════════════════════

    fun addVacation(vacation: VacationPeriod) {
        viewModelScope.launch {
            try {
                repository.addVacation(vacation)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteVacation(vacationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteVacation(vacationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun isDateOnVacation(date: LocalDate): Boolean {
        return _uiState.value.vacations.any { it.containsDate(date) }
    }

    // ═══════════════════════════════════════════════
    // Export/Import Operations
    // ═══════════════════════════════════════════════

    suspend fun exportToJson(): String {
        return try {
            repository.exportToJson()
        } catch (e: Exception) {
            throw Exception("Ошибка экспорта: ${e.message}")
        }
    }

    fun importFromJson(json: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.importFromJson(json)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Ошибка импорта")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка импорта")
            }
        }
    }

    // ═══════════════════════════════════════════════
    // Utility Functions
    // ═══════════════════════════════════════════════

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getShiftForDate(date: LocalDate): ShiftDetails? {
        return _uiState.value.assignments[date]
    }

    fun getExpensesForMonth(yearMonth: YearMonth): List<ExpenseEntry> {
        return _uiState.value.expenses.filter { expense ->
            YearMonth.from(expense.date) == yearMonth
        }
    }

    fun getShiftsForMonth(yearMonth: YearMonth): Map<LocalDate, ShiftDetails> {
        return _uiState.value.assignments.filter { entry ->
            YearMonth.from(entry.key) == yearMonth
        }
    }
}
