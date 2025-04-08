package com.example.manager.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.data.model.entity.Customer
import com.example.manager.viewmodel.CustomerListUiState
import com.example.manager.viewmodel.CustomerViewModel // Import your ViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // For Scaffold, TopAppBar etc.
@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel = hiltViewModel() // Hilt provides the ViewModel
) {
    // Observe the UI state from the ViewModel safely
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) } // For delete confirmation

    // Effect to show snackbar when error message changes
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "知道了" // Optional action
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.errorShown() // Notify ViewModel the error was handled
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("客户管理") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "添加客户")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp) // Add horizontal padding for content
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged, // Use method reference
                label = { Text("搜索客户 (姓名或电话)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content Area: Loading, Empty, or List
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.customers.isEmpty() && !uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("还没有客户，点击右下角添加吧！")
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = uiState.customers,
                            key = { customer -> customer.id } // Important for performance
                        ) { customer ->
                            CustomerItem(
                                customer = customer,
                                onDeleteClick = { customerToDelete = it } // Show delete confirmation
                                // onItemClick = { /* TODO: Navigate to customer detail */ }
                            )
                            Divider() // Add a divider between items
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Add Customer Dialog
    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addCustomer(name, phone.ifBlank { null }, address.ifBlank { null })
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除客户 ${customer.name} 吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        customerToDelete = null
                    }
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun CustomerItem(
    customer: Customer,
    onDeleteClick: (Customer) -> Unit,
    // onItemClick: (Customer) -> Unit // For future use
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* onItemClick(customer) */ } // Make the row clickable
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Pushes delete icon to the end
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) { // Take available space, leave room for icon
            Text(text = customer.name, style = MaterialTheme.typography.bodyLarge)
            customer.phone?.let { // Only show phone if it exists
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = { onDeleteClick(customer) }) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "删除客户",
                tint = MaterialTheme.colorScheme.error // Use error color for delete
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新客户") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = it.isBlank() },
                    label = { Text("客户姓名 *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) {
                    Text("姓名不能为空", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("联系电话") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, phone, address)
                    } else {
                        nameError = true
                    }
                },
                // enabled = name.isNotBlank() // Enable only if name is not blank
            ) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}