package com.example.workshiftcalendar

import android.content.Context
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.workshiftcalendar.ui.theme.AppStyle
import com.example.workshiftcalendar.ui.theme.WorkshiftTheme
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ═══════════════════════════════════════════════
// DataStore
// ═══════════════════════════════════════════════

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workshift_v2")
private val APP_DATA_KEY = stringPreferencesKey("app_data_v2")
private val appGson = Gson()

// ═══════════════════════════════════════════════
// Domain Model
// ═══════════════════════════════════════════════

private enum class BottomTab(val label: String) {
    CALENDAR("Календарь"), STATS("Статистика"), TEMPLATES("Шаблоны"), SETTINGS("Настройки")
}

private enum class ShiftKind(
    val displayName: String,
    val shortName: String,
    val color: Color,
    val lightColor: Color,
    val hoursPerShift: Int
) {
    MORNING("Утренняя", "У", Color(0xFFBF360C), Color(0xFFFFE0B2), 8),
    EVENING("Вечерняя", "В", Color(0xFF4A148C), Color(0xFFE1BEE7), 8),
    NIGHT("Ночная", "Н", Color(0xFF0D47A1), Color(0xFFBBDEFB), 12),
    OFF("Выходной", "О", Color(0xFF424242), Color(0xFFF5F5F5), 0)
}

private data class ShiftDetails(
    val kind: ShiftKind,
    val note: String = "",
    val location: String = "",
    val customSalary: String = ""
)

private data class ShiftTemplate(
    val id: String,
    val name: String,
    val description: String,
    val pattern: List<ShiftKind>,
    val isBuiltIn: Boolean = false
)

private data class DayCell(val date: LocalDate, val isCurrentMonth: Boolean)

// ═══════════════════════════════════════════════
// Serialisation DTOs
// ═══════════════════════════════════════════════

private data class ShiftDetailsDto(val kind: String = "", val note: String = "", val location: String = "", val customSalary: String = "")
private data class ShiftTemplateDto(val id: String = "", val name: String = "", val description: String = "", val pattern: List<String> = emptyList())
private data class AppDataDto(
    val assignments: Map<String, ShiftDetailsDto> = emptyMap(),
    val customTemplates: List<ShiftTemplateDto> = emptyList(),
    val shiftRates: Map<String, String> = emptyMap(),
    val appStyle: String = AppStyle.MODERN_BLUE.name
)

private fun Map<LocalDate, ShiftDetails>.toDto() = entries.associate { (d, v) ->
    d.toString() to ShiftDetailsDto(v.kind.name, v.note, v.location, v.customSalary)
}

private fun Map<String, ShiftDetailsDto>.toDomain() = entries.mapNotNull { (ds, dto) ->
    try { LocalDate.parse(ds) to ShiftDetails(ShiftKind.valueOf(dto.kind), dto.note, dto.location, dto.customSalary) }
    catch (e: Exception) { null }
}.toMap()

private fun ShiftTemplate.toDto() = ShiftTemplateDto(id, name, description, pattern.map { it.name })
private fun ShiftTemplateDto.toDomain() = try {
    ShiftTemplate(id, name, description, pattern.map { ShiftKind.valueOf(it) })
} catch (e: Exception) { null }

private fun Map<ShiftKind, String>.toStringMap() = entries.associate { (k, v) -> k.name to v }
private fun Map<String, String>.toShiftKindMap() = entries.mapNotNull { (k, v) ->
    try { ShiftKind.valueOf(k) to v } catch (e: Exception) { null }
}.toMap()

// ═══════════════════════════════════════════════
// Built-in Templates
// ═══════════════════════════════════════════════

private fun builtInTemplates() = listOf(
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

// ═══════════════════════════════════════════════
// MainActivity
// ═══════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WorkshiftAppRoot() }
    }
}

// ═══════════════════════════════════════════════
// Root Composable (with DataStore persistence)
// ═══════════════════════════════════════════════

