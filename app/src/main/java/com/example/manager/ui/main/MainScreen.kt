package com.example.manager.ui.main


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.manager.ui.customer.CustomerListScreen // 我们已有的客户列表
import com.example.manager.ui.me.MeScreen
import com.example.manager.ui.navigation.BottomNavItem // 导入导航项定义
import com.example.manager.viewmodel.AuthViewModel

// import com.example.manager.ui.work.WorkScreen // 后续创建
// import com.example.manager.ui.me.MeScreen // 后续创建

// 创建主屏幕容器，用于包含底部导航栏和内容区域，这个compoosable将会是登录后用户看到的主要界面

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // 通常由 NavHost 处理 padding
@Composable
fun MainScreen(
    mainNavController: NavHostController, // 这是从 AppNavigation 传过来的，用于 MainScreen 内部的导航（如果需要）
    // 或者，底部导航直接控制内容切换，不需要嵌套 NavHostController
    authViewModel: AuthViewModel
) {
    val bottomNavItems = listOf(
        BottomNavItem.Work,
        BottomNavItem.Me
    )
    // 我们需要一个 NavController 来管理底部导航栏切换的内容
    // 这个 NavController 是 MainScreen 内部的，与 AppNavigation 的 navController 不同
    val bottomSheetNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar { // Material 3 的底部导航栏
                val navBackStackEntry by bottomSheetNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomSheetNavController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(bottomSheetNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding -> // 从 Scaffold 获取 padding
        // 内容区域，使用 NavHost 来根据底部导航切换不同的屏幕
        NavHost(
            navController = bottomSheetNavController,
            startDestination = BottomNavItem.Work.route, // 默认显示“工作”
            modifier = Modifier.padding(innerPadding) // 应用 Scaffold 的 padding
        ) {
            composable(BottomNavItem.Work.route) {
                // WorkScreen() // 旧的占位符或多入口版本
                CustomerListScreen(authViewModel = authViewModel) // **直接将客户列表作为“工作”页的初始内容**
            }
            composable(BottomNavItem.Me.route) {
                MeScreen(
                    authViewModel = authViewModel,     // <-- **将接收到的 AuthViewModel 传递下去**
                    mainAppNavController = mainNavController
                )
                // MeScreen(mainAppNavController = mainNavController) // MeScreen 暂时还是占位符或骨架
            }
            // 你可以将 CustomerListScreen 作为一个独立的路由目标，从 WorkScreen 导航过去
            // 或者将 CustomerListScreen 的内容直接嵌入 WorkScreen
            // 为简单起见，我们可以先让 WorkScreen 直接显示 CustomerListScreen
            composable("customer_list_for_work") { // 举例，如果 WorkScreen 导航到它
                CustomerListScreen(authViewModel = authViewModel)
            }
        }
    }
}
