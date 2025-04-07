package com.example.manager.repository

import androidx.annotation.WorkerThread
import com.example.manager.FakeOrderDao
import kotlinx.coroutines.flow.Flow


/*
 *不理解：这些函数的写法为什么是这样的？
 *
 * 目前，OrderRepository 看起来只是简单地调用了 OrderDao 的方法。但它的价值在于封装和抽象。
 * ViewModel 将通过 OrderRepository 来获取和修改订单数据，而无需知道 OrderDao 或 Room 的存在。
 * 如果未来我们需要从网络获取数据，我们只需要修改 OrderRepository 内部的逻辑，
 * 而 ViewModel 层基本不需要改动。
 *
 * class OrderRepository(private val orderDao: OrderDao):
 *
 * val allOrders: Flow<List<Order>> = orderDao.getAllOrders():
 *
 * @WorkerThread: 这是一个提示性注解，表明这个方法应该在后台线程调用。
 * 虽然 Room 的 suspend 函数已经确保了这一点，但加上注解可以提高代码可读性。
 *
 * suspend fun insert(order: Order)：这是一个挂起函数，它直接调用 orderDao.insert(order)。
 * 因为 DAO 的 insert 也是 suspend 函数，所以 Repository 的 insert 也必须是 suspend 函数或在协程作用域内调用。
 *
 * getOrderById, update, delete: 添加了这些方法作为示例，它们同样直接调用了对应的 DAO 方法
 */
