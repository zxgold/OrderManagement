package com.example.manager.ui.workorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
    var statusToUpdate by remember { mutableStateOf<OrderItemStatus?>(null) }//用于控制确认对话框的状态
    val snackbarHostState = remember { SnackbarHostState() } // **确保有 snackbarHostState**

    // --- 处理加载、详情、成功、错误消息 ---
    // 我们将 `key1` 设置为整个 `uiState`。
    // 这意味着只要 `uiState` 对象中的任何一个字段发生变化（包括 `errorMessage` 或 `updateSuccessMessage`），
    // 这个 `LaunchedEffect` 就会重新运行，检查并显示相应的 `Snackbar`。
    LaunchedEffect(key1 = uiState) {
        // 处理错误消息
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.errorShown() // 通知 ViewModel 错误已被显示
        }
        // 处理成功消息
        uiState.updateSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.successMessageShown() // 通知 ViewModel 成功消息已被显示
        }
    }

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
                        statusToUpdate = newStatus // 点击可更新的节点时调用
                    }
                )

                // TODO: 到库备注 UI

                // 只有当状态为“已到库”或之后时，才显示备注区
                if (workOrderItem.orderItem.status >= OrderItemStatus.IN_STOCK) {
                    var showEditNotesDialog by remember { mutableStateOf(false) } // 控制备注编辑对话框

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("到库备注", style = MaterialTheme.typography.titleLarge)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { showEditNotesDialog = true }, // 点击卡片即可编辑
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = workOrderItem.orderItem.notes ?: "无到库备注信息，点击添加",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Filled.Edit, contentDescription = "编辑备注")
                        }
                    }

                    // --- 备注编辑对话框 ---
                    if (showEditNotesDialog) {
                        EditNotesDialog(
                            initialNotes = workOrderItem.orderItem.notes ?: "",
                            onDismiss = { showEditNotesDialog = false },
                            onConfirm = { newNotes ->
                                viewModel.updateOrderItemNotes(newNotes)
                                showEditNotesDialog = false
                            }
                        )
                    }
                }

            }
        } else {
            // ... 显示错误或未找到信息 ...
        }
    }

    // --- 状态更新确认对话框 ---
    statusToUpdate?.let { newStatus -> // 当 statusToUpdate 不为 null 时显示
        AlertDialog(
            onDismissRequest = { statusToUpdate = null }, // 点击外部或返回键时关闭
            icon = { Icon(Icons.Filled.Info, contentDescription = "确认信息") }, // 添加一个图标
            title = { Text("确认状态变更") },
            text = {
                Text("确定要将工单状态更新为 “${newStatus.name}” 吗？") // TODO: 本地化
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateStatus(newStatus) // **在这里调用 ViewModel 的方法**
                        // TODO：立即更新节点颜色
                        statusToUpdate = null // 关闭对话框
                    },
                    enabled = !uiState.isUpdatingStatus // **当正在更新时，禁用按钮**
                ) {
                    if (uiState.isUpdatingStatus) {
                        // **正在更新时，显示加载圈**
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("确认更新")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { statusToUpdate = null }) { // 点击取消按钮
                    Text("取消")
                }
            }
        )
    }
    // ---------------------------------
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

// EditNotesDialog.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNotesDialog(
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑到库备注") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().height(150.dp), // 给备注区更多空间
                label = { Text("备注内容") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(notes.ifBlank { null }) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}