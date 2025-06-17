package com.example.manager.ui.staff

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
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
    var staffToEdit by remember { mutableStateOf<Staff?>(null) } // <-- 新增
    var staffToResetPassword by remember { mutableStateOf<Staff?>(null) } // <-- 新增

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
                            onStatusChange = { viewModel.toggleStaffStatus(staff) },
                            onEditClick = { staffToEdit = staff }, // <-- 实现
                            onResetPasswordClick = { staffToResetPassword = staff } // <-- 实现
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

    staffToEdit?.let { staff ->
        Log.d("StaffManagementScreen", "staffToEdit is not null, should show EditStaffDialog") // <-- 添加日志
        EditStaffDialog( // <-- **确认这里调用的是你创建的 EditStaffDialog**
            staff = staff,
            onDismiss = { staffToEdit = null },
            onConfirm = { updatedName, updatedRole ->
                viewModel.updateStaffInfo(staff, updatedName, updatedRole)
                staffToEdit = null
            }
        )
    }

    staffToResetPassword?.let { staff ->
        Log.d("StaffManagementScreen", "staffToResetPassword is not null, should show ResetPasswordDialog") // <-- 添加日志
        ResetPasswordDialog( // <-- **确认这里调用的是你创建的 ResetPasswordDialog**
            staffName = staff.name,
            onDismiss = { staffToResetPassword = null },
            onConfirm = { newPassword ->
                viewModel.resetPassword(staff, newPassword)
                staffToResetPassword = null
            }
        )
    }
}

@Composable
fun StaffItemCard(
    staff: Staff,
    onStatusChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,      // <-- 新增：编辑回调
    onResetPasswordClick: () -> Unit // <-- 新增：重置密码回调
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("状态", style = MaterialTheme.typography.labelSmall)
                Switch(
                    checked = staff.isActive,
                    onCheckedChange = onStatusChange,
                    // 如果是老板自己，不能禁用自己
                    enabled = staff.role != StaffRole.BOSS
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "更多操作")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑信息") },
                        onClick = {
                            onEditClick()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "编辑") }
                    )
                    DropdownMenuItem(
                        text = { Text("重置密码") },
                        onClick = {
                            onResetPasswordClick()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Password, contentDescription = "重置密码") }
                    )
                }
            }

        }
    }
}