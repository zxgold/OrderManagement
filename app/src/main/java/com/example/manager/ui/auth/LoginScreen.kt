package com.example.manager.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.ui.theme.ManagerTheme // 确保这是你的主题
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.viewmodel.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToMainApp: () -> Unit, // 登录成功后导航到主应用
    // onNavigateToRegistration: () -> Unit // 如果需要从登录页跳转到注册（这里我们场景是老板注册优先）
) {
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // 处理导航事件
    LaunchedEffect(key1 = authUiState.navigationEvent) {
        Log.d("LoginScreen", "Navigation Event observed: ${authUiState.navigationEvent}")
        if (authUiState.navigationEvent is NavigationEvent.GoToMainApp) {
            onNavigateToMainApp()
            viewModel.navigationEventConsumed() // 消耗事件
        }
        // 其他导航事件（如 GoToBossRegistration）在此屏幕通常不需要处理，
        // 因为 AppStartState 会在启动时决定是否直接去老板注册
    }

    // 处理错误消息
    LaunchedEffect(key1 = authUiState.error) {
        authUiState.error?.let {
            Log.d("LoginScreen", "Error observed: $it")
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.errorShown() // 消耗错误
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("员工登录") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("欢迎回来！", style = MaterialTheme.typography.headlineSmall)
            Text("请输入您的凭据登录。", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = if (it.isBlank()) "用户名不能为空" else null
                },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                isError = usernameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(16.dp))

            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = if (it.isBlank()) "密码不能为空" else null
                },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus() // 清除焦点以隐藏键盘
                    // 执行登录操作 (可选，通常通过按钮触发)
                    if (username.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(username.trim(), password)
                    } else {
                        if (username.isBlank()) usernameError = "用户名不能为空"
                        if (password.isBlank()) passwordError = "密码不能为空"
                    }
                }),
                isError = passwordError != null,
                singleLine = true
            )
            passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(32.dp))

            // 登录按钮
            Button(
                onClick = {
                    keyboardController?.hide() // 点击按钮时隐藏键盘
                    focusManager.clearFocus()

                    usernameError = if (username.isBlank()) "用户名不能为空" else null
                    passwordError = if (password.isBlank()) "密码不能为空" else null

                    if (usernameError == null && passwordError == null) {
                        viewModel.login(username.trim(), password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authUiState.isLoading
            ) {
                if (authUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("登录")
                }
            }

            // (可选) 如果需要，可以在这里添加一个“忘记密码？”或“联系管理员”的文本/按钮
        }
    }
}

@Preview(showBackground = true, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    ManagerTheme {
        LoginScreen(
            onNavigateToMainApp = { Log.d("Preview", "Navigate to Main App from Login") }
        )
    }
}