package com.example.manager.viewmodel // 确保包名正确

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.enums.StaffRole
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.preferences.UserSession
import com.example.manager.data.repository.StaffRepository
// import com.example.manager.util.PasswordHasher // TODO: 引入并使用安全的密码哈希工具
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 状态，包含加载、错误和导航事件
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationEvent: NavigationEvent = NavigationEvent.Idle, // 默认为 Idle
    val loggedInStaffInfo: Staff? = null // 用于登录成功后传递员工信息，可选
)

// 导航事件的 Sealed Class
sealed class NavigationEvent {
    object GoToMainApp : NavigationEvent()
    object GoToLogin : NavigationEvent()
    object GoToBossRegistration : NavigationEvent()
    object Idle : NavigationEvent() // 表示没有待处理的导航事件
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // 这个 StateFlow 用于在启动时决定是去老板注册还是登录/主应用
    private val _isInitialSetupNeeded = MutableStateFlow(true) // 初始假设需要设置
    val isInitialSetupNeeded: StateFlow<Boolean> = _isInitialSetupNeeded.asStateFlow()

    // 直接暴露 SessionManager 中的 userSessionFlow，供需要观察登录状态的地方使用
    val currentUserSessionFlow: Flow<UserSession> = sessionManager.userSessionFlow

    init {
        Log.d("AuthViewModel", "ViewModel Initialized. Checking initial app state...")
        checkInitialAppState()
    }

