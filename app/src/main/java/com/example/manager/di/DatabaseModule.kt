package com.example.manager.di

import android.content.Context
import androidx.room.Room
import com.example.manager.data.dao.*
import com.example.manager.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module // 标记这是一个Hilt Module
@InstallIn(SingletonComponent::class) // 指定这个 Module的作用域和生命周期
object DatabaseModule { // 为什么这样定义？

    // --- 提供AppDatabase实例---
    @Provides
    @Singleton // 保证AppDatabase在整个应用中是单例
    fun provideAppDatabase(
        @ApplicationContext context: Context //Hilt自动提供
    ) : AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "order_manager_db" // 数据库文件名，保持一致
        )
            .build()
    }

    // --- 提供各个DAO实例 ---
    // Hilt会自动找到上面的provideAppDatabase方法获取database实例

    @Provides
    @Singleton // DAO通常也设为单例随Database实例生命周期
               // 但是我仍不明白设为单例的原因是什么
    fun provideStaffDao(database: AppDatabase) : StaffDao {
        return database.staffDao()
    }

    @Provides
    @Singleton
    fun provideCustomerDao(database: AppDatabase) : CustomerDao {
        return database.customerDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase) : ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun provideOrderItemDao(database: AppDatabase): OrderItemDao {
        return database.orderItemDao()
    }

    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }

    @Provides
    @Singleton
    fun provideFollowUpDao(database: AppDatabase): FollowUpDao {
        return database.followUpDao()
    }

    @Provides
    @Singleton
    fun provideLedgerEntryDao(database: AppDatabase): LedgerEntryDao {
        return database.ledgerEntryDao()
    }

    @Provides
    @Singleton
    fun provideActionLogDao(database: AppDatabase): ActionLogDao {
        return database.actionLogDao()
    }
}


