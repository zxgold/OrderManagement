package com.example.manager.di

import com.example.manager.data.repository.CustomerRepository // 导入接口
import com.example.manager.data.repository.CustomerRepositoryImpl // 导入实现
import com.example.manager.data.repository.StaffRepository // 导入接口
import com.example.manager.data.repository.StaffRepositoryImpl // 导入实现
import com.example.manager.data.repository.StoreRepository
import com.example.manager.data.repository.StoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/*
 * 这段代码是使用hilt依赖注入框架来配置接口与其实现类绑定的模块，
 * 它告诉Hilt当某个组件请求一个接口的实例时，应该提供哪个实现类的实例
 */
@Module //标记这个类是一个Hilt/Dagger模块，模块是提供依赖项如何被创建，或如何被绑定的地方
@InstallIn(SingletonComponent::class) // Repository 通常也是应用范围的单例
abstract class RepositoryModule { // 使用 abstract class 或 interface for @Binds

    @Binds
    @Singleton // 确保绑定的实现也是单例
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl // 参数是实现类
    ): CustomerRepository // 返回类型是接口

    @Binds
    @Singleton
    abstract fun bindStaffRepository(
        staffRepositoryImpl: StaffRepositoryImpl
    ): StaffRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        storeRepositoryImpl: StoreRepositoryImpl
    ): StoreRepository
}

