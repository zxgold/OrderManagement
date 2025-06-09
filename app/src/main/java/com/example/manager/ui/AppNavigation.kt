package com.example.manager.ui // 你的包名

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.manager.viewmodel.NavigationEvent // 确保导入这个

// 定义屏幕路由 (可以放在单独的文件 AppDestinations.kt 中)
object AppScreenRoutes { // 改个名字以区分之前的 AppDestinations
    const val LOADING_SCREEN_ROUTE = "loading_screen" // 新增加载屏幕路由
    const val LOGIN_ROUTE = "login"
    const val BOSS_REGISTRATION_ROUTE = "boss_registration"
    const val CUSTOMER_LIST_ROUTE = "customer_list" // 假设这是主应用入口
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    // isInitialSetupNeeded 和 currentUserSessionFlow 主要由 ViewModel 内部用于决定 navigationEvent

    // 这个 LaunchedEffect 负责处理 ViewModel 发出的所有导航事件
    LaunchedEffect(key1 = authUiState.navigationEvent, key2 = navController) {
        val event = authUiState.navigationEvent
        Log.d("AppNavigation", "Navigation Event received: $event, CurrentDest: ${navController.currentDestination?.route}")

        val currentRoute = navController.currentDestination?.route

        when (event) {
            is NavigationEvent.GoToBossRegistration -> {
                if (currentRoute != AppScreenRoutes.BOSS_REGISTRATION_ROUTE) {
                    navController.navigate(AppScreenRoutes.BOSS_REGISTRATION_ROUTE) {
                        popUpTo(0) { inclusive = true } // 清空整个回退栈
                        launchSingleTop = true
                    }
                    authViewModel.navigationEventConsumed()
                }
            }
            is NavigationEvent.GoToLogin -> {
                if (currentRoute != AppScreenRoutes.LOGIN_ROUTE) {
                    navController.navigate(AppScreenRoutes.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.navigationEventConsumed()
                }
            }
            is NavigationEvent.GoToMainApp -> {
                if (currentRoute != AppScreenRoutes.CUSTOMER_LIST_ROUTE) {
                    navController.navigate(AppScreenRoutes.CUSTOMER_LIST_ROUTE) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.navigationEventConsumed()
                }
            }
            NavigationEvent.Idle -> {
                // 当 ViewModel 初始化完成且 navigationEvent 仍为 Idle 时，
                // 这通常意味着 ViewModel 的 init 块已经执行完毕，
                // 但可能没有发出新的导航指令（比如，如果初始状态就是应该停留在加载屏）
                // 或者，这意味着初始导航已由之前的事件处理。
                // 我们主要依赖 ViewModel 的 init 块发出第一个明确的导航事件。
                // 如果ViewModel的init块没有发出明确指令，且startDestination是LOADING_SCREEN_ROUTE
                // 那么应用会停留在加载屏。
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppScreenRoutes.LOADING_SCREEN_ROUTE // 始终从加载屏幕开始
    ) {
        composable(AppScreenRoutes.LOADING_SCREEN_ROUTE) {
            // 一个简单的加载屏幕
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("应用加载中...", modifier = Modifier.padding(top = 16.dp))
            }
            // ViewModel 的 init 块会异步运行 checkInitialAppState()
            // 并最终通过 authUiState.navigationEvent 更新导航目标。
            // 上面的 LaunchedEffect 会捕获这个事件并执行导航。
        }

        composable(AppScreenRoutes.BOSS_REGISTRATION_ROUTE) {
            BossRegistrationScreen(
                viewModel = authViewModel, // 传递 AuthViewModel
                onNavigateToMainApp = {
                    // 这个回调现在由 ViewModel 的 navigationEvent 驱动，
                    // 所以这里不需要直接调用 navController.navigate
                    // ViewModel 在注册成功后会发出 GoToMainApp 事件
                    Log.d("BossRegScreen", "onNavigateToMainApp called (event should be handled by AppNavigation)")
                },
                onNavigateToLogin = {
                    Log.d("BossRegScreen", "onNavigateToLogin called (event should be handled by AppNavigation)")
                }
            )
        }

        composable(AppScreenRoutes.LOGIN_ROUTE) {
            LoginScreen(
                viewModel = authViewModel, // 传递 AuthViewModel
                onNavigateToMainApp = {
                    Log.d("LoginScreen", "onNavigateToMainApp called (event should be handled by AppNavigation)")
                }
            )
        }

        composable(AppScreenRoutes.CUSTOMER_LIST_ROUTE) {
            CustomerListScreen(
                // 这里 CustomerListScreen 会通过 hiltViewModel() 获取它自己的 CustomerViewModel
                // 如果需要登出功能，可以考虑从这里调用 authViewModel.logout()
            )
            // 示例：添加一个登出按钮
            // Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            //     Button(onClick = { authViewModel.logout() }) { Text("登出") }
            // }
        }
    }
}