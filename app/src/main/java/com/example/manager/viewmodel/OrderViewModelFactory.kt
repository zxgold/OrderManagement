package com.example.manager.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.manager.repository.OrderRepository


/*
 * 因为 OrderViewModel 的构造函数需要一个 OrderRepository 参数，我们不能直接让系统创建它。
 * 我们需要一个 ViewModelProvider.Factory 来告诉系统如何创建 OrderViewModel 实例。
 */
// 下面一行，实现了ViewModelProvider.Factory接口
class OrderViewModelFactory(private val repository: OrderRepository) : ViewModelProvider.Factory {

    // 下面一行是工厂方法，系统会调用它来创建ViewModel实例
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //检查请求创建的ViewModel是否是OrderViewModel或其子类
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            //检查请求创建的ViewModel是否是OrderViewModel或其子类，
            // 如果是，则使用我们传入的repository来创建OrderViewModel
            return OrderViewModel(repository) as T
        }
        // 如果请求创建的不是 OrderViewModel，则抛出异常，因为这个工厂只知道如何创建 OrderViewModel。
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
