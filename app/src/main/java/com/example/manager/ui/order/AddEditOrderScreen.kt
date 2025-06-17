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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontWeight
import com.example.manager.viewmodel.TempOrderItem
import com.example.manager.ui.order.OrderItemInputRow // 新的订单项输入行
import com.example.manager.ui.customer.AddCustomerDialog // **现在不再需要这个了**
import com.example.manager.ui.navigation.AppDestinations // **导入 AppDestinations**
import androidx.compose.runtime.livedata.observeAsState


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
    var showAddOrderItemDialog by remember { mutableStateOf(false) } // 新增状态控制添加产品对话框
    val productSearchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle() // <-- **获取产品搜索相关状态**
    val productSearchResults by viewModel.productSearchResults.collectAsStateWithLifecycle() // <-- **获取产品搜索相关状态**

    var notesState by remember(uiState.orderId) { mutableStateOf(TextFieldValue(uiState.notes ?: "")) }

    // --- **监听从 AddCustomerScreen 返回的结果** ---
    val newCustomerIdResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Long>(AppDestinations.NEW_CUSTOMER_ID_RESULT_KEY)
        ?.observeAsState()

    LaunchedEffect(newCustomerIdResult?.value) {
        newCustomerIdResult?.value?.let { id ->
            Log.d("AddEditOrderScreen", "Received new customer ID: $id")
            viewModel.selectCustomerById(id)
            // 清除结果，防止重复处理
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Long>(AppDestinations.NEW_CUSTOMER_ID_RESULT_KEY)
        }
    }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // 给每个区段一些间距
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
                    Text("产品列表 *", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showAddOrderItemDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加产品")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加产品")
                    }
                }
            }
            if (uiState.tempOrderItems.isEmpty()) {
                item { Text("请点击“添加产品”按钮将产品加入订单。") }
            } else {
                items(uiState.tempOrderItems, key = { it.tempId }) { tempItem ->
                    OrderItemInputRow(
                        item = tempItem,
                        onQuantityChange = { tempId, newQuantity ->
                            // 调用 ViewModel 的 update 方法，但只更新数量
                            viewModel.updateTempOrderItem(tempId, newQuantity, tempItem.actualUnitPrice)
                        },
                        onPriceChange = { tempId, newPrice ->
                            // 调用 ViewModel 的 update 方法，但只更新价格
                            viewModel.updateTempOrderItem(tempId, tempItem.quantity, newPrice)
                        },
                        onRemoveClick = viewModel::removeTempOrderItem
                    )
                }
            }

            // Section 3: 金额和备注
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OrderSummarySection(
                    totalAmount = uiState.calculatedTotalAmount,
                    discount = uiState.discount,
                    finalAmount = uiState.calculatedFinalAmount,
                    downPayment = uiState.downPayment,
                    onDiscountChange = viewModel::onDiscountChanged,
                    onDownPaymentChange = viewModel::onDownPaymentChanged,
                    notes = notesState,
                    onNotesChange = { newValue ->
                        notesState = newValue // UI 层更新自己的状态
                        // 只有当文本改变时，才通知 ViewModel
                        if (notesState.text != uiState.notes) {
                            viewModel.onOrderNotesChanged(newValue.text)
                        }
                    }
                )
            }

            // Section 4: 保存按钮 (可以放在 LazyColumn 底部或 TopAppBar 中)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveOrder() },
                    enabled = !uiState.isSaving && uiState.selectedCustomer != null && uiState.tempOrderItems.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("保存订单")
                    }
                }
            }
        }
    }

    // 显示添加产品对话框
    // --- 显示添加产品对话框 ---
    if (showAddOrderItemDialog) {
        // **修改这里：我们需要一个新的、支持搜索的对话框**
        // 我们先创建一个新的对话框 Composable
        ProductSearchSelectorDialog(
            searchQuery = productSearchQuery,
            onSearchQueryChanged = viewModel::onProductSearchQueryChanged,
            searchResults = productSearchResults,
            onDismiss = {
                showAddOrderItemDialog = false
                viewModel.onProductSearchQueryChanged("") // 关闭时清空搜索词
            },
            onConfirm = { product, quantity, price, notes, isCustomized ->
                viewModel.addTempOrderItem(product, quantity, price, notes, isCustomized)
                showAddOrderItemDialog = false
            }
        )
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

// 新增：用于显示和编辑单个临时订单项的 Composable
@Composable
fun TempOrderItemView(
    item: TempOrderItem,
    onUpdate: (tempId: String, quantity: Int, price: Double, notes: String?) -> Unit,
    onRemove: (tempId: String) -> Unit
) {
    // 我们可以让这个视图更简单，或者也提供一个编辑对话框
    // 这里先只显示信息，并提供一个删除按钮
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("数量: ${item.quantity}  |  单价: ¥${item.actualUnitPrice}  |  小计: ¥${item.itemTotalAmount}")
            item.notes?.let { Text("备注: $it", style = MaterialTheme.typography.bodySmall) }
        }
        // TODO: 添加编辑按钮
        IconButton(onClick = { onRemove(item.tempId) }) {
            Icon(Icons.Filled.Delete, contentDescription = "移除产品项")
        }
    }
}

// 新增：用于显示订单金额汇总和备注的 Composable
@Composable
fun OrderSummarySection(
    totalAmount: Double,
    discount: Double,
    finalAmount: Double,
    downPayment: Double,
    onDiscountChange: (Double) -> Unit,
    onDownPaymentChange: (Double) -> Unit,
    notes: TextFieldValue,
    onNotesChange: (TextFieldValue) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("金额与备注", style = MaterialTheme.typography.titleMedium)
        Text("订单总额: ¥${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = if (discount == 0.0) "" else discount.toString(),
            onValueChange = { onDiscountChange(it.toDoubleOrNull() ?: 0.0) },
            label = { Text("优惠金额") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
        )
        Text("应付金额: ¥${String.format("%.2f", finalAmount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = if (downPayment == 0.0) "" else downPayment.toString(),
            onValueChange = { onDownPaymentChange(it.toDoubleOrNull() ?: 0.0) },
            label = { Text("首付款") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("订单备注") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )
    }
}
