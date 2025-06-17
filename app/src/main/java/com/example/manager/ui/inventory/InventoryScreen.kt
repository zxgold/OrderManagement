package com.example.manager.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.Supplier
import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import com.example.manager.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("店铺库存") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                // TODO: 添加手动入库按钮
            )
        }
    ) { paddingValues ->
        Row(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // 左栏：供应商列表
            Column(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                // "所有" 选项
                SupplierItem(
                    supplierName = "所有供应商",
                    isSelected = uiState.selectedSupplier == null,
                    onClick = { viewModel.selectSupplier(null) }
                )
                HorizontalDivider()

                if (uiState.isLoadingSuppliers) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        items(uiState.suppliers, key = { it.id }) { supplier ->
                            SupplierItem(
                                supplierName = supplier.name,
                                isSelected = supplier.id == uiState.selectedSupplier?.id,
                                onClick = { viewModel.selectSupplier(supplier) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // 分割线
            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // 右栏：库存列表
            Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                // ... 标题等 ...
                if (uiState.isLoadingInventory) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (uiState.inventoryItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无库存记录") }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                        items(uiState.inventoryItems, key = { it.inventoryItem.id }) { item ->
                            InventoryItemRow(item = item)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierItem(supplierName: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = supplierName,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun InventoryItemRow(item: InventoryItemWithProductInfo) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("${item.productName} (${item.productModel ?: "无型号"})", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text("库存数量: ${item.inventoryItem.quantity}", style = MaterialTheme.typography.bodyMedium)
        Text("状态: ${item.inventoryItem.status.name}", style = MaterialTheme.typography.bodyMedium)
        if (item.inventoryItem.isStandardStock == false) {
            Text("类型: 定制品", style = MaterialTheme.typography.bodySmall)
        }
        item.inventoryItem.locationInWarehouse?.let {
            Text("位置: $it", style = MaterialTheme.typography.bodySmall)
        }
    }
}