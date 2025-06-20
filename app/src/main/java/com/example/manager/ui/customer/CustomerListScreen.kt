package com.example.manager.ui.customer

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group // 员工管理图标
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.model.enums.StaffRole // 导入 StaffRole
import com.example.manager.data.preferences.UserSession // 导入 UserSession
import com.example.manager.viewmodel.AuthViewModel // 导入 AuthViewModel
import com.example.manager.viewmodel.CustomerViewModel
import androidx.compose.material3.HorizontalDivider // 你使用的是 HorizontalDivider
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.manager.ui.navigation.AppDestinations




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel, // <-- **接收 AuthViewModel 参数**
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 获取当前会话状态，提供一个非 null 的初始值以避免 LaunchedEffect 首次运行问题
    val currentSession by authViewModel.currentUserSessionFlow.collectAsStateWithLifecycle(
        initialValue = UserSession(
            isLoggedIn = false,
            staffId = null,
            staffRole = null,
            username = null,
            staffName = null,
            storeId = null,
            storeName = null
        )
    )
    val currentRole = currentSession.staffRole

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    // --- **当屏幕重新进入前台时，刷新数据** ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("CustomerListScreen", "Screen Resumed. Reloading customers.")
                // 调用 ViewModel 的方法来刷新数据
                // 我们之前已经有了 loadCustomersBasedOnSession 这个方法，但它是 private 的
                // 我们需要一个 public 的方法来触发刷新
                viewModel.refreshCustomerList()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // ----------------------------------------------------

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "知道了"
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.errorShown()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("客户管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (currentRole == StaffRole.BOSS) {
                        IconButton(onClick = {
                            Log.d("CustomerListScreen", "员工管理按钮点击 (TODO)")
                            // TODO: 导航到员工管理界面
                        }) {
                            Icon(Icons.Filled.Group, contentDescription = "员工管理")
                        }
                    }
                    IconButton(onClick = {
                        Log.d("CustomerListScreen", "登出按钮点击")
                        authViewModel.logout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "登出")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "添加客户")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("搜索客户 (姓名或电话)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.customers.isEmpty() && !uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("还没有客户，点击右下角添加吧！")
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = uiState.customers,
                            key = { customer -> customer.id }
                        ) { customer ->
                            CustomerItem(
                                customer = customer,
                                onItemClick = { selectedCustomer ->
                                    Log.d(
                                        "CustomerListScreen",
                                        "Navigating to detail for customer ID: ${selectedCustomer.id}"
                                    )
                                    // 使用 AppDestinations 构建带参数的路由
                                    navController.navigate(
                                        AppDestinations.customerDetailRoute(
                                            selectedCustomer.id
                                        )
                                    )
                                },
                                onDeleteClick = { customerToDelete = it }
                            )
                            HorizontalDivider() // 你使用的是 HorizontalDivider
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---
    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address, remark ->
                viewModel.addCustomer(name, phone, address.ifBlank { null }, remark.ifBlank { null })
                showAddDialog = false
            }
        )
    }

    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除客户 ${customer.name} 吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        customerToDelete = null
                    }
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("取消") }
            }
        )
    }

}

@Composable
fun CustomerItem(
    customer: Customer,
    onItemClick: (Customer) -> Unit, // 修改参数以接收点击事件
    onDeleteClick: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(customer) } // 使整个条目可点击
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,


        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = customer.name, style = MaterialTheme.typography.bodyLarge)
            customer.phone?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // 可以考虑在这里显示备注 customer.remark
            customer.remark?.takeIf { it.isNotBlank() }?.let {
                Text(text = "备注: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        IconButton(onClick = { onDeleteClick(customer) }) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "删除客户",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, remark: String) -> Unit // 这里参数目前没有包含备注
) {

    var nameState by remember { mutableStateOf(TextFieldValue("")) }
    var phoneState by remember { mutableStateOf(TextFieldValue("")) }
    var addressState by remember { mutableStateOf(TextFieldValue("")) }
    var remarkState by remember { mutableStateOf(TextFieldValue("")) }

    // 如果添加客户也需要备注，这里和 EditCustomerDialog 类似处理
    var nameError by remember { mutableStateOf<String?>(null) }    // 姓名错误信息
    var phoneError by remember { mutableStateOf<String?>(null) } // 新增 phoneError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新客户") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 姓名
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { newValue ->
                        nameState = newValue
                        nameError = if (newValue.text.isBlank()) "姓名不能为空" else null // 更新错误信息
                    },
                    label = { Text("客户姓名 *") },
                    isError = nameError != null, // 根据是否有错误信息来判断
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                nameError?.let { // 如果有错误信息，则显示
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 电话
                OutlinedTextField(
                    value = phoneState,
                    onValueChange = { newValue ->
                        phoneState = newValue
                        phoneError = if (newValue.text.isBlank()) "电话不能为空" else null // 更新错误信息
                    },
                    label = { Text("联系电话 *") }, // 标记为必填
                    isError = phoneError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                phoneError?.let { // 如果有错误信息，则显示
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 地址
                OutlinedTextField(
                    value = addressState,
                    onValueChange = { addressState = it },
                    label = { Text("地址") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 备注
                OutlinedTextField(
                    value = remarkState,
                    onValueChange = { remarkState = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val name = nameState.text
                    val phone = phoneState.text
                    val address = addressState.text
                    val  remark = remarkState.text
                    // 在点击确认时，再次设置错误状态（以防用户直接点击确认而不修改）
                    nameError = if (name.isBlank()) "姓名不能为空" else null
                    phoneError = if (phone.isBlank()) "电话不能为空" else null

                    // 只有当没有错误时才调用 onConfirm
                    if (nameError == null && phoneError == null) {
                        onConfirm(name, phone, address.ifBlank { "" }, remark.ifBlank { "" })
                    }
                }
            ) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// 编辑客户对话框的实现 (与我们之前讨论的一致)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var nameState by remember { mutableStateOf(TextFieldValue("")) }
    var phoneState by remember { mutableStateOf(TextFieldValue("")) }
    var addressState by remember { mutableStateOf(TextFieldValue("")) }
    var remarkState by remember { mutableStateOf(TextFieldValue("")) }

    var nameError by remember(customer.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑客户信息") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it; nameError = it.text.isBlank() },
                    label = { Text("客户姓名 *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) {
                    Text("姓名不能为空", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneState,
                    onValueChange = { phoneState = it },
                    label = { Text("联系电话") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = addressState,
                    onValueChange = { addressState = it },
                    label = { Text("地址") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remarkState,
                    onValueChange = { remarkState = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val name = nameState.text
                    val phone = phoneState.text
                    val address = addressState.text
                    val remark = remarkState.text

                    if (name.isNotBlank()) {
                        val updatedCustomer = customer.copy(
                            name = name,
                            phone = phone,
                            address = address.ifBlank { null },
                            remark = remark.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        onConfirm(updatedCustomer)
                    } else {
                        nameError = true
                    }
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}