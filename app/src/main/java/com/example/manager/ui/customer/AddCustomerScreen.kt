package com.example.manager.ui.customer

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.manager.ui.navigation.AppDestinations
import com.example.manager.viewmodel.AddCustomerResult
import com.example.manager.viewmodel.CustomerViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    navController: NavController,
    defaultName: String,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

    // --- 状态变量应该在 Column 外部定义 ---
    var nameState by remember { mutableStateOf(TextFieldValue(defaultName)) }
    var phoneState by remember { mutableStateOf(TextFieldValue("")) }
    var addressState by remember { mutableStateOf(TextFieldValue("")) }
    var remarkState by remember { mutableStateOf(TextFieldValue("")) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // --- 监听来自 ViewModel 的结果事件 ---
    LaunchedEffect(key1 = Unit) {
        viewModel.addCustomerResultFlow.collectLatest { result ->
            isLoading = false // 收到结果，结束加载
            when (result) {
                is AddCustomerResult.Success -> {
                    Log.d("AddCustomerScreen", "Received success result. New ID: ${result.newCustomerId}")
                    // 1. 设置返回结果
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AppDestinations.NEW_CUSTOMER_ID_RESULT_KEY, result.newCustomerId)
                    // 2. 返回上一个屏幕
                    navController.popBackStack()
                }
                is AddCustomerResult.Failure -> {
                    snackbarHostState.showSnackbar(result.errorMessage)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("创建新客户") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val name = nameState.text.trim()
                            val phone = phoneState.text.trim()
                            nameError = if (name.isBlank()) "姓名不能为空" else null
                            phoneError = if (phone.isBlank()) "电话不能为空" else null
                            if (nameError == null && phoneError == null) {
                                isLoading = true
                                viewModel.addCustomer(name, phone, addressState.text.ifBlank { null }, remarkState.text.ifBlank { null })
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                        else Icon(Icons.Filled.Save, contentDescription = "保存客户")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // **将状态变量定义移到 Column 外部**

            // 姓名输入框
            OutlinedTextField(
                value = nameState,
                onValueChange = { newValue ->
                    nameState = newValue
                    nameError = if (newValue.text.isBlank()) "姓名不能为空" else null
                },
                label = { Text("客户姓名 *") },
                isError = nameError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            nameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            // 电话输入框
            OutlinedTextField(
                value = phoneState,
                onValueChange = { newValue ->
                    phoneState = newValue
                    phoneError = if (newValue.text.isBlank()) "电话不能为空" else null
                },
                label = { Text("联系电话 *") },
                isError = phoneError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            // 地址输入框
            OutlinedTextField(
                value = addressState,
                onValueChange = { addressState = it },
                label = { Text("地址") },
                modifier = Modifier.fillMaxWidth()
            )

            // 备注输入框
            OutlinedTextField(
                value = remarkState,
                onValueChange = { remarkState = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}