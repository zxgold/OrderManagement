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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material.icons.filled.MoreVert // 用于三点菜单图标
import androidx.compose.material.icons.filled.Edit // 用于编辑图标
import com.example.manager.ui.supplier.EditProductDialog // 导入新创建的对话框


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierProductScreen(
    navController: NavController,
    viewModel: SupplierProductViewModel = hiltViewModel()
) {
    // --- 添加探针日志 ---
    Log.d("SupplierProductScreen", "Composable is being composed/recomposed.")
    // ----------------------

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) } // <-- 新增：要编辑的产品
    var productToDelete by remember { mutableStateOf<Product?>(null) } // <-- 新增：要删除的产品

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
                                    onEditClick = { productToEdit = product },
                                    onDeleteClick = { productToDelete = product }
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

    // 当需要显示添加产品对话框，并且确实有一个选中的供应商时
    if (showAddProductDialog && uiState.selectedSupplier != null) {
        AddProductDialog(
            forSupplier = uiState.selectedSupplier!!, // 传递当前选中的供应商
            onDismiss = { showAddProductDialog = false },
            onConfirm = { productWithoutIds ->
                // 调用 ViewModel 的 addProduct 方法
                viewModel.addProduct(productWithoutIds)
                showAddProductDialog = false // 添加后关闭对话框
            }
        )
    }

    // 编辑产品的对话框
    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null // 关闭对话框
            }
        )
    }

    // 新增：删除产品的确认对话框
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("确认删除产品") },
            text = { Text("确定要删除产品 “${product.name}” 吗？此操作会影响已关联此产品的历史订单（显示为非标品），但不会从历史订单中移除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    }
                ) { Text("确认删除") }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("取消") }
            }
        )
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
    onEditClick: () -> Unit,  // <-- 新增：编辑点击回调
    onDeleteClick: () -> Unit // <-- 新增：删除点击回调
    // onClick: () -> Unit // 如果整个条目点击是查看详情，可以保留
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // .clickable(onClick = onClick) // 如果需要整体点击，可以保留
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
        // 三点菜单按钮
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "更多操作")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("编辑") },
                    onClick = {
                        onEditClick()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "编辑") }
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        onDeleteClick()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "删除") }
                )
            }
        }
    }
}
