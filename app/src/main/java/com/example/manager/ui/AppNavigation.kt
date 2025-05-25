package com.example.manager.ui



import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.manager.ui.auth.BossRegistrationScreen // <-- 新增 Boss 注册屏幕导入
import com.example.manager.ui.auth.LoginScreen // <-- 修改 LoginScreen 导入路径 (如果需要)
import com.example.manager.ui.customer.CustomerListScreen
import com.example.manager.viewmodel.AppStartState // 导入 AppStartState
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.data.preferences.UserSession // 导入 UserSession

// 定义屏幕路由 (确保 BossRegistrationScreen 路由已添加)
object AppDestinations {
    const val LOADING_ROUTE = "loading" // 新增一个加载状态的路由名
    const val LOGIN_ROUTE = "login"
    const val BOSS_REGISTRATION_ROUTE = "boss_registration" // 新增
    const val CUSTOMER_LIST_ROUTE = "customer_list"
    // ... 其他路由
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val appStartState by authViewModel.appStartState.collectAsStateWithLifecycle()
    // 从 SessionManager 收集用户会话状态，初始值表示未知/未登录
    val userSession by authViewModel.userSessionFlow.collectAsState(
        initial = UserSession(isLoggedIn = false, staffId = null, staffRole = null) // 提供一个明确的初始值
    )

    // 根据 appStartState 和 userSession.isLoggedIn 动态确定起始路由
    // LaunchedEffect 用于在 appStartState 或 userSession.isLoggedIn 变化时执行一次导航
    // 这样可以确保 NavHost 的 startDestination 是在状态稳定后确定的
    LaunchedEffect(appStartState, userSession.isLoggedIn) {
        val destination = when (appStartState) {
            AppStartState.Unknown -> {
                // 如果 SessionManager 还在加载，保持在加载状态或不做任何事
                // 如果 userSession.isLoggedIn 已经为 true (来自缓存)，则直接跳主页
                if (userSession.isLoggedIn && appStartState != AppStartState.NeedsBossRegistration) {
                    AppDestinations.CUSTOMER_LIST_ROUTE
                } else {
                    // 否则等待 appStartState 确定
                    null // 暂时不导航，等待 appStartState 变为确定状态
                }
            }
            AppStartState.NeedsBossRegistration -> AppDestinations.BOSS_REGISTRATION_ROUTE
            AppStartState.CanLogin -> {
                if (userSession.isLoggedIn) {
                    AppDestinations.CUSTOMER_LIST_ROUTE
                } else {
                    AppDestinations.LOGIN_ROUTE
                }
            }
        }

        destination?.let {
            // 只有当目标路由确定且与当前路由不同时才执行导航
            // 并且在 NavHost 已经组合之后执行
            if (navController.currentDestination?.route != it) {
                navController.navigate(it) {
                    // 清除所有回退栈，使新目标成为唯一的屏幕
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }


    // NavHost 的 startDestination 可以是一个临时的加载路由，或者根据初始状态决定
    // 一个简单的方式是，如果 appStartState 还是 Unknown，则显示加载动画，否则构建 NavHost
    when (appStartState) {
        AppStartState.Unknown -> {
            // 初始加载状态，可以显示一个全局加载指示器
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                // Text("正在检查应用状态...") // 或者更友好的提示
            }
        }
        else -> {
            // 一旦 appStartState 确定，就构建 NavHost
            // startDestination 仍然重要，但实际的初始屏幕由 LaunchedEffect 控制
            // 可以将 startDestination 设为一个不会直接显示的占位路由，或 LOGIN_ROUTE
            val initialNavHostRoute = when (appStartState) {
                AppStartState.NeedsBossRegistration -> AppDestinations.BOSS_REGISTRATION_ROUTE
                else -> AppDestinations.LOGIN_ROUTE // 默认到登录，如果已登录会被 LaunchedEffect 导航走
            }

            NavHost(navController = navController, startDestination = initialNavHostRoute) {
                composable(AppDestinations.LOGIN_ROUTE) {
                    LoginScreen(
                        onLoginSuccess = { _, _ -> // staffId, staffRole
                            navController.navigate(AppDestinations.CUSTOMER_LIST_ROUTE) {
                                popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(AppDestinations.BOSS_REGISTRATION_ROUTE) {
                    BossRegistrationScreen( // 下一步创建这个屏幕
                        onRegistrationSuccess = {
                            // 老板注册成功后，也视为已登录，导航到客户列表
                            navController.navigate(AppDestinations.CUSTOMER_LIST_ROUTE) {
                                popUpTo(AppDestinations.BOSS_REGISTRATION_ROUTE) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(AppDestinations.CUSTOMER_LIST_ROUTE) {
                    CustomerListScreen(
                        // ... (如果需要登出等，可以传递 navController 或回调)
                        // onLogout = {
                        //    authViewModel.logout()
                        //    // AuthViewModel 中的 userSessionFlow 会更新，
                        //    // 上面的 LaunchedEffect 会自动处理导航到 LOGIN_ROUTE
                        // }
                    )
                }
                // 你可以添加一个临时的加载屏 Composable
                // composable(AppDestinations.LOADING_ROUTE) {
                //    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                //        CircularProgressIndicator()
                //        Text("加载中...")
                //    }
                // }
            }
        }
    }
}