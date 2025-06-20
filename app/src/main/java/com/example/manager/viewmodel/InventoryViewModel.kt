package com.example.manager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Supplier
import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.InventoryRepository
import com.example.manager.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val suppliers: List<Supplier> = emptyList(),
    val selectedSupplier: Supplier? = null,
    val inventoryItems: List<InventoryItemWithProductInfo> = emptyList(),
    val isLoadingSuppliers: Boolean = false,
    val isLoadingInventory: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val supplierRepository: SupplierRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取店铺信息") }
                return@launch
            }

            // 启动对供应商列表的订阅
            launch {
                _uiState.update { it.copy(isLoadingSuppliers = true) }
                supplierRepository.getAllSuppliersByStoreIdFlow(storeId)
                    .catch { e -> _uiState.update { it.copy(isLoadingSuppliers = false, errorMessage = "加载供应商失败") } }
                    .collect { suppliers ->
                        _uiState.update { currentState ->
                            val stillSelected = suppliers.find { it.id == currentState.selectedSupplier?.id }
                            currentState.copy(
                                suppliers = suppliers,
                                isLoadingSuppliers = false,
                                selectedSupplier = stillSelected ?: suppliers.firstOrNull() // 默认选中第一个
                            )
                        }
                    }
            }

            // 启动对库存列表的响应式订阅，它会根据 selectedSupplier 的变化而变化
            viewModelScope.launch {
                _uiState
                    .map { it.selectedSupplier }
                    .distinctUntilChanged()
                    .flatMapLatest { supplier ->
                        _uiState.update { it.copy(isLoadingInventory = true) }
                        val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
                        if (storeId == null) {
                            // 如果没有 storeId，返回一个包含错误的 Flow
                            return@flatMapLatest flow {
                                emit(emptyList<InventoryItemWithProductInfo>())
                                _uiState.update { it.copy(isLoadingInventory = false, errorMessage = "无法获取店铺信息") }
                            }
                        }

                        if (supplier != null) {
                            // **如果选中了特定供应商，调用按供应商筛选的方法**
                            Log.d("InventoryVM", "Loading inventory for specific supplier: ${supplier.name}")
                            inventoryRepository.getInventoryItemsBySupplierFlow(supplier.id)
                        } else {
                            // **如果 selectedSupplier 为 null (即选中了“所有供应商”)，调用获取全部的方法**
                            Log.d("InventoryVM", "Loading inventory for all suppliers in store.")
                            inventoryRepository.getInventoryItemsWithProductInfoFlow(storeId)
                        }
                    }
                    .catch { e ->
                        Log.e("InventoryVM", "Error loading inventory.", e)
                        _uiState.update { it.copy(isLoadingInventory = false, errorMessage = "加载库存失败: ${e.message}") }
                    }
                    .collect { items ->
                        _uiState.update { it.copy(inventoryItems = items, isLoadingInventory = false) }
                    }

            }
        }
    }

    fun selectSupplier(supplier: Supplier?) { // 允许传入 null 来查看所有
        _uiState.update { it.copy(selectedSupplier = supplier) }
    }

    // TODO: 添加手动入库等方法
}