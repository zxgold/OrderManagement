package com.example.manager.ui.supplier

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business // 供应商图标
import androidx.compose.material.icons.filled.Category // 产品图标
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.manager.data.model.entity.Product
import com.example.manager.data.model.entity.Supplier
import com.example.manager.viewmodel.SupplierProductViewModel
import com.example.manager.ui.supplier.AddSupplierDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierProductScreen(
    navController: NavController,
    viewModel: SupplierProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("供应商与产品") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSupplierDialog = true }) {
                        Icon(Icons.Filled.GroupAdd, contentDescription = "添加供应商")
                    }
                }
            )
        },
        floatingActionButton = {
            // 只有选中了供应商才能添加产品
            if (uiState.selectedSupplier != null) {
                FloatingActionButton(onClick = { showAddProductDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "添加产品")
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // --- 左栏：供应商列表 ---
            Column(modifier = Modifier.weight(0.4f)) {
                Text(
                    "供应商",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                if (uiState.isLoadingSuppliers) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        items(uiState.suppliers, key = { it.id }) { supplier ->
                            SupplierItem(
                                supplier = supplier,
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

            // --- 右栏：产品列表 ---
            Column(modifier = Modifier.weight(0.6f)) {
                val selectedSupplierName = uiState.selectedSupplier?.name ?: "请选择供应商"
                Text(
                    "产品列表 ($selectedSupplierName)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                when {
                    uiState.isLoadingProducts -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    uiState.selectedSupplier == null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("请先在左侧选择一个供应商") }
                    }
                    uiState.productsOfSelectedSupplier.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("该供应商下暂无产品，点击右下角添加吧！") }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(uiState.productsOfSelectedSupplier, key = { it.id }) { product ->
                                ProductItem(
                                    product = product,
                                    onClick = { /* TODO: 导航到产品详情或编辑 */ }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---
    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, contactPerson, phone, address, remark ->
                viewModel.addSupplier(name, contactPerson, phone, address, remark)
                showAddSupplierDialog = false // 添加后关闭对话框
            }
        )
    }

    if (showAddProductDialog) {
        // TODO: 创建 AddProductDialog Composable
        // AddProductDialog(
        //     onDismiss = { showAddProductDialog = false },
        //     onConfirm = { product -> // 接收一个不含ID和supplierId的Product对象
        //         viewModel.addProduct(product)
        //         showAddProductDialog = false
        //     }
        // )
        Log.d("SupplierScreen", "Add Product Dialog should be shown (TODO)")
        showAddProductDialog = false
    }
}

// --- 可复用的子组件 ---

@Composable
fun SupplierItem(
    supplier: Supplier,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Business,
            contentDescription = "供应商",
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = supplier.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Category,
            contentDescription = "产品",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            product.model?.let { Text("型号: $it", style = MaterialTheme.typography.bodyMedium) }
            Text("参考价: ¥${String.format("%.2f", product.defaultPrice)}", style = MaterialTheme.typography.bodyMedium)
        }
        // TODO: 可以添加编辑或删除图标按钮
    }
}