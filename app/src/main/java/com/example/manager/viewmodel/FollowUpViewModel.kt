package com.example.manager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.model.entity.FollowUp
import com.example.manager.data.model.uimodel.FollowUpWithCustomerName
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository
import com.example.manager.data.repository.FollowUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowUpListUiState(
    val followUps: List<FollowUpWithCustomerName> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FollowUpViewModel @Inject constructor(
    private val followUpRepository: FollowUpRepository,
    private val customerRepository: CustomerRepository, // 用于在添加跟进时搜索客户
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowUpListUiState())
    val uiState: StateFlow<FollowUpListUiState> = _uiState.asStateFlow()

    // --- 用于在添加跟进时，响应式搜索客户 ---
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    val customerSearchResults: StateFlow<List<Customer>> = _customerSearchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId != null && query.isNotBlank()) {
                customerRepository.searchCustomersByStoreIdFlow(query, storeId)
            } else {
                flowOf(emptyList())
            }
        }
        .catch { e -> Log.e("FollowUpVM", "Customer search error", e); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 响应式加载跟进列表
        viewModelScope.launch {
            sessionManager.userSessionFlow
                .map { it.storeId }
                .distinctUntilChanged()
                .flatMapLatest { storeId ->
                    if (storeId != null) {
                        _uiState.update { it.copy(isLoading = true) }
                        followUpRepository.getAllFollowUpsByStoreIdFlow(storeId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = "加载跟进列表失败") } }
                .collect { followUps ->
                    _uiState.update { it.copy(followUps = followUps, isLoading = false) }
                }
        }
    }

    fun onCustomerSearchQueryChanged(query: String) {
        _customerSearchQuery.value = query
    }

    fun addFollowUp(customerId: Long, notes: String) {
        viewModelScope.launch {
            val staffId = sessionManager.userSessionFlow.firstOrNull()?.staffId
            if (staffId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取用户信息，无法添加跟进") }
                return@launch
            }
            if (notes.isBlank()) {
                _uiState.update { it.copy(errorMessage = "跟进内容不能为空") }
                return@launch
            }

            val newFollowUp = FollowUp(
                customerId = customerId,
                orderId = null, // 手动添加的通用跟进，不关联特定订单
                followUpDate = System.currentTimeMillis(),
                notes = notes,
                staffId = staffId,
                isPlanned = false
            )

            followUpRepository.insertFollowUp(newFollowUp)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "跟进记录添加成功！") }
                    // 列表会自动刷新
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = "添加失败: ${e.message}") }
                }
        }
    }

    fun messageShown() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}