package com.example.manager.viewmodel

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
)

@HiltViewModel // <-- **关键注解：标记为 Hilt ViewModel**
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository // <-- Hilt 注入 Repository 接口
) : ViewModel() {

    // 使用 StateFlow 暴露 UI 状态
    private val _uiState = MutableStateFlow(CustomerListUiState()) // 初始状态
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    // 在 ViewModel 初始化时加载客户列表
    init {
        loadCustomers()
    }

    // 加载客户数据的方法
    fun loadCustomers() {
        // 更新状态为加载中
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // 使用 viewModelScope 启动协程执行后台任务
        viewModelScope.launch {
            try {
                // 根据搜索条件决定是获取所有客户还是搜索客户
                val customers = if (_uiState.value.searchQuery.isBlank()) {
                    customerRepository.getAllCustomers()
                } else {
                    customerRepository.searchCustomers(_uiState.value.searchQuery)
                }
                // 更新状态，显示客户列表，结束加载
                _uiState.update {
                    it.copy(customers = customers, isLoading = false)
                }
            } catch (e: Exception) {
                // 发生错误，更新状态显示错误信息
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "加载客户失败: ${e.localizedMessage}")
                }
                // 可以在这里添加更详细的日志记录
                // Log.e("CustomerViewModel", "Error loading customers", e)
            }
        }
    }

    // 处理搜索查询变化的方法
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // 触发重新加载，可以添加 debounce 优化防止频繁加载
        loadCustomers()
    }

    // 添加新客户的方法 (示例)
    fun addCustomer(name: String, phone: String?, address: String?) {
        viewModelScope.launch {
            try {
                val newCustomer = Customer(name = name, phone = phone, address = address)
                customerRepository.insertOrUpdateCustomer(newCustomer)
                // 添加成功后，重新加载列表以显示新客户
                loadCustomers()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "添加客户失败: ${e.localizedMessage}")
                }
            }
        }
    }

    // 删除客户的方法 (示例)
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                val deletedRows = customerRepository.deleteCustomer(customer)
                if (deletedRows > 0) {
                    // 删除成功后，重新加载列表
                    loadCustomers()
                } else {
                    _uiState.update { it.copy(errorMessage = "删除客户失败，未找到该客户或存在关联订单") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "删除客户失败: ${e.localizedMessage}") }
            }
        }
    }

    // 清除错误信息的方法 (UI 可以在显示后调用)
    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}