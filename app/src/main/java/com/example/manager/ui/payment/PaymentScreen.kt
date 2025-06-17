package com.example.manager.ui.payment

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
import com.example.manager.data.model.uimodel.PaymentWithDetails
import com.example.manager.viewmodel.PaymentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.manager.data.model.entity.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    val addPaymentUiState by viewModel.addPaymentUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    // --- **新增：获取客户搜索相关的状态** ---
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    val customerSearchResults by viewModel.customerSearchResults.collectAsStateWithLifecycle()
    // ------------------------------------

    // 监听一次性消息
    LaunchedEffect(Unit) {
        viewModel.messageFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("回款管理") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } }
                // TODO: 添加日期范围选择器
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.loadDataForDialog() // 打开对话框前加载数据
                showAddDialog = true
            }) {
                Icon(Icons.Filled.Add, "添加回款")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // ... 可以复用 LedgerScreen 的 SummaryHeader 来显示总回款 ...
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.payments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无回款记录") }
            } else {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(uiState.payments, key = { it.payment.id }) { item ->
                        PaymentItemRow(item = item)
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        if (addPaymentUiState.isLoading) {
            // 可以显示一个加载对话框或在 AddPaymentDialog 内部显示加载状态
        } else {
            AddPaymentDialog(
                availableOrders = addPaymentUiState.availableOrders,
                // 传递搜索相关的状态和回调
                customerSearchQuery = customerSearchQuery,
                onCustomerSearchQueryChanged = viewModel::onCustomerSearchQueryChanged,
                customerSearchResults = customerSearchResults,
                onDismiss = { showAddDialog = false },
                onConfirm = { amount, method, notes, order, customer ->
                    // onConfirm 现在接收 customer 对象
                    viewModel.addPayment(amount, method, notes, System.currentTimeMillis(), order, customer)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun PaymentItemRow(item: PaymentWithDetails) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "客户: ${item.customerName ?: "未知客户"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            item.orderNumber?.let {
                Text("订单号: $it", style = MaterialTheme.typography.bodyMedium)
            }
            item.payment.notes?.let {
                Text("备注: $it", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.payment.paymentDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "¥${String.format("%,.2f", item.payment.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}