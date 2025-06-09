package com.example.manager.viewmodel

import android.util.Log // 建议添加 Log 以便调试
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.repository.CustomerRepository // 导入 Repository 接口
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // --- 用于客户列表和搜索等主要 UI 状态 ---
    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    // --- 新增：用于管理当前正在编辑的客户对象 ---
    private val _editingCustomer = MutableStateFlow<Customer?>(null) // 初始为 null，表示没有客户在编辑
    val editingCustomer: StateFlow<Customer?> = _editingCustomer.asStateFlow()

    init {
        Log.d("CustomerViewModel", "ViewModel initialized")
        loadCustomers()
    }

    fun loadCustomers() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val customers = if (_uiState.value.searchQuery.isBlank()) {
                    customerRepository.getAllCustomers()
                } else {
                    customerRepository.searchCustomers(_uiState.value.searchQuery)
                }
                _uiState.update {
                    it.copy(customers = customers, isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error loading customers", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "加载客户失败: ${e.localizedMessage}")
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadCustomers()
    }

    fun addCustomer(name: String, phone: String?, address: String?, remark: String? = null) { // 添加 remark 参数
        viewModelScope.launch {
            try {
                // 注意：Customer 构造函数需要与你的 Entity 定义匹配
                // 如果你的 Customer Entity 有 remark 字段，请确保构造函数包含它
                val newCustomer = Customer(
                    name = name,
                    phone = phone,
                    address = address,
                    remark = remark // 使用 remark
                )
                customerRepository.insertOrUpdateCustomer(newCustomer)
                loadCustomers()
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error adding customer", e)
                _uiState.update {
                    it.copy(errorMessage = "添加客户失败: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                val deletedRows = customerRepository.deleteCustomer(customer)
                if (deletedRows > 0) {
                    loadCustomers()
                } else {
                    _uiState.update { it.copy(errorMessage = "删除客户失败，未找到该客户或存在关联订单") }
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
            try {
                val customer = customerRepository.getCustomerById(customerId)
                if (customer != null) {
                    _editingCustomer.value = customer
                    Log.d("CustomerViewModel", "Customer to edit loaded: ${customer.name}")
                } else {
                    _editingCustomer.value = null // 确保如果找不到也设为 null
                    _uiState.update { it.copy(errorMessage = "无法找到要编辑的客户信息") }
                    Log.w("CustomerViewModel", "Customer with ID $customerId not found for editing.")
                }
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error loading customer for editing", e)
                _editingCustomer.value = null
                _uiState.update { it.copy(errorMessage = "加载编辑客户信息失败: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * 当编辑对话框关闭（无论是保存还是取消）时调用，用于重置编辑状态。
     */
    fun doneEditingCustomer() {
        Log.d("CustomerViewModel", "Done editing customer.")
        _editingCustomer.value = null
    }

    /**
     * 保存更新后的客户信息。
     */
    fun updateCustomer(customerToUpdate: Customer) { // 确保参数是更新后的 Customer 对象
        Log.d("CustomerViewModel", "Updating customer: ${customerToUpdate.name}")
        viewModelScope.launch {
            try {
                // CustomerRepository.insertOrUpdateCustomer 会根据 ID 执行更新
                customerRepository.insertOrUpdateCustomer(customerToUpdate)
                Log.d("CustomerViewModel", "Customer updated successfully. Reloading list.")
                loadCustomers() // 重新加载列表以显示更新
                doneEditingCustomer() // 清除编辑状态
            } catch (e: Exception) {
                Log.e("CustomerViewModel", "Error updating customer", e)
                _uiState.update {
                    it.copy(errorMessage = "更新客户失败: ${e.localizedMessage}")
                }
                // 选择性地保留编辑状态或清除，取决于你希望的用户体验
                // doneEditingCustomer()
            }
        }
    }
}