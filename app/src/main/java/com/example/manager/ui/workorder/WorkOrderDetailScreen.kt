package com.example.manager.ui.workorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.viewmodel.WorkOrderDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    navController: NavController,
    viewModel: WorkOrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("工单详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.workOrderItem != null) {
            val workOrderItem = uiState.workOrderItem!!
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                // 产品、订单、客户信息
                Text(workOrderItem.orderItem.productName, style = MaterialTheme.typography.headlineSmall)
                Text("订单号: ${workOrderItem.orderNumber}", style = MaterialTheme.typography.bodyLarge)
                workOrderItem.customerName?.let { Text("客户: $it", style = MaterialTheme.typography.bodyLarge) }

                Spacer(modifier = Modifier.height(24.dp))

                // 状态时间线
                Text("工单进度", style = MaterialTheme.typography.titleLarge)
                StatusTimeline(
                    allPossibleStatuses = OrderItemStatus.values().toList(),
                    currentStatus = workOrderItem.orderItem.status,
                    logs = uiState.statusLogs,
                    staffNames = uiState.staffNames,
                    onStatusClick = { newStatus ->
                        viewModel.updateStatus(newStatus) // 点击可更新的节点时调用
                    }
                )

                // TODO: 到库备注 UI
            }
        } else {
            // ... 显示错误或未找到信息 ...
        }
    }
}

@Composable
fun StatusTimeline(
    allPossibleStatuses: List<OrderItemStatus>,
    currentStatus: OrderItemStatus,
    logs: List<OrderItemStatusLog>,
    staffNames: Map<Long, String>,
    onStatusClick: (OrderItemStatus) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        allPossibleStatuses.forEachIndexed { index, status ->
            val logForStatus = logs.findLast { it.status == status }
            val isCompleted = status <= currentStatus
            val isCurrent = status == currentStatus
            val isNextAction = status.ordinal == currentStatus.ordinal + 1 // 是否是下一个可点击的节点

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (isNextAction) it.clickable { onStatusClick(status) } else it }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimelineNode(isCompleted = isCompleted, isCurrent = isCurrent, isLast = index == allPossibleStatuses.lastIndex)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = status.name, // TODO: 本地化
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                    logForStatus?.let { log ->
                        val staffName = staffNames[log.staffId] ?: "未知员工"
                        val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(log.timestamp)
                        Text("由 $staffName 于 $time 操作", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineNode(isCompleted: Boolean, isCurrent: Boolean, isLast: Boolean) {
    val color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray
    val circleRadius = if (isCurrent) 12.dp else 8.dp

    Box(contentAlignment = Alignment.Center) {
        if (!isLast) {
            // 绘制节点间的连线
            Canvas(modifier = Modifier.matchParentSize()) {
                drawLine(
                    color = color,
                    start = Offset(center.x, circleRadius.toPx()),
                    end = Offset(center.x, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        // 绘制节点圆圈
        Canvas(modifier = Modifier.size(circleRadius * 2)) {
            drawCircle(color = color)
            if (isCurrent) {
                drawCircle(color = Color.White, radius = (circleRadius - 3.dp).toPx())
                drawCircle(color = color, radius = (circleRadius - 5.dp).toPx())
            }
        }
    }
}