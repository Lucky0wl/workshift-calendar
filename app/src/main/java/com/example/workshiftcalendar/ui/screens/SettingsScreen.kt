package com.example.workshiftcalendar.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.workshiftcalendar.domain.model.ShiftKind
import com.example.workshiftcalendar.domain.model.VacationPeriod
import com.example.workshiftcalendar.ui.theme.AppStyle
import com.example.workshiftcalendar.ui.viewmodel.WorkshiftViewModel
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Экран настроек
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WorkshiftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showStylePicker by remember { mutableStateOf(false) }
    var showShiftRates by remember { mutableStateOf(false) }
    var showVacationDialog by remember { mutableStateOf(false) }
    var showExportOptions by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }

    // Launcher для импорта
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val json = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
            json?.let { jsonData ->
                viewModel.importFromJson(
                    jsonData,
                    onSuccess = { /* Show success */ },
                    onError = { /* Show error */ }
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Настройки") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Секция: Внешний вид
            SettingsSection(title = "Внешний вид") {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    title = "Стиль приложения",
                    subtitle = uiState.appStyle.displayName,
                    onClick = { showStylePicker = true }
                )
            }

            // Секция: Уведомления
            SettingsSection(title = "Уведомления") {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Ежедневные напоминания",
                    subtitle = if (uiState.notificationsEnabled) 
                        "Включены в ${uiState.notificationTime}" 
                    else "Выключены",
                    trailing = {
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateNotificationSettings(
                                    enabled,
                                    uiState.notificationTime
                                )
                            }
                        )
                    },
                    onClick = { showNotificationSettings = true }
                )
            }

            // Секция: Финансы
            SettingsSection(title = "Финансы") {
                SettingsItem(
                    icon = Icons.Outlined.AttachMoney,
                    title = "Ставки за смены",
                    subtitle = ShiftKind.entries.filter { it != ShiftKind.OFF }.joinToString(", ") { kind ->
                        "${kind.shortName}: ${uiState.shiftRates[kind] ?: "0"} ₽"
                    },
                    onClick = { showShiftRates = true }
                )
            }

            // Секция: Отпуск
            SettingsSection(title = "Отпуск") {
                SettingsItem(
                    icon = Icons.Outlined.BeachAccess,
                    title = "Периоды отпуска",
                    subtitle = "${uiState.vacations.size} период(ов)",
                    onClick = { showVacationDialog = true }
                )
            }

            // Секция: Данные
            SettingsSection(title = "Данные") {
                SettingsItem(
                    icon = Icons.Outlined.Upload,
                    title = "Экспорт данных",
                    subtitle = "Сохранить в JSON файл",
                    onClick = { showExportOptions = true }
                )
                SettingsItem(
                    icon = Icons.Outlined.Download,
                    title = "Импорт данных",
                    subtitle = "Загрузить из JSON файла",
                    onClick = { importLauncher.launch("application/json") }
                )
            }

            // Секция: О приложении
            SettingsSection(title = "О приложении") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "Версия",
                    subtitle = "4.0.0"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Диалоги
    if (showStylePicker) {
        StylePickerDialog(
            currentStyle = uiState.appStyle,
            onStyleSelected = { viewModel.updateAppStyle(it) },
            onDismiss = { showStylePicker = false }
        )
    }

    if (showShiftRates) {
        ShiftRatesDialog(
            rates = uiState.shiftRates,
            onDismiss = { showShiftRates = false },
            onSave = { viewModel.updateShiftRates(it) }
        )
    }

    if (showVacationDialog) {
        VacationDialog(
            vacations = uiState.vacations,
            onDismiss = { showVacationDialog = false },
            onAddVacation = { vacation ->
                viewModel.addVacation(vacation)
            },
            onDeleteVacation = { vacation ->
                viewModel.deleteVacation(vacation.id)
            }
        )
    }

    if (showExportOptions) {
        ExportDialog(
            onDismiss = { showExportOptions = false },
            onExport = {
                exportData(context, viewModel)
                showExportOptions = false
            }
        )
    }

    if (showNotificationSettings) {
        NotificationSettingsDialog(
            enabled = uiState.notificationsEnabled,
            time = uiState.notificationTime,
            onDismiss = { showNotificationSettings = false },
            onSave = { enabled, time ->
                viewModel.updateNotificationSettings(enabled, time)
            }
        )
    }
}

/**
 * Секция настроек
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

/**
 * Элемент настройки
 */
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        trailing()
    }
}

/**
 * Диалог выбора стиля
 */
