package com.example.manager.ui.followup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFollowUpDialog(
    customerSearchQuery: String,
    onCustomerSearchQueryChanged: (String) -> Unit,
    customerSearchResults: List<Customer>,
    onDismiss: () -> Unit,
    onConfirm: (customerId: Long, notes: String) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var notes by remember { mutableStateOf("") }

    var customerError by remember { mutableStateOf<String?>(null) }
    var notesError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加跟进记录") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 客户选择器 (复用 CustomerSelector 的逻辑)
                CustomerSearchableDropdown(
                    searchQuery = customerSearchQuery,
                    onQueryChange = onCustomerSearchQueryChanged,
                    suggestions = customerSearchResults,
                    selectedCustomer = selectedCustomer,
                    onCustomerSelected = {
                        selectedCustomer = it
                        onCustomerSearchQueryChanged("") // 选择后清空
                    }
                )
                customerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(8.dp))

                // 跟进内容
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        notesError = if (it.isBlank()) "跟进内容不能为空" else null
                    },
                    label = { Text("跟进内容 *") },
                    isError = notesError != null,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                notesError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    customerError = if (selectedCustomer == null) "请选择一个客户" else null
                    notesError = if (notes.isBlank()) "跟进内容不能为空" else null
                    if (customerError == null && notesError == null) {
                        onConfirm(selectedCustomer!!.id, notes)
                    }
                }
            ) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// 建议将 CustomerSelector 提取为公共组件，这里暂时复制并简化
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerSearchableDropdown(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<Customer>,
    selectedCustomer: Customer?,
    onCustomerSelected: (Customer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    expanded = searchQuery.isNotBlank() && suggestions.isNotEmpty() && hasFocus

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {}) {
        OutlinedTextField(
            value = selectedCustomer?.name ?: searchQuery,
            onValueChange = {
                if (selectedCustomer != null) onCustomerSelected(Customer(id=-1, name="", phone="", storeId = 0)) // Clear selection
                onQueryChange(it)
            },
            label = { Text("搜索并选择客户 *") },
            readOnly = selectedCustomer != null,
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable).onFocusChanged { hasFocus = it.isFocused }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { hasFocus = false }) {
            suggestions.forEach { customer ->
                DropdownMenuItem(
                    text = { Text(customer.name) },
                    onClick = { onCustomerSelected(customer) }
                )
            }
        }
    }
}