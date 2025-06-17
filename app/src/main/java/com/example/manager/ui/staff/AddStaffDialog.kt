package com.example.manager.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.manager.data.model.enums.StaffRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, username: String, password: String, role: StaffRole) -> Unit
) {
    var nameState by remember { mutableStateOf(TextFieldValue("")) }
    var usernameState by remember { mutableStateOf(TextFieldValue("")) }
    var passwordState by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPasswordState by remember { mutableStateOf(TextFieldValue("")) }

    // --- 角色选择的状态 ---
    val rolesToShow = StaffRole.values().filter { it != StaffRole.BOSS } // 不允许在这里创建老板
    var selectedRole by remember { mutableStateOf(rolesToShow.first()) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    // ... (错误状态管理) ...

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新员工") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // ... (姓名、用户名的 OutlinedTextField) ...
                OutlinedTextField(value = nameState, onValueChange = { nameState = it }, label = { Text("员工姓名 *") })
                OutlinedTextField(value = usernameState, onValueChange = { usernameState = it }, label = { Text("登录用户名 *") })
                OutlinedTextField(
                    value = passwordState,
                    onValueChange = { passwordState = it },
                    label = { Text("初始密码 *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                OutlinedTextField(
                    value = confirmPasswordState,
                    onValueChange = { confirmPasswordState = it },
                    label = { Text("确认密码 *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // --- 角色选择下拉菜单 ---
                ExposedDropdownMenuBox(
                    expanded = roleMenuExpanded,
                    onExpandedChange = { roleMenuExpanded = !roleMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("角色 *") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        rolesToShow.forEach { role ->
                            // TODO: 后期想把这里的下拉列表显示在ui界面上的英文改成中文
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRole = role
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // TODO: 添加完整的校验
                    if (nameState.text.isNotBlank() && usernameState.text.isNotBlank() && passwordState.text.isNotBlank() && passwordState.text == confirmPasswordState.text) {
                        onConfirm(nameState.text, usernameState.text, passwordState.text, selectedRole)
                    } else {
                        // TODO: 显示错误提示
                    }
                }
            ) { Text("确认添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}