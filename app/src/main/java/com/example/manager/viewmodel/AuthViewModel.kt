package com.example.manager.viewmodel // 确保包名正确

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
    private val sessionManager: SessionManager,
    private val storeRepository: StoreRepository
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
            // 重置导航事件并开始加载状态
            _authUiState.update { it.copy(isLoading = true, navigationEvent = NavigationEvent.Idle) }
            Log.d("AuthViewModel", "Checking session for initial navigation...")

            // 仍然检查并更新 isInitialSetupNeeded，其他屏幕可能需要这个信息
            // 但它不再直接决定初始导航去哪里
            val setupNeeded = staffRepository.isInitialSetupNeeded()
            _isInitialSetupNeeded.value = setupNeeded
            Log.d("AuthViewModel", "Initial setup needed (for reference): $setupNeeded")

            // 获取当前会话状态
            val currentSession = sessionManager.userSessionFlow.first()
            Log.d("AuthViewModel", "Current session on app start: isLoggedIn=${currentSession.isLoggedIn}, staffId=${currentSession.staffId}, role=${currentSession.staffRole}")

            if (currentSession.isLoggedIn && currentSession.staffId != null && currentSession.staffRole != null) {
                // 如果会话中声称已登录，验证数据库中的员工信息
                val staffFromDb = staffRepository.getStaffById(currentSession.staffId)
                if (staffFromDb != null && staffFromDb.isActive) {
                    // 会话有效且员工活动，导航到主应用
                    _authUiState.update {
                        it.copy(
                            isLoading = false,
                            navigationEvent = NavigationEvent.GoToMainApp,
                            loggedInStaffInfo = staffFromDb // 传递员工信息
                        )
                    }
                    Log.d("AuthViewModel", "Session active and staff valid. Navigating to Main App.")
                } else {
                    // 会话中的用户ID在数据库中找不到，或者用户已被禁用
                    Log.w("AuthViewModel", "Session staffId ${currentSession.staffId} not found in DB or staff is inactive. Clearing session.")
                    sessionManager.clearLoginSession() // 清除无效会话
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToLogin) }
                    Log.d("AuthViewModel", "Invalid session. Navigating to Login.")
                }
            } else {
                // 没有任何有效会话，导航到登录界面
                _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToLogin) }
                Log.d("AuthViewModel", "No active session. Navigating to Login.")
            }
        }
    }

    fun registerAccount(
        username: String,
        passwordAttempt: String,
        confirmPasswordAttempt: String,
        staffName: String, // 之前可能是 bossName
        storeName: String  // 新增参数
    ) {
        _authUiState.update { it.copy(isLoading = true, error = null, navigationEvent = NavigationEvent.Idle) }
        Log.d("AuthViewModel", "Attempting to register account: $username for staff: $staffName, store: $storeName")
        viewModelScope.launch {
            try {
                // 1. 输入校验
                if (username.isBlank() || passwordAttempt.isBlank() || staffName.isBlank() || storeName.isBlank()) {
                    _authUiState.update { it.copy(isLoading = false, error = "所有带 * 标记的字段均不能为空") }
                    return@launch
                }
                if (passwordAttempt.length < 6) {
                    _authUiState.update { it.copy(isLoading = false, error = "密码至少需要6位") }
                    return@launch
                }
                if (passwordAttempt != confirmPasswordAttempt) {
                    _authUiState.update { it.copy(isLoading = false, error = "两次输入的密码不一致") }
                    return@launch
                }

                // 2. 用户名唯一性检查
                val existingUser = staffRepository.getStaffByUsername(username)
                if (existingUser != null) {
                    _authUiState.update { it.copy(isLoading = false, error = "登录用户名 '$username' 已被注册") }
                    return@launch
                }

                // 3. 判断是否首次设置，并确定角色
                val isFirstSetup = staffRepository.isInitialSetupNeeded()
                val userRole = StaffRole.BOSS // 当前公共注册默认都创建BOSS

                // 4. 处理店铺创建或检查
                var newStoreId: Long
                val existingStoreByName = storeRepository.getStoreByName(storeName) // 检查店铺名是否已存在

                if (isFirstSetup) {
                    if (existingStoreByName != null) {
                        // 理论上首次设置，店铺名不应存在，除非之前有残留数据或并发问题
                        _authUiState.update { it.copy(isLoading = false, error = "店铺名称 '$storeName' 已作为初始店铺存在，请尝试其他名称或联系支持。") }
                        return@launch
                    }
                    val newStore = Store(storeName = storeName)
                    newStoreId = storeRepository.insertStore(newStore)
                    if (newStoreId <= 0) {
                        _authUiState.update { it.copy(isLoading = false, error = "创建新店铺失败 (首次设置)") }
                        return@launch
                    }
                    Log.d("AuthViewModel", "New store '$storeName' created with ID: $newStoreId for first setup.")
                } else {
                    // 非首次设置，如果允许通过公共注册创建新店铺
                    if (existingStoreByName != null) {
                        // 如果要求店铺名全局唯一，且已存在，则报错
                        // 如果允许同名店铺（不推荐，除非有其他区分方式），则可以继续创建
                        // 为了简化，我们假设店铺名需要唯一
                        _authUiState.update { it.copy(isLoading = false, error = "店铺名称 '$storeName' 已存在，请使用其他名称或联系现有老板在该店铺下添加账户。") }
                        return@launch
                    }
                    val newStore = Store(storeName = storeName)
                    newStoreId = storeRepository.insertStore(newStore)
                    if (newStoreId <= 0) {
                        _authUiState.update { it.copy(isLoading = false, error = "创建新店铺失败") }
                        return@launch
                    }
                    Log.d("AuthViewModel", "New store '$storeName' created with ID: $newStoreId.")
                }

                // 5. 密码哈希 (TODO)
                val hashedPassword = passwordAttempt // 开发初期简化

                // 6. 创建 Staff 对象
                val newStaff = Staff(
                    storeId = newStoreId, // 关联到新创建或已存在的店铺ID
                    name = staffName,
                    role = userRole,
                    username = username,
                    passwordHash = hashedPassword,
                    isActive = true
                )
                val newStaffId = staffRepository.insertOrUpdateStaff(newStaff)
                Log.d("AuthViewModel", "Staff '$username' registered with ID: $newStaffId, role: $userRole, for storeId: $newStoreId")

                if (newStaffId > 0) {
                    // 7. (可选) 回填 Store 的 ownerStaffId (仅当创建新店铺且是BOSS时)
                    if (userRole == StaffRole.BOSS) { // 只有老板创建店铺时回填
                        val createdStore = storeRepository.getStoreById(newStoreId)
                        if (createdStore != null) {
                            val storeToUpdateWithOwner = createdStore.copy(
                                ownerStaffId = newStaffId,
                                updatedAt = System.currentTimeMillis() // 更新时间戳
                            )
                            val updateResult = storeRepository.updateStore(storeToUpdateWithOwner)
                            if (updateResult > 0) {
                                Log.d("AuthViewModel", "Store '${createdStore.storeName}' updated with owner ID: $newStaffId")
                            } else {
                                Log.w("AuthViewModel", "Failed to update store '${createdStore.storeName}' with owner ID, or no changes made.")
                            }
                        } else {
                            Log.e("AuthViewModel", "Failed to retrieve store with ID $newStoreId to update owner.")
                        }
                    }

                    // 8. 保存登录会话
                    val registeredStaff = newStaff.copy(id = newStaffId)
                    sessionManager.saveLoginSession(
                        staff = registeredStaff,
                        storeId = newStoreId,
                        storeName = storeName // 使用用户输入的店铺名
                    )
                    _isInitialSetupNeeded.value = false // 初始设置完成
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = registeredStaff) }
                    Log.d("AuthViewModel", "Account registration successful. Navigating to Main App.")
                } else {
                    _authUiState.update { it.copy(isLoading = false, error = "账户注册失败") }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error registering account", e)
                _authUiState.update { it.copy(isLoading = false, error = "注册时发生严重错误: ${e.localizedMessage}") }
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

                // TODO: Implement secure password verification
                val isPasswordCorrect = (passwordAttempt == staff.passwordHash)

                if (isPasswordCorrect) {
                    // --- 登录成功后，获取店铺信息 ---
                    val store = storeRepository.getStoreById(staff.storeId) // staff.storeId 应该是 Long (非空)
                    if (store == null) {
                        Log.e("AuthViewModel", "Login successful but could not find store with ID: ${staff.storeId} for staff: ${staff.username}")
                        _authUiState.update { it.copy(isLoading = false, error = "登录成功，但无法加载店铺信息") }
                        // 这里可能需要清除会话或标记为部分登录状态
                        return@launch
                    }

                    sessionManager.saveLoginSession(
                        staff = staff,
                        storeId = store.id,
                        storeName = store.storeName
                    )
                    _authUiState.update { it.copy(isLoading = false, navigationEvent = NavigationEvent.GoToMainApp, loggedInStaffInfo = staff) }
                    Log.d("AuthViewModel", "Login successful for ${staff.username} at store ${store.storeName}")
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