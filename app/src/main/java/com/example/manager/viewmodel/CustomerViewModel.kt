package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log // 建议添加 Log 以便调试
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository // 导入 Repository 接口
import com.example.manager.data.repository.OrderRepository
import com.example.manager.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddCustomerResult {
    data class Success(val newCustomerId: Long) : AddCustomerResult()
    data class Failure(val errorMessage: String) : AddCustomerResult()
}

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
    private val sessionManager: SessionManager,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    // --- 用于客户列表和搜索等主要 UI 状态 ---
    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    // 将 loadCustomersBasedOnSession 合并到 init 或 refreshCustomerList 中
    // 或者保持它为 private，让 refreshCustomerList 调用它
    init {
        Log.d("CustomerViewModel", "ViewModel initialized")
        refreshCustomerList() // init 时也调用刷新
    }

    // --- 用于广播添加客户的结果 ---
    private val _addCustomerResultChannel = Channel<AddCustomerResult>()
    val addCustomerResultFlow = _addCustomerResultChannel.receiveAsFlow()
    // ------------------------------------

    // 我们将 loadCustomersBasedOnSession 的核心逻辑提取出来，并使其 public
    fun refreshCustomerList() {
        viewModelScope.launch {
            Log.d("CustomerViewModel", "refreshCustomerList called.")
            val storeId = getCurrentStoreId()
            if (storeId == null) {
                _uiState.update { it.copy(isLoading = false, customers = emptyList(), errorMessage = "无法获取当前店铺信息，无法加载客户列表。") }
                return@launch
            }
            // 重新加载当前搜索条件下的客户列表
            loadCustomers(storeId)
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

    private suspend fun loadCustomersBasedOnSession() {
        val storeId = getCurrentStoreId() ?: run {
            _uiState.update { it.copy(isLoading = false, errorMessage = "无法获取店铺信息") }
            return
        }
        // 对于响应式，我们应该直接订阅 Flow，而不是一次性加载
        // 我们在 loadCustomers 中处理
    }

    // searchCustomers 也需要返回 Flow
    fun searchCustomers(query: String) {
        viewModelScope.launch {
            val storeId = getCurrentStoreId() ?: return@launch
            // 响应式搜索应该在 OrderViewModel 中实现，CustomerViewModel 只负责列表页的简单搜索
            val customers = customerRepository.searchCustomers(query, storeId)
            _uiState.update { it.copy(customers = customers) }
        }
    }

    fun loadCustomers(storeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // **从 Repository 获取 Flow 并 collect**
                customerRepository.getAllCustomersByStoreIdFlow(storeId)
                    .catch { e ->
                        Log.e("CustomerViewModel", "Error collecting customers", e)
                        _uiState.update { it.copy(isLoading = false, errorMessage = "加载客户列表失败") }
                    }
                    .collect { customers ->
                        _uiState.update { it.copy(customers = customers, isLoading = false) }
                    }
            } catch (e: Exception) {
                // `collect` 通常会捕获 Flow 的异常，但这里保留以防万一
                _uiState.update { it.copy(isLoading = false, errorMessage = "加载客户时发生未知错误") }
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
    // --- **修改 addCustomer 方法以发送结果** ---
    fun addCustomer(name: String, phone: String, address: String?, remark: String?) {
        viewModelScope.launch {
            val storeId = getCurrentStoreId()
            if (storeId == null) {
                _addCustomerResultChannel.send(AddCustomerResult.Failure("无法获取店铺信息，无法添加客户。"))
                return@launch
            }
            if (customerRepository.getCustomerByPhoneAndStoreId(phone, storeId) != null) {
                _addCustomerResultChannel.send(AddCustomerResult.Failure("电话号码 '$phone' 在本店已存在。"))
                return@launch
            }
            val newCustomer = Customer(storeId = storeId, name = name, phone = phone, address = address, remark = remark)
            customerRepository.insertCustomer(newCustomer)
                .onSuccess { newId ->
                    Log.d("CustomerViewModel", "Customer added successfully with ID: $newId")
                    _addCustomerResultChannel.send(AddCustomerResult.Success(newId))
                    // 列表会自动刷新，因为我们使用了 Flow
                }
                .onFailure { e ->
                    val errorMsg = if (e is SQLiteConstraintException) "添加失败：数据冲突。" else "添加客户失败: ${e.localizedMessage}"
                    _addCustomerResultChannel.send(AddCustomerResult.Failure(errorMsg))
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
}