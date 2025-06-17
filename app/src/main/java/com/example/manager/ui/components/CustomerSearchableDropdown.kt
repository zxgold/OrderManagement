package com.example.manager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Customer

/**
 * 一个可复用的、支持响应式搜索的客户选择器下拉菜单。
 *
 * @param searchQuery 当前的搜索查询词。
 * @param onSearchQueryChanged 当搜索查询词变化时的回调。
 * @param searchResults 根据查询词搜索到的客户列表。
 * @param selectedCustomer 当前已选中的客户，如果为 null 则未选中。
 * @param onCustomerSelected 当一个客户被选择或清除时的回调。
 * @param label 输入框的标签文本。
 * @param modifier Modifier for the entire component.
 * @param enabled 是否启用该组件。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSearchableDropdown(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    searchResults: List<Customer>,
    selectedCustomer: Customer?,
    onCustomerSelected: (Customer?) -> Unit, // **改为接收可空的 Customer，null 表示清除**
    label: String = "搜索并选择客户 *",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    // 只有当有搜索词、有搜索结果、并且输入框有焦点时，才展开菜单
    expanded = searchQuery.isNotBlank() && searchResults.isNotEmpty() && hasFocus

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { /* 由焦点和搜索词驱动 */ }
        ) {
            OutlinedTextField(
                value = selectedCustomer?.name ?: searchQuery,
                onValueChange = {
                    // 当用户开始输入时，如果之前有选中的客户，需要清除它
                    if (selectedCustomer != null) {
                        onCustomerSelected(null) // **调用回调以清除选中状态**
                    }
                    onSearchQueryChanged(it)
                },
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                    .onFocusChanged { hasFocus = it.isFocused },
                readOnly = selectedCustomer != null,
                enabled = enabled, // **使用传入的 enabled 参数**
                trailingIcon = {
                    if (selectedCustomer != null) {
                        IconButton(onClick = { onCustomerSelected(null) }) { // **清除选择**
                            Icon(Icons.Filled.Clear, contentDescription = "清除所选客户")
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { hasFocus = false } // 失去焦点时收起菜单
            ) {
                searchResults.forEach { customer ->
                    DropdownMenuItem(
                        text = { Text(text = "${customer.name} - ${customer.phone}") },
                        onClick = {
                            onCustomerSelected(customer) // **选择一个客户**
                            hasFocus = false // 选择后让输入框失去焦点
                        }
                    )
                }
            }
        }
    }
}