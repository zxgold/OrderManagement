package com.example.manager.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.enums.StaffRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// 在 Context 扩展中定义 DataStore 实例
// 文件名 "user_session" 会是设备上 DataStore 文件的实际名称
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session_v2")

// UserSession data class 定义
data class UserSession(
    val isLoggedIn: Boolean,
    val staffId: Long?,
    val staffRole: StaffRole?, // <-- 改为 StaffRole 枚举类型
    val username: String?,
    val staffName: String?,
    val storeId: Long?,
    val storeName: String?
)

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val LOGGED_IN_STAFF_ID = longPreferencesKey("logged_in_staff_id")
        val LOGGED_IN_STAFF_ROLE = stringPreferencesKey("logged_in_staff_role_name") // 名称更清晰
        val LOGGED_IN_STAFF_USERNAME = stringPreferencesKey("logged_in_staff_username")
        val LOGGED_IN_STAFF_NAME = stringPreferencesKey("logged_in_staff_name")
        val LOGGED_IN_STORE_ID = longPreferencesKey("logged_in_store_id") // <-- 新增
        val LOGGED_IN_STORE_NAME = stringPreferencesKey("logged_in_store_name") // <-- 新增
    }

    val userSessionFlow: Flow<UserSession> = context.dataStore.data
        .catch { exception ->
            Log.e("SessionManager", "Error reading preferences.", exception)
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val staffId = preferences[PreferencesKeys.LOGGED_IN_STAFF_ID]
            val roleName = preferences[PreferencesKeys.LOGGED_IN_STAFF_ROLE]
            val username = preferences[PreferencesKeys.LOGGED_IN_STAFF_USERNAME]
            val staffName = preferences[PreferencesKeys.LOGGED_IN_STAFF_NAME]
            val storeId = preferences[PreferencesKeys.LOGGED_IN_STORE_ID] // <-- 读取
            val storeName = preferences[PreferencesKeys.LOGGED_IN_STORE_NAME] // <-- 读取

            UserSession(
                isLoggedIn = staffId != null && storeId != null, // 可以让登录状态更严格，必须有店铺ID
                staffId = staffId,
                staffRole = roleName?.let { runCatching { StaffRole.valueOf(it) }.getOrNull() },
                username = username,
                staffName = staffName,
                storeId = storeId,      // <-- 填充
                storeName = storeName   // <-- 填充
            )
        }

    // 修改 saveLoginSession 以接收店铺信息
    suspend fun saveLoginSession(staff: Staff, storeId: Long, storeName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_STAFF_ID] = staff.id
            preferences[PreferencesKeys.LOGGED_IN_STAFF_ROLE] = staff.role.name
            preferences[PreferencesKeys.LOGGED_IN_STAFF_USERNAME] = staff.username
            preferences[PreferencesKeys.LOGGED_IN_STAFF_NAME] = staff.name
            preferences[PreferencesKeys.LOGGED_IN_STORE_ID] = storeId // <-- 保存
            preferences[PreferencesKeys.LOGGED_IN_STORE_NAME] = storeName // <-- 保存
        }
        Log.d("SessionManager", "Login session saved for staff: ${staff.username}, store: $storeName (ID: $storeId)")
    }
    // 或者，如果你更愿意传递整个 Store 对象 (需要确保 Store 对象已创建并有 ID):
    // suspend fun saveLoginSession(staff: Staff, store: Store) {
    // context.dataStore.edit { preferences ->
    // preferences[PreferencesKeys.LOGGED_IN_STAFF_ID] = staff.id
    // preferences[PreferencesKeys.LOGGED_IN_STAFF_ROLE] = staff.role.name
    // preferences[PreferencesKeys.LOGGED_IN_STAFF_USERNAME] = staff.username
    // preferences[PreferencesKeys.LOGGED_IN_STAFF_NAME] = staff.name
    // preferences[PreferencesKeys.LOGGED_IN_STORE_ID] = store.id
    // preferences[PreferencesKeys.LOGGED_IN_STORE_NAME] = store.storeName
    //     }
    // }


    suspend fun clearLoginSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_ID)
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_ROLE)
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_USERNAME)
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_NAME)
            preferences.remove(PreferencesKeys.LOGGED_IN_STORE_ID) // <-- 清除
            preferences.remove(PreferencesKeys.LOGGED_IN_STORE_NAME) // <-- 清除
        }
        Log.d("SessionManager", "Login session cleared.")
    }
}