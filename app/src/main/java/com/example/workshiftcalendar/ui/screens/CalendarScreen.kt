package com.example.workshiftcalendar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshiftcalendar.domain.model.ShiftDetails
import com.example.workshiftcalendar.domain.model.ShiftKind
import com.example.workshiftcalendar.ui.components.ShiftCalendar
import com.example.workshiftcalendar.ui.viewmodel.WorkshiftViewModel
import java.time.LocalDate
import java.time.YearMonth

/**
 * Экран календаря
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: WorkshiftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showCopyDialog by remember { mutableStateOf(false) }

    val selectedDateShift = selectedDate?.let { uiState.assignments[it] }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Календарь смен") },
                actions = {
                    IconButton(onClick = { showCopyDialog = true }) {
                        Icon(Icons.Outlined.CopyAll, contentDescription = "Копировать месяц")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { selectedDate = LocalDate.now() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить смену")
            }
        }
    ) { paddingValues ->
        ShiftCalendar(
            month = selectedMonth,
            assignments = uiState.assignments,
            vacations = uiState.vacations,
            onDateClick = { selectedDate = it },
            onMonthChanged = { selectedMonth = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    // Диалог редактирования смены
    selectedDate?.let { date ->
        com.example.workshiftcalendar.ui.components.ShiftEditDialog(
            date = date,
            shift = selectedDateShift,
            shiftRates = uiState.shiftRates,
            customShiftColors = uiState.customShiftColors,
            onDismiss = { selectedDate = null },
            onSave = { shift ->
                viewModel.saveShift(date, shift)
                selectedDate = null
            },
            onClear = {
                viewModel.deleteShift(date)
                selectedDate = null
            }
        )
    }

    // Диалог копирования смен
    if (showCopyDialog) {
        CopyMonthDialog(
            sourceMonth = selectedMonth,
            onDismiss = { showCopyDialog = false },
            onCopy = { targetMonth ->
                viewModel.copyAssignmentsFromMonth(selectedMonth, targetMonth)
                showCopyDialog = false
            }
        )
    }
}

/**
 * Диалог копирования смен на другой месяц
 */
@Composable
fun CopyMonthDialog(
    sourceMonth: YearMonth,
    onDismiss: () -> Unit,
    onCopy: (YearMonth) -> Unit
) {
    var targetYear by remember { mutableIntStateOf(sourceMonth.year) }
    var targetMonth by remember { mutableIntStateOf(sourceMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Копировать смены") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Копировать смены из ${sourceMonth.month} в:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetYear.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { year -> 
                                if (year in 2000..2100) targetYear = year 
                            }
                        },
                        label = { Text("Год") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetMonth.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { month -> 
                                if (month in 1..12) targetMonth = month 
                            }
                        },
                        label = { Text("Месяц") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCopy(YearMonth.of(targetYear, targetMonth)) },
                enabled = YearMonth.of(targetYear, targetMonth) != sourceMonth
            ) {
                Text("Копировать")
            }
        }
    )
}