@Composable
fun StylePickerDialog(
    currentStyle: AppStyle,
    onStyleSelected: (AppStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите стиль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppStyle.entries.forEach { style ->
                    FilterChip(
                        selected = style == currentStyle,
                        onClick = { 
                            onStyleSelected(style)
                            onDismiss()
                        },
                        label = { Text(style.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

val AppStyle.displayName: String
    get() = when (this) {
        AppStyle.MODERN_BLUE -> "Современный синий"
        AppStyle.DARK_AMOLED -> "Тёмный AMOLED"
        AppStyle.WARM_PASTEL -> "Тёплый пастельный"
    }

/**
 * Диалог ставок смен
 */
@Composable
fun ShiftRatesDialog(
    rates: Map<ShiftKind, String>,
    onDismiss: () -> Unit,
    onSave: (Map<ShiftKind, String>) -> Unit
) {
    var currentRates by remember { mutableStateOf(rates) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ставки за смены") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShiftKind.entries.filter { it != ShiftKind.OFF }.forEach { kind ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(kind.emoji, modifier = Modifier.width(32.dp))
                        Text(kind.shortName, modifier = Modifier.width(24.dp))
                        OutlinedTextField(
                            value = currentRates[kind] ?: "",
                            onValueChange = { 
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    currentRates = currentRates + (kind to it)
                                }
                            },
                            label = { Text("₽") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(onClick = { onSave(currentRates) }) { Text("Сохранить") }
        }
    )
}

/**
 * Диалог отпусков
 */
@Composable
fun VacationDialog(
    vacations: List<VacationPeriod>,
    onDismiss: () -> Unit,
    onAddVacation: (VacationPeriod) -> Unit,
    onDeleteVacation: (VacationPeriod) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Периоды отпуска") },
        text = {
            if (vacations.isEmpty()) {
                Text("Нет периодов отпуска", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vacations) { vacation ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(vacation.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${vacation.startDate.format(DateTimeFormatter.ofPattern("dd.MM"))} - ${vacation.endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { onDeleteVacation(vacation) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
        confirmButton = {
            TextButton(onClick = { showAddDialog = true }) { Text("Добавить") }
        }
    )

    if (showAddDialog) {
        AddVacationDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, start, end ->
                onAddVacation(
                    VacationPeriod(
                        id = java.util.UUID.randomUUID().toString(),
                        startDate = start,
                        endDate = end,
                        name = name
                    )
                )
                showAddDialog = false
            }
        )
    }
}

/**
 * Диалог добавления отпуска
 */
@Composable
fun AddVacationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, LocalDate, LocalDate) -> Unit
) {
    var name by remember { mutableStateOf("Отпуск") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(14)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить отпуск") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Упрощённый выбор дат
                Text("Период: ${startDate.format(DateTimeFormatter.ofPattern("dd.MM"))} - ${endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, startDate, endDate) },
                enabled = !endDate.isBefore(startDate)
            ) {
                Text("Добавить")
            }
        }
    )
}

/**
 * Диалог экспорта
 */
@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Экспорт данных") },
        text = {
            Text("Данные будут сохранены в JSON файл и предложены для открытия через другие приложения.")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(onClick = onExport) { Text("Экспортировать") }
        }
    )
}

/**
 * Экспорт данных в файл
 */
private fun exportData(context: Context, viewModel: WorkshiftViewModel) {
    try {
        val json = kotlinx.coroutines.runBlocking { viewModel.exportToJson() }
        val file = File(context.cacheDir, "workshift_backup_${System.currentTimeMillis()}.json")
        file.writeText(json)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Сохранить данные"))
    } catch (e: Exception) {
        // Show error
    }
}

/**
 * Диалог настроек уведомлений
 */
@Composable
fun NotificationSettingsDialog(
    enabled: Boolean,
    time: String,
    onDismiss: () -> Unit,
    onSave: (Boolean, String) -> Unit
) {
    var isEnabled by remember { mutableStateOf(enabled) }
    var notificationTime by remember { mutableStateOf(time) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки уведомлений") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Включить уведомления")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
                if (isEnabled) {
                    OutlinedTextField(
                        value = notificationTime,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^([01]?[0-9]|2[0-3]):?[0-5]?[0-9]?$"))) {
                                notificationTime = it 
                            }
                        },
                        label = { Text("Время (ЧЧ:ММ)") },
                        placeholder = { Text("20:00") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(isEnabled, notificationTime.ifBlank { "20:00" }) }
            ) { 
                Text("Сохранить") 
            }
        }
    )
} 