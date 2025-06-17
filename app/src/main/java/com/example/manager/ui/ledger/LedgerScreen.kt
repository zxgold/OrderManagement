package com.example.manager.ui.ledger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.LedgerEntry
import com.example.manager.data.model.enums.LedgerEntryType
import com.example.manager.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    navController: NavController,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    // --- 用于日期选择器的状态 ---
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        // 默认选中当前日期范围的开始日期
        initialSelectedDateMillis = uiState.startDate
    )
    // -----------------------------
    // --- 用于日期范围选择器的状态 ---
    var showDateRangePickerDialog by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState(
        // 设置初始显示的范围
        initialSelectedStartDateMillis = uiState.startDate,
        initialSelectedEndDateMillis = uiState.endDate
    )
    // ------------------------------------



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val startDateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(uiState.startDate))
                    val endDateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(uiState.endDate))
                    Text(if (startDateText == endDateText) startDateText else "$startDateText ~ $endDateText")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // **修改此按钮为触发范围选择**
                    IconButton(onClick = { showDateRangePickerDialog = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "选择日期范围")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "记一笔")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // --- 汇总信息 ---
            LedgerSummaryHeader(
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                balance = uiState.totalIncome - uiState.totalExpense
            )
            HorizontalDivider()

            // --- 流水列表 ---
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("当前日期范围无流水记录。") }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        LedgerItemRow(entry = entry)
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // --- 添加日期选择器对话框 ---
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // 调用 ViewModel 的方法来设置新的一天
                            viewModel.setDate(selectedDate)
                        }
                        showDatePickerDialog = false
                    }
                ) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- 日期范围选择器对话框 ---
    if (showDateRangePickerDialog) {
        DateRangePickerDialog(
            dateRangePickerState = dateRangePickerState,
            onDismiss = { showDateRangePickerDialog = false },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) {
                    // 我们需要将用户选择的结束日期调整为当天的最后一刻
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = end
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    val endOfDay = calendar.timeInMillis

                    viewModel.setDateRange(start, endOfDay)
                }
                showDateRangePickerDialog = false
            }
        )
    }

    if (showAddDialog) {
        AddLedgerEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, amount, desc, date ->
                viewModel.addLedgerEntry(type, amount, desc, date)
                showAddDialog = false
            }
        )
    }
}


@Composable
private fun LedgerSummaryHeader(totalIncome: Double, totalExpense: Double, balance: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        SummaryItem("总收入", totalIncome, Color(0xFF4CAF50))
        SummaryItem("总支出", totalExpense, MaterialTheme.colorScheme.error)
        SummaryItem("结余", balance, MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            "¥${String.format("%,.2f", amount)}", // 使用千分位格式化
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LedgerItemRow(entry: LedgerEntry) {
    val amountColor = if (entry.entryType == LedgerEntryType.INCOME) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    val amountPrefix = if (entry.entryType == LedgerEntryType.INCOME) "+" else "-"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(entry.entryDate)),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "$amountPrefix ¥${String.format("%,.2f", entry.amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    dateRangePickerState: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text("确认") }
                }
            }
        }
    }
}