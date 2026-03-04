package com.example.workshiftcalendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.pointer.pointerInput
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.FileOutputStream
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Switch
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
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.workshiftcalendar.BuildConfig
import com.example.workshiftcalendar.ui.theme.AppStyle
import com.example.workshiftcalendar.ui.theme.WorkshiftTheme
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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

class WorkshiftRepository(private val context: Context) {
    suspend fun isNotificationsEnabled(): Boolean {
        return try {
            val prefs = context.dataStore.data.first()
            val dto = appGson.fromJson(prefs[APP_DATA_KEY], AppDataDto::class.java)
            dto?.notificationsEnabled ?: false
        } catch (e: Exception) { false }
    }

    suspend fun getNotificationTime(): String {
        return try {
            val prefs = context.dataStore.data.first()
            val dto = appGson.fromJson(prefs[APP_DATA_KEY], AppDataDto::class.java)
            dto?.notificationTime ?: "20:00"
        } catch (e: Exception) { "20:00" }
    }

    suspend fun getShift(date: LocalDate): ShiftDetails? {
        return try {
            val prefs = context.dataStore.data.first()
            val dto = appGson.fromJson(prefs[APP_DATA_KEY], AppDataDto::class.java)
            dto?.assignments?.get(date.toString())?.let { shiftDto ->
                ShiftDetails(
                    kind = ShiftKind.valueOf(shiftDto.kind),
                    note = shiftDto.note,
                    location = shiftDto.location,
                    customSalary = shiftDto.customSalary,
                    customHours = shiftDto.customHours,
                    startTime = shiftDto.startTime,
                    endTime = shiftDto.endTime
                )
            }
        } catch (e: Exception) { null }
    }
}

// ═══════════════════════════════════════════════
// Domain Model
// ═══════════════════════════════════════════════

private enum class BottomTab(val label: String) {
    CALENDAR("Календарь"), STATS("Статистика"), BUDGET("Бюджет"), TEMPLATES("Шаблоны"), SETTINGS("Настройки")
}

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
    OFF("Выходной", "О", "🏠", Color(0xFF424242), Color(0xFFE0E0E0), 0)
}

