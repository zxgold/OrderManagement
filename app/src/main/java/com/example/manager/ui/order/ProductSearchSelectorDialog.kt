package com.example.manager.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchSelectorDialog(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    searchResults: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (product: Product, quantity: Int, price: Double, notes: String?, isCustomized: Boolean) -> Unit
) {
    // --- 对话框内部状态 ---
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityString by remember { mutableStateOf("1") }
    var priceString by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isCustomized by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }
    expanded = searchQuery.isNotBlank() && searchResults.isNotEmpty() && hasFocus

    // --- 错误状态 ---
    var productError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加产品到订单") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // --- 响应式产品搜索框 ---
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { /* 由焦点和搜索词驱动 */ }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: searchQuery,
                        onValueChange = {
                            if (selectedProduct != null) selectedProduct = null // 清除选中，允许重新搜索
                            onSearchQueryChanged(it)
                        },
                        readOnly = selectedProduct != null,
                        label = { Text("搜索并选择产品 *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .onFocusChanged { hasFocus = it.isFocused },
                        isError = productError != null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { hasFocus = false }) {
                        searchResults.forEach { product ->
                            DropdownMenuItem(
                                text = { Text("${product.name} (¥${product.defaultPrice})") },
                                onClick = {
                                    selectedProduct = product
                                    priceString = product.defaultPrice.toString()
                                    expanded = false
                                    productError = null
                                    onSearchQueryChanged("") // 选择后清空搜索词
                                }
                            )
                        }
                    }
                }
                productError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(8.dp))

                // --- 数量、价格、备注、是否定制 ---
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantityString, onValueChange = { quantityString = it }, label = {Text("数量*")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = quantityError != null, enabled = !isCustomized)
                    OutlinedTextField(value = priceString, onValueChange = { priceString = it }, label = {Text("单价*")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = priceError != null)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCustomized, onCheckedChange = {
                        isCustomized = it
                        if(it) quantityString = "1" // 定制品数量强制为1
                    })
                    Text("是否为定制品")
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 校验逻辑
                    val quantity = quantityString.toIntOrNull()
                    val price = priceString.toDoubleOrNull()
                    productError = if (selectedProduct == null) "请选择一个产品" else null
                    quantityError = if (quantity == null || quantity <= 0) "数量必须是正整数" else null
                    priceError = if (price == null || price < 0) "价格无效" else null

                    if (productError == null && quantityError == null && priceError == null) {
                        onConfirm(selectedProduct!!, quantity!!, price!!, notes.ifBlank { null }, isCustomized)
                    }
                }
            ) { Text("确认添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}