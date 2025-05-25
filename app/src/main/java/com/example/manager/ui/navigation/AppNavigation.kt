package com.example.manager.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.manager.ui.auth.BossRegistrationScreen
import com.example.manager.ui.auth.LoginScreen
import com.example.manager.ui.customer.CustomerListScreen
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.viewmodel.NavigationEvent

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(), // 创建并记住 NavController
    authViewModel: AuthViewModel = hiltViewModel() // 获取 AuthViewModel
) {
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    val isInitialSetupNeeded by authViewModel.isInitialSetupNeeded.collectAsStateWithLifecycle()
    val currentUserSession by authViewModel.currentUserSessionFlow.collectAsStateWithLifecycle(initialValue = null)

    // 启动时的导航逻辑 (在 authViewModel.init 中已经设置了 navigationEvent)
    // 这个 LaunchedEffect 监听 authUiState.navigationEvent 的变化
    LaunchedEffect(key1 = authUiState.navigationEvent) {
        Log.d("AppNavigation", "Auth Navigation Event: ${authUiState.navigationEvent}, Current Route: ${navController.currentBackStackEntry?.destination?.route}")
        when (val event = authUiState.navigationEvent) {
            is NavigationEvent.GoToBossRegistration -> {
                navController.navigate(AppDestinations.BOSS_REGISTRATION_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true } // 清空回退栈
                    launchSingleTop = true
                }
                authViewModel.navigationEventConsumed()
            }
            is NavigationEvent.GoToLogin -> {
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                authViewModel.navigationEventConsumed()
            }
            is NavigationEvent.GoToMainApp -> {
                navController.navigate(AppDestinations.CUSTOMER_LIST_ROUTE) { // 假设客户列表是主应用入口
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                authViewModel.navigationEventConsumed()
            }
            NavigationEvent.Idle -> { /* Do nothing */ }
        }
    }

    // NavHost 定义了导航图
    NavHost(
        navController = navController,
        // 决定起始路由：
        // 如果 ViewModel 还在加载初始状态，可以显示一个加载屏或保持空白，等待 LaunchedEffect 导航
        // 或者，更健壮的方式是有一个明确的“加载中”或“闪屏”路由作为 startDestination
        // 这里我们简化，假设 authViewModel.init 很快会设置正确的 navigationEvent
        startDestination = determineStartDestination(isInitialSetupNeeded, currentUserSession?.isLoggedIn)
    ) {
        composable(AppDestinations.BOSS_REGISTRATION_ROUTE) {
            BossRegistrationScreen(
                viewModel = authViewModel, // 可以复用 AuthViewModel
                onNavigateToMainApp = {
                    navController.navigate(AppDestinations.CUSTOMER_LIST_ROUTE) {
                        popUpTo(AppDestinations.BOSS_REGISTRATION_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { // 通常老板注册成功后不会去登录，而是直接进主应用
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.BOSS_REGISTRATION_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                viewModel = authViewModel, // 可以复用 AuthViewModel
                onNavigateToMainApp = {
                    navController.navigate(AppDestinations.CUSTOMER_LIST_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                // onNavigateToRegistration = { navController.navigate(AppDestinations.BOSS_REGISTRATION_ROUTE) } // 如果需要从登录页去注册
            )
        }

        composable(AppDestinations.CUSTOMER_LIST_ROUTE) {
            // 这里 CustomerListScreen 应该获取它自己的 CustomerViewModel
            // AuthViewModel 的职责主要是认证和初始导航
            CustomerListScreen(
                // viewModel: CustomerViewModel = hiltViewModel() // CustomerListScreen 内部会获取
                // onLogout = { // 示例：如何从主应用触发登出并返回登录
                //    authViewModel.logout()
                // }
            )
            // 你可以在这里添加一个登出按钮，调用 authViewModel.logout()
            // 然后上面的 LaunchedEffect 会处理 GoToLogin 导航事件
        }

        // TODO: 未来可以添加一个加载/闪屏路由作为 startDestination
        // composable("loading_screen") { LoadingScreenComposable() }
    }
}

// 辅助函数，用于在 NavHost 初始化时决定起始路由
// 注意：这个函数的逻辑应该与 AuthViewModel 中 init 块的 checkInitialAppState 逻辑紧密配合，
// 避免 NavHost 刚初始化就因为 startDestination 不匹配而立即发生一次不必要的导航。
// 更稳妥的方式可能是让 startDestination 固定为一个 "splash" 或 "loading" 路由，
// 然后完全依赖 LaunchedEffect 和 ViewModel 的状态来驱动第一次实际的屏幕跳转。
@Composable
private fun determineStartDestination(
    isInitialSetupNeeded: Boolean,
    isLoggedIn: Boolean? // 来自 session
): String {
    // ViewModel 的 init 块会发送导航事件，LaunchedEffect 会处理
    // NavHost 的 startDestination 最好是一个固定的、简单的屏幕（比如加载屏）
    // 或者，如果 ViewModel 初始化非常快，可以尝试这样决定，但要小心竞争条件
    return when {
        isInitialSetupNeeded -> AppDestinations.BOSS_REGISTRATION_ROUTE
        isLoggedIn == true -> AppDestinations.CUSTOMER_LIST_ROUTE // 假设客户列表是主应用入口
        else -> AppDestinations.LOGIN_ROUTE
    }
    // 对于更复杂的启动逻辑，通常推荐一个专门的 "SplashScreen" 路由作为 startDestination,
    // 然后在该 SplashScreen 中观察 ViewModel 状态并执行导航。
    // 为了简化，我们暂时这样处理，但要意识到 ViewModel 的初始化和 NavHost 的构建可能存在时序问题。
}