package com.example.workshiftcalendar.domain.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * Детали смены
 */
data class ShiftDetails(
    val kind: ShiftKind,
    val note: String = "",
    val location: String = "",
    val customSalary: String = "",
    val customHours: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val customColor: Long? = null // ARGB color value for custom colors
) {
    /**
     * Расчёт продолжительности смены в часах
     */
    fun calculateTotalHours(): Double {
        if (kind == ShiftKind.OFF) return 0.0
        
        // Приоритет: кастомные часы > время начала/конца > стандартные часы
        val customH = customHours.replace(',', '.').toDoubleOrNull()
        if (customH != null && customH > 0) return customH

        if (startTime.isNotBlank() && endTime.isNotBlank()) {
            try {
                val startParts = startTime.split(":")
                val endParts = endTime.split(":")
                if (startParts.size >= 2 && endParts.size >= 2) {
                    val startMins = startParts[0].toInt() * 60 + startParts[1].toInt()
                    var endMins = endParts[0].toInt() * 60 + endParts[1].toInt()
                    if (endMins <= startMins) endMins += 24 * 60
                    return (endMins - startMins) / 60.0
                }
            } catch (_: Exception) {}
        }
        return kind.hoursPerShift.toDouble()
    }

    /**
     * Получение цвета смены (кастомный или стандартный)
     */
    fun getColor(): Color = customColor?.let { Color(it) } ?: kind.color

    /**
     * Получение светлого цвета смены (для фона)
     */
    fun getLightColor(): Color = customColor?.let { 
        Color(
            alpha = 50,
            red = ((it shr 16) and 0xFF).toInt(),
            green = ((it shr 8) and 0xFF).toInt(),
            blue = (it and 0xFF).toInt()
        )
    } ?: kind.lightColor
}
