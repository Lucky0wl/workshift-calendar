package com.example.workshiftcalendar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.workshiftcalendar.data.model.AppDataDto
import com.example.workshiftcalendar.data.mapper.*
import com.example.workshiftcalendar.domain.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workshift_v4")
private val APP_DATA_KEY = stringPreferencesKey("app_data_v4")

/**
 * Локальный источник данных с использованием DataStore
 */
class WorkshiftLocalDataSource(private val context: Context, private val gson: Gson) {

    /**
     * Поток всех данных приложения
     */
    val appDataFlow: Flow<AppDataDto> = context.dataStore.data.map { prefs ->
        prefs[APP_DATA_KEY]?.let { json ->
            try {
                gson.fromJson(json, AppDataDto::class.java)
            } catch (e: Exception) {
                AppDataDto()
            }
        } ?: AppDataDto()
    }

    /**
     * Получить данные смен
     */
    suspend fun getAssignments(): Map<LocalDate, ShiftDetails> {
        return try {
            val prefs = context.dataStore.data.firstOrNull()
            val dto = prefs?.get(APP_DATA_KEY)?.let { json ->
                gson.fromJson(json, AppDataDto::class.java)
            } ?: AppDataDto()
            dto.assignments.toDomain()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Получить смену на конкретную дату
     */
    suspend fun getShift(date: LocalDate): ShiftDetails? {
        return try {
            val prefs = context.dataStore.data.firstOrNull()
            val dto = prefs?.get(APP_DATA_KEY)?.let { json ->
                gson.fromJson(json, AppDataDto::class.java)
            } ?: AppDataDto()
            dto.assignments[date.toString()]?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Сохранить все данные
     */
    suspend fun saveAppData(data: AppDataDto) {
        context.dataStore.edit { prefs ->
            prefs[APP_DATA_KEY] = gson.toJson(data)
        }
    }

    /**
     * Обновить данные смен
     */
    suspend fun updateAssignments(assignments: Map<LocalDate, ShiftDetails>) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(assignments = assignments.toDto())
        saveAppData(updatedData)
    }

    /**
     * Обновить шаблоны
     */
    suspend fun updateTemplates(templates: List<ShiftTemplate>) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(
            customTemplates = templates.filter { !it.isBuiltIn }.map { it.toDto() }
        )
        saveAppData(updatedData)
    }

    /**
     * Обновить ставки смен
     */
    suspend fun updateShiftRates(rates: Map<ShiftKind, String>) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(shiftRates = rates.toStringMap())
        saveAppData(updatedData)
    }

    /**
     * Обновить расходы
     */
    suspend fun updateExpenses(expenses: List<ExpenseEntry>) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(expenses = expenses.map { it.toDto() })
        saveAppData(updatedData)
    }

    /**
     * Обновить стиль приложения
     */
    suspend fun updateAppStyle(style: String) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(appStyle = style)
        saveAppData(updatedData)
    }

    /**
     * Обновить настройки уведомлений
     */
    suspend fun updateNotificationSettings(enabled: Boolean, time: String) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(
            notificationsEnabled = enabled,
            notificationTime = time
        )
        saveAppData(updatedData)
    }

    /**
     * Обновить кастомные цвета смен
     */
    suspend fun updateCustomShiftColors(colors: Map<String, Long>) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(customShiftColors = colors)
        saveAppData(updatedData)
    }

    /**
     * Обновить профили
     */
    suspend fun updateProfiles(profiles: List<UserProfile>, currentProfileId: String?) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(
            profiles = profiles.toDto(),
            currentProfileId = currentProfileId
        )
        saveAppData(updatedData)
    }

    /**
     * Обновить периоды отпуска
     */
    suspend fun updateVacations(vacations: List<VacationPeriod>) {
        val currentData = getCurrentData()
        val updatedData = currentData.copy(vacations = vacations.toDto())
        saveAppData(updatedData)
    }

    /**
     * Получить текущие данные
     */
    private suspend fun getCurrentData(): AppDataDto {
        val prefs = context.dataStore.data.firstOrNull()
        return prefs?.get(APP_DATA_KEY)?.let { json ->
            try {
                gson.fromJson(json, AppDataDto::class.java)
            } catch (e: Exception) {
                AppDataDto()
            }
        } ?: AppDataDto()
    }

    /**
     * Экспорт данных в JSON строку
     */
    suspend fun exportToJson(): String {
        val data = getCurrentData()
        return gson.toJson(data)
    }

    /**
     * Импорт данных из JSON строки
     */
    suspend fun importFromJson(json: String): Result<Unit> {
        return try {
            val data = gson.fromJson(json, AppDataDto::class.java)
            saveAppData(data)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
