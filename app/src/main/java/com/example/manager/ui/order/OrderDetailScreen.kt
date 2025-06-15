package com.example.manager.ui.order

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.OrderItem
import com.example.manager.ui.navigation.AppDestinations
import com.example.manager.viewmodel.OrderDetailUiState
import com.example.manager.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long, // 从导航参数接收
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel() // 复用 OrderViewModel
) {
    val uiState by viewModel.orderDetailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 当 orderId 变化或屏幕首次组合时加载订单详情
    LaunchedEffect(key1 = orderId) {
        Log.d("OrderDetailScreen", "Loading details for order ID: $orderId")
        viewModel.loadOrderDetail(orderId)
    }

    // 处理错误消息
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = "知道了")
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.clearOrderDetailError() // 调用 OrderViewModel 中对应的清除错误方法
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.order?.orderNumber ?: "订单详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.order != null) { // 只有加载到订单信息才显示编辑按钮
                        IconButton(onClick = {
                            Log.d("OrderDetailScreen", "Edit button clicked for order ID: ${uiState.order?.id}")
                            uiState.order?.let {
                                navController.navigate(AppDestinations.editOrderRoute(it.id))
                            }
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑订单")
                        }
                    }
                }
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
            } else if (uiState.order != null) {
                val order = uiState.order!!
                val orderItems = uiState.items
                val customerName = uiState.customerName ?: "匿名客户"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("订单详情", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

                    OrderDetailSection("订单信息") {
                        InfoRow("订单号:", order.orderNumber)
                        InfoRow("客户:", customerName)
                        InfoRow("下单日期:", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(order.orderDate)))
                        InfoRow("订单状态:", order.status.name) // TODO: 本地化状态显示
                        InfoRow("订单总额:", "¥${String.format("%.2f", order.totalAmount)}")
                        InfoRow("优惠金额:", "¥${String.format("%.2f", order.discount)}")
                        InfoRow("应付金额:", "¥${String.format("%.2f", order.finalAmount)}")
                        InfoRow("首付款:", "¥${String.format("%.2f", order.downPayment)}")
                        InfoRow("创建员工ID:", order.creatingStaffId?.toString() ?: "未知") // TODO: 显示员工姓名
                        order.notes?.let { InfoRow("订单备注:", it) }
                    }

                    if (orderItems.isNotEmpty()) {
                        OrderDetailSection("产品列表 (${orderItems.size}项)") {
                            orderItems.forEach { item ->
                                OrderItemDetailView(item = item)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    } else {
                        Text("此订单没有产品项。", style = MaterialTheme.typography.bodyMedium)
                    }
                    // TODO: 未来可以添加收款记录、跟进记录等
                }
            } else if (uiState.errorMessage != null) {
                Text(
                    "错误: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                Text("未找到订单信息。", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun OrderDetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    Column(content = content)
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(100.dp),
            fontWeight = FontWeight.SemiBold
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun OrderItemDetailView(item: OrderItem) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(item.productName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        item.productModel?.let { Text("型号: $it", style = MaterialTheme.typography.bodyMedium) }
        Text("数量: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
        Text("单价: ¥${String.format("%.2f", item.actualUnitPrice)}", style = MaterialTheme.typography.bodyMedium)
        Text("小计: ¥${String.format("%.2f", item.itemTotalAmount)}", style = MaterialTheme.typography.bodyMedium)
        Text("状态: ${item.status.name}", style = MaterialTheme.typography.bodyMedium) // TODO: 本地化
        item.notes?.let { Text("备注: $it", style = MaterialTheme.typography.bodySmall) }
    }
}