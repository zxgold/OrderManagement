package com.example.manager.ui.ledger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.enums.LedgerEntryType
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLedgerEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: LedgerEntryType, amount: Double, description: String, date: Long) -> Unit
) {
    var entryType by remember { mutableStateOf(LedgerEntryType.EXPENSE) } // 默认是支出
    var amountString by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    // TODO: 实现日期选择器，暂时使用当前时间
    val entryDate by remember { mutableStateOf(Date().time) }

    var amountError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记一笔账") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 类型选择 (收入/支出)
                SegmentedButtonRow(entryType) { newType -> entryType = newType }
                Spacer(modifier = Modifier.height(16.dp))

                // 金额
                OutlinedTextField(
                    value = amountString,
                    onValueChange = {
                        amountString = it
                        amountError = if (it.toDoubleOrNull() == null || it.toDouble() <= 0) "金额必须是正数" else null
                    },
                    label = { Text("金额 *") },
                    isError = amountError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                amountError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(8.dp))

                // 描述
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = if (it.isBlank()) "描述不能为空" else null
                    },
                    label = { Text("描述 *") },
                    isError = descriptionError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountString.toDoubleOrNull()
                    amountError = if (amount == null || amount <= 0) "金额必须是正数" else null
                    descriptionError = if (description.isBlank()) "描述不能为空" else null
                    if (amountError == null && descriptionError == null) {
                        onConfirm(entryType, amount!!, description, entryDate)
                    }
                }
            ) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonRow(
    selectedType: LedgerEntryType,
    onTypeSelected: (LedgerEntryType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedType == LedgerEntryType.EXPENSE,
            onClick = { onTypeSelected(LedgerEntryType.EXPENSE) },
            shape = MaterialTheme.shapes.medium
        ) { Text("支出") }
        SegmentedButton(
            selected = selectedType == LedgerEntryType.INCOME,
            onClick = { onTypeSelected(LedgerEntryType.INCOME) },
            shape = MaterialTheme.shapes.medium
        ) { Text("收入") }
    }
}