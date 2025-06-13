package com.example.manager.ui.me

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.preferences.UserSession
import com.example.manager.ui.navigation.AppScreenRoutes // 用于导航
import com.example.manager.viewmodel.AuthViewModel

@Composable
fun MeScreen(
    // authViewModel: AuthViewModel = hiltViewModel(),
    authViewModel: AuthViewModel,
    // 需要 AppNavigation 的 NavController 来执行全局导航，例如导航到员工管理屏幕
    mainAppNavController: NavController
) {
    val currentSession by authViewModel.currentUserSessionFlow.collectAsStateWithLifecycle(
        initialValue = UserSession(false, null, null, null, null, null, null)
    )
    val currentRole = currentSession.staffRole
    val staffName = currentSession.staffName ?: "用户"
    val storeName = currentSession.storeName ?: "未知店铺"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // verticalArrangement = Arrangement.Center // 可以根据内容调整
    ) {
        Text("我的账户", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("你好, $staffName!", style = MaterialTheme.typography.titleMedium)
        Text("店铺: $storeName", style = MaterialTheme.typography.bodyLarge)
        Text("角色: ${currentRole?.name ?: "未知"}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        if (currentRole == StaffRole.BOSS) {
            Button(
                onClick = {
                    Log.d("MeScreen", "员工管理按钮点击")
                    // TODO: mainAppNavController.navigate("staff_management_route")
                    // 需要在 AppNavigation 中定义 staff_management_route
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("员工管理 (TODO)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    Log.d("MeScreen", "注销店铺按钮点击")
                    // TODO: 实现注销店铺逻辑 (需要非常谨慎的操作)
                    // 1. 确认对话框
                    // 2. ViewModel 调用 Repository/DAO 删除所有与该店铺相关的数据
                    // 3. 清除当前老板会话，导航到登录或注册
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("注销店铺 (TODO - 危险操作!)")
            }
        } else { // 普通员工
            Button(
                onClick = { /* TODO: Navigate to Profile Edit or Change Password */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("修改密码 (TODO)")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Log.d("MeScreen", "登出按钮点击")
                authViewModel.logout() // 登出后，AppNavigation 会处理导航到登录页
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("登出")
        }
    }
}