package com.example.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.manager.repository.OrderRepository
import kotlinx.coroutines.launch


/*
 * ViewModel负责为UI准备和管理数据
 * 它通过Repository获取数据，并将数据转换为UI可以观察的格式（通常是LiveData或StateFlow）
 * ViewModel的一个重要特性是它能在配置更改（如屏幕旋转）后存活下来，这样数据就不会丢失
 *
 *
 *
 *
 *
 */

// 为社么是定义类呢？
// 下面一行继承自ViewModel（这个继承关系怎么来的？），构造函数接收OrderRepository实例（依赖注入）
class OrderViewModel(private val repository: OrderRepository): ViewModel() {
    // 下面一行是核心部分，我们从repository中获取Flow<List<Order>>，然后调用.asLiveData()扩展函数（来自 lifecycle-livedata-ktx 库）。
    // asLiveData()扩展函数会将Flow转换为LiveData，这个LiveData会观察底层的Flow，当Flow发出新数据时，LiveData会自动更新
    // 并通知所有活跃的观察者（UI）
    val allOrders: LiveData<List<Order>> = repository.allOrders.asLiveData()

    // 这是暴露给UI调用的公共方法，用于添加新订单
    // 它接收来自用户输入的字符串
    // 添加了基础的输入验证: 检查输入是否为空，金额是否为有效的正数。实际应用中可能需要更复杂的验证和错误反馈机制。
    // 如果验证通过，创建一个Order对象
    // 调用私有的insert(order: Order)辅助函数
    fun insert(orderNumber: String, customerName: String, amountSting: String) {
        if (orderNumber.isBlank() || customerName.isBlank() || amountSting.isBlank()) {
            println("Error: All fields must be filled.")
            return
        }

        val amount = amountSting.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            println("Error: Invalid amount.")
            return
        }

        val newOrder = Order(orderNumber = orderNumber, customerName = customerName, amount = amount)
        insert(newOrder)

    }

    // viewModelScope：这是ViewModel类提供的一个内置的CoroutineScope
    // 在这个作用域内启动的协程与 ViewModel 的生命周期绑定：如果 ViewModel 被销毁
    // （例如，关联的 Activity/Fragment 完全结束），viewModelScope 会自动取消其中的所有协程，
    // 防止内存泄漏和不必要的工作。
    //
    // launch{...}启动一个新的协程
    //
    // repository.insert(order): 在协程中调用 repository 的 suspend 函数 insert。
    // 这确保了数据库插入操作在后台线程执行，不会阻塞主线程。
    private  fun insert(order: Order) = viewModelScope.launch {
        repository.insert(order)
    }

    fun update(order: Order) = viewModelScope.launch {
        repository.update(order)
    }

    fun delete(order: Order) = viewModelScope.launch {
        repository.delete(order)
    }


}