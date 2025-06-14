package com.example.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle // 用于 "我的"
import androidx.compose.material.icons.filled.Work // 用于 "工作"
import androidx.compose.ui.graphics.vector.ImageVector


// 定义底部导航项数据
// 我们需要一个方式来表示底部导航栏的每个项目。使用 sealed class 或 enum class 是个好方法
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Work : BottomNavItem(route = "work_screen", icon = Icons.Filled.Work, label = "工作")
    object Me : BottomNavItem(route = "me_screen", icon = Icons.Filled.AccountCircle, label = "我的") // 使用 AccountCircle 作为示例
}

// 确保导入了 androidx.compose.material.icons.filled.Work 和 AccountCircle
// 如果没有 Icons.Filled.Work，可以暂时用其他图标替代，比如 Icons.Filled.Home
