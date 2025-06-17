package com.example.manager.ui.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.enums.StaffRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordDialog(
    staffName: String,
    onDismiss: () -> Unit,
    onConfirm: (newPassword: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("为 “$staffName” 重置密码") },
        text = {
            Column {
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("新密码 (至少6位)") }, visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("确认新密码") }, visualTransformation = PasswordVisualTransformation())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword.length >= 6 && newPassword == confirmPassword) {
                        onConfirm(newPassword)
                    } else {
                        // TODO: 显示错误
                    }
                }
            ) { Text("确认重置") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}