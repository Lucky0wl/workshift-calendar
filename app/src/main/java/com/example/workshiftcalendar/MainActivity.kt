package com.example.workshiftcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workshiftcalendar.ui.theme.AppStyle
import com.example.workshiftcalendar.ui.theme.WorkshiftTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkshiftAppRoot()
        }
    }
}

private enum class BottomTab(val label: String) {
    CALENDAR("Календарь"),
    TEMPLATES("Шаблоны"),
    STYLES("Стили")
}

private enum class ShiftKind(val displayName: String, val color: Color) {
    MORNING("Утро", Color(0xFF4CAF50)),
    EVENING("Вечер", Color(0xFFFF9800)),
    NIGHT("Ночь", Color(0xFF3F51B5)),
    OFF("Выходной", Color(0xFF9E9E9E))
}

private data class ShiftTemplate(
    val id: String,
    val name: String,
    val description: String,
    val pattern: List<ShiftKind>
)

private data class DayCell(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Composable
private fun defaultTemplates(): List<ShiftTemplate> = listOf(
    ShiftTemplate(
        id = "2_2_4",
        name = "2 дня / 2 ночи / 4 выходных",
        description = "Классический вахтовый график 2-2-4",
        pattern = listOf(
            ShiftKind.MORNING, ShiftKind.MORNING,
            ShiftKind.NIGHT, ShiftKind.NIGHT,
            ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF, ShiftKind.OFF
        )
    ),
    ShiftTemplate(
        id = "5_2_morning",
        name = "5/2 дневные",
        description = "Пн–Пт утро, Сб–Вс выходные",
        pattern = listOf(
            ShiftKind.MORNING,
            ShiftKind.MORNING,
            ShiftKind.MORNING,
            ShiftKind.MORNING,
            ShiftKind.MORNING,
            ShiftKind.OFF,
            ShiftKind.OFF
        )
    ),
    ShiftTemplate(
        id = "2_2",
        name = "2 через 2",
        description = "Две смены, два выходных",
        pattern = listOf(
            ShiftKind.MORNING,
            ShiftKind.MORNING,
            ShiftKind.OFF,
            ShiftKind.OFF
        )
    )
)

@Composable
fun WorkshiftAppRoot() {
    var currentStyle by remember { mutableStateOf(AppStyle.MODERN_BLUE) }
    var currentTab by remember { mutableStateOf(BottomTab.CALENDAR) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var assignments by remember { mutableStateOf(mapOf<LocalDate, ShiftKind>()) }
    var templates by remember { mutableStateOf(defaultTemplates()) }

    WorkshiftTheme(style = currentStyle) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == BottomTab.CALENDAR,
                        onClick = { currentTab = BottomTab.CALENDAR },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        label = { Text("Календарь") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.TEMPLATES,
                        onClick = { currentTab = BottomTab.TEMPLATES },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Шаблоны") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.STYLES,
                        onClick = { currentTab = BottomTab.STYLES },
                        icon = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                        label = { Text("Стили") }
                    )
                }
            }
        ) { innerPadding ->
            when (currentTab) {
                BottomTab.CALENDAR -> CalendarScreen(
                    modifier = Modifier.padding(innerPadding),
                    month = selectedMonth,
                    onMonthChanged = { selectedMonth = it },
                    assignments = assignments,
                    onAssignmentsChange = { assignments = it },
                    templates = templates
                )

                BottomTab.TEMPLATES -> TemplatesScreen(
                    modifier = Modifier.padding(innerPadding),
                    templates = templates
                )

                BottomTab.STYLES -> StylesScreen(
                    modifier = Modifier.padding(innerPadding),
                    currentStyle = currentStyle,
                    onStyleChange = { currentStyle = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreen(
    modifier: Modifier = Modifier,
    month: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    assignments: Map<LocalDate, ShiftKind>,
    onAssignmentsChange: (Map<LocalDate, ShiftKind>) -> Unit,
    templates: List<ShiftTemplate>
) {
    val locale = Locale.getDefault()
    var showShiftDialogForDate by remember { mutableStateOf<LocalDate?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    val days = remember(month) { buildMonthGrid(month) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(month.minusMonths(1)) }) {
                Text("<", fontSize = 20.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = month.year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { onMonthChanged(month.plusMonths(1)) }) {
                Text(">", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.values().forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)
        ) {
            items(days) { dayCell ->
                val shift = assignments[dayCell.date]
                val isToday = dayCell.date == LocalDate.now()
                val background = when {
                    shift != null -> shift.color.copy(alpha = 0.18f)
                    else -> Color.Transparent
                }

                Card(
                    modifier = Modifier
                        .height(56.dp)
                        .animateItemPlacement()
                        .clickable { showShiftDialogForDate = dayCell.date },
                    colors = CardDefaults.cardColors(
                        containerColor = background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = dayCell.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (dayCell.isCurrentMonth)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        if (shift != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = shift.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                color = shift.color
                            )
                        }
                    }
                }
            }
        }

        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            onClick = { showTemplateDialog = true }
        ) {
            Text("Заполнить месяц по шаблону")
        }

        if (showShiftDialogForDate != null) {
            val date = showShiftDialogForDate!!
            ShiftPickerDialog(
                date = date,
                current = assignments[date],
                onDismiss = { showShiftDialogForDate = null },
                onShiftSelected = { selected ->
                    val updated = assignments.toMutableMap()
                    if (selected == null) {
                        updated.remove(date)
                    } else {
                        updated[date] = selected
                    }
                    onAssignmentsChange(updated)
                    showShiftDialogForDate = null
                }
            )
        }

        if (showTemplateDialog) {
            TemplatePickerDialog(
                month = month,
                templates = templates,
                onDismiss = { showTemplateDialog = false },
                onTemplateSelected = { template ->
                    val updated = applyTemplateToMonth(
                        month = month,
                        template = template,
                        initialAssignments = assignments
                    )
                    onAssignmentsChange(updated)
                    showTemplateDialog = false
                }
            )
        }
    }
}

private fun buildMonthGrid(month: YearMonth): List<DayCell> {
    val firstOfMonth = month.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // переводим так, чтобы понедельник был 1, воскресенье 0
    val daysInMonth = month.lengthOfMonth()

    val cells = mutableListOf<DayCell>()

    // Предыдущий месяц
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val leadingDays = if (firstDayOfWeek == 0) 6 else firstDayOfWeek - 1
    for (i in leadingDays downTo 1) {
        val date = prevMonth.atDay(daysInPrevMonth - i + 1)
        cells.add(DayCell(date = date, isCurrentMonth = false))
    }

    // Текущий месяц
    for (day in 1..daysInMonth) {
        cells.add(DayCell(date = month.atDay(day), isCurrentMonth = true))
    }

    // Следующий месяц
    val totalCells = ((cells.size + 6) / 7) * 7
    val nextMonth = month.plusMonths(1)
    var dayCounter = 1
    while (cells.size < totalCells) {
        cells.add(DayCell(date = nextMonth.atDay(dayCounter++), isCurrentMonth = false))
    }

    return cells
}

@Composable
private fun ShiftPickerDialog(
    date: LocalDate,
    current: ShiftKind?,
    onDismiss: () -> Unit,
    onShiftSelected: (ShiftKind?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Смена на ${date.dayOfMonth}.${date.monthValue}.${date.year}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Выберите тип смены или очистите день",
                    style = MaterialTheme.typography.bodyMedium
                )
                ShiftKind.values().forEach { shift ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShiftSelected(shift) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(
                            modifier = Modifier
                                .height(16.dp)
                                .width(16.dp)
                                .background(
                                    color = shift.color,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        Text(
                            text = shift.displayName,
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (current == shift)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                TextButton(onClick = { onShiftSelected(null) }) {
                    Text("Очистить день")
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

@Composable
private fun TemplatePickerDialog(
    month: YearMonth,
    templates: List<ShiftTemplate>,
    onDismiss: () -> Unit,
    onTemplateSelected: (ShiftTemplate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Применить шаблон к ${month.monthValue}.${month.year}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                templates.forEach { template ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTemplateSelected(template) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun applyTemplateToMonth(
    month: YearMonth,
    template: ShiftTemplate,
    initialAssignments: Map<LocalDate, ShiftKind>
): Map<LocalDate, ShiftKind> {
    val result = initialAssignments.toMutableMap()
    val daysInMonth = month.lengthOfMonth()
    var patternIndex = 0

    for (day in 1..daysInMonth) {
        val date = month.atDay(day)
        val shift = template.pattern[patternIndex % template.pattern.size]
        result[date] = shift
        patternIndex++
    }

    return result
}

@Composable
private fun TemplatesScreen(
    modifier: Modifier = Modifier,
    templates: List<ShiftTemplate>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Готовые шаблоны графиков",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Вы можете применять шаблоны на экране календаря. Здесь показано их краткое описание.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        templates.forEach { template ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        template.pattern.distinct().forEach { shift ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .height(12.dp)
                                        .width(12.dp)
                                        .background(
                                            color = shift.color,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                                Text(
                                    text = shift.displayName,
                                    modifier = Modifier.padding(start = 4.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StylesScreen(
    modifier: Modifier = Modifier,
    currentStyle: AppStyle,
    onStyleChange: (AppStyle) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Стили оформления",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Выберите визуальный стиль приложения. Он сразу применяется ко всем экранам.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        StyleCard(
            title = "Современный синий",
            description = "Аккуратный светлый интерфейс в сине-зелёной палитре.",
            isSelected = currentStyle == AppStyle.MODERN_BLUE,
            accent = Color(0xFF1565C0),
            onClick = { onStyleChange(AppStyle.MODERN_BLUE) }
        )

        StyleCard(
            title = "Тёмный AMOLED",
            description = "Глубокий чёрный фон для экономии батареи на OLED-экранах.",
            isSelected = currentStyle == AppStyle.DARK_AMOLED,
            accent = Color(0xFFBB86FC),
            onClick = { onStyleChange(AppStyle.DARK_AMOLED) }
        )

        StyleCard(
            title = "Тёплый пастельный",
            description = "Ненавязчивые тёплые оттенки для мягкого дневного интерфейса.",
            isSelected = currentStyle == AppStyle.WARM_PASTEL,
            accent = Color(0xFFF97316),
            onClick = { onStyleChange(AppStyle.WARM_PASTEL) }
        )
    }
}

@Composable
private fun StyleCard(
    title: String,
    description: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .height(36.dp)
                    .width(6.dp)
                    .background(
                        color = accent,
                        shape = MaterialTheme.shapes.small
                    )
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

