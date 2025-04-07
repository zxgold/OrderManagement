package com.example.manager.di

import com.example.manager.data.repository.CustomerRepository // 导入接口
import com.example.manager.data.repository.CustomerRepositoryImpl // 导入实现
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Repository 通常也是应用范围的单例
abstract class RepositoryModule { // 使用 abstract class 或 interface for @Binds

    @Binds
    @Singleton // 确保绑定的实现也是单例
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl // 参数是实现类
    ): CustomerRepository // 返回类型是接口
}
