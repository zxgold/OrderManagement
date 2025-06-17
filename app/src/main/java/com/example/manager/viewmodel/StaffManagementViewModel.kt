package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * 这个ViewModel的职责是：
 * 1. 从当前会话获取storeId
 * 2. 加载并持有一个响应式的员工列表
 * 3. 处理添加、更新、切换员工状态等业务逻辑
 */

// StaffManagementUiState：一个简单的数据类，持有员工列表、加载和消息状态
data class StaffManagementUiState(
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState.asStateFlow()

    // ------ init：响应式加载 ------>>>>>
    init {
        // 使用响应式的方式加载员工列表
        viewModelScope.launch {
            sessionManager.userSessionFlow
                .map { it.storeId } // 1. 获取 storeId
                .distinctUntilChanged() // 2. 只有当 storeId 变化时才继续 (虽然在登录后它通常不变)
                .flatMapLatest { storeId -> // 3. 切换到新的员工列表 Flow
                    if (storeId != null) {
                        _uiState.update { it.copy(isLoading = true) }
                        staffRepository.getAllStaffsByStoreIdFlow(storeId)
                    } else {
                        // 如果没有 storeId，返回一个空列表的 Flow
                        flowOf(emptyList())
                    }
                }
                .catch { e ->
                    Log.e("StaffManagementVM", "Error collecting staff list", e)
                    _uiState.update { it.copy(isLoading = false, errorMessage = "加载员工列表失败") }
                }
                .collect { staffList ->
                    _uiState.update { it.copy(staffList = staffList, isLoading = false) }
                }
        }
    }
    // <<<<<----- init

    fun addStaff(name: String, username: String, initialPassword: String, role: StaffRole) {
        viewModelScope.launch {
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) {
                _uiState.update { it.copy(errorMessage = "无法获取店铺信息，无法添加员工") }
                return@launch
            }
            if (name.isBlank() || username.isBlank() || initialPassword.isBlank()) {
                _uiState.update { it.copy(errorMessage = "姓名、用户名和初始密码不能为空") }
                return@launch
            }
            if (role == StaffRole.BOSS) { // 安全检查
                _uiState.update { it.copy(errorMessage = "不能通过此方式添加老板角色") }
                return@launch
            }

            // 检查用户名是否已存在
            if (staffRepository.getStaffByUsername(username) != null) {
                _uiState.update { it.copy(errorMessage = "登录用户名 '$username' 已存在") }
                return@launch
            }

            // TODO: 对 initialPassword 进行安全的哈希处理
            val hashedPassword = initialPassword

            val newStaff = Staff(
                storeId = storeId,
                name = name,
                username = username,
                passwordHash = hashedPassword,
                role = role,
                isActive = true // 新员工默认激活
            )

            staffRepository.insertOrUpdateStaff(newStaff)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "员工 “$name” 添加成功") }
                    // 列表会自动刷新
                }
                .onFailure { e ->
                    val errorMsg = if (e is SQLiteConstraintException) "添加失败：数据冲突" else "添加员工失败: ${e.localizedMessage}"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                }
        }
    }

    fun toggleStaffStatus(staff: Staff) {
        viewModelScope.launch {
            val updatedStaff = staff.copy(
                isActive = !staff.isActive,
                updatedAt = System.currentTimeMillis()
            )
            staffRepository.updateStaff(updatedStaff)
                .onSuccess {
                    val statusText = if (updatedStaff.isActive) "激活" else "禁用"
                    _uiState.update { it.copy(successMessage = "员工 “${staff.name}” 已${statusText}") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = "更新员工状态失败: ${e.localizedMessage}") }
                }
        }
    }

    // TODO: 添加 updateStaffInfo 和 resetPassword 方法

    fun messageShown() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}