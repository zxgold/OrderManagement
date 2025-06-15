package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Product
import com.example.manager.data.model.entity.Supplier
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.ProductRepository
import com.example.manager.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * ViewModel的核心职责：
 * 1. 加载并持有当前店铺的所有供应商列表
 * 2. 管理当前被选中的供应商
 * 3. 当选中的供应商变化时，自动记载并持有该供应商的产品列表（响应式）
 * 4. 处理添加/编辑供应商的逻辑
 * 5. 处理在特定供应商下添加/编辑产品的逻辑
 */

// UI State for the entire Supplier-Product screen
data class SupplierProductUiState(
    val suppliers: List<Supplier> = emptyList(),
    val selectedSupplier: Supplier? = null,
    val productsOfSelectedSupplier: List<Product> = emptyList(),
    val isLoadingSuppliers: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class) // For flatMapLatest
@HiltViewModel
class SupplierProductViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierProductUiState())
    val uiState: StateFlow<SupplierProductUiState> = _uiState.asStateFlow()

    // Flow to hold the current storeId, to drive other flows
    private val storeIdFlow: Flow<Long?> = sessionManager.userSessionFlow.map { it.storeId }

    init {
        Log.i("SupplierProductVM", "ViewModel INSTANCE CREATED AND INITIALIZED.") // 使用一个更醒目的日志
        Log.d("SupplierProductVM", "ViewModel Initialized")
        loadSuppliers()

        // This coroutine will react to changes in the selected supplier
        viewModelScope.launch {
            uiState
                .map { it.selectedSupplier } // 1. Listen for changes to the selected supplier
                .distinctUntilChanged()     // 2. Only proceed if the supplier actually changes
                .flatMapLatest { supplier -> // 3. Switch to the new product loading flow
                    if (supplier != null) {
                        _uiState.update { it.copy(isLoadingProducts = true) }
                        // 4. Fetch the products for the new supplier
                        productRepository.getAllActiveProductsBySupplierIdFlow(supplier.id)
                    } else {
                        // 5. If no supplier is selected, emit an empty list
                        flowOf(emptyList())
                    }
                }
                .catch { e ->
                    Log.e("SupplierProductVM", "Error loading products for supplier.", e)
                    _uiState.update { it.copy(isLoadingProducts = false, errorMessage = "加载产品列表失败") }
                }
                .collect { products -> // 6. Collect the products and update the UI state
                    _uiState.update {
                        it.copy(
                            productsOfSelectedSupplier = products,
                            isLoadingProducts = false
                        )
                    }
                }
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            storeIdFlow.firstOrNull()?.let { storeId -> // Get the current storeId
                _uiState.update { it.copy(isLoadingSuppliers = true) }
                supplierRepository.getAllSuppliersByStoreIdFlow(storeId)
                    .catch { e -> _uiState.update { it.copy(isLoadingSuppliers = false, errorMessage = "加载供应商列表失败") } }
                    .collect { suppliers ->
                        _uiState.update { currentState ->
                            // Check if the previously selected supplier still exists in the new list
                            val stillSelectedSupplier = suppliers.find { it.id == currentState.selectedSupplier?.id }
                            currentState.copy(
                                suppliers = suppliers,
                                isLoadingSuppliers = false,
                                // If no supplier is selected, or the old one disappeared, select the first one if available
                                selectedSupplier = stillSelectedSupplier ?: suppliers.firstOrNull()
                            )
                        }
                    }
            } ?: _uiState.update { it.copy(errorMessage = "无法获取店铺信息") }
        }
    }

    fun selectSupplier(supplier: Supplier) {
        _uiState.update { it.copy(selectedSupplier = supplier) }
    }

    fun addSupplier(name: String, contactPerson: String?, phone: String?, address: String?, remark: String?) {
        viewModelScope.launch {
            val storeId = getCurrentStoreId()
            if (storeId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取店铺信息，无法添加供应商。") }
                return@launch
            }
            _uiState.update { it.copy(isLoadingSuppliers = true) }
            val newSupplier = Supplier(
                storeId = storeId,
                name = name,
                contactPerson = contactPerson,
                phone = phone,
                address = address,
                remark = remark
            )
            supplierRepository.insertSupplier(newSupplier)
                .onSuccess {
                    // loadSuppliers() will be automatically triggered by the Flow,
                    // so we don't need to call it manually. We just need to clear the loading state.
                    Log.d("SupplierProductVM", "Supplier added successfully.")
                }
                .onFailure { e ->
                    val errorMsg = if (e is SQLiteConstraintException) "添加失败：该供应商名称在本店已存在。" else "添加供应商失败: ${e.localizedMessage}"
                    _uiState.update { it.copy(isLoadingSuppliers = false, errorMessage = errorMsg) }
                }
        }
    }

    /* addProduct
     * 这个方法的核心就是接收 UI 创建的 Product 对象，
     * 然后用 copy() 方法将当前选中的 selectedSupplier.id 赋给 supplierId，
     * 最后调用 productRepository.insertProduct()
     */
    fun addProduct(product: Product) {
        viewModelScope.launch {
            val selectedSupplier = _uiState.value.selectedSupplier
            if (selectedSupplier == null) {
                _uiState.update { it.copy(errorMessage = "请先选择一个供应商") }
                return@launch
            }
            // 使用选中的供应商的 ID 来创建最终要保存到数据库的 Product 对象
            val productToInsert = product.copy(supplierId = selectedSupplier.id)

            // _uiState.update { it.copy(isLoadingProducts = true) } // 可以在这里设置加载状态，但因为是 Flow，列表会自动刷新，所以可能不需要
            productRepository.insertProduct(productToInsert)
                .onSuccess {
                    Log.d("SupplierProductVM", "Product '${productToInsert.name}' added successfully to supplier '${selectedSupplier.name}'")
                    // 产品列表会自动刷新，无需手动调用
                }
                .onFailure { e ->
                    val errorMsg = if (e is SQLiteConstraintException) "添加失败：产品名称和型号在该供应商下已存在。" else "添加产品失败: ${e.localizedMessage}"
                    _uiState.update { it.copy(isLoadingProducts = false, errorMessage = errorMsg) }
                }
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun getCurrentStoreId(): Long? {
        return sessionManager.userSessionFlow.firstOrNull()?.storeId
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            // 确保 product 的 supplierId 与当前选中的一致
            if (product.supplierId != _uiState.value.selectedSupplier?.id) {
                _uiState.update { it.copy(errorMessage = "操作无效：产品不属于当前供应商。") }
                return@launch
            }
            productRepository.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
                .onSuccess {
                    Log.d("SupplierProductVM", "Product updated successfully.")
                    // 列表会自动刷新
                }
                .onFailure { e ->
                    val errorMsg = if (e is SQLiteConstraintException) "更新失败：产品名称和型号已存在。" else "更新产品失败: ${e.localizedMessage}"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            if (product.supplierId != _uiState.value.selectedSupplier?.id) {
                _uiState.update { it.copy(errorMessage = "操作无效：产品不属于当前供应商。") }
                return@launch
            }
            productRepository.deleteProduct(product).also { result -> // deleteProduct 返回 Result<Int>
                result.onSuccess { deletedRows ->
                    if (deletedRows > 0) {
                        Log.d("SupplierProductVM", "Product deleted successfully.")
                    } else {
                        _uiState.update { it.copy(errorMessage = "删除失败，未找到产品。") }
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(errorMessage = "删除产品失败: ${e.localizedMessage}") }
                }
            }
        }
    }

}
