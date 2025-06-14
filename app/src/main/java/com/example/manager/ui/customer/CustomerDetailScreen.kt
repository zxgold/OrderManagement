package com.example.manager.ui.customer

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.Customer
import com.example.manager.viewmodel.CustomerDetailViewModel
import com.example.manager.viewmodel.CustomerViewModel // 用于编辑对话框
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    // customerId: Long, // ViewModel 会从 SavedStateHandle 获取
    navController: NavController, // 用于返回或导航到编辑
    viewModel: CustomerDetailViewModel = hiltViewModel(), // 获取 CustomerDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) } // 控制编辑对话框的显示
    val snackbarHostState = remember { SnackbarHostState() }    // 用于 Snackbar 的状态
    val coroutineScope = rememberCoroutineScope() // 获取协程作用域

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.errorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.customer?.name ?: "客户详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.customer != null) { // 只有加载到客户信息才显示编辑按钮
                        IconButton(onClick = {
                            Log.d(
                                "CustomerDetailScreen",
                                "Edit button clicked for: ${uiState.customer?.name}"
                            )
                            showEditDialog = true // 显示编辑对话框
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑客户")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> { // 使用已收集的 uiState
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // 当 customer 为 null 且 errorMessage 也为 null (表示不是因为错误导致 customer 为 null)
                // 或者 errorMessage 不为 null 但我们选择优先显示错误信息（取决于你的UI决策）
                // 我们之前的逻辑是:
                // uiState.errorMessage != null && uiState.customer == null -> { ... 显示错误 ... }
                // uiState.customer != null -> { ... 显示客户信息 ... }
                // else -> { /* 未找到客户信息，且没有明确的错误信息 */ }

                // 让我们调整一下逻辑，优先显示错误（如果存在且customer数据没有）
                // 或者优先显示数据（如果存在）
                // 最后才是“未找到”

                uiState.customer != null -> { // **优先显示客户信息**
                    val customer = uiState.customer!! // 既然非空，可以直接用 !!
                    CustomerInfoSection("基本信息") {
                        InfoRow("姓名:", customer.name)
                        InfoRow("电话:", customer.phone) // 假设 phone 已改为非空 String
                        InfoRow("地址:", customer.address ?: "未提供")
                        InfoRow("备注:", customer.remark ?: "无")
                        InfoRow("店铺ID:", customer.storeId.toString())
                        InfoRow("创建时间:", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(customer.createdAt)))
                        InfoRow("更新时间:", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(customer.updatedAt)))
                    }
                    // TODO: 未来在这里显示与此客户相关的订单列表、跟进记录等
                }
                uiState.isLoading -> { // **如果还在加载，显示加载指示器**
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> { // **如果有错误信息，显示错误**
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("错误: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> { // **其他所有情况 (非加载中，无客户数据，无错误信息) 才显示“未找到”**
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("未找到客户信息。")
                    }
                }
            }
        }

    }
    // --- 编辑客户对话框的显示逻辑 ---
    if (showEditDialog && uiState.customer != null) {
        EditCustomerDialog( // 复用我们之前创建的 EditCustomerDialog
            customer = uiState.customer!!, // 将当前加载到的客户信息作为初始值
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedCustomer ->
                // 使用 coroutineScope 启动协程来调用挂起的 ViewModel 方法
                coroutineScope.launch {
                    val result = viewModel.saveUpdatedCustomer(updatedCustomer) // 调用 CustomerDetailViewModel 的方法
                    result.onSuccess { success ->
                        if (success) {
                            showEditDialog = false // 保存成功后关闭对话框
                            // CustomerDetailViewModel 的 saveUpdatedCustomer 内部会调用 loadCustomerDetails 刷新
                            Log.d("CustomerDetailScreen", "Customer update successful, dialog closed.")
                        } else {
                            Log.w("CustomerDetailScreen", "Customer update reported no rows affected by ViewModel.")
                            // 错误信息应该由 ViewModel 的 uiState.errorMessage 驱动 Snackbar 显示
                        }
                    }.onFailure {
                        Log.e("CustomerDetailScreen", "Customer update failed in ViewModel: ${it.localizedMessage}")
                        // 错误信息由 ViewModel 的 uiState.errorMessage 驱动 Snackbar 显示
                    }
                }
            }
        )
    }
    // ------------------------------------
}

@Composable
private fun CustomerInfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}