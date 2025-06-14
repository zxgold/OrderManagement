package com.example.manager.ui.order

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.viewmodel.OrderViewModel
import androidx.compose.ui.text.input.TextFieldValue
import com.example.manager.data.model.entity.Customer
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderScreen(
    orderId: Long?, // 如果是编辑模式，则有 orderId；如果是新增模式，则为 null
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.addEditOrderUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditMode = orderId != null && orderId != -1L

    // 根据是新增还是编辑模式，在 Composable 首次组合时准备数据
    LaunchedEffect(key1 = orderId) {
        if (isEditMode) {
            Log.d("AddEditOrderScreen", "Preparing to edit order ID: $orderId")
            viewModel.prepareOrderForEditing(orderId!!)
        } else {
            Log.d("AddEditOrderScreen", "Preparing new order form.")
            viewModel.prepareNewOrderForm()
        }
    }

    // 处理保存成功后的导航
    LaunchedEffect(key1 = uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Log.d("AddEditOrderScreen", "Save successful, navigating back.")
            snackbarHostState.showSnackbar("订单已保存", duration = SnackbarDuration.Short)
            viewModel.resetSaveSuccessFlag() // 重置标志位
            navController.popBackStack() // 保存成功后返回上一个界面
        }
    }

    // 处理错误消息
    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, actionLabel = "知道了")
            viewModel.clearAddEditOrderError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑订单" else "创建新订单") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveOrder() }, enabled = !uiState.isSaving) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Filled.Save, contentDescription = "保存订单")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- 这里是核心表单内容 ---
        // 我们将在下一步填充这部分
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Section 1: 客户选择
            item {
                // --- Section 1: 客户选择器 ---

            }

            // Section 2: 订单项列表
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("产品列表", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { /* TODO: 弹出产品选择器 */ }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加产品")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加产品")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (uiState.tempOrderItems.isEmpty()) {
                item { Text("请点击右上角添加产品到订单。") }
            } else {
                items(uiState.tempOrderItems, key = { it.tempId }) { tempItem ->
                    // TODO: 实现订单项条目 UI (可编辑)
                    Text("订单项: ${tempItem.productName} x ${tempItem.quantity} (TODO)")
                    HorizontalDivider()
                }
            }

            // Section 3: 金额和备注
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("金额与备注", style = MaterialTheme.typography.titleMedium)
                // TODO: 实现折扣、首付款、备注等输入框
                Text("总金额: ¥${uiState.calculatedTotalAmount} (TODO)")
                Text("应付金额: ¥${uiState.calculatedFinalAmount} (TODO)")
            }
        }
        // ------------------------
    }
}