package com.example.manager.viewmodel

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manager.data.model.entity.Customer // 需要导入 Customer，因为订单与客户关联
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.entity.OrderItem
import com.example.manager.data.model.entity.Product // 需要导入 Product，用于订单项
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.enums.OrderStatus
import com.example.manager.data.preferences.SessionManager // 改为注入 SessionManager
import com.example.manager.data.repository.CustomerRepository
import com.example.manager.data.repository.OrderRepository
import com.example.manager.data.repository.ProductRepository // 需要注入 ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID // 用于生成临时订单号或唯一标识
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.* // 导入所有 Flow 操作符
import javax.inject.Inject

// --- UI State 定义 ---
data class OrderListUiState(
    val ordersWithCustomerNames: List<Pair<Order, String?>> = emptyList(), // 存储订单和对应的客户名
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // TODO: 添加搜索/筛选状态
)

// 用于添加/编辑订单时的临时订单项状态
data class TempOrderItem(
    val tempId: String = UUID.randomUUID().toString(), // 用于在列表中唯一标识未保存的订单项
    var productId: Long?, // 关联的产品ID
    var productName: String,
    var quantity: Int = 1,
    var actualUnitPrice: Double,
    var notes: String? = null,
    val isCustomized: Boolean = false
    // 可以根据需要添加 category, model 等快照信息
) {
    val itemTotalAmount: Double
        get() = quantity * actualUnitPrice
}

data class AddEditOrderUiState(
    val orderId: Long? = null, // 如果是编辑模式，则有订单ID
    val selectedCustomer: Customer? = null,
    val orderDate: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val tempOrderItems: List<TempOrderItem> = emptyList(),
    val discount: Double = 0.0,
    val downPayment: Double = 0.0,
    // TODO: responsibleStaffIds (选择负责人)

    val isLoadingInitialData: Boolean = false, // 用于初始加载（例如加载产品和已有订单信息）
    val isLoadingCustomers: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val calculatedTotalAmount: Double
        get() = tempOrderItems.sumOf { it.itemTotalAmount }
    val calculatedFinalAmount: Double
        get() = calculatedTotalAmount - discount
}

