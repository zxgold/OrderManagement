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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long, // 从导航参数接收
    navController: NavController, // 用于返回或导航到编辑
    viewModel: CustomerDetailViewModel = hiltViewModel(), // 获取 CustomerDetailViewModel
    // 我们可能还需要 CustomerViewModel 来触发已有的编辑对话框
    // 或者 CustomerDetailViewModel 自己处理编辑状态和逻辑
    customerListViewModel: CustomerViewModel = hiltViewModel() // 获取 CustomerViewModel 以复用其编辑逻辑和对话框
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editingCustomerForDialog by customerListViewModel.editingCustomer.collectAsStateWithLifecycle() // 观察用于编辑对话框的状态

    // 当 CustomerDetailViewModel 加载完客户数据后，如果我们要复用 CustomerListViewModel 的编辑对话框，
    // 需要一种方式将加载到的客户信息传递给 CustomerListViewModel 来触发编辑。
    // 或者，CustomerDetailScreen 自己弹出一个编辑对话框。

    // 为了复用 EditCustomerDialog 和 CustomerViewModel 中的编辑逻辑：
    // 当用户点击编辑按钮时，我们调用 customerListViewModel.startEditingCustomer(customerIdFromDetail)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.customer?.name ?: "客户详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // 触发 CustomerListViewModel 中的编辑流程
                        // 确保 CustomerDetailViewModel 中的 customer 对象已加载
                        uiState.customer?.let { customerToEdit ->
                            Log.d("CustomerDetailScreen", "Edit button clicked for: ${customerToEdit.name}")
                            customerListViewModel.startEditingCustomer(customerToEdit.id)
                        }
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑客户")
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
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("错误: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.customer != null -> {
                    val customer = uiState.customer!!
                    CustomerInfoSection("基本信息") {
                        InfoRow("姓名:", customer.name)
                        InfoRow("电话:", customer.phone ?: "未提供") // phone 现在是非空
                        InfoRow("地址:", customer.address ?: "未提供")
                        InfoRow("备注:", customer.remark ?: "无")
                        InfoRow("店铺ID:", customer.storeId.toString()) // 显示店铺ID
                        InfoRow("创建时间:", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(customer.createdAt)))
                        InfoRow("更新时间:", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(customer.updatedAt)))
                    }
                    // TODO: 未来在这里显示与此客户相关的订单列表、跟进记录等
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("未找到客户信息。")
                    }
                }
            }
        }
    }

    // 复用 CustomerListScreen 中的编辑对话框逻辑
    editingCustomerForDialog?.let { customerToActuallyEdit ->
        // 确保这个 customerToActuallyEdit 是从 CustomerDetailViewModel 加载的那个
        // 或者 EditCustomerDialog 直接接收一个 Customer 对象
        if (customerToActuallyEdit.id == uiState.customer?.id) { // 确保是当前详情页的客户
            EditCustomerDialog(
                customer = customerToActuallyEdit,
                onDismiss = { customerListViewModel.doneEditingCustomer() },
                onConfirm = { updatedCustomer ->
                    customerListViewModel.updateCustomer(updatedCustomer)
                    // 编辑成功后，CustomerDetailViewModel 需要重新加载数据以显示最新信息
                    // 或者，如果 CustomerListViewModel 的 loadCustomers 会刷新列表，
                    // 并且 CustomerDetailScreen 在返回后会重新加载，那么这里可能不需要额外操作。
                    // 最好的方式是让 CustomerDetailViewModel 也能观察到数据变化或被通知刷新。
                    // 简单起见，编辑成功后，对话框关闭，用户仍在详情页，可以手动刷新或返回。
                    // 或者，updateCustomer 成功后，让 CustomerDetailViewModel 也刷新一下。
                    // viewModel.loadCustomerDetails() // 可以考虑在 onConfirm 后调用
                }
            )
        }
    }
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