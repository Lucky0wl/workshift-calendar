package com.example.workshiftcalendar.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workshiftcalendar.domain.model.ShiftDetails
import com.example.workshiftcalendar.domain.model.ShiftKind
import com.example.workshiftcalendar.ui.viewmodel.WorkshiftViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Экран статистики
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: WorkshiftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    val monthShifts = remember(uiState.assignments, selectedMonth) {
        uiState.assignments.filter { entry ->
            YearMonth.from(entry.key) == selectedMonth
        }
    }

    val stats = calculateStats(monthShifts, uiState.shiftRates)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Месяц selector
        MonthSelector(
            month = selectedMonth,
            onPrevMonth = { selectedMonth = selectedMonth.minusMonths(1) },
            onNextMonth = { selectedMonth = selectedMonth.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Карточки статистики
        StatsCards(stats)

        Spacer(modifier = Modifier.height(16.dp))

        // Диаграмма смен
        Text(
            text = "Распределение смен",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ShiftsPieChart(
            shiftsCount = stats.shiftsByType,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Детализация по типам смен
        Text(
            text = "Детализация",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        stats.shiftsByType.forEach { (kind, count) ->
            if (count > 0) {
                ShiftTypeRow(kind, count, stats.totalHoursByType[kind] ?: 0.0)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Данные статистики
 */
data class StatsData(
    val totalShifts: Int,
    val totalHours: Double,
    val totalEarnings: Double,
    val shiftsByType: Map<ShiftKind, Int>,
    val totalHoursByType: Map<ShiftKind, Double>,
    val earningsByType: Map<ShiftKind, Double>,
    val workDays: Int,
    val daysOff: Int
)

/**
 * Расчёт статистики
 */
private fun calculateStats(
    monthShifts: Map<java.time.LocalDate, ShiftDetails>,
    shiftRates: Map<ShiftKind, String>
): StatsData {
    val shiftsByType = ShiftKind.entries.associateWith { kind ->
        monthShifts.count { it.value.kind == kind }
    }

    val totalHoursByType = ShiftKind.entries.associateWith { kind ->
        monthShifts
            .filter { it.value.kind == kind }
            .values
            .sumOf { it.calculateTotalHours() }
    }

    val earningsByType = ShiftKind.entries.filter { it != ShiftKind.OFF }.associateWith { kind ->
        val count = shiftsByType[kind] ?: 0
        val rate = shiftRates[kind]?.toDoubleOrNull() ?: 0.0
        count * rate
    }

    val totalShifts = monthShifts.size
    val totalHours = totalHoursByType.values.sum()
    val totalEarnings = earningsByType.values.sum()
    val workDays = shiftsByType.filterKeys { it != ShiftKind.OFF }.values.sum()
    val daysOff = shiftsByType[ShiftKind.OFF] ?: 0

    return StatsData(
        totalShifts = totalShifts,
        totalHours = totalHours,
        totalEarnings = totalEarnings,
        shiftsByType = shiftsByType,
        totalHoursByType = totalHoursByType,
        earningsByType = earningsByType,
        workDays = workDays,
        daysOff = daysOff
    )
}

/**
 * Selector месяца
 */
@Composable
fun MonthSelector(
    month: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth) {
            Text("‹", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Text(
            text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() } +
                   " ${month.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Text("›", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Карточки статистики
 */
@Composable
fun StatsCards(stats: StatsData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Смен", stats.totalShifts.toString(), Modifier.weight(1f))
        StatCard("Часов", stats.totalHours.formatHours(), Modifier.weight(1f))
        StatCard("Зарплата", "${stats.totalEarnings.toInt()} ₽", Modifier.weight(1f))
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Рабочих дней", stats.workDays.toString(), Modifier.weight(1f))
        StatCard("Выходных", stats.daysOff.toString(), Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Строка с типом смены
 */
@Composable
fun ShiftTypeRow(kind: ShiftKind, count: Int, hours: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(kind.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(kind.displayName)
        }
        Row {
            Text("$count смен")
            Spacer(modifier = Modifier.width(16.dp))
            Text("${hours.formatHours()} ч", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Круговая диаграмма смен
 */
@Composable
fun ShiftsPieChart(
    shiftsCount: Map<ShiftKind, Int>,
    modifier: Modifier = Modifier
) {
    val total = shiftsCount.values.sum().toFloat()
    if (total == 0f) {
        Text(
            text = "Нет данных за этот период",
            modifier = modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val data = shiftsCount
        .filter { it.value > 0 && it.key != ShiftKind.OFF }
        .map { PieSlice(it.key.color, it.value.toFloat() / total, it.key.displayName) }

    if (data.isEmpty()) {
        Text(
            text = "Только выходные",
            modifier = modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            drawPieChart(data)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Легенда
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            data.forEach { slice ->
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(slice.color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(slice.label, fontSize = 12.sp)
                }
            }
        }
    }
}

data class PieSlice(val color: Color, val fraction: Float, val label: String)

fun DrawScope.drawPieChart(slices: List<PieSlice>) {
    var startAngle = 0f
    slices.forEach { slice ->
        val sweepAngle = slice.fraction * 360f
        drawArc(
            color = slice.color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, size.height)
        )
        startAngle += sweepAngle
    }
}

private fun Double.formatHours(): String {
    val i = this.toInt()
    return if (this == i.toDouble()) i.toString() else String.format(java.util.Locale.US, "%.1f", this)
}
