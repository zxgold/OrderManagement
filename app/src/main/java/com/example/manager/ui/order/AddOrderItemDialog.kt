package com.example.manager.ui.order // 或 components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Product
import com.example.manager.viewmodel.TempOrderItem // 导入 TempOrderItem (如果需要编辑)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderItemDialog(
    availableProducts: List<Product>, // 从 ViewModel 获取的所有可用产品
    onDismiss: () -> Unit,
    // 返回选中的 Product，以及用户输入的数量和价格
    onConfirm: (product: Product, quantity: Int, price: Double, notes: String?) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityString by remember { mutableStateOf("1") }
    var priceString by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var productError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加产品到订单") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 产品选择下拉菜单
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "请选择产品 *",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("产品 *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        isError = productError != null
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableProducts.forEach { product ->
                            DropdownMenuItem(
                                text = { Text("${product.name} (${product.model ?: ""}) - ¥${product.defaultPrice}") },
                                onClick = {
                                    selectedProduct = product
                                    priceString = product.defaultPrice.toString() // 自动填充默认价格
                                    expanded = false
                                    productError = null // 清除错误
                                }
                            )
                        }
                    }
                }
                productError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                // 数量和价格输入
                Row {
                    OutlinedTextField(
                        value = quantityString,
                        onValueChange = {
                            quantityString = it
                            quantityError = if (it.toIntOrNull() == null || it.toInt() <= 0) "数量必须是正整数" else null
                        },
                        label = { Text("数量 *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        isError = quantityError != null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = priceString,
                        onValueChange = {
                            priceString = it
                            priceError = if (it.toDoubleOrNull() == null || it.toDouble() < 0) "价格无效" else null
                        },
                        label = { Text("成交单价 *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        isError = priceError != null
                    )
                }
                quantityError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                priceError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                // 备注
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("产品备注 (可选)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantity = quantityString.toIntOrNull()
                    val price = priceString.toDoubleOrNull()
                    productError = if (selectedProduct == null) "请选择一个产品" else null
                    quantityError = if (quantity == null || quantity <= 0) "数量必须是正整数" else null
                    priceError = if (price == null || price < 0) "价格无效" else null

                    if (productError == null && quantityError == null && priceError == null) {
                        onConfirm(selectedProduct!!, quantity!!, price!!, notes.ifBlank { null })
                    }
                }
            ) { Text("确认添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}