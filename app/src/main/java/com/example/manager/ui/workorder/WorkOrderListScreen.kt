package com.example.manager.ui.workorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.uimodel.WorkOrderItem
import com.example.manager.viewmodel.WorkOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderListScreen(
    navController: NavController,
    viewModel: WorkOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            // TODO: Add viewModel.errorShown() if needed
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("工单管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                // TODO: 添加筛选按钮
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.workOrders.isEmpty()) {
                Text(
                    "当前没有待处理的工单。",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.workOrders, key = { it.orderItem.id }) { workOrderItem ->
                        WorkOrderItemCard(
                            item = workOrderItem,
                            onClick = {
                                // TODO: 导航到工单详情页，传递 orderItemId
                                // navController.navigate(AppDestinations.workOrderDetailRoute(workOrderItem.orderItem.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkOrderItemCard(
    item: WorkOrderItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(item.orderItem.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("订单: ${item.orderNumber}", style = MaterialTheme.typography.bodyMedium)
            item.customerName?.let {
                Text("客户: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = getOrderItemStatusColor(item.orderItem.status).copy(alpha = 0.1f) // 背景色
            ) {
                Text(
                    text = item.orderItem.status.name, // TODO: 本地化
                    style = MaterialTheme.typography.labelLarge,
                    color = getOrderItemStatusColor(item.orderItem.status), // 字体颜色
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun getOrderItemStatusColor(status: OrderItemStatus): Color = when (status) {
    OrderItemStatus.NOT_ORDERED -> MaterialTheme.colorScheme.error
    OrderItemStatus.ORDERED -> MaterialTheme.colorScheme.secondary
    OrderItemStatus.IN_TRANSIT -> MaterialTheme.colorScheme.tertiary
    OrderItemStatus.IN_STOCK -> Color(0xFF1E88E5) // Blue
    OrderItemStatus.INSTALLING -> Color(0xFFFFA000) // Orange
    OrderItemStatus.INSTALLED -> Color(0xFF43A047) // Green
}