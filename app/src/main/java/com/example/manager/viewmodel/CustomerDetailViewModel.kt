package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
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

    // --- 新增：用于处理客户更新的方法 ---
    // 这个方法将被 EditCustomerDialog 或 EditCustomerScreen 调用
    suspend fun saveUpdatedCustomer(customerToUpdate: Customer): Result<Boolean> {
        _uiState.update { it.copy(isLoading = true) } // 可以用一个不同的加载状态，比如 isSaving
        val currentStoreId = sessionManager.userSessionFlow.firstOrNull()?.storeId
        if (currentStoreId == null || customerToUpdate.storeId != currentStoreId) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "店铺信息不匹配，无法更新") }
            return Result.failure(IllegalStateException("Store ID mismatch or missing"))
        }

        // 电话号码唯一性检查 (如果电话有变动)
        if (!customerToUpdate.phone.isBlank()) { // 假设 phone 是 String (非空)
            val existingCustomerByPhone = customerRepository.getCustomerByPhoneAndStoreId(customerToUpdate.phone, currentStoreId)
            if (existingCustomerByPhone != null && existingCustomerByPhone.id != customerToUpdate.id) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "电话号码 '${customerToUpdate.phone}' 在本店已被其他客户使用。") }
                return Result.failure(IllegalArgumentException("Phone number already exists for another customer in this store."))
            }
        }

        val result = customerRepository.updateCustomer(customerToUpdate.copy(updatedAt = System.currentTimeMillis()))
        result.onSuccess { updatedRows ->
            if (updatedRows > 0) {
                Log.d("CustomerDetailVM", "Customer updated successfully.")
                loadCustomerDetails() // 更新成功后重新加载详情以显示最新数据
                _uiState.update { it.copy(isLoading = false) } // 清除加载状态
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "更新客户失败，未影响任何行。") }
            }
        }.onFailure { e ->
            val errorMsg = if (e is SQLiteConstraintException) "更新客户失败：数据冲突（如电话重复）。" else "更新客户失败: ${e.localizedMessage}"
            _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
        }
        return result.map { it > 0 } // 返回操作是否成功 (影响行数 > 0)
    }
}

