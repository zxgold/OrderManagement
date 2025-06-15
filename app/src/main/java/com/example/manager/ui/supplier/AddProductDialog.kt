package com.example.manager.ui.supplier // 或 components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Product
import com.example.manager.data.model.entity.Supplier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    forSupplier: Supplier, // 明确告知为哪个供应商添加产品
    onDismiss: () -> Unit,
    // onConfirm 返回一个不包含 id 和 supplierId 的 Product 对象，
    // ViewModel 会负责处理这些。
    onConfirm: (product: Product) -> Unit
) {
    // --- 修改：使用 TextFieldValue 管理状态 ---
    var nameState by remember { mutableStateOf(TextFieldValue("")) }
    var modelState by remember { mutableStateOf(TextFieldValue("")) }
    var categoryState by remember { mutableStateOf(TextFieldValue("")) }
    var priceStringState by remember { mutableStateOf(TextFieldValue("")) }
    var specificationsState by remember { mutableStateOf(TextFieldValue("")) }
    var descriptionState by remember { mutableStateOf(TextFieldValue("")) }

    // --- 错误状态管理 ---
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("为 “${forSupplier.name}” 添加新产品") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 产品名称 (必填)
                OutlinedTextField(
                    value = nameState,
                    onValueChange = {
                        nameState = it
                        nameError = if (it.text.isBlank()) "产品名称不能为空" else null
                    },
                    label = { Text("产品名称 *") },
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                // 型号 (可选)
                OutlinedTextField(
                    value = modelState,
                    onValueChange = { modelState = it },
                    label = { Text("产品型号") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 默认售价 (必填)
                OutlinedTextField(
                    value = priceStringState,
                    onValueChange = {
                        priceStringState = it
                        priceError = if (it.text.toDoubleOrNull() == null || it.text.toDouble() < 0) "价格无效" else null
                    },
                    label = { Text("默认售价 *") },
                    isError = priceError != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                )
                priceError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                // 分类 (可选)
                OutlinedTextField(
                    value = categoryState,
                    onValueChange = { categoryState = it },
                    label = { Text("产品分类") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 规格 (可选)
                OutlinedTextField(
                    value = specificationsState,
                    onValueChange = { specificationsState = it },
                    label = { Text("规格 (如颜色、尺寸)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 描述 (可选)
                OutlinedTextField(
                    value = descriptionState,
                    onValueChange = { descriptionState = it },
                    label = { Text("产品描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceStringState.text.toDoubleOrNull()
                    nameError = if (nameState.text.isBlank()) "产品名称不能为空" else null
                    priceError = if (price == null || price < 0) "价格无效" else null

                    if (nameError == null && priceError == null) {
                        val newProduct = Product(
                            // id 和 supplierId 会在 ViewModel 中设置
                            // 所以这里可以传 0L
                            id = 0L,
                            supplierId = 0L,
                            name = nameState.text.trim(),
                            model = modelState.text.ifBlank { null },
                            category = categoryState.text.ifBlank { null },
                            defaultPrice = price!!, // 已经校验过非 null
                            specifications = specificationsState.text.ifBlank { null },
                            description = descriptionState.text.ifBlank { null },
                            isActive = true
                        )
                        onConfirm(newProduct)
                    }
                }
            ) { Text("确认添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}