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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        contactPerson: String?,
        phone: String?,
        address: String?,
        remark: String?
    ) -> Unit
) {
    // --- 修改：使用 TextFieldValue 管理状态 ---
    var nameState by remember { mutableStateOf(TextFieldValue("")) }
    var contactPersonState by remember { mutableStateOf(TextFieldValue("")) }
    var phoneState by remember { mutableStateOf(TextFieldValue("")) }
    var addressState by remember { mutableStateOf(TextFieldValue("")) }
    var remarkState by remember { mutableStateOf(TextFieldValue("")) }

    // 用于UI校验
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新供应商") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 供应商名称 (必填)
                OutlinedTextField(
                    value = nameState,
                    onValueChange = {
                        nameState = it
                        nameError = if (it.text.isBlank()) "供应商名称不能为空" else null
                    },
                    label = { Text("供应商名称 *") },
                    isError = nameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(modifier = Modifier.height(8.dp))

                // 联系人 (可选)
                OutlinedTextField(
                    value = contactPersonState,
                    onValueChange = { contactPersonState = it },
                    label = { Text("联系人") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 联系电话 (可选)
                OutlinedTextField(
                    value = phoneState,
                    onValueChange = { phoneState = it },
                    label = { Text("联系电话") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 地址 (可选)
                OutlinedTextField(
                    value = addressState,
                    onValueChange = { addressState = it },
                    label = { Text("地址") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 备注 (可选)
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
                    // 提交前再次校验
                    nameError = if (nameState.text.isBlank()) "供应商名称不能为空" else null
                    if (nameError == null) {
                        onConfirm(
                            nameState.text.trim(),
                            contactPersonState.text.ifBlank { null },
                            phoneState.text.ifBlank { null },
                            addressState.text.ifBlank { null },
                            remarkState.text.ifBlank { null }
                        )
                    }
                }
            ) { Text("确认添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}