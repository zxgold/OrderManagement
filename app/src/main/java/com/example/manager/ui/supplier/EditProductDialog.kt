package com.example.manager.ui.supplier // 或 components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product, // 接收要编辑的产品对象
    onDismiss: () -> Unit,
    onConfirm: (product: Product) -> Unit // 返回更新后的 Product 对象
) {
    // --- 使用 TextFieldValue 管理状态，并用 product 的初始值初始化 ---
    // remember(product.id) 确保当编辑不同的产品时，状态能正确重置
    var nameState by remember(product.id) { mutableStateOf(TextFieldValue(product.name)) }
    var modelState by remember(product.id) { mutableStateOf(TextFieldValue(product.model ?: "")) }
    var categoryState by remember(product.id) { mutableStateOf(TextFieldValue(product.category ?: "")) }
    var priceStringState by remember(product.id) { mutableStateOf(TextFieldValue(product.defaultPrice.toString())) }
    var specificationsState by remember(product.id) { mutableStateOf(TextFieldValue(product.specifications ?: "")) }
    var descriptionState by remember(product.id) { mutableStateOf(TextFieldValue(product.description ?: "")) }
    var isActive by remember(product.id) { mutableStateOf(product.isActive) }


    // --- 错误状态管理 ---
    var nameError by remember(product.id) { mutableStateOf<String?>(null) }
    var priceError by remember(product.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑产品") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 各个输入字段，与 AddProductDialog 类似，但 value 使用上面的状态变量
                OutlinedTextField(
                    value = nameState,
                    onValueChange = {
                        nameState = it
                        nameError = if (it.text.isBlank()) "产品名称不能为空" else null
                    },
                    label = { Text("产品名称 *") },
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = modelState,
                    onValueChange = { modelState = it },
                    label = { Text("产品型号") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceStringState,
                    onValueChange = {
                        priceStringState = it
                        priceError = if (it.text.toDoubleOrNull() == null || it.text.toDouble() < 0) "价格无效" else null
                    },
                    label = { Text("默认售价 *") },
                    isError = priceError != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                priceError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                // ... category, specifications, description 的 OutlinedTextField ...

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("产品是否激活", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceStringState.text.toDoubleOrNull()
                    nameError = if (nameState.text.isBlank()) "产品名称不能为空" else null
                    priceError = if (price == null || price < 0) "价格无效" else null

                    if (nameError == null && priceError == null) {
                        val updatedProduct = product.copy( // 使用 copy 保留 id 和 supplierId
                            name = nameState.text.trim(),
                            model = modelState.text.ifBlank { null },
                            category = categoryState.text.ifBlank { null },
                            defaultPrice = price!!,
                            specifications = specificationsState.text.ifBlank { null },
                            description = descriptionState.text.ifBlank { null },
                            isActive = isActive,
                            updatedAt = System.currentTimeMillis() // 更新时间戳
                        )
                        onConfirm(updatedProduct)
                    }
                }
            ) { Text("保存更改") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}