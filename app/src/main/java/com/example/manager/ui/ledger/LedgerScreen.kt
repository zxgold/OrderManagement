package com.example.manager.ui.ledger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.LedgerEntry
import com.example.manager.data.model.enums.LedgerEntryType
import com.example.manager.viewmodel.LedgerViewModel
import java.text.SimpleDateFormat
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("店铺日记账") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                // TODO: 添加日期范围选择器按钮
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