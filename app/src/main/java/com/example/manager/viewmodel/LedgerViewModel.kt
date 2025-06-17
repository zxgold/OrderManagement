package com.example.manager.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.LedgerEntry
import com.example.manager.data.model.enums.LedgerEntryType
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.LedgerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class LedgerUiState(
    val entries: List<LedgerEntry> = emptyList(),
    val startDate: Long,
    val endDate: Long,
    val totalIncome: Double = 0.0, // 新增：用于显示总收入
    val totalExpense: Double = 0.0, // 新增：用于显示总支出
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- StateFlow for Date Range ---
    // 使用一个单独的 StateFlow 来驱动日期范围的变化，这使得逻辑更清晰
    private val _dateRange = MutableStateFlow(getThisMonthDateRange())
    val dateRange: StateFlow<Pair<Long, Long>> = _dateRange.asStateFlow()

    // --- Main UI State Flow ---
    // 它由 dateRange Flow 驱动，自动加载和更新流水列表
    val uiState: StateFlow<LedgerUiState> = _dateRange
        .flatMapLatest { (start, end) ->
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) {
                return@flatMapLatest flowOf(
                    LedgerUiState(
                        startDate = start,
                        endDate = end,
                        isLoading = false,
                        errorMessage = "无法获取店铺信息"
                    )
                )
            }

            // 从 Repository 获取响应式的流水列表 Flow
            ledgerRepository.getLedgerEntriesByDateRangeFlow(storeId, start, end)
                .map { entries ->
                    // 在这里计算总收入和总支出
                    val income = entries.filter { it.entryType == LedgerEntryType.INCOME }.sumOf { it.amount }
                    val expense = entries.filter { it.entryType == LedgerEntryType.EXPENSE }.sumOf { it.amount }

                    LedgerUiState(
                        entries = entries,
                        startDate = start,
                        endDate = end,
                        totalIncome = income,
                        totalExpense = expense,
                        isLoading = false // 加载完成
                    )
                }
                .catch { e ->
                    Log.e("LedgerViewModel", "Error collecting ledger entries", e)
                    emit(LedgerUiState(startDate = start, endDate = end, isLoading = false, errorMessage = "加载账本失败"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LedgerUiState( // 提供一个初始状态
                startDate = _dateRange.value.first,
                endDate = _dateRange.value.second,
                isLoading = true
            )
        )

    // --- UI Actions ---

    fun setDateRange(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

    // 这个方法接收一个时间戳（通常是某天的开始），
    // 并将日期范围设置为这一整天。
    fun setDate(dateInMillis: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis

        // 设置为当天的开始时间 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // 设置为当天的结束时间 23:59:59
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        // 更新 dateRange StateFlow，这将自动触发数据重新加载
        _dateRange.value = Pair(startOfDay, endOfDay)
    }

    fun addLedgerEntry(type: LedgerEntryType, amount: Double, description: String, date: Long) {
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.firstOrNull()
            val storeId = session?.storeId
            val staffId = session?.staffId
            if (storeId == null || staffId == null) {
                // 不能直接更新 uiState，因为它是由 Flow 驱动的。我们需要一个单独的事件流来传递一次性消息。
                // 暂时先用 Log 记录
                Log.e("LedgerViewModel", "Cannot add entry: user or store info missing.")
                return@launch
            }
            if (amount <= 0 || description.isBlank()) {
                Log.w("LedgerViewModel", "Invalid entry: amount or description is empty.")
                return@launch
            }

            val newEntry = LedgerEntry(
                storeId = storeId,
                entryType = type,
                amount = amount,
                entryDate = date,
                description = description,
                staffId = staffId
            )

            ledgerRepository.insertLedgerEntry(newEntry)
                .onSuccess {
                    // 因为列表是响应式的，所以不需要手动刷新。
                    // 我们可以通过一个单独的 Flow 发送成功消息。
                    Log.d("LedgerViewModel", "Ledger entry added successfully.")
                }
                .onFailure { e ->
                    Log.e("LedgerViewModel", "Failed to add ledger entry", e)
                }
        }
    }
}

// --- Helper Functions ---

/**
 * 获取当前月份的开始和结束时间戳
 */
private fun getThisMonthDateRange(): Pair<Long, Long> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val start = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    val end = calendar.timeInMillis
    return Pair(start, end)
}