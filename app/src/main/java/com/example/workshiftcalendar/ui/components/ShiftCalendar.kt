package com.example.workshiftcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workshiftcalendar.domain.model.ShiftDetails
import com.example.workshiftcalendar.domain.model.ShiftKind
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Компонент календаря смен
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftCalendar(
    month: YearMonth,
    assignments: Map<LocalDate, ShiftDetails>,
    vacations: List<com.example.workshiftcalendar.domain.model.VacationPeriod>,
    onDateClick: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var showYearMonthPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Заголовок с месяцем
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(month.minusMonths(1)) }) {
                Text("‹", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            
            Text(
                text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() } + 
                       " ${month.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showYearMonthPicker = true }
            )
            
            IconButton(onClick = { onMonthChanged(month.plusMonths(1)) }) {
                Text("›", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Дни недели
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Сетка календаря
        val days = generateCalendarDays(month)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { day ->
                CalendarDayCell(
                    day = day,
                    isCurrentMonth = day.month == month.month,
                    isToday = day == LocalDate.now(),
                    shift = assignments[day],
                    isOnVacation = vacations.any { it.containsDate(day) },
                    onClick = { onDateClick(day) }
                )
            }
        }
    }

    if (showYearMonthPicker) {
        YearMonthPickerDialog(
            currentMonth = month,
            onMonthSelected = { 
                onMonthChanged(it)
                showYearMonthPicker = false
            },
            onDismiss = { showYearMonthPicker = false }
        )
    }
}

/**
 * Генерация дней календаря для отображения
 */
private fun generateCalendarDays(month: YearMonth): List<LocalDate> {
    val firstDay = month.atDay(1)
    val lastDay = month.atEndOfMonth()
    val days = mutableListOf<LocalDate>()

    // Добавляем дни предыдущего месяца для заполнения первой недели
    val firstDayOfWeek = firstDay.dayOfWeek.value // 1 = Пн, 7 = Вс
    for (i in 0 until firstDayOfWeek - 1) {
        days.add(firstDay.minusDays((firstDayOfWeek - 1 - i).toLong()))
    }

    // Добавляем дни текущего месяца
    for (day in 1..lastDay.dayOfMonth) {
        days.add(month.atDay(day))
    }

    // Добавляем дни следующего месяца для заполнения последней недели
    val remainingDays = 42 - days.size // 6 рядов по 7 дней
    for (i in 1..remainingDays) {
        days.add(lastDay.plusDays(i.toLong()))
    }

    return days
}

/**
 * Ячейка дня календаря
 */
@Composable
fun CalendarDayCell(
    day: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    shift: ShiftDetails?,
    isOnVacation: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        shift != null -> shift.getLightColor()
        isOnVacation -> Color(0xFFE8F5E9)
        else -> Color.Transparent
    }

    val textColor = when {
        shift != null -> shift.getColor()
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (shift != null) {
                Text(
                    text = shift.kind.emoji,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else if (isOnVacation) {
                Text(
                    text = "🏖️",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Диалог выбора месяца
 */
@Composable
fun YearMonthPickerDialog(
    currentMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите месяц") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(12) { monthIndex ->
                    val month = YearMonth.of(currentMonth.year, monthIndex + 1)
                    val isSelected = month == currentMonth
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMonthSelected(month) },
                        label = {
                            Text(
                                month.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

/**
 * Получить цвет смены с учётом кастомного цвета
 */
fun ShiftKind.getColor(customColors: Map<ShiftKind, Long>): Color =
    customColors[this]?.let { Color(it) } ?: this.color
