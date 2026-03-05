package com.example.workshiftcalendar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.workshiftcalendar.domain.model.ExpenseCategory
import com.example.workshiftcalendar.domain.model.ExpenseEntry
import com.example.workshiftcalendar.domain.model.ShiftKind
import com.example.workshiftcalendar.ui.viewmodel.WorkshiftViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

/**
 * Экран бюджета
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: WorkshiftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddExpense by remember { mutableStateOf(false) }

    val monthExpenses = remember(uiState.expenses, selectedMonth) {
        uiState.expenses.filter { YearMonth.from(it.date) == selectedMonth }
    }

    val monthShifts = remember(uiState.assignments, selectedMonth) {
        uiState.assignments.filter { YearMonth.from(it.key) == selectedMonth }
    }

    val budget = calculateBudget(monthShifts, monthExpenses, uiState.shiftRates)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Бюджет") },
            actions = {
                IconButton(onClick = { showAddExpense = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить расход")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Selector месяца
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                    Text("‹", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Text(
                    text = selectedMonth.month.getDisplayName(TextStyle.GENITIVE, Locale("ru"))
                        .replaceFirstChar { it.uppercase() } + " ${selectedMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                    Text("›", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

            // Карточки бюджета
            BudgetCards(budget)

            Spacer(modifier = Modifier.height(24.dp))

            // Расходы по категориям
            Text(
                text = "Расходы по категориям",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val expensesByCategory = monthExpenses.groupBy { it.category }
            expensesByCategory.forEach { (category, expenses) ->
                ExpenseCategoryRow(
                    category = category,
                    total = expenses.sumOf { it.amount },
                    count = expenses.size
                )
            }

            if (expensesByCategory.isEmpty()) {
                Text(
                    text = "Нет расходов за этот месяц",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Список расходов
            Text(
                text = "Все расходы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (monthExpenses.isEmpty()) {
                Text(
                    text = "Нет расходов за этот месяц",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                monthExpenses.sortedByDescending { it.date }.forEach { expense ->
                    ExpenseItem(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(expense.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAddExpense) {
        AddExpenseDialog(
            onDismiss = { showAddExpense = false },
            onAdd = { amount, category, note, date ->
                viewModel.saveExpense(
                    ExpenseEntry(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        amount = amount,
                        category = category,
                        note = note
                    )
                )
                showAddExpense = false
            }
        )
    }
}

/**
 * Данные бюджета
 */
data class BudgetData(
    val totalEarnings: Double,
    val totalExpenses: Int,
    val balance: Double,
    val averageDaily: Double
)

/**
 * Расчёт бюджета
 */
private fun calculateBudget(
    monthShifts: Map<java.time.LocalDate, ShiftDetails>,
    monthExpenses: List<ExpenseEntry>,
    shiftRates: Map<ShiftKind, String>
): BudgetData {
    val totalEarnings = monthShifts.sumOf { (_, shift) ->
        if (shift.kind == ShiftKind.OFF) return@sumOf 0.0
        shift.customSalary.toDoubleOrNull() ?: (shiftRates[shift.kind]?.toDoubleOrNull() ?: 0.0)
    }

    val totalExpenses = monthExpenses.sumOf { it.amount }
    val balance = totalEarnings - totalExpenses
    val averageDaily = if (monthExpenses.isNotEmpty()) {
        totalExpenses.toDouble() / monthExpenses.size
    } else 0.0

    return BudgetData(totalEarnings, totalExpenses, balance, averageDaily)
}

/**
 * Карточки бюджета
 */
@Composable
fun BudgetCards(budget: BudgetData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BudgetCard("Доход", "${budget.totalEarnings.toInt()} ₽", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        BudgetCard("Расход", "${budget.totalExpenses} ₽", MaterialTheme.colorScheme.error, Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val balanceColor = if (budget.balance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
        BudgetCard("Баланс", "${budget.balance.toInt()} ₽", balanceColor, Modifier.weight(1f))
        BudgetCard("Средний/день", "${budget.averageDaily.toInt()} ₽", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
    }
}

import androidx.compose.ui.graphics.Color

@Composable
fun BudgetCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = color
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
 * Строка категории расходов
 */
@Composable
fun ExpenseCategoryRow(category: ExpenseCategory, total: Int, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(category.displayName)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${total} ₽", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("$count записей", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Элемент расхода
 */
@Composable
fun ExpenseItem(expense: ExpenseEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(expense.category.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("${expense.amount} ₽", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(
                        text = expense.note.ifBlank { expense.category.displayName },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
            }
        }
    }
}

/**
 * Диалог добавления расхода
 */
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (Int, ExpenseCategory, String, LocalDate) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить расход") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) amount = it },
                    label = { Text("Сумма") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExpenseCategorySelector(
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toIntOrNull()?.let { onAdd(it, selectedCategory, note, selectedDate) }
                },
                enabled = amount.toIntOrNull() != null
            ) {
                Text("Добавить")
            }
        }
    )
}

/**
 * Selector категории расходов
 */
@Composable
fun ExpenseCategorySelector(
    selected: ExpenseCategory,
    onSelected: (ExpenseCategory) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(ExpenseCategory.entries) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text("${category.emoji} ${category.displayName}") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
