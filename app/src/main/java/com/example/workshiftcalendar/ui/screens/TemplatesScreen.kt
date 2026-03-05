package com.example.workshiftcalendar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workshiftcalendar.domain.model.ShiftKind
import com.example.workshiftcalendar.domain.model.ShiftTemplate
import com.example.workshiftcalendar.ui.viewmodel.WorkshiftViewModel
import java.time.YearMonth
import java.util.UUID

/**
 * Экран шаблонов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: WorkshiftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTemplate by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Шаблоны") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTemplate = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать шаблон")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.templates) { template ->
                TemplateCard(
                    template = template,
                    onDelete = { if (!template.isBuiltIn) viewModel.deleteTemplate(template.id) },
                    onApply = { /* TODO: Apply template to current month */ }
                )
            }
        }
    }

    if (showCreateTemplate) {
        CreateTemplateDialog(
            onDismiss = { showCreateTemplate = false },
            onCreate = { name, description, pattern ->
                viewModel.saveTemplate(
                    ShiftTemplate(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        pattern = pattern,
                        isBuiltIn = false
                    )
                )
                showCreateTemplate = false
            }
        )
    }
}

/**
 * Карточка шаблона
 */
@Composable
fun TemplateCard(
    template: ShiftTemplate,
    onDelete: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (template.isBuiltIn) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!template.isBuiltIn) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Паттерн смен
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                template.pattern.forEach { kind ->
                    Surface(
                        color = kind.color.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(kind.emoji)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка применения
            Button(
                onClick = onApply,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Применить")
            }
        }
    }
}

/**
 * Диалог создания шаблона
 */
@Composable
fun CreateTemplateDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, List<ShiftKind>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf(listOf<ShiftKind>()) }
    var showPatternEditor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый шаблон") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Паттерн: ${pattern.size} дней",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (pattern.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                pattern.forEach { kind ->
                                    Text(kind.emoji)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showPatternEditor = true }) {
                            Text("Редактировать паттерн")
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, description, pattern) },
                enabled = name.isNotBlank() && pattern.isNotEmpty()
            ) {
                Text("Создать")
            }
        }
    )

    if (showPatternEditor) {
        PatternEditorDialog(
            pattern = pattern,
            onDismiss = { showPatternEditor = false },
            onSave = { pattern = it }
        )
    }
}

/**
 * Диалог редактирования паттерна
 */
@Composable
fun PatternEditorDialog(
    pattern: List<ShiftKind>,
    onDismiss: () -> Unit,
    onSave: (List<ShiftKind>) -> Unit
) {
    var currentPattern by remember { mutableStateOf(pattern) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактор паттерна") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Текущий паттерн
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentPattern.forEachIndexed { index, kind ->
                        IconButton(
                            onClick = { currentPattern = currentPattern - index },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(kind.emoji)
                        }
                    }
                }

                // Кнопки добавления
                Text("Добавить:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShiftKind.entries.filter { it != ShiftKind.OFF }.forEach { kind ->
                        FilterChip(
                            selected = false,
                            onClick = { currentPattern = currentPattern + kind },
                            label = { Text(kind.emoji) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Быстрые шаблоны
                Text("Быстрые шаблоны:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { currentPattern = List(4) { if (it < 2) ShiftKind.MORNING else ShiftKind.OFF } },
                        label = { Text("2/2") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = false,
                        onClick = { currentPattern = List(7) { if (it < 5) ShiftKind.MORNING else ShiftKind.OFF } },
                        label = { Text("5/2") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(onClick = { onSave(currentPattern) }) {
                Text("Готово")
            }
        }
    )
}
