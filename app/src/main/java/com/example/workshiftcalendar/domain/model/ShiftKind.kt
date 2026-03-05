package com.example.workshiftcalendar.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Типы рабочих смен
 */
enum class ShiftKind(
    val displayName: String,
    val shortName: String,
    val emoji: String,
    val color: Color,
    val lightColor: Color,
    val hoursPerShift: Int
) {
    MORNING("Утренняя", "У", "☀️", Color(0xFFBF360C), Color(0xFFFFCC80), 8),
    EVENING("Вечерняя", "В", "🌙", Color(0xFF6A1B9A), Color(0xFFCE93D8), 8),
    NIGHT("Ночная", "Н", "🌌", Color(0xFF0D47A1), Color(0xFF90CAF9), 12),
    OFF("Выходной", "О", "🏠", Color(0xFF424242), Color(0xFFE0E0E0), 0);

    companion object {
        fun fromStringOrNull(value: String): ShiftKind? =
            entries.find { it.name == value }
    }
}