    private fun checkInitialAppState() {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true) }
            Log.d("AuthViewModel", "Checking if initial setup is needed...")
            val setupNeeded = staffRepository.isInitialSetupNeeded()
            _isInitialSetupNeeded.value = setupNeeded
            Log.d("AuthViewModel", "Initial setup needed: $setupNeeded")

            if (setupNeeded) {
                _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToBossRegistration) }
                Log.d("AuthViewModel", "Navigating to Boss Registration.")
            } else {
                Log.d("AuthViewModel", "Boss exists, checking session...")
                val currentSession = sessionManager.userSessionFlow.first() // 获取当前会话状态
                Log.d("AuthViewModel", "Current session: isLoggedIn=${currentSession.isLoggedIn}, staffId=${currentSession.staffId}")
                if (currentSession.isLoggedIn && currentSession.staffId != null && currentSession.staffRole != null) {
                    // 如果已登录，尝试获取完整的 Staff 信息（如果需要的话，或直接使用会话信息）
                    val staffFromDb = staffRepository.getStaffById(currentSession.staffId)
                    if (staffFromDb != null && staffFromDb.isActive) {
                        _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = staffFromDb) }
                        Log.d("AuthViewModel", "Session active. Navigating to Main App.")
                    } else {
                        // 会话无效或用户被禁用，清除会话并去登录
                        sessionManager.clearLoginSession()
                        _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToLogin) }
                        Log.d("AuthViewModel", "Session invalid or staff inactive. Navigating to Login.")
                    }
                } else {
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToLogin) }
                    Log.d("AuthViewModel", "No active session. Navigating to Login.")
                }
            }
        }
    }

    fun registerBossAccount(username: String, passwordAttempt: String, confirmPasswordAttempt: String, bossName: String = "老板") {
        _authUiState.update { it.copy(isLoading = true, error = null, navigationEvent = NavigationEvent.Idle) }
        Log.d("AuthViewModel", "Attempting to register boss: $username")
        viewModelScope.launch {
            try {
                if (username.isBlank() || passwordAttempt.isBlank() || bossName.isBlank()) {
                    _authUiState.update { it.copy(isLoading = false, error = "所有必填字段均不能为空") }
                    return@launch
                }
                if (passwordAttempt.length < 6) { // 简单的密码长度校验
                    _authUiState.update { it.copy(isLoading = false, error = "密码至少需要6位") }
                    return@launch
                }
                if (passwordAttempt != confirmPasswordAttempt) {
                    _authUiState.update { it.copy(isLoading = false, error = "两次输入的密码不一致") }
                    return@launch
                }

                if (!staffRepository.isInitialSetupNeeded()) {
                    _authUiState.update { it.copy(isLoading = false, error = "老板账户已存在，请直接登录", navigationEvent = NavigationEvent.GoToLogin) }
                    _isInitialSetupNeeded.value = false // 确保状态更新
                    return@launch
                }

                // TODO: Implement secure password hashing using a library like bcrypt
                // val hashedPassword = PasswordHasher.hashPassword(passwordAttempt)
                val hashedPassword = passwordAttempt // 开发初期简化
                Log.d("AuthViewModel", "Password for boss $username (simplified): $hashedPassword")


                val newBoss = Staff(
                    name = bossName,
                    role = StaffRole.BOSS,
                    username = username,
                    passwordHash = hashedPassword,
                    isActive = true
                )
                val newStaffId = staffRepository.insertOrUpdateStaff(newBoss)
                Log.d("AuthViewModel", "Boss registered with ID: $newStaffId")


                if (newStaffId > 0) {
                    val registeredBoss = newBoss.copy(id = newStaffId)
                    sessionManager.saveLoginSession(registeredBoss) // 保存完整 Staff 对象
                    _isInitialSetupNeeded.value = false
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = registeredBoss) }
                } else {
                    _authUiState.update { it.copy(isLoading = false, error = "老板账户注册失败") }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error registering boss", e)
                _authUiState.update { it.copy(isLoading = false, error = "注册老板账户时发生错误: ${e.localizedMessage}") }
            }
        }
    }

    fun login(username: String, passwordAttempt: String) {
        _authUiState.update { it.copy(isLoading = true, error = null, navigationEvent = NavigationEvent.Idle) }
        Log.d("AuthViewModel", "Attempting to login user: $username")
        viewModelScope.launch {
            try {
                if (username.isBlank() || passwordAttempt.isBlank()) {
                    _authUiState.update { it.copy(isLoading = false, error = "用户名和密码不能为空") }
                    return@launch
                }

                val staff = staffRepository.getStaffByUsername(username)

                if (staff == null || !staff.isActive) {
                    _authUiState.update { it.copy(isLoading = false, error = "用户名或密码错误，或账户无效") }
                    return@launch
                }

                // TODO: Implement secure password verification using PasswordHasher.verifyPassword
                // val isPasswordCorrect = PasswordHasher.verifyPassword(passwordAttempt, staff.passwordHash)
                val isPasswordCorrect = (passwordAttempt == staff.passwordHash) // 开发初期简化
                Log.d("AuthViewModel", "Password check for $username: $isPasswordCorrect (simplified)")


                if (isPasswordCorrect) {
                    sessionManager.saveLoginSession(staff) // 保存完整 Staff 对象
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = staff) }
                } else {
                    _authUiState.update { it.copy(isLoading = false, error = "用户名或密码错误") }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during login", e)
                _authUiState.update { it.copy(isLoading = false, error = "登录时发生错误: ${e.localizedMessage}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true) }
            Log.d("AuthViewModel", "Logging out...")
            sessionManager.clearLoginSession()
            _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToLogin, loggedInStaffInfo = null) }
        }
    }

    fun navigationEventConsumed() {
        Log.d("AuthViewModel", "Navigation event consumed.")
        _authUiState.update { it.copy(navigationEvent = NavigationEvent.Idle) }
    }

    fun errorShown() {
        Log.d("AuthViewModel", "Error message shown and consumed.")
        _authUiState.update { it.copy(error = null) }
    }

    // --- 员工管理相关方法 (可以考虑移到专门的 StaffViewModel) ---
    private val _allStaffState = MutableStateFlow<List<Staff>>(emptyList())
    val allStaffState: StateFlow<List<Staff>> = _allStaffState.asStateFlow()

    fun loadAllStaff() {
        // 应该检查当前用户是否有权限查看所有员工
        viewModelScope.launch {
            _allStaffState.value = staffRepository.getAllStaffs()
        }
    }

    fun addOrUpdateStaff(staff: Staff, isNewUser: Boolean = true) {
        // 应该检查当前用户是否有权限添加/更新员工
        viewModelScope.launch {
            try {
                var staffToSave = staff
                if (isNewUser) {
                    // TODO: Implement secure password hashing for new staff
                    // staffToSave = staff.copy(passwordHash = PasswordHasher.hashPassword(staff.passwordHash))
                    Log.d("AuthViewModel", "Adding new staff ${staff.username} with simplified password.")
                } else {
                    Log.d("AuthViewModel", "Updating staff ${staff.username}.")
                }
                staffRepository.insertOrUpdateStaff(staffToSave)
                loadAllStaff()
                // 可以添加成功提示
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error adding/updating staff", e)
                _authUiState.update { it.copy(error = "操作员工信息失败: ${e.localizedMessage}") }
            }
        }
    }
}