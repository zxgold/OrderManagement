package com.example.manager.ui.main


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.manager.ui.customer.CustomerListScreen // 我们已有的客户列表
import com.example.manager.ui.me.MeScreen
import com.example.manager.ui.navigation.AppDestinations
import com.example.manager.ui.navigation.BottomNavItem // 导入导航项定义
import com.example.manager.ui.work.WorkScreen
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.ui.customer.CustomerDetailScreen
import com.example.manager.ui.order.OrderDetailScreen
import com.example.manager.ui.order.OrderListScreen
import com.example.manager.ui.order.AddEditOrderScreen
import com.example.manager.ui.supplier.SupplierProductScreen
import com.example.manager.ui.workorder.WorkOrderDetailScreen
import com.example.manager.ui.workorder.WorkOrderListScreen

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
            NavigationBar {
                val navBackStackEntry by bottomSheetNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen -> // screen 是 BottomNavItem.Work 或 BottomNavItem.Me
                    // screen.route 是底部标签的根路由，如 "work_tab_root"

                    // isSelected 的计算保持不变，用于视觉高亮
                    val isSelected = currentDestination?.hierarchy?.any { navDest ->
                        navDest.route == screen.route
                    } == true

                    Log.d("MainScreenSelected", "Tab: ${screen.label}, screen.route: ${screen.route}, currentDest.route: ${currentDestination?.route}, isSelected: $isSelected")



                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = isSelected, // <-- **确保这里使用的是正确的 isSelected 计算结果**
                        onClick = {
                            val targetRootRoute = screen.route // 被点击的底部导航项的根路由

                            Log.d("MainScreenOnClick", "--- Clicked ${screen.label} ---")
                            Log.d("MainScreenOnClick", "Target Root: $targetRootRoute, Current Actual Route: ${currentDestination?.route}")

                            // **核心简化逻辑：**
                            // 无论当前是否已选中，只要点击，就导航到该标签页的根路由，
                            // 并清空该标签页之前的回退栈，同时确保根路由是新的栈顶。
                            bottomSheetNavController.navigate(targetRootRoute) {
                                // 弹出到导航图的起始目的地，并保存其状态
                                popUpTo(bottomSheetNavController.graph.findStartDestination().id) {
                                    saveState = true // 保存根目的地的状态，以便在不同标签页间切换时恢复
                                }
                                // 确保目标路由在回退栈中是唯一的实例
                                launchSingleTop = true
                                // 当重新选择已选中的项目时，恢复其状态
                                // 或者，如果你希望每次点击都重新加载根屏幕，可以设为 false
                                restoreState = true // 如果你希望返回时看到 WorkScreen 的之前状态（比如滚动位置）
                                // 如果你希望每次都“全新”加载 WorkScreen，可以设为 false
                                // 或者，如果想确保总是回到“干净”的WorkScreen，可以这样做：
                                // popUpTo(targetRootRoute) { inclusive = true } // 弹出到并包括目标根，然后重新导航
                                // launchSingleTop = true
                            }
                            Log.i("MainScreenOnClick", "Action: Navigated to tab root: $targetRootRoute")

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
                WorkScreen(bottomNavController = bottomSheetNavController) // 旧的占位符或多入口版本

            }
            composable(BottomNavItem.Me.route) {
                MeScreen(
                    authViewModel = authViewModel,     // <-- **将接收到的 AuthViewModel 传递下去**
                    mainAppNavController = mainNavController
                )
                // MeScreen(mainAppNavController = mainNavController) // MeScreen 暂时还是占位符或骨架
            }

            composable(AppDestinations.CUSTOMER_LIST_ROUTE) {
                CustomerListScreen(authViewModel = authViewModel, navController = bottomSheetNavController)

            }

            // --- 新增：客户详情页的 composable ---
            // 与上面CustomerListScreen的区别在于这个可以点击进入单个客户的详情页
            composable(
                route = AppDestinations.CUSTOMER_DETAIL_ROUTE_TEMPLATE,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType }) // 定义参数类型
            ) { backStackEntry ->
                // 从 backStackEntry 中获取参数
                val customerId = backStackEntry.arguments?.getLong("customerId")
                if (customerId != null && customerId != -1L) { // 确保 customerId 有效
                    CustomerDetailScreen(
                        navController = bottomSheetNavController // 传递 NavController 以便详情页可以返回或导航到编辑
                    )
                } else {
                    // 处理 customerId 无效的情况，例如显示错误或导航回列表
                    Text("错误：无效的客户ID")
                    LaunchedEffect(Unit) { // 避免在重组时重复导航
                        // navController.popBackStack() // 或者导航到一个错误提示页
                    }
                }
            }

            // 导航至订单列表
            composable(AppDestinations.ORDER_LIST_ROUTE) {
                OrderListScreen(navController = bottomSheetNavController) // 传递 NavController
            }

            // 导航至订单详情页
            composable(
                route = AppDestinations.ORDER_DETAIL_ROUTE_TEMPLATE, // "order_detail_screen/{orderId}"
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId")
                if (orderId != null && orderId != -1L) {
                    OrderDetailScreen(
                        orderId = orderId,
                        navController = bottomSheetNavController // 传递 bottomSheetNavController
                    )
                } else {
                    Text("错误：无效的订单ID")
                    // navController.popBackStack() // 或者导航回列表
                }
            }

            // 订单编辑页
            composable(
                route = AppDestinations.ADD_EDIT_ORDER_ROUTE_TEMPLATE,
                arguments = listOf(navArgument("orderId") {
                    type = NavType.StringType // 可选参数最好用 String 类型，再转换为 Long
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val orderIdString = backStackEntry.arguments?.getString("orderId")
                val orderId = orderIdString?.toLongOrNull() // 如果是新增，orderId 会是 null
                AddEditOrderScreen(orderId = orderId, navController = bottomSheetNavController)
            }

            // MainScreen.kt -> NavHost
            composable(AppDestinations.SUPPLIER_PRODUCT_ROUTE) {
                SupplierProductScreen(navController = bottomSheetNavController) // 传递 NavController
            }

            composable(AppDestinations.WORK_ORDER_LIST_ROUTE) {
                WorkOrderListScreen(navController = bottomSheetNavController)
            }

            composable(
                route = AppDestinations.WORK_ORDER_DETAIL_ROUTE_TEMPLATE,
                arguments = listOf(navArgument(AppDestinations.WORK_ORDER_DETAIL_ARG_ID) {
                    type = NavType.LongType // 明确参数类型为 Long
                })
            ) { backStackEntry ->
                // 从 backStackEntry 的 arguments 中安全地获取 orderItemId
                val orderItemId = backStackEntry.arguments?.getLong(AppDestinations.WORK_ORDER_DETAIL_ARG_ID)
                if (orderItemId != null && orderItemId != -1L) {
                    WorkOrderDetailScreen(
                        // customerId = customerId, // 这里应该是 orderItemId
                        // 我们之前在 WorkOrderDetailViewModel 中已经设置了从 SavedStateHandle 获取
                        // 所以这里其实不需要显式传递 ID 给 Composable，除非你的 Composable 需要它
                        // 但为了清晰，我们传递 NavController
                        navController = bottomSheetNavController
                    )
                } else {
                    // 处理 ID 无效的情况
                    Text("错误：无效的工单ID。")
                }
            }

        }
    }
}
