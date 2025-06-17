package com.example.manager.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.enums.LedgerEntryType
import com.example.manager.ui.components.CustomerSearchableDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    availableOrders: List<Order>,
    // --- 新增搜索相关的参数 ---
    customerSearchQuery: String,
    onCustomerSearchQueryChanged: (String) -> Unit,
    customerSearchResults: List<Customer>,
    onDismiss: () -> Unit,
    // onConfirm 回调现在接收选中的 customer 对象
    onConfirm: (amount: Double, paymentMethod: String?, notes: String?, order: Order?, customer: Customer?) -> Unit
) {
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }

    var amountString by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("现金") }
    var notes by remember { mutableStateOf("") }

    var amountError by remember { mutableStateOf<String?>(null) }

    // 当订单被选择时，自动更新选中的客户
    // 这个逻辑需要调整，因为 availableCustomers 不再被传递
    // 我们可以让 ViewModel 在选中订单后，也更新选中的客户状态
    // 为了简化，我们暂时让用户在选中订单后，客户选择器显示客户名但仍可被清除
    val customerFromOrder = remember(selectedOrder) {
        // 这需要一个方法来从 availableCustomers 找到客户，或者 ViewModel 来处理
        // 暂时简化
        null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加回款记录") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 订单选择器
                OrderSelector(
                    orders = availableOrders,
                    selectedOrder = selectedOrder,
                    onOrderSelected = { selectedOrder = it }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 客户显示/选择器 (如果订单未选，则可独立选择)
                CustomerSearchableDropdown(
                    searchQuery = customerSearchQuery,
                    onSearchQueryChanged = onCustomerSearchQueryChanged,
                    searchResults = customerSearchResults,
                    selectedCustomer = selectedCustomer,
                    onCustomerSelected = { customer -> selectedCustomer = customer },
                    enabled = selectedOrder == null // 如果选了订单，则客户选择器禁用
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 金额
                OutlinedTextField(
                    value = amountString,
                    onValueChange = {
                        amountString = it
                        amountError = if (it.toDoubleOrNull() == null || it.toDouble() <= 0) "金额必须是正数" else null
                    },
                    label = { Text("回款金额 *") },
                    isError = amountError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                amountError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(8.dp))

                // 支付方式
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("支付方式") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 备注
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountString.toDoubleOrNull()
                    amountError = if (amount == null || amount <= 0) "金额必须是正数" else null
                    val isCustomerValid = selectedCustomer != null

                    if (amountError == null && isCustomerValid) {
                        onConfirm(amount!!, paymentMethod.ifBlank { null }, notes.ifBlank { null }, selectedOrder, selectedCustomer)
                    } else if (!isCustomerValid) {
                        // TODO: Show snackbar or error message for customer selection
                    }
                }
            ) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderSelector(
    orders: List<Order>,
    selectedOrder: Order?,
    onOrderSelected: (Order?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOrder?.orderNumber ?: "关联订单 (可选)",
            onValueChange = {}, readOnly = true,
            label = { Text("关联订单") },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // 添加一个“不关联”的选项
            DropdownMenuItem(text = { Text("不关联订单") }, onClick = {
                onOrderSelected(null)
                expanded = false
            })
            orders.forEach { order ->
                DropdownMenuItem(
                    text = { Text("${order.orderNumber} - 欠款: ¥...") }, // TODO: 计算并显示欠款
                    onClick = {
                        onOrderSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}

