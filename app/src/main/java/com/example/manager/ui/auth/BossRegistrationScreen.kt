package com.example.manager.ui.auth // 或者你放置 UI 文件的包

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
fun BossRegistrationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToMainApp: () -> Unit, // 注册成功后导航到主应用
    onNavigateToLogin: () -> Unit    // 如果老板已存在，导航到登录
) {
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current // 用于控制键盘

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var bossName by remember { mutableStateOf("老板") } // 默认值，允许用户修改

    // 本地 UI 校验状态
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var bossNameError by remember { mutableStateOf<String?>(null) }

    // 处理导航事件
    LaunchedEffect(key1 = authUiState.navigationEvent) {
        Log.d("BossRegScreen", "Navigation Event observed: ${authUiState.navigationEvent}")
        when (authUiState.navigationEvent) {
            is NavigationEvent.GoToMainApp -> {
                onNavigateToMainApp()
                viewModel.navigationEventConsumed() // 消耗事件
            }
            is NavigationEvent.GoToLogin -> {
                onNavigateToLogin()
                viewModel.navigationEventConsumed() // 消耗事件
            }
            else -> { /* 其他事件或 Idle，不做处理 */ }
        }
    }

    // 处理错误消息
    LaunchedEffect(key1 = authUiState.error) {
        authUiState.error?.let {
            Log.d("BossRegScreen", "Error observed: $it")
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
            TopAppBar(title = { Text("创建老板账户 (首次运行)") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // 使内容可滚动
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("欢迎使用 OrderManager！", style = MaterialTheme.typography.headlineSmall)
            Text("请创建您的老板账户以开始使用。", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // 老板名称输入框
            OutlinedTextField(
                value = bossName,
                onValueChange = {
                    bossName = it
                    bossNameError = if (it.isBlank()) "老板名称不能为空" else null
                },
                label = { Text("老板名称 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = bossNameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            bossNameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(16.dp))

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = if (it.isBlank()) "用户名不能为空" else null
                },
                label = { Text("登录用户名 *") },
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
                    passwordError = if (it.length < 6) "密码至少需要6位" else null
                    // 实时校验确认密码（如果确认密码已输入）
                    if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                        confirmPasswordError = "两次输入的密码不一致"
                    } else if (confirmPassword.isNotEmpty() && it == confirmPassword) {
                        confirmPasswordError = null // 如果现在匹配了，清除错误
                    }
                },
                label = { Text("设置密码 (至少6位) *") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                isError = passwordError != null,
                singleLine = true
            )
            passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(16.dp))

            // 确认密码输入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it != password) "两次输入的密码不一致" else null
                },
                label = { Text("确认密码 *") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                isError = confirmPasswordError != null,
                singleLine = true
            )
            confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(32.dp))

            // 注册按钮
            Button(
                onClick = {
                    keyboardController?.hide() // 点击按钮时隐藏键盘
                    // 执行所有校验
                    bossNameError = if (bossName.isBlank()) "老板名称不能为空" else null
                    usernameError = if (username.isBlank()) "用户名不能为空" else null
                    passwordError = if (password.length < 6) "密码至少需要6位" else null
                    confirmPasswordError = if (password != confirmPassword) "两次输入的密码不一致" else null

                    if (bossNameError == null && usernameError == null && passwordError == null && confirmPasswordError == null) {
                        viewModel.registerBossAccount(username.trim(), password, confirmPassword, bossName.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authUiState.isLoading // 加载中禁用按钮
            ) {
                if (authUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("创建并登录老板账户")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Boss Registration Screen")
@Composable
fun BossRegistrationScreenPreview() {
    ManagerTheme {
        // 为了预览，你可以创建一个模拟的 AuthViewModel，或者传入 null/默认参数
        // 这里简单地调用，实际预览可能无法完全模拟 ViewModel 行为
        BossRegistrationScreen(
            onNavigateToMainApp = { Log.d("Preview", "Navigate to Main App") },
            onNavigateToLogin = { Log.d("Preview", "Navigate to Login") }
        )
    }
}