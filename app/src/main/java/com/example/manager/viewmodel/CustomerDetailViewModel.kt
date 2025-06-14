package com.example.manager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.preferences.SessionManager // 仍然需要 sessionManager 获取 storeId
import com.example.manager.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerDetailUIState( // 和之前的 CustomerDetailUiState 一样
    val customer: Customer? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUIState())
    val uiState: StateFlow<CustomerDetailUIState> = _uiState.asStateFlow()

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L // 假设路由参数是Long

    init {
        if (customerId != -1L) {
            loadCustomerDetails()
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "无效的客户ID") }
        }
    }

    fun loadCustomerDetails() { // 可以设为 public 以便下拉刷新等操作
        if (customerId == -1L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "无法加载：无效的客户ID") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "无法获取店铺信息") }
                return@launch
            }
            try {
                val customer = customerRepository.getCustomerByIdAndStoreId(customerId, storeId)
                _uiState.update { it.copy(customer = customer, isLoading = false, errorMessage = if(customer == null) "未找到客户" else null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "加载客户详情失败: ${e.localizedMessage}") }
            }
        }
    }
    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}