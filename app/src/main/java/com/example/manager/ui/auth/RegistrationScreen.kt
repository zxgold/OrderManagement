package com.example.manager.ui.auth

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
import com.example.manager.ui.theme.ManagerTheme
import com.example.manager.viewmodel.AuthViewModel
import com.example.manager.viewmodel.NavigationEvent
import androidx.compose.ui.text.input.TextFieldValue // <-- **导入 TextFieldValue**

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToMainApp: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val isInitialSetupNeeded by viewModel.isInitialSetupNeeded.collectAsStateWithLifecycle() // 获取是否首次设置

    val snackbarHostState = remember { SnackbarHostState() }

    // --- 使用 TextFieldValue 来管理状态 ---
    var storeNameState by remember { mutableStateOf(TextFieldValue("")) } // <-- 使用 TextFieldValue
    var staffNameState by remember { mutableStateOf(TextFieldValue("老板")) }
    var usernameState by remember { mutableStateOf(TextFieldValue("")) }


    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    // 本地 UI 校验状态
    var staffNameError by remember { mutableStateOf<String?>(null) } // 从 bossNameError 改为 staffNameError
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var storeNameError by remember { mutableStateOf<String?>(null) } // <-- **新增店铺名称错误状态**

    // 处理导航事件 (保持不变)
    LaunchedEffect(key1 = authUiState.navigationEvent) {
        Log.d("RegistrationScreen", "Navigation Event observed: ${authUiState.navigationEvent}")
        when (authUiState.navigationEvent) {
            is NavigationEvent.GoToMainApp -> {
                onNavigateToMainApp()
                viewModel.navigationEventConsumed() // 消耗事件
            }
            is NavigationEvent.GoToLogin -> {
                onNavigateToLogin()
                viewModel.navigationEventConsumed() // 消耗事件
            }
            // 如果有 GoToRegistration 事件，也在这里处理（虽然从注册页通常不会再导航到注册页）
            is NavigationEvent.GoToRegistration -> {  }
            NavigationEvent.Idle -> { /* No-op */ }
        }
    }

    // --- 处理错误消息 (这是你需要替换或填充的部分) ---
    LaunchedEffect(key1 = authUiState.error) {
        authUiState.error?.let { errorMessage -> // 当 error 不为 null 时执行
            Log.d("RegistrationScreen", "Error state updated: $errorMessage. Showing Snackbar.")
            val result = snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = "知道了", // 提供一个操作让用户关闭
                duration = SnackbarDuration.Indefinite // 持续显示直到用户交互
            )
            // 当 Snackbar 因为用户点击 action 或超时/滑动消失时，再消耗错误
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                Log.d("RegistrationScreen", "Snackbar dismissed or action performed for error: $errorMessage")
                viewModel.authErrorShown() // 通知 ViewModel 错误已被处理
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // 根据是否首次设置，动态修改标题
            val titleText = if (isInitialSetupNeeded) "创建初始老板账户" else "注册新账户"
            TopAppBar(title = { Text(titleText) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // 应用Scaffold的padding
                .fillMaxSize()
                .imePadding() // <-- 【关键修改】添加对键盘的处理
                .verticalScroll(rememberScrollState()) // 使内容可滚动
                .padding(16.dp), // 应用内容区域的padding
            horizontalAlignment = Alignment.CenterHorizontally
            // <-- 【可选但推荐】移除了 verticalArrangement.Center
        ) {
            Text(
                text = if (isInitialSetupNeeded) "欢迎使用 OrderManager！" else "注册新账户",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (isInitialSetupNeeded) "请创建您的老板账户和店铺以开始使用。" else "请输入账户和店铺信息。",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- 店铺名称输入框 ---

            OutlinedTextField(
                value = storeNameState, // 绑定TextFieldValue状态
                onValueChange = { newValue -> // onValueChange 现在返回 TextFieldValue
                    storeNameState = newValue
                    storeNameError = if (newValue.text.isBlank()) "店铺名称不能为空" else null
                },
                label = { Text("店铺名称 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = storeNameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            storeNameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(16.dp))
            // --------------------------

            // 您的称呼/员工名称输入框 (之前是 bossName)
            OutlinedTextField(
                value = staffNameState, // 使用 staffName
                onValueChange = { newValue ->
                    staffNameState = newValue
                    staffNameError = if (newValue.text.isBlank()) "您的称呼不能为空" else null
                },
                label = { Text("您的称呼/姓名 *") }, // 标签更通用
                modifier = Modifier.fillMaxWidth(),
                isError = staffNameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            staffNameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(16.dp))

            // 用户名输入框 (保持不变)
            OutlinedTextField(
                value = usernameState,
                onValueChange = { newValue ->
                    usernameState = newValue
                    usernameError = if (newValue.text.isBlank()) "用户名不能为空" else null
                },
                label = { Text("登录用户名 *") }, /* ... */
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
                onValueChange = { // <-- **修正这里**
                    password = it
                    passwordError = if (it.length < 6) "密码至少需要6位" else null
                    if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                        confirmPasswordError = "两次输入的密码不一致"
                    } else if (confirmPassword.isNotEmpty() && it == confirmPassword) {
                        confirmPasswordError = null
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
                onValueChange = { // <-- **修正这里**
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

            // 注册按钮
            Button(
                onClick = {
                    keyboardController?.hide()
                    // **现在从 .text 属性获取字符串进行校验**
                    val storeName = storeNameState.text
                    val staffName = staffNameState.text
                    val username = usernameState.text

                    // 执行所有校验
                    storeNameError = if (storeName.isBlank()) "店铺名称不能为空" else null // **校验店铺名称**
                    staffNameError = if (staffName.isBlank()) "您的称呼不能为空" else null
                    usernameError = if (username.isBlank()) "用户名不能为空" else null
                    passwordError = if (password.length < 6) "密码至少需要6位" else null
                    confirmPasswordError = if (password != confirmPassword) "两次输入的密码不一致" else null

                    if (storeNameError == null && staffNameError == null && usernameError == null && passwordError == null && confirmPasswordError == null) {
                        // 调用 ViewModel 的新注册方法 (假设我们将其命名为 registerAccount)
                        viewModel.registerAccount( // <-- **调用新的/修改后的注册方法**
                            username = username.trim(),
                            passwordAttempt = password,
                            confirmPasswordAttempt = confirmPassword,
                            staffName = staffName.trim(),
                            storeName = storeName.trim() // <-- **传递店铺名称**
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authUiState.isLoading
            ) {
                if (authUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isInitialSetupNeeded) "创建老板账户并开店" else "注册新账户")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Registration Screen") // 更新 Preview 名称
@Composable
fun RegistrationScreenPreview() { // 更新 Preview 函数名
    ManagerTheme {
        RegistrationScreen( // Composable 函数名可以暂时保留
            onNavigateToMainApp = { Log.d("Preview", "Navigate to Main App") },
            onNavigateToLogin = { Log.d("Preview", "Navigate to Login") }
        )
    }
}