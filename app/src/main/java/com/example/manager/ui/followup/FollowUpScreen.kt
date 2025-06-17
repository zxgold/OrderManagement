package com.example.manager.ui.followup

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.uimodel.FollowUpWithCustomerName
import com.example.manager.viewmodel.FollowUpViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpScreen(
    navController: NavController,
    viewModel: FollowUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()
    val customerSearchResults by viewModel.customerSearchResults.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("客户跟进") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "添加跟进")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.followUps.isEmpty()) {
                Text("暂无跟进记录", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(uiState.followUps, key = { it.followUp.id }) { item ->
                        FollowUpItemCard(item = item)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFollowUpDialog(
            customerSearchQuery = customerSearchQuery,
            onCustomerSearchQueryChanged = viewModel::onCustomerSearchQueryChanged,
            customerSearchResults = customerSearchResults,
            onDismiss = { showAddDialog = false },
            onConfirm = { customerId, notes ->
                viewModel.addFollowUp(customerId, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FollowUpItemCard(item: FollowUpWithCustomerName) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "客户: ${item.customerName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(item.followUp.notes, style = MaterialTheme.typography.bodyLarge)
            Text(
                "跟进于: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(item.followUp.followUpDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}