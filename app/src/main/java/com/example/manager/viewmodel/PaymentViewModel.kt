package com.example.manager.viewmodel

// 这个 ViewModel 将负责处理回款界面的所有业务逻辑，包括加载回款流水、处理添加新回款的逻辑，以及未来可能的筛选和汇总。
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.entity.Payment
import com.example.manager.data.model.uimodel.PaymentWithDetails
import com.example.manager.data.preferences.SessionManager
import com.example.manager.data.repository.CustomerRepository
import com.example.manager.data.repository.OrderRepository
import com.example.manager.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// UI State for the Payment List Screen
data class PaymentListUiState(
    val payments: List<PaymentWithDetails> = emptyList(),
    val startDate: Long,
    val endDate: Long,
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// UI State for the Add Payment Dialog
data class AddPaymentUiState(
    val availableOrders: List<Order> = emptyList(), // 用于订单选择
    val availableCustomers: List<Customer> = emptyList(), // 用于客户选择
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,     // 用于获取订单列表
    private val customerRepository: CustomerRepository, // 用于获取客户列表
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- State for the Payment List Screen ---
    private val _dateRange = MutableStateFlow(getThisMonthDateRange())
    val dateRange: StateFlow<Pair<Long, Long>> = _dateRange.asStateFlow()

    val listUiState: StateFlow<PaymentListUiState> = _dateRange
        .flatMapLatest { (start, end) ->
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) {
                return@flatMapLatest flowOf(
                    PaymentListUiState(startDate = start, endDate = end, isLoading = false, errorMessage = "无法获取店铺信息")
                )
            }
            paymentRepository.getPaymentsByDateRangeFlow(storeId, start, end)
                .map { payments ->
                    PaymentListUiState(
                        payments = payments,
                        startDate = start,
                        endDate = end,
                        totalAmount = payments.sumOf { it.payment.amount },
                        isLoading = false
                    )
                }
                .catch { e -> emit(PaymentListUiState(startDate = start, endDate = end, isLoading = false, errorMessage = "加载回款列表失败")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentListUiState(startDate = _dateRange.value.first, endDate = _dateRange.value.second))

    // --- State for the Add Payment Dialog ---
    private val _addPaymentUiState = MutableStateFlow(AddPaymentUiState())
    val addPaymentUiState: StateFlow<AddPaymentUiState> = _addPaymentUiState.asStateFlow()

    // --- Single event channel for success/error messages from actions ---
    private val _messageChannel = Channel<String>()
    val messageFlow = _messageChannel.receiveAsFlow()


    fun loadDataForDialog() {
        viewModelScope.launch {
            _addPaymentUiState.update { it.copy(isLoading = true) }
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId == null) { /* ... 错误处理 ... */ return@launch }

            try {
                // 1. 获取本店所有订单
                val allOrders = orderRepository.getAllOrdersByStoreId(storeId)

                // 2. 筛选出有欠款的订单
                val ordersWithDebt = mutableListOf<Order>()
                for (order in allOrders) {
                    // **调用 PaymentRepository 的方法**
                    val totalPaid = paymentRepository.getTotalPaymentsForOrder(order.id)
                    val totalAmountDue = order.finalAmount
                    val amountAlreadyPaid = totalPaid + order.downPayment // 已付总额 = 首付 + 后续回款

                    if (amountAlreadyPaid < totalAmountDue) {
                        ordersWithDebt.add(order)
                    }
                }

                val customers = customerRepository.getAllCustomersByStoreId(storeId) // 假设有这个方法
                _addPaymentUiState.update {
                    it.copy(
                        availableOrders = ordersWithDebt, // **只提供有欠款的订单**
                        availableCustomers = customers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _addPaymentUiState.update { it.copy(isLoading = false) }
                _messageChannel.send("加载数据失败: ${e.message}")
            }
        }
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

    fun addPayment(
        amount: Double,
        paymentMethod: String?,
        notes: String?,
        date: Long,
        order: Order?, // 接收整个 Order 对象
        customer: Customer? // 或者只接收 Customer 对象
    ) {
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.firstOrNull()
            val storeId = session?.storeId
            val staffId = session?.staffId
            if (storeId == null || staffId == null) {
                _messageChannel.send("无法获取用户信息，记账失败")
                return@launch
            }
            if (amount <= 0) {
                _messageChannel.send("金额必须大于0")
                return@launch
            }
            val customerId = order?.customerId ?: customer?.id
            if (customerId == null) {
                _messageChannel.send("必须关联一个客户或订单")
                return@launch
            }

            val newPayment = Payment(
                storeId = storeId,
                orderId = order?.id,
                customerId = customerId,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes,
                paymentDate = date,
                staffId = staffId
            )

            paymentRepository.insertPayment(newPayment)
                .onSuccess {
                    _messageChannel.send("回款记录添加成功！")
                    // 列表会自动刷新
                }
                .onFailure { e ->
                    _messageChannel.send("添加失败: ${e.message}")
                }
        }
    }
}

private fun getThisMonthDateRange(): Pair<Long, Long> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1); calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
    val start = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1); calendar.add(Calendar.DAY_OF_MONTH, -1); calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
    val end = calendar.timeInMillis
    return Pair(start, end)
}