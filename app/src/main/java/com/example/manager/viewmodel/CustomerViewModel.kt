package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log // 建议添加 Log 以便调试
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository // 导入 Repository 接口
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 定义一个数据类来表示 UI 状态
data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
    // 注意：我们不在 UiState 中直接存储 customerToEditId 或 editingCustomer 对象，
    // 而是用一个单独的 StateFlow 来管理当前编辑的客户，这样更清晰。
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- 用于客户列表和搜索等主要 UI 状态 ---
    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    // --- 新增：用于管理当前正在编辑的客户对象 ---
    private val _editingCustomer = MutableStateFlow<Customer?>(null) // 初始为 null，表示没有客户在编辑
    val editingCustomer: StateFlow<Customer?> = _editingCustomer.asStateFlow()

    init {
        Log.d("CustomerViewModel", "ViewModel initialized")
        // 在 init 中加载客户时，也需要 storeId
        viewModelScope.launch {
            loadCustomersBasedOnSession()
        }
    }

    private suspend fun getCurrentStoreId(): Long? {
        val currentSession = sessionManager.userSessionFlow.firstOrNull()
        // 你可以根据业务逻辑决定，如果 storeId 为 null 是否要抛出异常或返回特定错误
        if (currentSession?.storeId == null) {
            Log.e("CustomerViewModel", "Critical: storeId is null in current session.")
        }
        return currentSession?.storeId
    }

    // 新的加载客户方法，它会先获取 storeId
    private suspend fun loadCustomersBasedOnSession() {
        val storeId = getCurrentStoreId()
        if (storeId == null) {
            _uiState.update { it.copy(isLoading = false, customers = emptyList(), errorMessage = "无法获取当前店铺信息，无法加载客户列表。") }
            Log.e("CustomerViewModel", "StoreId is null, cannot load customers.")
            return
        }
        loadCustomers(storeId) // 调用实际的加载方法
    }

    // 修改 loadCustomers 以接收 storeId
    fun loadCustomers(storeId: Long) { // 改为 public 或 internal 如果其他地方需要直接调用
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val customers = if (_uiState.value.searchQuery.isBlank()) {
                    customerRepository.getAllCustomersByStoreId(storeId)
                } else {
                    customerRepository.searchCustomers(_uiState.value.searchQuery, storeId)
                }
                _uiState.update { it.copy(customers = customers, isLoading = false) }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error loading customers for store $storeId", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "加载客户失败: ${e.localizedMessage}") }
            }
        }
    }


    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch { // 重新加载也需要 storeId
            loadCustomersBasedOnSession()
        }
    }

    // 添加客户时，也需要 storeId
    fun addCustomer(name: String, phone: String, address: String?, remark: String?) {
        viewModelScope.launch {
            val storeId = getCurrentStoreId()
            if (storeId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取当前店铺信息，无法添加客户。") }
                return@launch
            }

            // 提前检查电话号码在当前店铺是否已存在 (如果电话非空)
            val existingCustomerByPhone = customerRepository.getCustomerByPhoneAndStoreId(phone, storeId)
            if (existingCustomerByPhone != null) {
                _uiState.update { it.copy(errorMessage = "电话号码 '$phone' 在本店已存在。") }
                return@launch
            }


            val newCustomer = Customer(
                storeId = storeId, // **设置 storeId**
                name = name,
                phone = phone,
                address = address,
                remark = remark
            )
            customerRepository.insertCustomer(newCustomer)
                .onSuccess {
                    Log.d("CustomerViewModel", "Customer added successfully. Reloading list for store $storeId")
                    loadCustomers(storeId) // 使用已知的 storeId 重新加载
                }
                .onFailure { e ->
                    Log.e("CustomerViewModel", "Error adding customer for store $storeId", e)
                    val errorMsg = if (e is SQLiteConstraintException) "添加客户失败：电话号码可能已在本店注册。" else "添加客户失败: ${e.localizedMessage}"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                }
        }
    }

    // 更新客户时，Customer 对象应已包含正确的 storeId 和 id
    fun updateCustomer(customerToUpdate: Customer) {
        Log.d("CustomerViewModel", "Updating customer: ${customerToUpdate.name} for store: ${customerToUpdate.storeId}")
        viewModelScope.launch {
            // 提前检查电话号码唯一性 (如果电话有变动且非空)
            if (!customerToUpdate.phone.isNullOrBlank()) {
                val existingCustomerByPhone = customerRepository.getCustomerByPhoneAndStoreId(customerToUpdate.phone, customerToUpdate.storeId)
                // 如果找到的客户不是当前正在编辑的这个客户，说明电话号码冲突
                if (existingCustomerByPhone != null && existingCustomerByPhone.id != customerToUpdate.id) {
                    _uiState.update { it.copy(errorMessage = "电话号码 '${customerToUpdate.phone}' 在本店已被其他客户使用。") }
                    return@launch
                }
            }

            customerRepository.updateCustomer(customerToUpdate)
                .onSuccess { updatedRows ->
                    if (updatedRows > 0) {
                        Log.d("CustomerViewModel", "Customer updated successfully. Reloading list for store ${customerToUpdate.storeId}")
                        loadCustomers(customerToUpdate.storeId)
                        doneEditingCustomer()
                    } else {
                        Log.w("CustomerViewModel", "Update customer returned 0 rows affected.")
                        _uiState.update { it.copy(errorMessage = "更新客户失败，未找到记录或数据无变化。") }
                    }
                }
                .onFailure { e ->
                    Log.e("CustomerViewModel", "Error updating customer", e)
                    val errorMsg = if (e is SQLiteConstraintException) "更新客户失败：电话号码可能已在本店注册。" else "更新客户失败: ${e.localizedMessage}"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                }
        }
    }



    fun deleteCustomer(customer: Customer) { // 参数仍然可以是 Customer 对象，方便UI传递
        viewModelScope.launch {
            val storeId = getCurrentStoreId()
            if (storeId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取当前店铺信息，无法删除客户。") }
                return@launch
            }

            // **双重校验：确保要删除的客户属于当前店铺**
            if (customer.storeId != storeId) {
                Log.e("CustomerViewModel", "Attempted to delete customer (ID: ${customer.id}) not belonging to current store (ID: $storeId). Customer's store ID: ${customer.storeId}")
                _uiState.update { it.copy(errorMessage = "操作无效：试图删除不属于本店的客户。") }
                return@launch
            }

            try {
                // 调用新的、更安全的删除方法
                val deletedRows = customerRepository.deleteCustomerByIdAndStoreId(customer.id, storeId)
                if (deletedRows > 0) {
                    Log.d("CustomerViewModel", "Customer (ID: ${customer.id}) deleted successfully from store $storeId. Reloading list.")
                    loadCustomers(storeId) // 使用当前店铺ID重新加载
                } else {
                    // 这可能意味着客户已被其他人删除，或者 storeId 不匹配 (理论上上面的校验已处理)
                    _uiState.update { it.copy(errorMessage = "删除客户失败，未找到该客户或操作未影响任何行。") }
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error deleting customer", e)
                _uiState.update { it.copy(errorMessage = "删除客户失败: ${e.localizedMessage}") }
            }
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- 新增：用于编辑客户的方法 ---

    /**
     * 当用户选择编辑一个客户时调用。
     * 会从数据库加载该客户的完整信息并更新 _editingCustomer StateFlow。
     */
    fun startEditingCustomer(customerId: Long) {
        Log.d("CustomerViewModel", "Starting to edit customer with ID: $customerId")
        viewModelScope.launch {
            val storeId = getCurrentStoreId()
            if (storeId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取当前店铺信息，无法编辑客户。") }
                return@launch
            }
            try {
                val customer = customerRepository.getCustomerByIdAndStoreId(customerId, storeId) // 使用带 storeId 的查询
                _editingCustomer.value = customer
                if (customer == null) {
                    Log.w("CustomerViewModel", "Customer with ID $customerId not found in store $storeId for editing.")
                    _uiState.update { it.copy(errorMessage = "无法找到要编辑的客户信息。") }
                }
            } catch (e: Exception) { /* ... */ }
        }
    }


    /**
     * 当编辑对话框关闭（无论是保存还是取消）时调用，用于重置编辑状态。
     */
    fun doneEditingCustomer() {
        Log.d("CustomerViewModel", "Done editing customer.")
        _editingCustomer.value = null
    }


}