package com.example.manager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.manager.data.dao.* // 导入所有 DAO 接口
import com.example.manager.data.model.entity.* // 导入所有 Entity 类
import com.example.manager.data.model.typeconverter.Converters // 导入 Converters Object

// --- !! 重要 !! ---
// 确定新的数据库版本号。
// 如果你之前的版本是 1，那么现在至少应该是 2。
// 由于我们做了大量结构改动 (V3 设计)，可以跳跃式增加，比如直接用 3，
// 关键是 **必须比之前的版本号大**。
private const val DATABASE_VERSION = 16 // <--- !! 修改为你确定的新版本号 !!

@Database(
    entities = [
        // --- 列出 V3 版本包含的所有 Entity 类 ---
        Store::class,
        Supplier::class,
        Staff::class,
        Customer::class,
        Product::class,
        Order::class,
        OrderItem::class,
        Payment::class,
        FollowUp::class,
        LedgerEntry::class,
        ActionLog::class,
        OrderItemStatusLog::class,
        InventoryItem::class
    ],
    version = DATABASE_VERSION, // <-- 使用上面定义的常量
    exportSchema = true // <-- 推荐设置为 true，用于导出 Schema JSON 文件
    // autoMigrations = [] // 自动迁移我们这次不用，因为结构变化大，需要手动 Migration
)
@TypeConverters(Converters::class) // <-- 将我们的 Converters 应用到整个数据库
abstract class AppDatabase : RoomDatabase() {

    // --- 提供获取每个 DAO 实例的抽象方法 ---
    abstract fun storeDao(): StoreDao
    abstract fun supplierDao(): SupplierDao
    abstract fun staffDao(): StaffDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
    abstract fun actionLogDao(): ActionLogDao
    abstract fun orderItemStatusLogDao(): OrderItemStatusLogDao
    abstract fun inventoryItemDao(): InventoryItemDao


    // --- Companion Object (通常用于提供单例实例，结合 Hilt 会更简单) ---
    // 暂时可以不写 getInstance() 方法，Hilt 会帮我们管理实例创建
    /*
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_manager_db" // 数据库文件名
                )
                // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // <-- 下一步我们会添加迁移脚本
                .fallbackToDestructiveMigration() // !! 临时方案，开发阶段用，发布前必须换成 Migration !!
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    */
}