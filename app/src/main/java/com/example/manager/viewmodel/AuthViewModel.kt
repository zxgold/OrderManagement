package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.entity.Store
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.preferences.UserSession
import com.example.manager.data.repository.StaffRepository
import com.example.manager.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// =================================================================================
// Data Classes for UI State
// =================================================================================

/**
 * 认证流程 (登录、注册、启动检查) 的 UI 状态
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationEvent: NavigationEvent = NavigationEvent.Idle,
    val loggedInStaffInfo: Staff? = null
)

/**
 * 员工管理界面的 UI 状态
 * 一个简单的数据类，持有员工列表、加载和消息状态
 */
data class StaffManagementUiState(
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = true, // 初始为 true，等待 Flow 加载
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * 导航事件
 */
sealed class NavigationEvent {
    object GoToMainApp : NavigationEvent()
    object GoToLogin : NavigationEvent()
    object GoToRegistration : NavigationEvent() // 统一使用 GoToRegistration
    object Idle : NavigationEvent()
}


// =================================================================================
// ViewModel
// =================================================================================

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val storeRepository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- StateFlows for different UI concerns ---
    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _staffManagementUiState = MutableStateFlow(StaffManagementUiState())
    val staffManagementUiState: StateFlow<StaffManagementUiState> = _staffManagementUiState.asStateFlow()

    val isInitialSetupNeeded: StateFlow<Boolean> = flow {
        emit(staffRepository.isInitialSetupNeeded())
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val currentUserSessionFlow: Flow<UserSession> = sessionManager.userSessionFlow

    init {
        Log.d("AuthViewModel", "ViewModel Initialized.")
        checkInitialAppState()
        observeStaffList() // 启动对员工列表的响应式观察
    }

    // =================================================================================
    // Authentication and Session Logic
    // =================================================================================

    private fun checkInitialAppState() {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, navigationEvent = NavigationEvent.Idle) }
            val currentSession = sessionManager.userSessionFlow.first()
            if (currentSession.isLoggedIn && currentSession.staffId != null) {
                val staffFromDb = staffRepository.getStaffById(currentSession.staffId)
                if (staffFromDb != null && staffFromDb.isActive) {
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = staffFromDb) }
                } else {
                    sessionManager.clearLoginSession()
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToLogin) }
                }
            } else {
                _authUiState.update { it.copy(isLoading = false, navigationEvent = if (staffRepository.isInitialSetupNeeded()) NavigationEvent.GoToRegistration else NavigationEvent.GoToLogin) }
            }
        }
    }

    fun registerAccount(username: String, passwordAttempt: String, confirmPasswordAttempt: String, staffName: String, storeName: String) {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, error = null) }
            // --- Validations ---
            if (username.isBlank() || passwordAttempt.isBlank() || staffName.isBlank() || storeName.isBlank()) {
                _authUiState.update { it.copy(isLoading = false, error = "所有必填字段均不能为空") }; return@launch
            }
            if (passwordAttempt != confirmPasswordAttempt) {
                _authUiState.update { it.copy(isLoading = false, error = "两次输入的密码不一致") }; return@launch
            }
            if (staffRepository.getStaffByUsername(username) != null) {
                _authUiState.update { it.copy(isLoading = false, error = "登录用户名 '$username' 已被注册") }; return@launch
            }
            if (storeRepository.getStoreByName(storeName) != null) {
                _authUiState.update { it.copy(isLoading = false, error = "店铺名称 '$storeName' 已存在") }; return@launch
            }

            // --- Logic ---
            try {
                // 1. Create Store
                val newStoreId = storeRepository.insertStore(Store(storeName = storeName))
                if (newStoreId <= 0) throw Exception("创建店铺失败")

                // 2. Create Staff (BOSS)
                // TODO: Implement secure password hashing
                val hashedPassword = passwordAttempt
                val newStaff = Staff(storeId = newStoreId, name = staffName, role = StaffRole.BOSS, username = username, passwordHash = hashedPassword)
                val newStaffId = staffRepository.insertOrUpdateStaff(newStaff).getOrThrow()

                // 3. (Optional but good) Update Store with owner ID
                storeRepository.updateStore(Store(id = newStoreId, storeName = storeName, ownerStaffId = newStaffId, createdAt = System.currentTimeMillis()))

                // 4. Save session and navigate
                val registeredStaff = newStaff.copy(id = newStaffId)
                sessionManager.saveLoginSession(registeredStaff, newStoreId, storeName)
                _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = registeredStaff) }

            } catch (e: Exception) {
                _authUiState.update { it.copy(isLoading = false, error = "注册失败: ${e.message}") }
            }
        }
    }

    fun login(username: String, passwordAttempt: String) {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, error = null) }
            try {
                val staff = staffRepository.getStaffByUsername(username)
                if (staff == null || !staff.isActive) {
                    _authUiState.update { it.copy(isLoading = false, error = "用户名或密码错误，或账户无效") }; return@launch
                }
                // TODO: Implement secure password verification
                val isPasswordCorrect = (passwordAttempt == staff.passwordHash)
                if (isPasswordCorrect) {
                    val store = storeRepository.getStoreById(staff.storeId) ?: throw IllegalStateException("找不到员工关联的店铺")
                    sessionManager.saveLoginSession(staff, store.id, store.storeName)
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = staff) }
                } else {
                    _authUiState.update { it.copy(isLoading = false, error = "用户名或密码错误") }
                }
            } catch (e: Exception) {
                _authUiState.update { it.copy(isLoading = false, error = "登录时发生错误: ${e.message}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearLoginSession()
            _authUiState.update { it.copy(navigationEvent = NavigationEvent.GoToLogin, loggedInStaffInfo = null) }
        }
    }

    fun navigationEventConsumed() {
        _authUiState.update { it.copy(navigationEvent = NavigationEvent.Idle) }
    }

    fun authErrorShown() {
        _authUiState.update { it.copy(error = null) }
    }


    // =================================================================================
    // Staff Management Logic
    // =================================================================================

    private fun observeStaffList() {
        viewModelScope.launch {
            sessionManager.userSessionFlow
                .map { it.storeId }
                .distinctUntilChanged()
                .flatMapLatest { storeId ->
                    if (storeId != null) {
                        staffRepository.getAllStaffsByStoreIdFlow(storeId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .catch { e -> _staffManagementUiState.update { it.copy(isLoading = false, errorMessage = "加载员工列表失败") } }
                .collect { staffList ->
                    _staffManagementUiState.update { it.copy(staffList = staffList, isLoading = false) }
                }
        }
    }

    fun addStaff(name: String, username: String, initialPassword: String, role: StaffRole) {
        viewModelScope.launch {
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) { /* ... 错误处理 ... */ return@launch }
            if (role == StaffRole.BOSS) { /* ... 错误处理 ... */ return@launch }
            if (staffRepository.getStaffByUsername(username) != null) { /* ... 错误处理 ... */ return@launch }

            val hashedPassword = initialPassword // TODO: Hash this
            val newStaff = Staff(storeId = storeId, name = name, username = username, passwordHash = hashedPassword, role = role)

            staffRepository.insertOrUpdateStaff(newStaff)
                .onSuccess { _staffManagementUiState.update { it.copy(successMessage = "员工 “$name” 添加成功") } }
                .onFailure { e -> _staffManagementUiState.update { it.copy(errorMessage = "添加员工失败: ${e.message}") } }
        }
    }

    fun toggleStaffStatus(staff: Staff) {
        viewModelScope.launch {
            val currentSession = sessionManager.userSessionFlow.firstOrNull()
            if (currentSession?.staffRole != StaffRole.BOSS || currentSession.storeId != staff.storeId) {
                _staffManagementUiState.update { it.copy(errorMessage = "权限不足") }; return@launch
            }
            val updatedStaff = staff.copy(isActive = !staff.isActive, updatedAt = System.currentTimeMillis())
            staffRepository.updateStaff(updatedStaff)
                .onSuccess {
                    val statusText = if (updatedStaff.isActive) "已激活" else "已禁用"
                    _staffManagementUiState.update { it.copy(successMessage = "员工 “${staff.name}” 状态已更新为“${statusText}”") }
                }.onFailure { e->
                    _staffManagementUiState.update { it.copy(errorMessage = "更新员工状态失败: ${e.message}") }
                }
        }
    }

    fun staffManagementMessageShown() {
        _staffManagementUiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}