data class OrderDetailUiState(
    val order: Order? = null,
    val items: List<OrderItem> = emptyList(),
    val customerName: String? = null, // 关联的客户名
    val creatingStaffName: String? = null, // 创建员工名 (TODO: 需要 StaffRepository)
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class) // 启用 Flow 操作符
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository, // 用于选择客户和获取客户名
    private val productRepository: ProductRepository,   // 用于选择产品
    private val sessionManager: SessionManager          // 用于获取 storeId 和 staffId
    // TODO: 未来可能需要 StaffRepository 来获取员工姓名
) : ViewModel() {

    // --- StateFlows ---
    private val _orderListUiState = MutableStateFlow(OrderListUiState())
    val orderListUiState: StateFlow<OrderListUiState> = _orderListUiState.asStateFlow()

    private val _addEditOrderUiState = MutableStateFlow(AddEditOrderUiState())
    val addEditOrderUiState: StateFlow<AddEditOrderUiState> = _addEditOrderUiState.asStateFlow()

    private val _orderDetailUiState = MutableStateFlow(OrderDetailUiState())
    val orderDetailUiState: StateFlow<OrderDetailUiState> = _orderDetailUiState.asStateFlow()

    // --- 客户搜索 (已完成) ---
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()
    val customerSearchResults: StateFlow<List<Customer>> = _customerSearchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId != null && query.isNotBlank()) {
                customerRepository.searchCustomersByStoreIdFlow(query, storeId)
                    .map { results ->
                        if (results.isEmpty()) {
                            // **即时添加新客户的占位符**
                            listOf(Customer(id = -1L, storeId = storeId, name = "创建新客户: “$query”", phone = query))
                        } else { results }
                    }
            } else { flowOf(emptyList()) }
        }
        .catch { e -> emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- **新增：产品搜索的响应式数据流** ---
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    val productSearchResults: StateFlow<List<Product>> = _productSearchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val storeId = sessionManager.userSessionFlow.firstOrNull()?.storeId
            if (storeId != null && query.isNotBlank()) {
                productRepository.searchActiveProductsByStoreIdFlow(query, storeId) // **需要这个 Repo 方法**
            } else {
                flowOf(emptyList())
            }
        }
        .catch { e ->
            Log.e("OrderViewModel", "Error in product search flow", e)
            _addEditOrderUiState.update { it.copy(errorMessage = "搜索产品时出错") }
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- 辅助方法获取当前会话信息 ---
    private suspend fun getCurrentSessionInfo(): Pair<Long?, Long?> { // Pair<storeId, staffId>
        val session = sessionManager.userSessionFlow.firstOrNull()
        return Pair(session?.storeId, session?.staffId)
    }

    // --- 订单列表 ---
    fun loadOrders() {
        viewModelScope.launch {
            val (storeId, _) = getCurrentSessionInfo()
            if (storeId == null) {
                _orderListUiState.update { it.copy(isLoading = false, errorMessage = "无法获取店铺信息加载订单") }
                return@launch
            }
            _orderListUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val orders = orderRepository.getAllOrdersByStoreId(storeId)
                // 为了在列表显示客户名，我们需要获取客户信息
                val ordersWithNames = orders.map { order ->
                    val customerName = order.customerId?.let { customerId ->
                        customerRepository.getCustomerByIdAndStoreId(customerId, storeId)?.name
                    }
                    Pair(order, customerName)
                }
                _orderListUiState.update { it.copy(ordersWithCustomerNames = ordersWithNames, isLoading = false) }
            } catch (e: Exception) {
                _orderListUiState.update { it.copy(isLoading = false, errorMessage = "加载订单列表失败: ${e.localizedMessage}") }
            }
        }
    }

    // UI 调用这个方法来更新搜索词
    fun onCustomerSearchQueryChanged(query: String) {
        _customerSearchQuery.value = query
    }

    fun onProductSearchQueryChanged(query: String) { _productSearchQuery.value = query }

    // UI 调用这个方法来设置选中的客户
    fun onCustomerSelected(customer: Customer) {
        _addEditOrderUiState.update { it.copy(selectedCustomer = if(customer.id > 0) customer else null) }
        onCustomerSearchQueryChanged("")
    }

    // prepareNewOrderForm 现在不再需要加载客户列表了
    fun prepareNewOrderForm() {
        _addEditOrderUiState.value = AddEditOrderUiState() // 重置状态
        onCustomerSearchQueryChanged("")
        onProductSearchQueryChanged("")
    }

    fun prepareOrderForEditing(orderId: Long) { // 用户点击“编辑订单”时调用
        viewModelScope.launch {
            val (storeId, _) = getCurrentSessionInfo()
            if (storeId == null) {
                _addEditOrderUiState.update { it.copy(errorMessage = "无法获取店铺信息以编辑订单") }
                return@launch
            }
            _addEditOrderUiState.update { it.copy(isLoadingCustomers = true, isLoadingProducts = true, isSaving = false, saveSuccess = false, errorMessage = null) }
            try {
                val orderWithItems = orderRepository.getOrderWithItemsByIdAndStoreId(orderId, storeId)
                if (orderWithItems != null) {
                    val order = orderWithItems.first
                    val items = orderWithItems.second
                    val selectedCustomer = order.customerId?.let { customerRepository.getCustomerByIdAndStoreId(it, storeId) }

                    _addEditOrderUiState.update {
                        it.copy(
                            orderId = order.id,
                            selectedCustomer = selectedCustomer,
                            orderDate = order.orderDate,
                            notes = order.notes,
                            tempOrderItems = items.map { oi -> // 将 OrderItem 转换为 TempOrderItem
                                TempOrderItem(
                                    productId = oi.productId,
                                    productName = oi.productName, // 假设OrderItem有这些快照字段
                                    quantity = oi.quantity,
                                    actualUnitPrice = oi.actualUnitPrice,
                                    notes = oi.notes
                                )
                            },
                            discount = order.discount,
                            downPayment = order.downPayment
                        )
                    }
                } else {
                    _addEditOrderUiState.update { it.copy(errorMessage = "未找到要编辑的订单") }
                }
            } catch (e: Exception) {
                _addEditOrderUiState.update { it.copy(errorMessage = "加载订单编辑信息失败: ${e.localizedMessage}") }
            }
            _addEditOrderUiState.update { it.copy(isLoadingCustomers = false, isLoadingProducts = false)}
        }
    }

    fun addTempOrderItem(product: Product, quantity: Int, price: Double, notes: String?, isCustomized: Boolean) {
        val newItem = TempOrderItem(
            productId = product.id,
            productName = if (isCustomized) "${product.name} (定制)" else product.name,
            quantity = if (isCustomized) 1 else quantity,
            actualUnitPrice = price,
            notes = notes,
            isCustomized = isCustomized
        )
        _addEditOrderUiState.update { it.copy(tempOrderItems = it.tempOrderItems + newItem) }
        onProductSearchQueryChanged("") // 添加后清空产品搜索
    }

    fun updateTempOrderItem(tempId: String, newQuantity: Int, newPrice: Double) {
        _addEditOrderUiState.update { currentState ->
            currentState.copy(
                tempOrderItems = currentState.tempOrderItems.map {
                    if (it.tempId == tempId) {
                        it.copy(quantity = newQuantity, actualUnitPrice = newPrice)
                    } else { it }
                }
            )
        }
    }

    fun removeTempOrderItem(tempId: String) {
        _addEditOrderUiState.update {
            it.copy(tempOrderItems = it.tempOrderItems.filterNot { item -> item.tempId == tempId })
        }
    }

    fun onOrderNotesChanged(notes: String) {
        _addEditOrderUiState.update { it.copy(notes = notes) }
    }
    fun onDiscountChanged(discount: Double) {
        _addEditOrderUiState.update { it.copy(discount = discount) }
    }
    fun onDownPaymentChanged(downPayment: Double) {
        _addEditOrderUiState.update { it.copy(downPayment = downPayment) }
    }

    fun saveOrder() {
        viewModelScope.launch {
            val (storeId, staffId) = getCurrentSessionInfo()
            val currentState = _addEditOrderUiState.value

            if (storeId == null || staffId == null) {
                _addEditOrderUiState.update { it.copy(isSaving = false, errorMessage = "无法获取店铺或用户信息，无法保存订单") }
                return@launch
            }
            if (currentState.selectedCustomer == null) {
                _addEditOrderUiState.update { it.copy(isSaving = false, errorMessage = "请选择客户") }
                return@launch
            }
            if (currentState.tempOrderItems.isEmpty()) {
                _addEditOrderUiState.update { it.copy(isSaving = false, errorMessage = "请至少添加一个产品到订单") }
                return@launch
            }

            _addEditOrderUiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }

            val orderToSave = Order(
                id = currentState.orderId ?: 0L, // 如果是新订单，id为0，否则为现有id
                storeId = storeId,
                orderNumber = currentState.orderId?.let { orderRepository.getOrderByIdAndStoreId(it, storeId)?.orderNumber } ?: generateOrderNumber(),
                customerId = currentState.selectedCustomer.id,
                orderDate = currentState.orderDate,
                totalAmount = currentState.calculatedTotalAmount,
                discount = currentState.discount,
                finalAmount = currentState.calculatedFinalAmount,
                downPayment = currentState.downPayment,
                status = currentState.orderId?.let { orderRepository.getOrderByIdAndStoreId(it, storeId)?.status } ?: OrderStatus.PENDING, // 编辑时保留原状态，新增为PENDING
                notes = currentState.notes,
                creatingStaffId = currentState.orderId?.let { orderRepository.getOrderByIdAndStoreId(it, storeId)?.creatingStaffId } ?: staffId, // 编辑时保留原创建人
                responsibleStaffIds = null, // TODO: 实现负责人选择
                updatedAt = System.currentTimeMillis(), // 更新时间
                createdAt = currentState.orderId?.let { orderRepository.getOrderByIdAndStoreId(it, storeId)?.createdAt } ?: System.currentTimeMillis() // 编辑时保留原创建时间
            )

            val orderItemsToSave = currentState.tempOrderItems.mapNotNull { tempItem ->
                // 使用 mapNotNull，如果找不到产品，则直接跳过该订单项
                val product = tempItem.productId?.let { productRepository.getProductById(it) }
                if (product == null && tempItem.productId != null) {
                    Log.w("OrderViewModel", "Could not find product with ID ${tempItem.productId} for order item snapshot. Skipping.")
                    return@mapNotNull null // 如果找不到产品，则不包含此订单项
                }
                OrderItem(
                    orderId = 0L,
                    productId = tempItem.productId,
                    // 从获取到的 product 对象或 tempItem 中获取快照信息
                    productCategory = product?.category,
                    productName = tempItem.productName,
                    productModel = product?.model,
                    quantity = tempItem.quantity,
                    actualUnitPrice = tempItem.actualUnitPrice,
                    itemTotalAmount = tempItem.itemTotalAmount,
                    status = OrderItemStatus.NOT_ORDERED,
                    isCustomized = tempItem.isCustomized,
                    notes = tempItem.notes
                )
            }

            if (currentState.tempOrderItems.size != orderItemsToSave.size) {
                _addEditOrderUiState.update { it.copy(isSaving = false, errorMessage = "部分产品信息已失效，请重新添加产品。") }
                return@launch
            }

            val result: Result<Long>
            if (currentState.orderId == null) { // 新增订单
                result = orderRepository.insertOrderWithItems(orderToSave, orderItemsToSave)
            } else { // 更新订单
                // 更新订单通常涉及：更新 Order 表，然后可能需要删除旧的 OrderItems 再插入新的，
                // 或者更精细地对比 OrderItems 进行增删改。
                // 为了简化，我们先只实现更新 Order 表本身，OrderItem 的更新可以后续迭代。
                // 或者，在 OrderRepositoryImpl 中，insertOrderWithItems 也可以设计成能处理更新（先删旧items再插新items）。
                // 目前，我们先只更新 Order 主信息。
                val updateOrderResult = orderRepository.updateOrder(orderToSave) // updateOrder 需要返回 Result<Int>
                // **这是一个简化，实际更新订单项会更复杂**
                // 你需要先删除与 orderId 关联的所有旧 OrderItem，然后再插入新的 orderItemsToSave
                // 这最好也在一个事务中完成
                result = updateOrderResult.map { if (it > 0) orderToSave.id else -1L } // 模拟返回 orderId
                if(updateOrderResult.isSuccess && updateOrderResult.getOrNull()!! > 0) {
                    Log.d("OrderViewModel", "Order header updated, order items would need separate handling for update.")
                }
            }

            result.onSuccess { generatedOrderId ->
                if (generatedOrderId > 0) {
                    _addEditOrderUiState.update { it.copy(isSaving = false, saveSuccess = true, errorMessage = null) }
                    loadOrders() // 刷新订单列表
                    Log.d("OrderViewModel", "Order saved successfully with ID: $generatedOrderId")
                } else {
                    _addEditOrderUiState.update { it.copy(isSaving = false, saveSuccess = false, errorMessage = "保存订单失败 (影响行数为0或ID无效)") }
                }
            }.onFailure { e ->
                val errorMsg = if (e is SQLiteConstraintException) "保存订单失败：数据冲突。" else "保存订单失败: ${e.localizedMessage}"
                _addEditOrderUiState.update { it.copy(isSaving = false, saveSuccess = false, errorMessage = errorMsg) }
                Log.e("OrderViewModel", "Error saving order", e)
            }
        }
    }

    // **新增：一个专门处理从“即时添加”返回后选中客户的方法**
    fun selectCustomerById(customerId: Long) {
        viewModelScope.launch {
            val storeId = getCurrentStoreId() ?: return@launch
            val customer = customerRepository.getCustomerByIdAndStoreId(customerId, storeId)
            if (customer != null) {
                onCustomerSelected(customer) // 复用现有的选中逻辑
            }
        }
    }


    // --- 订单详情 ---
    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            val (storeId, _) = getCurrentSessionInfo()
            if (storeId == null) {
                _orderDetailUiState.update { it.copy(isLoading = false, errorMessage = "无法获取店铺信息加载订单详情") }
                return@launch
            }
            _orderDetailUiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val orderWithItems = orderRepository.getOrderWithItemsByIdAndStoreId(orderId, storeId)
                if (orderWithItems != null) {
                    val order = orderWithItems.first
                    val items = orderWithItems.second
                    val customerName = order.customerId?.let { customerRepository.getCustomerByIdAndStoreId(it, storeId)?.name }
                    // TODO: 获取 creatingStaffName (需要 StaffRepository)
                    _orderDetailUiState.update {
                        it.copy(
                            order = order,
                            items = items,
                            customerName = customerName,
                            isLoading = false
                        )
                    }
                } else {
                    _orderDetailUiState.update { it.copy(isLoading = false, errorMessage = "未找到订单详情") }
                }
            } catch (e: Exception) {
                _orderDetailUiState.update { it.copy(isLoading = false, errorMessage = "加载订单详情失败: ${e.localizedMessage}") }
            }
        }
    }


    private fun generateOrderNumber(): String {
        return "ORD-${UUID.randomUUID().toString().takeLast(8).uppercase()}"
    }

    private suspend fun getCurrentStoreId(): Long? {
        val currentSession = sessionManager.userSessionFlow.firstOrNull()
        if (currentSession?.storeId == null) {
            Log.e("OrderViewModel", "Critical: storeId is null in current session.")
        }
        return currentSession?.storeId
    }


    // 清除错误信息的方法
    fun clearOrderListError() { _orderListUiState.update { it.copy(errorMessage = null) } }
    fun clearAddEditOrderError() { _addEditOrderUiState.update { it.copy(errorMessage = null) } }
    fun resetSaveSuccessFlag() { _addEditOrderUiState.update { it.copy(saveSuccess = false) } }
    fun clearOrderDetailError() { _orderDetailUiState.update { it.copy(errorMessage = null) } }
}