@Composable
fun WorkshiftAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentStyle by remember { mutableStateOf(AppStyle.MODERN_BLUE) }
    var currentTab by remember { mutableStateOf(BottomTab.CALENDAR) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var assignments by remember { mutableStateOf(mapOf<LocalDate, ShiftDetails>()) }
    var templates by remember { mutableStateOf(builtInTemplates()) }
    var shiftRates by remember {
        mutableStateOf(ShiftKind.values().filter { it != ShiftKind.OFF }.associateWith { "" })
    }
    var isLoaded by remember { mutableStateOf(false) }

    // Load from DataStore once
    LaunchedEffect(Unit) {
        try {
            val prefs = context.dataStore.data.first()
            prefs[APP_DATA_KEY]?.let { json ->
                val dto = appGson.fromJson(json, AppDataDto::class.java)
                assignments = dto.assignments.toDomain()
                val custom = dto.customTemplates.mapNotNull { it.toDomain() }
                templates = builtInTemplates() + custom
                shiftRates = ShiftKind.values().filter { it != ShiftKind.OFF }
                    .associateWith { dto.shiftRates[it.name] ?: "" }
                try { currentStyle = AppStyle.valueOf(dto.appStyle) } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        isLoaded = true
    }

    // Auto-save on state change (only after initial load)
    LaunchedEffect(assignments, shiftRates, currentStyle, isLoaded) {
        if (!isLoaded) return@LaunchedEffect
        val dto = AppDataDto(
            assignments = assignments.toDto(),
            customTemplates = templates.filter { !it.isBuiltIn }.map { it.toDto() },
            shiftRates = shiftRates.toStringMap(),
            appStyle = currentStyle.name
        )
        context.dataStore.edit { it[APP_DATA_KEY] = appGson.toJson(dto) }
    }

    WorkshiftTheme(style = currentStyle) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == BottomTab.CALENDAR,
                        onClick = { currentTab = BottomTab.CALENDAR },
                        icon = { Icon(Icons.Outlined.CalendarMonth, null) },
                        label = { Text("Календарь") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.STATS,
                        onClick = { currentTab = BottomTab.STATS },
                        icon = { Icon(Icons.Outlined.BarChart, null) },
                        label = { Text("Статистика") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.TEMPLATES,
                        onClick = { currentTab = BottomTab.TEMPLATES },
                        icon = { Icon(Icons.Default.List, null) },
                        label = { Text("Шаблоны") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.SETTINGS,
                        onClick = { currentTab = BottomTab.SETTINGS },
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        label = { Text("Настройки") }
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
                    templates = templates,
                    shiftRates = shiftRates
                )
                BottomTab.STATS -> StatsScreen(
                    modifier = Modifier.padding(innerPadding),
                    month = selectedMonth,
                    onMonthChanged = { selectedMonth = it },
                    assignments = assignments,
                    shiftRates = shiftRates
                )
                BottomTab.TEMPLATES -> TemplatesScreen(
                    modifier = Modifier.padding(innerPadding),
                    templates = templates,
                    onTemplatesChange = { templates = it },
                    month = selectedMonth,
                    assignments = assignments,
                    onAssignmentsChange = { assignments = it }
                )
                BottomTab.SETTINGS -> SettingsScreen(
                    modifier = Modifier.padding(innerPadding),
                    currentStyle = currentStyle,
                    onStyleChange = { currentStyle = it },
                    shiftRates = shiftRates,
                    onRatesChange = { shiftRates = it }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
// Calendar Screen
// ═══════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreen(
    modifier: Modifier,
    month: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    assignments: Map<LocalDate, ShiftDetails>,
    onAssignmentsChange: (Map<LocalDate, ShiftDetails>) -> Unit,
    templates: List<ShiftTemplate>,
    shiftRates: Map<ShiftKind, String>
) {
    val locale = Locale.getDefault()
    var showShiftDialogForDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDetailForDate by remember { mutableStateOf<LocalDate?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    val days = remember(month) { buildMonthGrid(month) }

    val monthFiltered = remember(month, assignments) {
        assignments.filter { (d, _) -> YearMonth.from(d) == month }
    }
    val monthWorkCount = remember(monthFiltered) { monthFiltered.values.count { it.kind != ShiftKind.OFF } }
    val monthSalary = remember(monthFiltered, shiftRates) {
        monthFiltered.values.sumOf { it.customSalary.toIntOrNull() ?: shiftRates[it.kind]?.toIntOrNull() ?: 0 }
    }
    val monthHours = remember(monthFiltered) { monthFiltered.values.sumOf { it.kind.hoursPerShift } }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)))
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(month.minusMonths(1)) }) {
                    Text("‹", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = month.year.toString(), style = MaterialTheme.typography.bodySmall)
                    if (monthWorkCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatChip("$monthWorkCount смен")
                            if (monthHours > 0) StatChip("$monthHours ч")
                            if (monthSalary > 0) StatChip("$monthSalary ₽", highlight = true)
                        }
                    }
                }
                IconButton(onClick = { onMonthChanged(month.plusMonths(1)) }) {
                    Text("›", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Day-of-week headers
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp)) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { i, d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (i >= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
        ) {
            items(days) { cell ->
                val shiftDetails = if (cell.isCurrentMonth) assignments[cell.date] else null
                DayCard(
                    cell = cell,
                    shiftDetails = shiftDetails,
                    onClick = { if (cell.isCurrentMonth) showShiftDialogForDate = cell.date },
                    onLongClick = {
                        if (cell.isCurrentMonth && shiftDetails != null) showDetailForDate = cell.date
                        else if (cell.isCurrentMonth) showShiftDialogForDate = cell.date
                    }
                )
            }
        }

        // Apply template button
        OutlinedButton(
            onClick = { showTemplateDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Применить шаблон к месяцу")
        }
    }

    // Dialogs
    showShiftDialogForDate?.let { date ->
        ShiftPickerDialog(
            date = date,
            currentDetails = assignments[date],
            shiftRates = shiftRates,
            onDetailsSaved = { details ->
                val m = assignments.toMutableMap()
                if (details == null) m.remove(date) else m[date] = details
                onAssignmentsChange(m)
                showShiftDialogForDate = null
            },
            onDismiss = { showShiftDialogForDate = null }
        )
    }

    showDetailForDate?.let { date ->
        assignments[date]?.let { details ->
            DayDetailDialog(
                date = date,
                details = details,
                onEdit = { showShiftDialogForDate = date; showDetailForDate = null },
                onDelete = {
                    val m = assignments.toMutableMap(); m.remove(date)
                    onAssignmentsChange(m); showDetailForDate = null
                },
                onDismiss = { showDetailForDate = null }
            )
        }
    }

    if (showTemplateDialog) {
        TemplatePicker(
            templates = templates, month = month,
            onApply = { added -> onAssignmentsChange(assignments + added); showTemplateDialog = false },
            onDismiss = { showTemplateDialog = false }
        )
    }
}

@Composable
private fun StatChip(text: String, highlight: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCard(
    cell: DayCell,
    shiftDetails: ShiftDetails?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isToday = cell.isCurrentMonth && cell.date == LocalDate.now()
    val isWeekend = cell.date.dayOfWeek == DayOfWeek.SATURDAY || cell.date.dayOfWeek == DayOfWeek.SUNDAY
    val bgColor = when {
        !cell.isCurrentMonth -> Color.Transparent
        shiftDetails != null && shiftDetails.kind != ShiftKind.OFF -> shiftDetails.kind.lightColor
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) else Modifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (!cell.isCurrentMonth) Color.Transparent
                else if (isWeekend) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onBackground
            )
            if (cell.isCurrentMonth && shiftDetails != null) {
                Box(
                    modifier = Modifier.size(18.dp).clip(CircleShape).background(shiftDetails.kind.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = shiftDetails.kind.shortName, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (!cell.isCurrentMonth) {
                Text(text = cell.date.dayOfMonth.toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f))
            }
            if (shiftDetails?.note?.isNotBlank() == true || shiftDetails?.location?.isNotBlank() == true) {
                Spacer(Modifier.height(1.dp))
                Box(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            }
        }
    }
}

// ═══════════════════════════════════════════════
// Stats Screen
// ═══════════════════════════════════════════════

@Composable
private fun StatsScreen(
    modifier: Modifier,
    month: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    assignments: Map<LocalDate, ShiftDetails>,
    shiftRates: Map<ShiftKind, String>
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    val filtered = remember(month, assignments) { assignments.filter { (d, _) -> YearMonth.from(d) == month } }
    val workShifts = remember(filtered) { filtered.values.filter { it.kind != ShiftKind.OFF } }
    val totalShifts = workShifts.size
    val totalHours = workShifts.sumOf { it.kind.hoursPerShift }
    val totalEarnings = remember(filtered, shiftRates) {
        filtered.values.sumOf { it.customSalary.toIntOrNull() ?: shiftRates[it.kind]?.toIntOrNull() ?: 0 }
    }

    val weeklyData = remember(month, filtered) {
        (0..5).mapNotNull { week ->
            val startDay = week * 7 + 1
            if (startDay > month.lengthOfMonth()) return@mapNotNull null
            val endDay = minOf(startDay + 6, month.lengthOfMonth())
            (startDay..endDay).count { day ->
                val d = filtered[month.atDay(day)]
                d != null && d.kind != ShiftKind.OFF
            }
        }
    }

    val shareText = remember(month, filtered, shiftRates) {
        buildShareText(month, filtered, shiftRates, locale)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(month.minusMonths(1)) }) {
                Text("‹", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "${month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChanged(month.plusMonths(1)) }) {
                Text("›", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Summary cards
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard("Смен", totalShifts.toString(), MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            SummaryCard("Часов", totalHours.toString(), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
            SummaryCard("Заработок", if (totalEarnings > 0) "$totalEarnings ₽" else "—", MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
        }

        // Bar chart
        if (weeklyData.any { it > 0 }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Смены по неделям", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    val maxVal = (weeklyData.maxOrNull() ?: 1).coerceAtLeast(1)
                    val barColor = MaterialTheme.colorScheme.primary
                    val bgColor = MaterialTheme.colorScheme.surfaceVariant
                    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        val totalBars = weeklyData.size
                        val barWidth = size.width / (totalBars * 2f)
                        weeklyData.forEachIndexed { i, v ->
                            val x = i * barWidth * 2 + barWidth / 2
                            drawRect(bgColor, topLeft = Offset(x, 0f), size = Size(barWidth, size.height))
                            val barH = (v.toFloat() / maxVal) * size.height
                            drawRect(barColor, topLeft = Offset(x, size.height - barH), size = Size(barWidth, barH))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        weeklyData.forEachIndexed { i, _ ->
                            Text("Нед ${i + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Shift breakdown
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("По типам смен", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ShiftKind.values().filter { it != ShiftKind.OFF }.forEach { kind ->
                    val count = workShifts.count { it.kind == kind }
                    val hours = count * kind.hoursPerShift
                    val earnings = filtered.values.filter { it.kind == kind }
                        .sumOf { it.customSalary.toIntOrNull() ?: shiftRates[kind]?.toIntOrNull() ?: 0 }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(12.dp).clip(CircleShape).background(kind.color))
                            Text(kind.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("$count×", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            if (hours > 0) Text("$hours ч", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (earnings > 0) Text("$earnings ₽", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Share button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Поделиться расписанием"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Share, null)
            Spacer(Modifier.width(8.dp))
            Text("Поделиться расписанием")
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, bgColor: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun buildShareText(
    month: YearMonth,
    filtered: Map<LocalDate, ShiftDetails>,
    shiftRates: Map<ShiftKind, String>,
    locale: Locale
): String {
    val fmt = DateTimeFormatter.ofPattern("dd.MM", locale)
    val header = "📅 Расписание: ${month.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${month.year}"
    val lines = (1..month.lengthOfMonth()).mapNotNull { day ->
        val date = month.atDay(day)
        val d = filtered[date] ?: return@mapNotNull null
        val emoji = when (d.kind) {
            ShiftKind.MORNING -> "🌅"; ShiftKind.EVENING -> "🌆"; ShiftKind.NIGHT -> "🌙"; ShiftKind.OFF -> "🏖️"
        }
        val loc = if (d.location.isNotBlank()) " | ${d.location}" else ""
        val note = if (d.note.isNotBlank()) " — ${d.note}" else ""
        "${date.format(fmt)} $emoji ${d.kind.displayName}$loc$note"
    }
    val totalShifts = filtered.values.count { it.kind != ShiftKind.OFF }
    val totalEarnings = filtered.values.sumOf { it.customSalary.toIntOrNull() ?: shiftRates[it.kind]?.toIntOrNull() ?: 0 }
    val footer = buildString {
        append("\n📊 Всего смен: $totalShifts")
        if (totalEarnings > 0) append("\n💰 Заработок: $totalEarnings ₽")
    }
    return "$header\n\n${lines.joinToString("\n")}$footer"
}

// ═══════════════════════════════════════════════
// Templates Screen
// ═══════════════════════════════════════════════

@Composable
private fun TemplatesScreen(
    modifier: Modifier,
    templates: List<ShiftTemplate>,
    onTemplatesChange: (List<ShiftTemplate>) -> Unit,
    month: YearMonth,
    assignments: Map<LocalDate, ShiftDetails>,
    onAssignmentsChange: (Map<LocalDate, ShiftDetails>) -> Unit
) {
    var applyTarget by remember { mutableStateOf<ShiftTemplate?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Шаблоны графиков",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onApply = { applyTarget = template },
                    onDelete = if (!template.isBuiltIn) {
                        { onTemplatesChange(templates.filter { it.id != template.id }) }
                    } else null
                )
            }
        }
    }

    applyTarget?.let { tpl ->
        TemplatePicker(
            templates = listOf(tpl),
            month = month,
            onApply = { added ->
                onAssignmentsChange(assignments + added)
                applyTarget = null
            },
            onDismiss = { applyTarget = null }
        )
    }
}

@Composable
private fun TemplateCard(template: ShiftTemplate, onApply: () -> Unit, onDelete: (() -> Unit)?) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(Modifier.height(8.dp))
            // Pattern dots
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                template.pattern.forEach { kind ->
                    Box(
                        Modifier.size(16.dp).clip(CircleShape).background(kind.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(kind.shortName, fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text("Применить к месяцу")
            }
        }
    }
}

// ═══════════════════════════════════════════════
// Settings Screen
// ═══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    modifier: Modifier,
    currentStyle: AppStyle,
    onStyleChange: (AppStyle) -> Unit,
    shiftRates: Map<ShiftKind, String>,
    onRatesChange: (Map<ShiftKind, String>) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        // Theme picker
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Тема оформления", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppStyle.values().forEach { style ->
                        FilterChip(
                            selected = currentStyle == style,
                            onClick = { onStyleChange(style) },
                            label = {
                                Text(
                                    when (style) {
                                        AppStyle.MODERN_BLUE -> "Синяя"
                                        AppStyle.DARK_AMOLED -> "Тёмная"
                                        AppStyle.WARM_PASTEL -> "Тёплая"
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        // Shift rates
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ставка оплаты за смену (₽)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ShiftKind.values().filter { it != ShiftKind.OFF }.forEach { kind ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(kind.color))
                        Text(kind.displayName, modifier = Modifier.width(90.dp))
                        OutlinedTextField(
                            value = shiftRates[kind] ?: "",
                            onValueChange = { v -> onRatesChange(shiftRates + (kind to v)) },
                            label = { Text("₽") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// Dialogs
// ═══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftPickerDialog(
    date: LocalDate,
    currentDetails: ShiftDetails?,
    shiftRates: Map<ShiftKind, String>,
    onDetailsSaved: (ShiftDetails?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedKind by remember { mutableStateOf(currentDetails?.kind) }
    var currentNote by remember { mutableStateOf(currentDetails?.note ?: "") }
    var currentLocation by remember { mutableStateOf(currentDetails?.location ?: "") }
    var currentCustomSalary by remember { mutableStateOf(currentDetails?.customSalary ?: "") }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) currentLocation = "Lat: ${String.format(Locale.US, "%.4f", loc.latitude)}, Lng: ${String.format(Locale.US, "%.4f", loc.longitude)}"
                }
            } catch (_: SecurityException) {}
        }
    }
    val fetchLocation: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) currentLocation = "Lat: ${String.format(Locale.US, "%.4f", loc.latitude)}, Lng: ${String.format(Locale.US, "%.4f", loc.longitude)}"
                }
            } catch (_: SecurityException) {}
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(fmt), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Тип смены:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShiftKind.values().forEach { kind ->
                        FilterChip(
                            selected = selectedKind == kind,
                            onClick = { selectedKind = kind },
                            label = { Text(kind.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                if (selectedKind != null && selectedKind != ShiftKind.OFF) {
                    val defaultRate = shiftRates[selectedKind] ?: ""
                    OutlinedTextField(
                        value = currentCustomSalary,
                        onValueChange = { currentCustomSalary = it },
                        label = { Text(if (defaultRate.isNotBlank()) "Оплата (ставка: $defaultRate ₽)" else "Оплата за этот день (₽)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = currentLocation,
                        onValueChange = { currentLocation = it },
                        label = { Text("Локация / Объект") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = fetchLocation) {
                                Icon(Icons.Outlined.Place, contentDescription = "GPS", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = currentNote,
                        onValueChange = { currentNote = it },
                        label = { Text("Заметка / Что сделано") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    onClick = { selectedKind = null; currentNote = ""; currentLocation = ""; currentCustomSalary = "" },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Очистить день") }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Отмена") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    onDetailsSaved(
                        if (selectedKind != null) ShiftDetails(selectedKind!!, currentNote, currentLocation, currentCustomSalary)
                        else null
                    )
                }) { Text("Сохранить") }
            }
        }
    )
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    details: ShiftDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(details.kind.color))
                Text(date.format(fmt), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Смена: ${details.kind.displayName}", style = MaterialTheme.typography.bodyMedium)
                if (details.location.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Place, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(details.location, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (details.note.isNotBlank()) {
                    Text("📝 ${details.note}", style = MaterialTheme.typography.bodySmall)
                }
                if (details.customSalary.isNotBlank()) {
                    Text("💰 ${details.customSalary} ₽", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить")
                }
                TextButton(onClick = onDismiss) { Text("OK") }
            }
        }
    )
}

@Composable
private fun TemplatePicker(
    templates: List<ShiftTemplate>,
    month: YearMonth,
    onApply: (Map<LocalDate, ShiftDetails>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(templates.firstOrNull()) }
    var startDay by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Применить шаблон", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (templates.size > 1) {
                    Text("Шаблон:", style = MaterialTheme.typography.labelMedium)
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(templates) { tpl ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { selected = tpl }.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(selected = selected?.id == tpl.id, onClick = { selected = tpl })
                                Text(tpl.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    selected = templates.firstOrNull()
                    selected?.let { Text("Шаблон: ${it.name}", style = MaterialTheme.typography.bodyMedium) }
                }
                OutlinedTextField(
                    value = startDay,
                    onValueChange = { startDay = it },
                    label = { Text("Начать с дня месяца") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Отмена") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    selected?.let { tpl ->
                        onApply(applyTemplateToMonth(tpl, month, startDay.toIntOrNull() ?: 1))
                    }
                }) { Text("Применить") }
            }
        }
    )
}

// ═══════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════

private fun buildMonthGrid(month: YearMonth): List<DayCell> {
    val firstDay = month.atDay(1)
    val daysOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val cells = mutableListOf<DayCell>()
    for (i in daysOffset downTo 1) {
        cells.add(DayCell(firstDay.minusDays(i.toLong()), false))
    }
    for (day in 1..month.lengthOfMonth()) {
        cells.add(DayCell(month.atDay(day), true))
    }
    val remainder = (7 - cells.size % 7) % 7
    for (i in 1..remainder) {
        cells.add(DayCell(month.atEndOfMonth().plusDays(i.toLong()), false))
    }
    return cells
}

private fun applyTemplateToMonth(template: ShiftTemplate, month: YearMonth, startDay: Int): Map<LocalDate, ShiftDetails> {
    val result = mutableMapOf<LocalDate, ShiftDetails>()
    var patternIndex = 0
    val actualStart = startDay.coerceIn(1, month.lengthOfMonth())
    for (day in actualStart..month.lengthOfMonth()) {
        val date = month.atDay(day)
        result[date] = ShiftDetails(kind = template.pattern[patternIndex % template.pattern.size])
        patternIndex++
    }
    return result
}
