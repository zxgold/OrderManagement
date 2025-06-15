package com.example.manager.ui.order

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.focus.onFocusChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderScreen(
    orderId: Long?, // 如果是编辑模式，则有 orderId；如果是新增模式，则为 null
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.addEditOrderUiState.collectAsStateWithLifecycle()
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    val customerSearchResults by viewModel.customerSearchResults.collectAsStateWithLifecycle()
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
                CustomerSelector(
                    searchQuery = customerSearchQuery,
                    onSearchQueryChanged = viewModel::onCustomerSearchQueryChanged,
                    searchResults = customerSearchResults,
                    selectedCustomer = uiState.selectedCustomer,
                    onCustomerSelected = { customer, fromDropdown ->
                        viewModel.onCustomerSelected(customer)
                        // 如果用户是从下拉菜单中选择的，通常不需要再做什么
                        // 如果是从其他地方（如“创建新客户”按钮）选择的，可能需要其他逻辑
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
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

// --- 新增：客户选择器的 Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelector(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    searchResults: List<Customer>,
    selectedCustomer: Customer?,
    onCustomerSelected: (Customer, Boolean) -> Unit // Boolean 表示是否来自下拉菜单
) {
    var expanded by remember { mutableStateOf(false) }

    // 当有搜索结果且搜索框非空时，展开下拉菜单
    // 并且当用户选择了客户后，菜单应该收起 (因为 searchQuery 会被清空)
    // 或者，当搜索框失去焦点时，也应该收起
    // 一个更简单的逻辑是：只有当输入框有焦点且有搜索词时才展开
    var hasFocus by remember { mutableStateOf(false) }

    // 只有当搜索词不为空且有搜索结果，并且输入框有焦点时，才展开菜单
    expanded = searchQuery.isNotBlank() && searchResults.isNotEmpty() && hasFocus

    Column {
        Text("选择客户 *", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                // 我们不直接通过这个来控制展开，而是通过搜索结果和焦点
                // expanded = !expanded
            }
        ) {
            // --- 输入框 ---
            OutlinedTextField(
                value = selectedCustomer?.name ?: searchQuery, // 如果有选中的客户，显示客户名，否则显示搜索词
                onValueChange = {
                    // 当用户输入时，如果之前有选中的客户，需要清除它，然后更新搜索词
                    if (selectedCustomer != null) {
                        onCustomerSelected(Customer(id = -1, name = "", phone = "", storeId = -1), false) // 用一个无效客户或null来清除选中状态
                    }
                    onSearchQueryChanged(it)
                },
                label = { Text("搜索客户 (姓名或电话)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true) // 将输入框与下拉菜单关联
                    .onFocusChanged { focusState -> hasFocus = focusState.isFocused }, // 监听焦点变化
                readOnly = selectedCustomer != null, // 如果已选中客户，则输入框只读，防止误修改
                trailingIcon = {
                    if (selectedCustomer != null) {
                        IconButton(onClick = {
                            // 清除已选中的客户，允许重新搜索
                            onCustomerSelected(Customer(id = -1, name = "", phone = "", storeId = -1), false)
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = "清除所选客户") // 需要导入 Clear 图标
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }
            )

            // --- 下拉菜单 ---
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { /* 我们不在这里处理收起，焦点变化会自动处理 */ }
            ) {
                searchResults.forEach { customer ->
                    DropdownMenuItem(
                        text = { Text("${customer.name} - ${customer.phone}") },
                        onClick = {
                            onCustomerSelected(customer, true) // 调用 ViewModel 的方法
                            // onSearchQueryChanged("") // 在 ViewModel 的 onCustomerSelected 中处理
                        }
                    )
                }
            }
        }
        // TODO: 在这里添加一个“创建新客户”按钮，点击后可以导航到客户创建页面或弹出对话框
        // TextButton(onClick = { /* ... */ }) { Text("+ 创建新客户") }
    }
}
