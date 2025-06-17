package com.example.manager.ui.me

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit // 用于修改密码
import androidx.compose.material.icons.filled.GroupAdd // 用于员工管理
import androidx.compose.material.icons.filled.Storefront // 可以用于店铺设置或注销
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.preferences.UserSession
import com.example.manager.ui.navigation.AppDestinations
import com.example.manager.viewmodel.AuthViewModel

@Composable
fun MeScreen(
    authViewModel: AuthViewModel = hiltViewModel(), // 从 MainScreen 传递过来
    mainAppNavController: NavController // AppNavigation 的 NavController，用于全局导航
) {
    val currentSession by authViewModel.currentUserSessionFlow.collectAsStateWithLifecycle(
        initialValue = UserSession(false, null, null, null, null, null, null)
    )
    val currentRole = currentSession.staffRole
    val staffName = currentSession.staffName ?: "用户"
    val storeName = currentSession.storeName ?: "未知店铺"
    val username = currentSession.username ?: "未知用户名"

    // 用于“注销店铺”的确认对话框状态
    var showDeleteStoreConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 使整个屏幕可滚动
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("我的账户", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow("你好:", staffName)
                ProfileInfoRow("用户名:", username)
                ProfileInfoRow("所属店铺:", storeName)
                ProfileInfoRow("我的角色:", currentRole?.name ?: "未知")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // 根据角色显示不同的操作选项
        if (currentRole == StaffRole.BOSS) {
            MeScreenActionItem(
                icon = Icons.Filled.GroupAdd,
                text = "员工管理",
                onClick = {
                    Log.d("MeScreen", "员工管理 点击")
                    mainAppNavController.navigate(AppDestinations.STAFF_MANAGEMENT_ROUTE) // 使用上层 NavController
                    // 需要在 AppNavigation 中定义 staff_management_route
                    // 并创建一个 StaffManagementScreen
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            MeScreenActionItem(
                icon = Icons.Filled.Storefront, // 或者更危险的图标如 DeleteForever
                text = "注销店铺 (危险!)",
                isDestructiveAction = true, // 可以用来改变按钮颜色等
                onClick = {
                    Log.d("MeScreen", "注销店铺 点击")
                    showDeleteStoreConfirmDialog = true // 显示确认对话框
                }
            )
        } else if (currentRole != null) { // 普通员工或其他非老板角色
            MeScreenActionItem(
                icon = Icons.Filled.Edit,
                text = "修改密码 (TODO)",
                onClick = {
                    Log.d("MeScreen", "修改密码 点击")
                    // TODO: 导航到修改密码界面
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // 将登出按钮推到底部

        Button(
            onClick = {
                Log.d("MeScreen", "登出按钮点击")
                authViewModel.logout() // 登出后，AppNavigation 会处理导航到登录页
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // 登出用醒目颜色
        ) {
            Text("登出")
        }
    }

    // 注销店铺确认对话框
    if (showDeleteStoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteStoreConfirmDialog = false },
            title = { Text("⚠️ 确认注销店铺？") },
            text = { Text("此操作将永久删除店铺“${storeName}”及其所有相关数据（包括员工、客户、订单等）！此操作无法撤销，请谨慎操作！") },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("MeScreen", "确认注销店铺: $storeName")
                        // TODO: 实现 ViewModel 中的注销店铺逻辑
                        // 1. ViewModel 调用 Repository 删除 Store 及所有关联数据 (需要级联或手动删除)
                        // 2. 操作完成后，调用 authViewModel.logout() 或直接导航到注册/登录
                        showDeleteStoreConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("确认注销") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteStoreConfirmDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.width(100.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun MeScreenActionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructiveAction: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = if (isDestructiveAction) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        else ButtonDefaults.buttonColors()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text)
        }
    }
}