data class ShiftDetails(
    val kind: ShiftKind,
    val note: String = "",
    val location: String = "",
    val customSalary: String = "",
    val customHours: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

private data class ShiftTemplate(
    val id: String,
    val name: String,
    val description: String,
    val pattern: List<ShiftKind>,
    val isBuiltIn: Boolean = false
)

private fun ShiftDetails.calculateTotalHours(): Double {
    if (kind == ShiftKind.OFF) return 0.0
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

private fun Double.formatHours(): String {
    val i = this.toInt()
    return if (this == i.toDouble()) i.toString() else String.format(Locale.US, "%.1f", this)
}

private data class DayCell(val date: LocalDate, val isCurrentMonth: Boolean)

private enum class ExpenseCategory(val displayName: String, val emoji: String, val color: Color) {
    FOOD("Продукты", "🛒", Color(0xFF2E7D32)),
    RESTAURANT("Кафе/Рестораны", "🍽️", Color(0xFF388E3C)),
    TRANSPORT("Транспорт", "🚗", Color(0xFF1565C0)),
    HOUSING("Жильё/ЖКХ", "🏠", Color(0xFF6A1B9A)),
    HEALTH("Здоровье", "💊", Color(0xFFC62828)),
    ENTERTAINMENT("Развлечения", "🎮", Color(0xFFE65100)),
    CLOTHING("Одежда", "👕", Color(0xFF00695C)),
    OTHER("Другое", "💸", Color(0xFF37474F))
}

private data class ExpenseEntry(
    val id: String,
    val date: LocalDate,
    val amount: Int,
    val category: ExpenseCategory,
    val note: String = ""
)

// ═══════════════════════════════════════════════
// Serialisation DTOs
// ═══════════════════════════════════════════════

private data class ShiftDetailsDto(val kind: String = "", val note: String = "", val location: String = "", val customSalary: String = "", val customHours: String = "", val startTime: String = "", val endTime: String = "")
private data class ShiftTemplateDto(val id: String = "", val name: String = "", val description: String = "", val pattern: List<String> = emptyList())
private data class ExpenseEntryDto(val id: String = "", val date: String = "", val amount: Int = 0, val category: String = "", val note: String = "")
private data class AppDataDto(
    val assignments: Map<String, ShiftDetailsDto> = emptyMap(),
    val customTemplates: List<ShiftTemplateDto> = emptyList(),
    val shiftRates: Map<String, String> = emptyMap(),
    val appStyle: String = AppStyle.MODERN_BLUE.name,
    val expenses: List<ExpenseEntryDto> = emptyList(),
    val notificationsEnabled: Boolean = false,
    val notificationTime: String = "20:00"
)

private fun Map<LocalDate, ShiftDetails>.toDto() = entries.associate { (d, v) ->
    d.toString() to ShiftDetailsDto(v.kind.name, v.note, v.location, v.customSalary, v.customHours, v.startTime, v.endTime)
}

private fun Map<String, ShiftDetailsDto>.toDomain() = entries.mapNotNull { (ds, dto) ->
    try { LocalDate.parse(ds) to ShiftDetails(ShiftKind.valueOf(dto.kind), dto.note, dto.location, dto.customSalary, dto.customHours, dto.startTime, dto.endTime) }
    catch (e: Exception) { null }
}.toMap()

private fun ShiftTemplate.toDto() = ShiftTemplateDto(id, name, description, pattern.map { it.name })
private fun ShiftTemplateDto.toDomain() = try {
    ShiftTemplate(id, name, description, pattern.map { ShiftKind.valueOf(it) })
} catch (e: Exception) { null }

private fun ExpenseEntry.toDto() = ExpenseEntryDto(id, date.toString(), amount, category.name, note)
private fun ExpenseEntryDto.toDomain() = try {
    ExpenseEntry(id, LocalDate.parse(date), amount, ExpenseCategory.valueOf(category), note)
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
        org.osmdroid.config.Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
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
    var expenses by remember { mutableStateOf(listOf<ExpenseEntry>()) }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var notificationTime by remember { mutableStateOf("20:00") }
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
                expenses = dto.expenses.mapNotNull { it.toDomain() }
                notificationsEnabled = dto.notificationsEnabled
                notificationTime = dto.notificationTime
                try { currentStyle = AppStyle.valueOf(dto.appStyle) } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        isLoaded = true
    }

    // Auto-save on state change (only after initial load)
    LaunchedEffect(assignments, shiftRates, currentStyle, expenses, notificationsEnabled, notificationTime, isLoaded) {
        if (!isLoaded) return@LaunchedEffect
        val dto = AppDataDto(
            assignments = assignments.toDto(),
            customTemplates = templates.filter { !it.isBuiltIn }.map { it.toDto() },
            shiftRates = shiftRates.toStringMap(),
            appStyle = currentStyle.name,
            expenses = expenses.map { it.toDto() },
            notificationsEnabled = notificationsEnabled,
            notificationTime = notificationTime
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
                        label = { Text("Итоги") }
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomTab.BUDGET,
                        onClick = { currentTab = BottomTab.BUDGET },
                        icon = { Icon(Icons.Outlined.Wallet, null) },
                        label = { Text("Бюджет") }
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
                BottomTab.BUDGET -> BudgetScreen(
                    modifier = Modifier.padding(innerPadding),
                    month = selectedMonth,
                    onMonthChanged = { selectedMonth = it },
                    assignments = assignments,
                    shiftRates = shiftRates,
                    expenses = expenses,
                    onExpensesChange = { expenses = it }
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
                    onRatesChange = { shiftRates = it },
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsToggled = { notificationsEnabled = it },
                    notificationTime = notificationTime,
                    onNotificationTimeChanged = { notificationTime = it }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
// Alarm Manager
// ═══════════════════════════════════════════════

fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = "com.workshift.calendar.ALARM_TRIGGER"
    }
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        1001,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
    }

    if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}

fun cancelDailyAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = "com.workshift.calendar.ALARM_TRIGGER"
    }
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        1001,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
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
    val monthHours = remember(monthFiltered) { monthFiltered.values.sumOf { it.calculateTotalHours() } }

    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // Update month when pager changes page (swipe)
    LaunchedEffect(pagerState.currentPage) {
        val pageOffset = pagerState.currentPage - initialPage
        val targetMonth = YearMonth.now().plusMonths(pageOffset.toLong())
        if (targetMonth != month) {
            onMonthChanged(targetMonth)
        }
    }

    // Sync pager when month changes externally (buttons)
    LaunchedEffect(month) {
        val monthsDiff = java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.now(), month).toInt()
        val targetPage = initialPage + monthsDiff
        if (targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
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
                            if (monthHours > 0) StatChip("${monthHours.formatHours()} ч")
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

        // Calendar grid (Pager for native smooth swipes)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            key = { page -> page }
        ) { page ->
            val pageMonth = YearMonth.now().plusMonths((page - initialPage).toLong())
            val targetDays = remember(pageMonth) { buildMonthGrid(pageMonth) }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                items(targetDays) { cell ->
                    val shiftDetails = if (cell.isCurrentMonth) assignments[cell.date] else null
                    DayCard(
                        cell = cell,
                        shiftDetails = shiftDetails,
                        onClick = {
                            if (cell.isCurrentMonth) {
                                if (shiftDetails != null) showDetailForDate = cell.date
                                else showShiftDialogForDate = cell.date
                            }
                        },
                        onLongClick = {
                            if (cell.isCurrentMonth) showShiftDialogForDate = cell.date
                        }
                    )
                }
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
    val hasShift = cell.isCurrentMonth && shiftDetails != null
    val isWorking = hasShift && shiftDetails!!.kind != ShiftKind.OFF

    // Background: tinted fill when shift is assigned — much more visible than border-only
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = when {
        !cell.isCurrentMonth -> Color.Transparent
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        isWorking -> if (isDark) shiftDetails!!.kind.color.copy(alpha = 0.35f) else shiftDetails!!.kind.lightColor.copy(alpha = 0.85f)
        hasShift -> if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE) // OFF day — subtle gray
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isToday)
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                else Modifier
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.TopCenter
    ) {
        // Left accent strip for working shifts (strong color indicator)
        if (isWorking) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(shiftDetails!!.kind.color, shiftDetails.kind.color.copy(alpha = 0.5f))
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Date number — always dark/contrasting regardless of background
            Text(
                text = cell.date.dayOfMonth.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = when {
                    !cell.isCurrentMonth -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    isToday -> MaterialTheme.colorScheme.primary
                    isWeekend -> MaterialTheme.colorScheme.error  // adapts to light/dark theme
                    else -> MaterialTheme.colorScheme.onSurface   // adapts to light/dark theme
                }
            )
            // Shift badge circle
            if (hasShift) {
                Box(
                    modifier = Modifier.size(20.dp).clip(CircleShape).background(shiftDetails!!.kind.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = shiftDetails.kind.shortName, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                if (shiftDetails.startTime.isNotBlank()) {
                    Text(
                        text = shiftDetails.startTime,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Note/location dot
            if (shiftDetails?.note?.isNotBlank() == true || shiftDetails?.location?.isNotBlank() == true) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(shiftDetails!!.kind.color))
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
    val totalHours = workShifts.sumOf { it.calculateTotalHours() }
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

    var calculateTax by remember { mutableStateOf(false) }
    val displayEarnings = if (calculateTax) (totalEarnings * 0.87).toInt() else totalEarnings

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
            SummaryCard("Часов", totalHours.formatHours(), MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
            SummaryCard(if (calculateTax) "На руки" else "Начислено", if (displayEarnings > 0) "$displayEarnings ₽" else "—", MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
        }

        // Tax toggle
        if (totalEarnings > 0) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Text("Вычесть НДФЛ (13%)", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Switch(checked = calculateTax, onCheckedChange = { calculateTax = it })
            }
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

        // All locations map button
        val coordsRegex = Regex("Lat:\\s*(-?\\d+\\.\\d+),\\s*Lng:\\s*(-?\\d+\\.\\d+)")
        val uniqueLocations = assignments.values.map { it.location }.filter { it.isNotBlank() && coordsRegex.containsMatchIn(it) }.distinct()
        if (uniqueLocations.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Place, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Мои объекты на карте", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text("Сохранено уникальных мест работы с геометками: ${uniqueLocations.size}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = {
                            val coords = uniqueLocations.mapNotNull { loc ->
                                val m = coordsRegex.find(loc)
                                if (m != null) "${m.groupValues[1]},${m.groupValues[2]}" else null
                            }
                            if (coords.isNotEmpty()) {
                                val url = "https://www.google.com/maps/dir/" + coords.joinToString("/")
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))) } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Открыть на Google Картах")
                    }
                }
            }
        }

        // Export PDF
        if (filtered.isNotEmpty()) {
            Button(
                onClick = { exportToPdf(context, month, filtered, shiftRates) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Скачать PDF (Табель за месяц)")
            }
        }

        // Shift breakdown
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("По типам смен", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ShiftKind.values().filter { it != ShiftKind.OFF }.forEach { kind ->
                    val count = workShifts.count { it.kind == kind }
                    val hours = workShifts.filter { it.kind == kind }.sumOf { it.calculateTotalHours() }
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
                            if (hours > 0) Text("${hours.formatHours()} ч", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onRatesChange: (Map<ShiftKind, String>) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit,
    notificationTime: String,
    onNotificationTimeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showTimePicker by remember { mutableStateOf(false) }

    // Android 13+ Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNotificationsToggled(true)
            scheduleDailyAlarm(context, notificationTime.split(":")[0].toInt(), notificationTime.split(":")[1].toInt())
        }
    }

    // Exact Alarm permission launcher for Android 12+
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
            scheduleDailyAlarm(context, notificationTime.split(":")[0].toInt(), notificationTime.split(":")[1].toInt())
        }
    }
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

        // Reminders & Notifications
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Напоминания (Уведомления)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Напоминать о завтрашней смене")
                        if (notificationsEnabled) {
                            Text("Время: $notificationTime (нажмите для смены)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { showTimePicker = true }.padding(vertical = 4.dp))
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                // Request permissions before enabling
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                    if (status != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Switch
                                    }
                                }

                                val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                                    exactAlarmLauncher.launch(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                }

                                onNotificationsToggled(true)
                                val parts = notificationTime.split(":")
                                scheduleDailyAlarm(context, parts[0].toInt(), parts[1].toInt())
                            } else {
                                onNotificationsToggled(false)
                                cancelDailyAlarm(context)
                            }
                        }
                    )
                }
            }
        }

        if (showTimePicker) {
            val parts = notificationTime.split(":")
            val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 20
            val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            TimePickerDialog(
                initialHour = initialHour,
                initialMinute = initialMinute,
                onTimeSelected = { h, m ->
                    val newTime = "%02d:%02d".format(h, m)
                    onNotificationTimeChanged(newTime)
                    showTimePicker = false
                    if (notificationsEnabled) scheduleDailyAlarm(context, h, m)
                },
                onDismiss = { showTimePicker = false }
            )
        }

        // Update Center
        UpdateCenterCard()
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
    var currentCustomHours by remember { mutableStateOf(currentDetails?.customHours ?: "") }
    var currentStartTime by remember { mutableStateOf(currentDetails?.startTime ?: "") }
    var currentEndTime by remember { mutableStateOf(currentDetails?.endTime ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var pickingStartTime by remember { mutableStateOf(false) }
    var pickingEndTime by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                    val defaultHours = selectedKind?.hoursPerShift ?: 0
                    OutlinedTextField(
                        value = currentCustomSalary,
                        onValueChange = { currentCustomSalary = it },
                        label = { Text(if (defaultRate.isNotBlank()) "Оплата (ставка: $defaultRate ₽)" else "Оплата за этот день (₽)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        val startInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        LaunchedEffect(startInteractionSource) {
                            startInteractionSource.interactions.collect {
                                if (it is androidx.compose.foundation.interaction.PressInteraction.Release) pickingStartTime = true
                            }
                        }
                        OutlinedTextField(
                            value = currentStartTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Начало") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            interactionSource = startInteractionSource
                        )

                        val endInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        LaunchedEffect(endInteractionSource) {
                            endInteractionSource.interactions.collect {
                                if (it is androidx.compose.foundation.interaction.PressInteraction.Release) pickingEndTime = true
                            }
                        }
                        OutlinedTextField(
                            value = currentEndTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Окончание") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            interactionSource = endInteractionSource
                        )
                    }
                    OutlinedTextField(
                        value = currentCustomHours,
                        onValueChange = { currentCustomHours = it },
                        label = { Text("Часов отработано (по умолчанию: $defaultHours ч)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = currentLocation,
                        onValueChange = { currentLocation = it },
                        label = { Text("Локация / Объект") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showMapPicker = true }) {
                                    Icon(Icons.Outlined.Map, contentDescription = "Map Picker", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = fetchLocation) {
                                    Icon(Icons.Outlined.Place, contentDescription = "GPS", tint = MaterialTheme.colorScheme.primary)
                                }
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
                    onClick = {
                        selectedKind = null
                        currentNote = ""
                        currentLocation = ""
                        currentCustomSalary = ""
                        currentCustomHours = ""
                        currentStartTime = ""
                        currentEndTime = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Очистить день") }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Отмена") }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    enabled = !isSaving,
                    onClick = {
                        if (selectedKind == null) {
                            onDetailsSaved(null)
                            return@TextButton
                        }
                        val loc = currentLocation.trim()
                        if (loc.isNotBlank() && !loc.contains("Lat:")) {
                            isSaving = true
                            scope.launch(Dispatchers.IO) {
                                val geocoder = android.location.Geocoder(context, Locale.getDefault())
                                val addresses = try {
                                    @Suppress("DEPRECATION")
                                    geocoder.getFromLocationName(loc, 1)
                                } catch (_: Exception) { null }

                                val finalLoc = if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    "$loc (Lat: ${String.format(Locale.US, "%.4f", addr.latitude)}, Lng: ${String.format(Locale.US, "%.4f", addr.longitude)})"
                                } else loc

                                withContext(Dispatchers.Main) {
                                    onDetailsSaved(ShiftDetails(selectedKind!!, currentNote, finalLoc, currentCustomSalary, currentCustomHours, currentStartTime, currentEndTime))
                                    isSaving = false
                                }
                            }
                        } else {
                            onDetailsSaved(ShiftDetails(selectedKind!!, currentNote, loc, currentCustomSalary, currentCustomHours, currentStartTime, currentEndTime))
                        }
                    }
                ) { Text(if (isSaving) "Поиск…" else "Сохранить") }
            }
        }
    )

    if (showMapPicker) {
        Dialog(onDismissRequest = { showMapPicker = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            val coordsRegex = Regex("Lat:\\s*(-?\\d+\\.\\d+),\\s*Lng:\\s*(-?\\d+\\.\\d+)")
            val match = coordsRegex.find(currentLocation)
            val initialLat = match?.groupValues?.get(1)?.toDoubleOrNull()
            val initialLng = match?.groupValues?.get(2)?.toDoubleOrNull()
            
            MapPickerScreen(
                initialLat = initialLat,
                initialLng = initialLng,
                onLocationPicked = { lat, lng ->
                    currentLocation = "Lat: ${String.format(Locale.US, "%.4f", lat)}, Lng: ${String.format(Locale.US, "%.4f", lng)}"
                    showMapPicker = false
                },
                onCancel = { showMapPicker = false }
            )
        }
    }

    if (pickingStartTime) {
        val parts = currentStartTime.split(":")
        TimePickerDialog(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            onTimeSelected = { h, m ->
                currentStartTime = "%02d:%02d".format(h, m)
                pickingStartTime = false
            },
            onDismiss = { pickingStartTime = false }
        )
    }

    if (pickingEndTime) {
        val parts = currentEndTime.split(":")
        TimePickerDialog(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 17,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            onTimeSelected = { h, m ->
                currentEndTime = "%02d:%02d".format(h, m)
                pickingEndTime = false
            },
            onDismiss = { pickingEndTime = false }
        )
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    details: ShiftDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    val coordsRegex = Regex("Lat:\\s*(-?\\d+\\.\\d+),\\s*Lng:\\s*(-?\\d+\\.\\d+)")
    val coordsMatch = coordsRegex.find(details.location)
    val scope = rememberCoroutineScope()

    // ── Weather state ──────────────────────────────────────────────────
    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    var weatherLoading by remember { mutableStateOf(false) }

    LaunchedEffect(date, details.location) {
        if (coordsMatch != null && details.kind != ShiftKind.OFF) {
            val today = LocalDate.now()
            val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, date)
            if (daysDiff in -7..16) {   // show for recent past + 16-day forecast
                weatherLoading = true
                weatherInfo = fetchWeatherForDate(
                    lat = coordsMatch.groupValues[1].toDouble(),
                    lon = coordsMatch.groupValues[2].toDouble(),
                    date = date
                )
                weatherLoading = false
            }
        }
    }

    // ── Live earnings counter (only for today's shift) ─────────────────
    var elapsedSeconds by remember { mutableStateOf(0L) }
    val isToday = date == LocalDate.now()

    LaunchedEffect(isToday) {
        if (isToday && details.kind != ShiftKind.OFF) {
            while (true) {
                val now = java.time.LocalTime.now()
                elapsedSeconds = now.toSecondOfDay().toLong()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(details.kind.color))
                Text(date.format(fmt), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Shift type chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(details.kind.color, details.kind.color.copy(alpha = 0.75f))))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(details.kind.emoji + " " + details.kind.displayName,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }

                // ── Live counter for today ──────────────────────────────────────
                if (isToday && details.kind != ShiftKind.OFF && details.customSalary.isNotBlank()) {
                    val ratePh = details.customSalary.toDoubleOrNull()
                    val hours = details.customHours.toDoubleOrNull() ?: details.kind.hoursPerShift.toDouble()
                    if (ratePh != null && hours > 0) {
                        val ratePerSec = ratePh / (hours * 3600)
                        val shiftStartSec = 0L  // approximate — full shift rate
                        val earned = (ratePerSec * elapsedSeconds).coerceAtMost(ratePh)
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⏳ Сегодня заработано", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%.0f ₽".format(earned),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Hours & Times
                val timeRangeText = if (details.startTime.isNotBlank() && details.endTime.isNotBlank()) "⏰ ${details.startTime} - ${details.endTime}" else null
                timeRangeText?.let { Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }

                val hoursText = details.customHours.ifBlank { null }
                    ?.let { "⏱ $it ч (факт)" }
                    ?: if (details.kind != ShiftKind.OFF) "⏱ ${details.kind.hoursPerShift} ч (план)" else null
                hoursText?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                // Salary
                if (details.customSalary.isNotBlank()) {
                    Text("💰 ${details.customSalary} ₽", style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // Location + open in Maps
                if (details.location.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Outlined.Place, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(details.location, style = MaterialTheme.typography.bodySmall)
                        }
                        if (coordsMatch != null) {
                            TextButton(onClick = {
                                val lat = coordsMatch.groupValues[1]
                                val lng = coordsMatch.groupValues[2]
                                val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, geoUri)) } catch (_: Exception) {}
                            }) { Text("Карта", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }

                // ── Weather forecast ────────────────────────────────────────────
                if (coordsMatch != null && details.kind != ShiftKind.OFF) {
                    when {
                        weatherLoading -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Text("Загружаем погоду…", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        weatherInfo != null -> ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Погода на смену", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(weatherInfo!!.emoji + " " + weatherInfo!!.description,
                                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${weatherInfo!!.tempMin}°…${weatherInfo!!.tempMax}°C",
                                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        if (weatherInfo!!.precipProb > 10) {
                                            Text("💧 Осадки ${weatherInfo!!.precipProb}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF1565C0))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Note
                if (details.note.isNotBlank()) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "📝 ${details.note}", modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall)
                    }
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

// ═══════════════════════════════════════════════
// Budget Screen
// ═══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetScreen(
    modifier: Modifier,
    month: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    assignments: Map<LocalDate, ShiftDetails>,
    shiftRates: Map<ShiftKind, String>,
    expenses: List<ExpenseEntry>,
    onExpensesChange: (List<ExpenseEntry>) -> Unit
) {
    val locale = Locale.getDefault()
    var showAddDialog by remember { mutableStateOf(false) }

    val monthExpenses = remember(month, expenses) {
        expenses.filter { YearMonth.from(it.date) == month }
            .sortedByDescending { it.id.toLongOrNull() ?: 0L }
    }
    val totalExpenses = monthExpenses.sumOf { it.amount }

    val monthIncome = remember(month, assignments, shiftRates) {
        assignments.filter { (d, _) -> YearMonth.from(d) == month }
            .values.sumOf { it.customSalary.toIntOrNull() ?: shiftRates[it.kind]?.toIntOrNull() ?: 0 }
    }

    val balance = monthIncome - totalExpenses
    val balancePositive = balance >= 0

    Column(modifier = modifier.fillMaxSize()) {
        // Header with month selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(month.minusMonths(1)) }) {
                    Text("‹", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Бюджет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${month.month.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${month.year}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { onMonthChanged(month.plusMonths(1)) }) {
                    Text("›", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BudgetCard("Доходы", monthIncome, Color(0xFF2E7D32), Modifier.weight(1f))
                    BudgetCard("Расходы", totalExpenses, Color(0xFFC62828), Modifier.weight(1f))
                    BudgetCard(
                        "Баланс", balance,
                        if (balancePositive) Color(0xFF1565C0) else Color(0xFFB71C1C),
                        Modifier.weight(1f),
                        prefix = if (balancePositive) "+" else ""
                    )
                }
            }

            // Balance progress bar
            if (monthIncome > 0) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Потрачено от дохода", style = MaterialTheme.typography.bodySmall)
                                val pct = (totalExpenses * 100 / monthIncome.coerceAtLeast(1)).coerceIn(0, 100)
                                Text("$pct%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                                    color = if (pct > 80) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                val spentFraction = (totalExpenses.toFloat() / monthIncome.coerceAtLeast(1)).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier.fillMaxHeight()
                                        .fillMaxWidth(spentFraction)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (spentFraction > 0.8f) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.primary
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Category breakdown
            if (monthExpenses.isNotEmpty()) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("По категориям", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            ExpenseCategory.values().forEach { cat ->
                                val catTotal = monthExpenses.filter { it.category == cat }.sumOf { it.amount }
                                if (catTotal > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(cat.emoji, fontSize = 16.sp)
                                            Text(cat.displayName, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text("$catTotal ₽", style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold, color = cat.color)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expense list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "История расходов (${monthExpenses.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expense entries
            if (monthExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💸", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Нет расходов за этот месяц", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(monthExpenses) { expense ->
                    val fmt = DateTimeFormatter.ofPattern("d MMM", locale)
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(expense.category.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Text(expense.category.emoji, fontSize = 18.sp) }
                                Column {
                                    Text(expense.category.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    if (expense.note.isNotBlank())
                                        Text(expense.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(expense.date.format(fmt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${expense.amount} ₽", style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = expense.category.color)
                                IconButton(onClick = {
                                    onExpensesChange(expenses.filter { it.id != expense.id })
                                }) {
                                    Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB-like add button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Outlined.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Добавить расход")
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            month = month,
            onAdd = { entry ->
                onExpensesChange((expenses + entry).sortedByDescending { it.id.toLongOrNull() ?: 0L })
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun BudgetCard(label: String, amount: Int, color: Color, modifier: Modifier, prefix: String = "") {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$prefix$amount ₽",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    month: YearMonth,
    onAdd: (ExpenseEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dayOfMonth by remember { mutableStateOf(LocalDate.now().let {
        if (YearMonth.from(it) == month) it.dayOfMonth.toString() else "1"
    }) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить расход", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Category selector
                Text("Категория:", style = MaterialTheme.typography.labelMedium)
                LazyColumn(modifier = Modifier.height(160.dp)) {
                    items(ExpenseCategory.values()) { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { selectedCategory = cat }
                                .background(if (selectedCategory == cat) cat.color.copy(alpha = 0.12f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(cat.emoji, fontSize = 18.sp)
                            Text(cat.displayName, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal)
                            if (selectedCategory == cat) {
                                Spacer(Modifier.weight(1f))
                                Box(Modifier.size(8.dp).clip(CircleShape).background(cat.color))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Сумма (₽)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dayOfMonth,
                    onValueChange = { dayOfMonth = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("День месяца (1–${month.lengthOfMonth()})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Комментарий (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Отмена") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    val amt = amount.toIntOrNull() ?: return@TextButton
                    val day = dayOfMonth.toIntOrNull()?.coerceIn(1, month.lengthOfMonth()) ?: 1
                    val date = month.atDay(day)
                    onAdd(ExpenseEntry(
                        id = System.currentTimeMillis().toString(),
                        date = date,
                        amount = amt,
                        category = selectedCategory,
                        note = note.trim()
                    ))
                }) { Text("Добавить") }
            }
        }
    )
}

// ═══════════════════════════════════════════════
// Update Center
// ═══════════════════════════════════════════════

private data class GitHubRelease(
    val tag_name: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList()
)
private data class GitHubAsset(
    val name: String = "",
    val browser_download_url: String = "",
    val size: Long = 0
)

private suspend fun fetchLatestRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/Lucky0wl/workshift-calendar/releases/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
        conn.setRequestProperty("User-Agent", "Workshift-Calendar-Updater/${BuildConfig.VERSION_NAME}")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        if (conn.responseCode == 200) {
            val json = conn.inputStream.bufferedReader().readText()
            Gson().fromJson(json, GitHubRelease::class.java)
        } else null
    } catch (_: Exception) { null }
}

/** Follow GitHub redirect chain to get direct CDN download URL (avoids DownloadManager redirect issues) */
private suspend fun resolveDirectUrl(originalUrl: String): String = withContext(Dispatchers.IO) {
    var current = originalUrl
    repeat(5) { // max 5 hops
        try {
            val conn = URL(current).openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = false
            conn.setRequestProperty("User-Agent", "Workshift-Calendar-Updater/${BuildConfig.VERSION_NAME}")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            conn.disconnect()
            if (code in 300..399) {
                val loc = conn.getHeaderField("Location") ?: return@withContext current
                current = if (loc.startsWith("http")) loc else "https://github.com$loc"
            } else {
                return@withContext current
            }
        } catch (_: Exception) { return@withContext current }
    }
    current
}

private fun parseVersionCode(body: String): Int? =
    Regex("version_code=(\\d+)").find(body)?.groupValues?.get(1)?.toIntOrNull()

private fun parseVersionName(body: String): String? =
    Regex("version_name=([\\d.]+)").find(body)?.groupValues?.get(1)

enum class UpdateState { IDLE, CHECKING, UP_TO_DATE, UPDATE_AVAILABLE, DOWNLOADING, DOWNLOADED, ERROR }

@Composable
fun UpdateCenterCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var updateState by remember { mutableStateOf(UpdateState.IDLE) }
    var latestRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var downloadId by remember { mutableStateOf<Long?>(null) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var downloadProgress by remember { mutableStateOf(0) }

    // Poll download completion
    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        val dm = context.getSystemService(android.app.DownloadManager::class.java)
        withContext(Dispatchers.IO) {
            while (true) {
                val cursor = dm.query(android.app.DownloadManager.Query().setFilterById(id))
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(android.app.DownloadManager.COLUMN_STATUS))
                    val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    cursor.close()
                    when (status) {
                        android.app.DownloadManager.STATUS_SUCCESSFUL -> {
                            withContext(Dispatchers.Main) { updateState = UpdateState.DOWNLOADED }
                            return@withContext
                        }
                        android.app.DownloadManager.STATUS_FAILED -> {
                            withContext(Dispatchers.Main) { updateState = UpdateState.ERROR }
                            return@withContext
                        }
                        android.app.DownloadManager.STATUS_RUNNING -> {
                            if (total > 0) {
                                val pct = (downloaded * 100 / total).toInt()
                                withContext(Dispatchers.Main) { downloadProgress = pct }
                            }
                        }
                        else -> { /* PENDING or PAUSED — keep waiting */ }
                    }
                } else {
                    cursor.close()
                }
                kotlinx.coroutines.delay(800)
            }
        }
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄 Центр обновлений", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Status row
            val (statusText, statusColor) = when (updateState) {
                UpdateState.IDLE -> "Нажмите «Проверить» для поиска обновлений" to MaterialTheme.colorScheme.onSurfaceVariant
                UpdateState.CHECKING -> "⏳ Проверяем…" to MaterialTheme.colorScheme.primary
                UpdateState.UP_TO_DATE -> "✅ У вас последняя версия (${BuildConfig.VERSION_NAME})" to Color(0xFF2E7D32)
                UpdateState.UPDATE_AVAILABLE -> {
                    val vn = latestRelease?.let { parseVersionName(it.body) } ?: latestRelease?.name ?: ""
                    "🆕 Доступна версия $vn" to MaterialTheme.colorScheme.primary
                }
                UpdateState.DOWNLOADING -> "⬇️ Загружаем APK… $downloadProgress%" to MaterialTheme.colorScheme.secondary
                UpdateState.DOWNLOADED -> "✅ Загружено! Готово к установке" to Color(0xFF2E7D32)
                UpdateState.ERROR -> "❌ Ошибка. Проверьте соединение" to MaterialTheme.colorScheme.error
            }
            Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Check for updates button
                if (updateState != UpdateState.DOWNLOADING) {
                    OutlinedButton(
                        onClick = {
                            updateState = UpdateState.CHECKING
                            scope.launch {
                                val release = fetchLatestRelease()
                                if (release == null) {
                                    updateState = UpdateState.ERROR
                                    return@launch
                                }
                                latestRelease = release
                                val remoteCode = parseVersionCode(release.body)
                                updateState = if (remoteCode != null && remoteCode > BuildConfig.VERSION_CODE) {
                                    UpdateState.UPDATE_AVAILABLE
                                } else {
                                    UpdateState.UP_TO_DATE
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = updateState != UpdateState.CHECKING
                    ) {
                        Text(if (updateState == UpdateState.CHECKING) "Проверяем…" else "Проверить")
                    }
                }

                // Download button
                if (updateState == UpdateState.UPDATE_AVAILABLE) {
                    Button(
                        onClick = {
                            val rawUrl = latestRelease?.assets?.firstOrNull()?.browser_download_url
                                ?: return@Button
                            downloadProgress = 0
                            updateState = UpdateState.DOWNLOADING
                            // Delete old APK if exists
                            context.getExternalFilesDir("updates")?.also { dir ->
                                File(dir, "app-update.apk").delete()
                            }
                            scope.launch {
                                // Resolve GitHub redirect to direct CDN URL
                                val directUrl = resolveDirectUrl(rawUrl)
                                val dm = context.getSystemService(android.app.DownloadManager::class.java)
                                val request = android.app.DownloadManager.Request(Uri.parse(directUrl))
                                    .setTitle("Workshift — обновление")
                                    .setDescription("Загрузка новой версии...")
                                    .addRequestHeader("User-Agent", "Workshift-Calendar-Updater/${BuildConfig.VERSION_NAME}")
                                    .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setDestinationInExternalFilesDir(context, "updates", "app-update.apk")
                                downloadId = dm.enqueue(request)
                                downloadedApkFile = File(context.getExternalFilesDir("updates"), "app-update.apk")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Загрузить")
                    }
                }

                // Install button
                if (updateState == UpdateState.DOWNLOADED) {
                    Button(
                        onClick = {
                            val file = downloadedApkFile ?: return@Button
                            try {
                                val apkUri = FileProvider.getUriForFile(
                                    context, "${context.packageName}.provider", file
                                )
                                val install = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(install)
                            } catch (_: Exception) {
                                updateState = UpdateState.ERROR
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Установить")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// Weather (Open-Meteo — no API key)
// ═══════════════════════════════════════════════

data class WeatherInfo(
    val emoji: String,
    val description: String,
    val tempMin: Int,
    val tempMax: Int,
    val precipProb: Int
)

private fun wmoToInfo(code: Int): Pair<String, String> = when (code) {
    0 -> "☀️" to "Ясно"
    1 -> "🌤️" to "Преимущественно ясно"
    2 -> "⛅" to "Переменная облачность"
    3 -> "☁️" to "Пасмурно"
    45, 48 -> "🌫️" to "Туман"
    51, 53, 55 -> "🌦️" to "Морось"
    61, 63, 65 -> "🌧️" to "Дождь"
    71, 73, 75 -> "🌨️" to "Снег"
    77 -> "❄️" to "Снежная крупа"
    80, 81, 82 -> "🌧️" to "Ливень"
    85, 86 -> "🌨️" to "Снегопад"
    95 -> "⛈️" to "Гроза"
    96, 99 -> "⛈️" to "Гроза с градом"
    else -> "🌡️" to "Переменно"
}

suspend fun fetchWeatherForDate(lat: Double, lon: Double, date: LocalDate): WeatherInfo? =
    withContext(Dispatchers.IO) {
        try {
            val dateStr = date.toString() // ISO: 2026-03-05
            val urlStr = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lon" +
                    "&hourly=temperature_2m,precipitation_probability,weathercode" +
                    "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode" +
                    "&timezone=auto&start_date=$dateStr&end_date=$dateStr"

            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "Workshift-Calendar-Weather/1.0")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode != 200) return@withContext null

            val json = conn.inputStream.bufferedReader().readText()
            val gson = Gson()
            val root = gson.fromJson(json, Map::class.java)

            @Suppress("UNCHECKED_CAST")
            val daily = root["daily"] as? Map<String, List<*>> ?: return@withContext null
            val tempMax = (daily["temperature_2m_max"]?.firstOrNull() as? Double)?.toInt() ?: return@withContext null
            val tempMin = (daily["temperature_2m_min"]?.firstOrNull() as? Double)?.toInt() ?: return@withContext null
            val precipProb = (daily["precipitation_probability_max"]?.firstOrNull() as? Double)?.toInt() ?: 0
            val wmoCode = (daily["weathercode"]?.firstOrNull() as? Double)?.toInt() ?: 0

            val (emoji, desc) = wmoToInfo(wmoCode)
            WeatherInfo(emoji, desc, tempMin, tempMax, precipProb)
        } catch (_: Exception) { null }
    }

// ═══════════════════════════════════════════════
// Map Picker (OSMDroid)
// ═══════════════════════════════════════════════

@Composable
fun MapPickerScreen(
    initialLat: Double?,
    initialLng: Double?,
    onLocationPicked: (Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    var pickedPoint by remember { mutableStateOf<GeoPoint?>(if (initialLat != null && initialLng != null) GeoPoint(initialLat, initialLng) else null) }

    Column(Modifier.fillMaxSize().background(Color.White)) {
        // top bar
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) { Icon(Icons.Outlined.ArrowBack, "Cancel") }
            Text("Выберите точку", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { pickedPoint?.let { onLocationPicked(it.latitude, it.longitude) } }, enabled = pickedPoint != null) {
                Text("Готово")
            }
        }

        // map
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)

                    val overlayReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let {
                                pickedPoint = it
                                overlays.removeAll { overlay -> overlay is Marker }
                                val marker = Marker(this@apply)
                                marker.position = it
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                overlays.add(marker)
                                invalidate()
                            }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(overlayReceiver))

                    if (initialLat != null && initialLng != null) {
                        val pt = GeoPoint(initialLat, initialLng)
                        controller.setCenter(pt)
                        val marker = Marker(this)
                        marker.position = pt
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        overlays.add(marker)
                    } else {
                        // Default center (Moscow for example)
                        controller.setCenter(GeoPoint(55.7558, 37.6173))
                    }
                }
            },
            update = { view ->
                // Map automatically handles updates through its own logic in this simple use-case
            }
        )
    }
}

// ═══════════════════════════════════════════════
// PDF Export
// ═══════════════════════════════════════════════

private fun exportToPdf(context: Context, month: YearMonth, filtered: Map<LocalDate, ShiftDetails>, shiftRates: Map<ShiftKind, String>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 14f
    }
    val titlePaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 18f
    }

    var y = 50f
    canvas.drawText("Отчет по сменам: ${month.month.name} ${month.year}", 50f, y, titlePaint)
    y += 40f

    var totalEarnings = 0
    var totalHours = 0.0
    val sortedShifts = filtered.entries.sortedBy { it.key.dayOfMonth }

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Дата", 50f, y, paint)
    canvas.drawText("Смена", 150f, y, paint)
    canvas.drawText("Часы", 300f, y, paint)
    canvas.drawText("Заработано", 400f, y, paint)
    y += 20f

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    for ((date, details) in sortedShifts) {
        if (details.kind == ShiftKind.OFF) continue
        val hours = details.calculateTotalHours()
        val earnings = details.customSalary.toIntOrNull() ?: shiftRates[details.kind]?.toIntOrNull() ?: 0
        
        canvas.drawText(date.toString(), 50f, y, paint)
        canvas.drawText(details.kind.displayName, 150f, y, paint)
        canvas.drawText("${hours.formatHours()} ч", 300f, y, paint)
        canvas.drawText("$earnings ₽", 400f, y, paint)
        
        totalHours += hours
        totalEarnings += earnings
        y += 20f
        
        if (y > 800f) break
    }

    y += 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("ИТОГО:", 150f, y, paint)
    canvas.drawText("${totalHours.formatHours()} ч", 300f, y, paint)
    canvas.drawText("$totalEarnings ₽", 400f, y, paint)

    pdfDocument.finishPage(page)

    try {
        val file = File(context.cacheDir, "Workshift_Report_$month.pdf")
        val out = FileOutputStream(file)
        pdfDocument.writeTo(out)
        pdfDocument.close()
        out.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Открыть PDF"))
    } catch (_: Exception) {
        pdfDocument.close()
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val dialog = android.app.TimePickerDialog(
            context,
            { _, h, m -> onTimeSelected(h, m) },
            initialHour,
            initialMinute,
            true
        )
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
    }
}
