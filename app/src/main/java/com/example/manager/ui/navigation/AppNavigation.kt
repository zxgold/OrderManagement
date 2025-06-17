package com.example.manager.ui.navigation // 你的包名

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
import com.example.manager.ui.auth.RegistrationScreen
import com.example.manager.ui.auth.LoginScreen
import com.example.manager.ui.customer.CustomerListScreen
import com.example.manager.ui.main.MainScreen
import com.example.manager.ui.staff.StaffManagementScreen
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.viewmodel.NavigationEvent // 确保导入这个




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
            is NavigationEvent.GoToRegistration -> {
                if (currentRoute != AppDestinations.REGISTRATION_ROUTE) {
                    navController.navigate(AppDestinations.REGISTRATION_ROUTE) {
                        popUpTo(0) { inclusive = true } // 清空整个回退栈
                        launchSingleTop = true
                    }
                    authViewModel.navigationEventConsumed()
                }
            }
            is NavigationEvent.GoToLogin -> {
                if (currentRoute != AppDestinations.LOGIN_ROUTE) {
                    Log.i("AppNavigation", "GoToLogin event received. Current route: $currentRoute. Navigating to Login.")
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true } // 清空整个回退栈
                        launchSingleTop = true
                    }
                    authViewModel.navigationEventConsumed()
                    Log.i("AppNavigation", "GoToLogin: Navigation event consumed AFTER navigate.")
                } else {
                    Log.i("AppNavigation", "GoToLogin event received, but already on Login screen. Consuming event.")
                    // 即使已经在登录页，也应该消耗事件，以防万一。
                    // 特别是如果这个事件是由于 ViewModel 初始化时就发出的（比如 checkInitialAppState）
                    // 并且应用启动时直接就是登录页。
                    authViewModel.navigationEventConsumed()
                }
            }
            is NavigationEvent.GoToMainApp -> {
                if (currentRoute != AppDestinations.MAIN_APP_HOST_ROUTE) {
                    navController.navigate(AppDestinations.MAIN_APP_HOST_ROUTE) {
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
        startDestination = AppDestinations.LOADING_ROUTE // 始终从加载屏幕开始
    ) {
        composable(AppDestinations.LOADING_ROUTE) {
            // 一个简单的加载屏幕
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("应用加载中...", modifier = Modifier.padding(top = 16.dp))
            }
            // ViewModel 的 init 块会异步运行 checkInitialAppState()
            // 并最终通过 authUiState.navigationEvent 更新导航目标。
            // 上面的 LaunchedEffect 会捕获这个事件并执行导航。
        }

        composable(AppDestinations.REGISTRATION_ROUTE) {
            RegistrationScreen(
                viewModel = authViewModel, // 传递 AuthViewModel
                onNavigateToMainApp = {
                    // 这个回调现在由 ViewModel 的 navigationEvent 驱动，
                    // 所以这里不需要直接调用 navController.navigate
                    // ViewModel 在注册成功后会发出 GoToMainApp 事件
                    Log.d("AppNav/BossReg", "onNavigateToMainApp called (event should be handled by AppNavigation)")
                },
                onNavigateToLogin = {
                    Log.d("AppNav/BossReg", "onNavigateToLogin called (event should be handled by AppNavigation or direct nav)")
                }
            )
        }

        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                viewModel = authViewModel, // ViewModel 从 AppNavigation 传递，保证是同一个实例
                onNavigateToMainApp = {
                    // 这个回调通常由 ViewModel 的 NavigationEvent 驱动，LoginScreen 本身不直接导航
                    Log.d("AppNav/LoginScreen", "onNavigateToMainApp called (event should be handled by AppNavigation)")
                },
                onNavigateToRegistration = {
                    Log.d("AppNav/LoginScreen", "onNavigateToRegistration requested, navigating to REGISTRATION_ROUTE") // 已更新为 REGISTRATION_ROUTE
                    // 确保导航到正确的注册路由
                    if (navController.currentDestination?.route != AppDestinations.REGISTRATION_ROUTE) {
                        navController.navigate(AppDestinations.REGISTRATION_ROUTE) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(AppDestinations.MAIN_APP_HOST_ROUTE) { // 新增 MainScreen 的路由
            MainScreen(mainNavController = navController, authViewModel = authViewModel) // 传递上层 navController
        }

        // 新增员工管理屏幕的路由
        composable(AppDestinations.STAFF_MANAGEMENT_ROUTE) {
            StaffManagementScreen(navController = navController)
        }


    }
}