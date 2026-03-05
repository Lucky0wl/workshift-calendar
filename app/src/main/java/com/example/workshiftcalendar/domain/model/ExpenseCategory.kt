package com.example.workshiftcalendar.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Категории расходов
 */
enum class ExpenseCategory(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    FOOD("Продукты", "🛒", Color(0xFF2E7D32)),
    RESTAURANT("Кафе/Рестораны", "🍽️", Color(0xFF388E3C)),
    TRANSPORT("Транспорт", "🚗", Color(0xFF1565C0)),
    HOUSING("Жильё/ЖКХ", "🏠", Color(0xFF6A1B9A)),
    HEALTH("Здоровье", "💊", Color(0xFFC62828)),
    ENTERTAINMENT("Развлечения", "🎮", Color(0xFFE65100)),
    CLOTHING("Одежда", "👕", Color(0xFF00695C)),
    OTHER("Другое", "💸", Color(0xFF37474F));

    companion object {
        fun fromStringOrNull(value: String): ExpenseCategory? =
            entries.find { it.name == value }
    }
}
