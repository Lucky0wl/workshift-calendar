package com.example.workshiftcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.workshiftcalendar.ui.screens.*
import com.example.workshiftcalendar.ui.theme.WorkshiftTheme
import com.example.workshiftcalendar.ui.viewmodel.WorkshiftViewModel

/**
 * Главная активность приложения
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WorkshiftViewModel by viewModels()
            WorkshiftApp(viewModel)
        }
    }
}

/**
 * Навигационные вкладки
 */
enum class BottomTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CALENDAR("Календарь", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    STATS("Итоги", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BUDGET("Бюджет", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    TEMPLATES("Шаблоны", Icons.Filled.List, Icons.Outlined.List),
    SETTINGS("Настройки", Icons.Filled.Settings, Icons.Outlined.Settings)
}

/**
 * Корневой компонент приложения
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshiftApp(viewModel: WorkshiftViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by androidx.compose.runtime.saveable.rememberSaveable { 
        androidx.compose.runtime.mutableStateOf(BottomTab.CALENDAR) 
    }

    WorkshiftTheme(style = uiState.appStyle) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    if (currentTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (currentTab) {
                BottomTab.CALENDAR -> CalendarScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                BottomTab.STATS -> StatsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                BottomTab.BUDGET -> BudgetScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                BottomTab.TEMPLATES -> TemplatesScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                BottomTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Диалог ошибки
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Ошибка") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
