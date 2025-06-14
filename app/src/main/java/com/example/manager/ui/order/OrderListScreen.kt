package com.example.manager.ui.order // 新建一个 order 包用于存放订单相关UI

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward // 用于指示进入详情
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert // 用于未来可能的每行操作
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.enums.OrderStatus
import com.example.manager.ui.navigation.AppDestinations // 导入路由
import com.example.manager.ui.theme.ManagerTheme // 你的主题
import com.example.manager.viewmodel.OrderListUiState
import com.example.manager.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    navController: NavController, // 用于导航到订单详情或添加订单
    viewModel: OrderViewModel = hiltViewModel()
    // authViewModel: AuthViewModel // 如果需要获取当前用户信息来做某些判断，可以注入
) {
    val uiState by viewModel.orderListUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 当屏幕进入时（或 storeId 变化时）加载订单
    // 我们假设 OrderViewModel 内部的 loadOrders 会自动获取 storeId
    LaunchedEffect(key1 = Unit) { // key1 = Unit 表示只在 Composable 首次组合时运行
        Log.d("OrderListScreen", "Initial composition or key change, loading orders.")
        viewModel.loadOrders()
    }

    // 处理错误消息
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = "知道了")
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.clearOrderListError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("订单管理") })
            // TODO: 未来可以添加搜索/筛选图标按钮
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("OrderListScreen", "Navigating to Add/Edit Order screen for new order.")
                navController.navigate(AppDestinations.addOrderRoute())
            }) {
                Icon(Icons.Filled.Add, contentDescription = "添加订单")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.ordersWithCustomerNames.isEmpty()) {
                Text(
                    "还没有订单，点击右下角添加一个吧！",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = uiState.ordersWithCustomerNames,
                        key = { (order, _) -> order.id } // 使用订单ID作为key
                    ) { (order, customerName) -> // 解构 Pair
                        OrderItemCard(
                            order = order,
                            customerName = customerName ?: "匿名客户", // 如果客户名为空，显示匿名
                            onClick = {
                                Log.d("OrderListScreen", "Order clicked: ID ${order.id}")
                                navController.navigate(AppDestinations.orderDetailRoute(order.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    order: Order,
    customerName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "订单号: ${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "客户: $customerName",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "金额: ¥${String.format("%.2f", order.finalAmount)}", // 格式化金额
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "状态: ${order.status.name}", // 直接显示枚举名，后续可以本地化
                        style = MaterialTheme.typography.bodySmall,
                        color = getOrderStatusColor(order.status) // 根据状态给不同颜色
                    )
                    Text(
                        text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(order.orderDate)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "查看详情",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun getOrderStatusColor(status: OrderStatus) = when (status) {
    OrderStatus.PENDING -> MaterialTheme.colorScheme.secondary
    OrderStatus.PROCESSING -> MaterialTheme.colorScheme.tertiary
    OrderStatus.COMPLETED -> Color(0xFF4CAF50) // Green
    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    // 为其他可能的状态添加颜色
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun OrderListScreenPreview() {
    ManagerTheme {
        // 创建一个模拟的 NavController
        val navController = rememberNavController()
        // 为了预览，我们可以创建一个模拟的 OrderViewModel，或者让 hiltViewModel() 返回一个默认实例
        // 更简单的方式是直接调用，让其内部的 hiltViewModel() 工作（但在 Preview 中可能不会完全按预期工作）
        OrderListScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun OrderItemCardPreview() {
    ManagerTheme {
        OrderItemCard(
            order = Order(
                id = 1,
                storeId = 1,
                orderNumber = "ORD-20240101-001",
                customerId = 1,
                orderDate = System.currentTimeMillis(),
                totalAmount = 200.0,
                discount = 10.0,
                finalAmount = 190.0,
                status = OrderStatus.COMPLETED,
                creatingStaffId = 1
            ),
            customerName = "张三",
            onClick = {}
        )
    }
}