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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.ui.theme.ManagerTheme
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.viewmodel.NavigationEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToMainApp: () -> Unit,
    onNavigateToRegistration: () -> Unit // <-- **新增：导航到注册页面的回调**
) {
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val isInitialSetupNeeded by viewModel.isInitialSetupNeeded.collectAsStateWithLifecycle() // <-- **新增：观察是否需要初始设置**
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var password by remember { mutableStateOf("") }
    var usernameState by remember { mutableStateOf(TextFieldValue("")) }


    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    

    // 处理导航事件
    LaunchedEffect(key1 = authUiState.navigationEvent) {
        Log.d("LoginScreen", "Navigation Event observed: ${authUiState.navigationEvent}")
        if (authUiState.navigationEvent is NavigationEvent.GoToMainApp) {
            onNavigateToMainApp()
            viewModel.navigationEventConsumed()
        }
        // 注意：如果 BossRegistrationScreen 注册成功后发出的也是 GoToMainApp 事件，
        // 那么这里不需要额外处理 GoToBossRegistration，因为 AppNavigation 会处理初始导航。
        // LoginScreen 主要负责在用户明确点击“注册”时，通过 onNavigateToRegistration 回调触发导航。
    }

    // 处理错误消息
    LaunchedEffect(key1 = authUiState.error) {
        authUiState.error?.let {
            Log.d("LoginScreen", "Error observed: $it")
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.authErrorShown()
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
                value = usernameState,
                onValueChange = {
                    usernameState = it
                    usernameError = if (it.text.isBlank()) "用户名不能为空" else null
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
                    focusManager.clearFocus()
                    val username = usernameState.text // <-- 从 .text 获取字符串
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

            // 登录按钮 (确保这是完整的)
            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val username = usernameState.text // <-- 从 .text 获取字符串
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


            Spacer(modifier = Modifier.height(16.dp)) // <-- **新增：间距**

            // --- 注册账户按钮 ---
            TextButton(
                onClick = {
                    Log.d("LoginScreen", "注册账户按钮点击。 InitialSetupNeeded: $isInitialSetupNeeded")
                    onNavigateToRegistration() // **调用新的导航回调**
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isInitialSetupNeeded) "还没有账户？创建老板账户" else "注册新店铺/老板账户")
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    ManagerTheme {
        LoginScreen(
            onNavigateToMainApp = { Log.d("Preview", "Navigate to Main App from Login") },
            onNavigateToRegistration = { Log.d("Preview", "Navigate to Registration from Login") } // <-- **更新 Preview**
        )
    }
}