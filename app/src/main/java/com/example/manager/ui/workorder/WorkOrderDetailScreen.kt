package com.example.manager.ui.workorder

// ... imports ...
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.manager.viewmodel.WorkOrderDetailViewModel

@Composable
fun WorkOrderDetailScreen(
    navController: NavController,
    viewModel: WorkOrderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { /* ... TopAppBar with back button ... */ }) { paddingValues ->
        // ... (Loading, Error handling) ...
        uiState.workOrderItem?.let { workOrderItem ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                // 显示产品、订单、客户信息
                Text(workOrderItem.orderItem.productName, style = MaterialTheme.typography.headlineMedium)
                // ...

                // 状态时间线
                StatusTimeline(
                    currentStatus = workOrderItem.orderItem.status,
                    logs = uiState.statusLogs,
                    staffNames = uiState.staffNames,
                    onStatusClick = { newStatus ->
                        // TODO: 显示确认对话框
                        viewModel.updateStatus(newStatus)
                    }
                )

                // TODO: 到库备注的 UI 和逻辑
            }
        }
    }
}

// TODO: 实现 StatusTimeline Composable
@Composable
fun StatusTimeline() {
    // 使用 Column, Row, Canvas 等来绘制你设想的节点线
}