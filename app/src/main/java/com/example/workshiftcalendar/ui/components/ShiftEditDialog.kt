package com.example.workshiftcalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.workshiftcalendar.domain.model.ShiftDetails
import com.example.workshiftcalendar.domain.model.ShiftKind
import java.time.LocalDate

/**
 * Диалог редактирования смены
 */
@Composable
fun ShiftEditDialog(
    date: LocalDate,
    shift: ShiftDetails?,
    shiftRates: Map<ShiftKind, String>,
    customShiftColors: Map<ShiftKind, Long>,
    onDismiss: () -> Unit,
    onSave: (ShiftDetails) -> Unit,
    onClear: () -> Unit
) {
    var selectedKind by remember { mutableStateOf(shift?.kind ?: ShiftKind.OFF) }
    var note by remember { mutableStateOf(shift?.note ?: "") }
    var location by remember { mutableStateOf(shift?.location ?: "") }
    var customSalary by remember { mutableStateOf(shift?.customSalary ?: "") }
    var customHours by remember { mutableStateOf(shift?.customHours ?: "") }
    var startTime by remember { mutableStateOf(shift?.startTime ?: "") }
    var endTime by remember { mutableStateOf(shift?.endTime ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Смена: ${date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Выбор типа смены
                Text("Тип смены:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShiftKind.entries.filter { it != ShiftKind.OFF }.forEach { kind ->
                        FilterChip(
                            selected = selectedKind == kind,
                            onClick = { selectedKind = kind },
                            label = { Text(kind.shortName) },
                            leadingIcon = if (selectedKind == kind) {
                                { Text(kind.emoji, fontSize = 12.sp) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Место работы") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^([0-1]?[0-9]|2[0-3])?[0-9]?:?[0-5]?[0-9]?$"))) {
                                startTime = it 
                            }
                        },
                        label = { Text("Начало") },
                        placeholder = { Text("08:00") },
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^([0-1]?[0-9]|2[0-3])?[0-9]?:?[0-5]?[0-9]?$"))) {
                                endTime = it 
                            }
                        },
                        label = { Text("Конец") },
                        placeholder = { Text("20:00") },
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }

                OutlinedTextField(
                    value = customHours,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d?$"))) {
                            customHours = it 
                        }
                    },
                    label = { Text("Часы (необязательно)") },
                    placeholder = { Text("8.5") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                OutlinedTextField(
                    value = customSalary,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                            customSalary = it 
                        }
                    },
                    label = { Text("Зарплата за смену") },
                    placeholder = { Text(shiftRates[selectedKind] ?: "0") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onClear) {
                        Text("Очистить", color = MaterialTheme.colorScheme.error)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена")
                        }
                        Button(onClick = {
                            onSave(
                                ShiftDetails(
                                    kind = selectedKind,
                                    note = note,
                                    location = location,
                                    customSalary = customSalary,
                                    customHours = customHours,
                                    startTime = startTime,
                                    endTime = endTime,
                                    customColor = customShiftColors[selectedKind]
                                )
                            )
                        }) {
                            Text("Сохранить")
                        }
                    }
                }
            }
        }
    }
}
