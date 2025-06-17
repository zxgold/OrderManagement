package com.example.manager.ui.staff

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.viewmodel.StaffManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    navController: NavController,
    viewModel: StaffManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    // 处理成功或失败的消息
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("员工管理") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddStaffDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "添加新员工")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.staffList, key = { it.id }) { staff ->
                        StaffItemCard(
                            staff = staff,
                            onStatusChange = { viewModel.toggleStaffStatus(staff) }
                            // TODO: onEditClick, onResetPasswordClick
                        )
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { name, username, password, role ->
                viewModel.addStaff(name, username, password, role)
                showAddStaffDialog = false
            }
        )
    }
}

@Composable
fun StaffItemCard(
    staff: Staff,
    onStatusChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(staff.name, style = MaterialTheme.typography.titleMedium)
                Text("用户名: ${staff.username}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "角色: ${staff.role.name}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (staff.role == StaffRole.BOSS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("状态", style = MaterialTheme.typography.labelSmall)
                Switch(
                    checked = staff.isActive,
                    onCheckedChange = onStatusChange
                )
            }
            // TODO: 添加三点菜单用于编辑和重置密码
        }
    }
}