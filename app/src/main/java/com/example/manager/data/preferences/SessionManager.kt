package com.example.manager.data.preferences

import android.content.Context
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
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

// UserSession data class 定义
data class UserSession(
    val isLoggedIn: Boolean,
    val staffId: Long?,
    val staffRole: StaffRole?, // <-- 改为 StaffRole 枚举类型
    val username: String?,
    val staffName: String?
)

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val LOGGED_IN_STAFF_ID = longPreferencesKey("logged_in_staff_id")
        val LOGGED_IN_STAFF_ROLE = stringPreferencesKey("logged_in_staff_role_name") // 名称更清晰
        val LOGGED_IN_STAFF_USERNAME = stringPreferencesKey("logged_in_staff_username")
        val LOGGED_IN_STAFF_NAME = stringPreferencesKey("logged_in_staff_name")
    }

    val userSessionFlow: Flow<UserSession> = context.dataStore.data
        .catch { exception ->
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

            UserSession(
                isLoggedIn = staffId != null,
                staffId = staffId,
                staffRole = roleName?.let { runCatching { StaffRole.valueOf(it) }.getOrNull() }, // 安全转换为枚举
                username = username,
                staffName = staffName
            )
        }

    // 传入整个 Staff 对象，方便获取所有需要的信息
    suspend fun saveLoginSession(staff: Staff) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_STAFF_ID] = staff.id
            preferences[PreferencesKeys.LOGGED_IN_STAFF_ROLE] = staff.role.name // 存储枚举的名称
            preferences[PreferencesKeys.LOGGED_IN_STAFF_USERNAME] = staff.username
            preferences[PreferencesKeys.LOGGED_IN_STAFF_NAME] = staff.name
        }
    }

    suspend fun clearLoginSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_ID)
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_ROLE)
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_USERNAME)
            preferences.remove(PreferencesKeys.LOGGED_IN_STAFF_NAME)
        }
    }
}