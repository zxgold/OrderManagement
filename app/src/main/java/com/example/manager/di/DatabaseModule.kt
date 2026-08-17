package com.example.manager.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.manager.data.dao.*
import com.example.manager.data.db.AppDatabase
import com.example.manager.data.model.entity.Customer
import com.example.manager.data.model.entity.InventoryItem
import com.example.manager.data.model.entity.Order
import com.example.manager.data.model.entity.OrderItem
import com.example.manager.data.model.entity.OrderItemStatusLog
import com.example.manager.data.model.entity.Product
import com.example.manager.data.model.entity.Staff
import com.example.manager.data.model.entity.Store
import com.example.manager.data.model.entity.Supplier
import com.example.manager.data.model.enums.InventoryItemStatus
import com.example.manager.data.model.enums.OrderItemStatus
import com.example.manager.data.model.enums.OrderStatus
import com.example.manager.data.model.enums.StaffRole
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider // <-- **这才是正确的导入！**
import javax.inject.Singleton

@Module // 标记这是一个Hilt Module
@InstallIn(SingletonComponent::class) // 指定这个 Module的作用域和生命周期
object DatabaseModule { // 为什么这样定义？

    // --- 提供AppDatabase实例---
    @Provides
    @Singleton
    fun provideStoreDao(database: AppDatabase): StoreDao { // <-- 添加提供 StoreDao 的方法
        return database.storeDao()
    }

    @Provides
    @Singleton // 保证AppDatabase在整个应用中是单例
    fun provideAppDatabase(
        @ApplicationContext context: Context, //Hilt自动提供
        // 使用 Provider<T> 延迟获取，确保 Callback 中安全使用
        storeDaoProvider: Provider<StoreDao>,
        staffDaoProvider: Provider<StaffDao>,
        supplierDaoProvider: Provider<SupplierDao>,
        productDaoProvider: Provider<ProductDao>,
        customerDaoProvider: Provider<CustomerDao>,
        orderDaoProvider: Provider<OrderDao>,
        orderItemDaoProvider: Provider<OrderItemDao>,
        orderItemStatusLogDaoProvider: Provider<OrderItemStatusLogDao>,
        inventoryItemDaoProvider: Provider<InventoryItemDao>

    ) : AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "order_manager_db" // 数据库文件名，保持一致
        )
            .fallbackToDestructiveMigration() // <-- 临时方案，开发阶段用，发布前必须换成 Migration
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // --- 手动创建部分唯一索引 ---
                    db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_inventory_items_standard_stock_unique` 
                    ON `inventory_items` (`store_id`, `product_id`) 
                    WHERE `is_standard_stock` = 1
                """)
                    // -----------------------------

                    // --- 启动协程填充数据 ---
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    scope.launch {
                        Log.d("DatabaseModule", "Populating database with rich test data...")

                        // 获取所有需要的 DAO 实例
                        val storeDao = storeDaoProvider.get()
                        val staffDao = staffDaoProvider.get()
                        val supplierDao = supplierDaoProvider.get()
                        val productDao = productDaoProvider.get()
                        val customerDao = customerDaoProvider.get()
                        val orderDao = orderDaoProvider.get()
                        val orderItemDao = orderItemDaoProvider.get()
                        val orderItemStatusLogDao = orderItemStatusLogDaoProvider.get()


                        // 1. 创建店铺
                        val storeId = storeDao.insertStore(Store(storeName = "未来之家全屋定制"))

                        // 2. 创建员工
                        val bossId = staffDao.insertOrUpdateStaff(
                            Staff(
                                storeId = storeId,
                                name = "王经理",
                                role = StaffRole.BOSS,
                                username = "boss",
                                passwordHash = "123456"
                            )
                        )
                        val staffId1 = staffDao.insertOrUpdateStaff(
                            Staff(
                                storeId = storeId,
                                name = "员工小李",
                                role = StaffRole.STAFF,
                                username = "staff",
                                passwordHash = "123456"
                            )
                        )
                        val staffId2 = staffDao.insertOrUpdateStaff(
                            Staff(
                                storeId = storeId,
                                name = "股东张总",
                                role = StaffRole.SHAREHOLDER,
                                username = "shareholder",
                                passwordHash = "123456"
                            )
                        )

                        // 3. 回填店铺创始人
                        storeDao.updateStore(
                            Store(
                                id = storeId,
                                storeName = "未来之家全屋定制",
                                ownerStaffId = bossId
                            )
                        )

                        // 4. 创建供应商
                        val supplierId1 = supplierDao.insertSupplier(
                            Supplier(
                                storeId = storeId,
                                name = "米兰家具集团",
                                contactPerson = "李经理"
                            )
                        )
                        val supplierId2 = supplierDao.insertSupplier(
                            Supplier(
                                storeId = storeId,
                                name = "欧普智慧照明",
                                contactPerson = "赵总"
                            )
                        )

                        // 5. 创建产品
                        val p1Id = productDao.insertProduct(
                            Product(
                                supplierId = supplierId1,
                                category = "沙发",
                                name = "意式云朵真皮沙发",
                                model = "SF-CLOUD-01",
                                defaultPrice = 12800.0
                            )
                        )
                        val p2Id = productDao.insertProduct(
                            Product(
                                supplierId = supplierId1,
                                category = "床",
                                name = "悬浮智能储物床",
                                model = "BD-FLOAT-KING",
                                defaultPrice = 7500.0
                            )
                        )
                        val p3Id = productDao.insertProduct(
                            Product(
                                supplierId = supplierId1,
                                category = "衣柜",
                                name = "一门到顶定制衣柜",
                                model = "WD-CUSTOM-A",
                                defaultPrice = 2500.0
                            )
                        ) // 定制品，价格可能是每平米
                        val p4Id = productDao.insertProduct(
                            Product(
                                supplierId = supplierId2,
                                category = "吊灯",
                                name = "客厅无主灯磁吸轨道灯",
                                model = "L-TRACK-PRO",
                                defaultPrice = 350.0
                            )
                        ) // 标准品，价格是每米
                        val p5Id = productDao.insertProduct(
                            Product(
                                supplierId = supplierId2,
                                category = "筒灯",
                                name = "智能调光防眩筒灯",
                                model = "DL-PRO-7W",
                                defaultPrice = 120.0
                            )
                        )

                        // 6. 创建客户
                        val c1Id = customerDao.insertCustomer(
                            Customer(
                                storeId = storeId,
                                name = "张三先生",
                                phone = "13800138001",
                                address = "幸福小区A栋101"
                            )
                        )
                        val c2Id = customerDao.insertCustomer(
                            Customer(
                                storeId = storeId,
                                name = "李四女士",
                                phone = "13800138002",
                                address = "阳光花园B栋202"
                            )
                        )
                        val c3Id = customerDao.insertCustomer(
                            Customer(
                                storeId = storeId,
                                name = "王五公司",
                                phone = "13800138003",
                                address = "科技园C座3楼"
                            )
                        )

                        // 7. 创建订单和订单项 (工单)
                        // 订单1: 张三的订单，包含一个标准品沙发和一个定制衣柜
                        var order1Id = orderDao.insertOrder(
                            Order(
                                storeId = storeId,
                                orderNumber = "ORD-001",
                                customerId = c1Id,
                                totalAmount = 37800.0,
                                discount = 800.0,
                                finalAmount = 37000.0,
                                downPayment = 10000.0,
                                status = OrderStatus.PROCESSING,
                                creatingStaffId = staffId1,
                                responsibleStaffIds = listOf(staffId1)
                            )
                        )
                        var oi1Id = orderItemDao.insertOrUpdateOrderItem(
                            OrderItem(
                                orderId = order1Id,
                                productId = p1Id,
                                productName = "意式云朵真皮沙发",
                                quantity = 1,
                                actualUnitPrice = 12800.0,
                                itemTotalAmount = 12800.0,
                                isCustomized = false,
                                status = OrderItemStatus.ORDERED
                            )
                        )
                        var oi2Id = orderItemDao.insertOrUpdateOrderItem(
                            OrderItem(
                                orderId = order1Id,
                                productId = p3Id,
                                productName = "一门到顶定制衣柜(10平米)",
                                quantity = 10,
                                actualUnitPrice = 2500.0,
                                itemTotalAmount = 25000.0,
                                isCustomized = true,
                                status = OrderItemStatus.NOT_ORDERED
                            )
                        )

                        // 订单2: 李四的订单，包含灯具
                        var order2Id = orderDao.insertOrder(
                            Order(
                                storeId = storeId,
                                orderNumber = "ORD-002",
                                customerId = c2Id,
                                totalAmount = 4700.0,
                                finalAmount = 4700.0,
                                status = OrderStatus.PROCESSING,
                                creatingStaffId = staffId1,
                                responsibleStaffIds = listOf(staffId1)
                            )
                        )
                        var oi3Id = orderItemDao.insertOrUpdateOrderItem(
                            OrderItem(
                                orderId = order2Id,
                                productId = p4Id,
                                productName = "客厅无主灯磁吸轨道灯",
                                quantity = 10,
                                actualUnitPrice = 350.0,
                                itemTotalAmount = 3500.0,
                                isCustomized = false,
                                status = OrderItemStatus.IN_STOCK
                            )
                        )
                        var oi4Id = orderItemDao.insertOrUpdateOrderItem(
                            OrderItem(
                                orderId = order2Id,
                                productId = p5Id,
                                productName = "智能调光防眩筒灯",
                                quantity = 10,
                                actualUnitPrice = 120.0,
                                itemTotalAmount = 1200.0,
                                isCustomized = false,
                                status = OrderItemStatus.IN_STOCK
                            )
                        )

                        // 订单3: 王五公司的订单，已完成
                        var order3Id = orderDao.insertOrder(
                            Order(
                                storeId = storeId,
                                orderNumber = "ORD-003",
                                customerId = c3Id,
                                totalAmount = 7500.0,
                                finalAmount = 7500.0,
                                status = OrderStatus.COMPLETED,
                                completionDate = System.currentTimeMillis() - 86400000,
                                creatingStaffId = staffId2
                            )
                        )
                        var oi5Id = orderItemDao.insertOrUpdateOrderItem(
                            OrderItem(
                                orderId = order3Id,
                                productId = p2Id,
                                productName = "悬浮智能储物床",
                                quantity = 1,
                                actualUnitPrice = 7500.0,
                                itemTotalAmount = 7500.0,
                                isCustomized = false,
                                status = OrderItemStatus.INSTALLED
                            )
                        )

                        // 8. 创建状态变更日志
                        orderItemStatusLogDao.insertLog(
                            OrderItemStatusLog(
                                orderItemId = oi1Id,
                                status = OrderItemStatus.NOT_ORDERED,
                                staffId = staffId1,
                                timestamp = System.currentTimeMillis() - 200000
                            )
                        )
                        orderItemStatusLogDao.insertLog(
                            OrderItemStatusLog(
                                orderItemId = oi1Id,
                                status = OrderItemStatus.ORDERED,
                                staffId = bossId,
                                timestamp = System.currentTimeMillis() - 100000
                            )
                        )
                        orderItemStatusLogDao.insertLog(
                            OrderItemStatusLog(
                                orderItemId = oi5Id,
                                status = OrderItemStatus.INSTALLED,
                                staffId = staffId2,
                                timestamp = System.currentTimeMillis() - 86400000
                            )
                        )

                        // --- 9. 创建库存记录 (修改这部分) ---
                        val inventoryItemDao = inventoryItemDaoProvider.get()

                        // **手动实现 increaseStock 的逻辑**
                        // 入库 20 个磁吸轨道灯 (p4Id)
                        inventoryItemDao.insert(InventoryItem(storeId = storeId, productId = p4Id, isStandardStock = true, quantity = 20, status = InventoryItemStatus.AVAILABLE))

                        // 入库 30 个筒灯 (p5Id)
                        inventoryItemDao.insert(InventoryItem(storeId = storeId, productId = p5Id, isStandardStock = true, quantity = 30, status = InventoryItemStatus.AVAILABLE))

                        // 为订单2预定的库存项 (假设它们已经到库，我们从标准化库存中扣除，并创建独立的预定库存项)

                        // a. 扣减标准化库存 (磁吸轨道灯)
                        val p4Stock = inventoryItemDao.findStandardStockByStoreAndProduct(storeId, p4Id)
                        if (p4Stock != null) {
                            inventoryItemDao.update(p4Stock.copy(quantity = p4Stock.quantity - 10))
                        }

                        // b. 创建预定的定制化库存项 (磁吸轨道灯)
                        inventoryItemDao.insert(InventoryItem(
                            storeId = storeId,
                            productId = p4Id,
                            quantity = 10, // 这里的 quantity 表示这个订单项需要10个
                            isStandardStock = false, // 标记为非标准库存（因为它已为特定订单项预留）
                            reservedForOrderItemId = oi3Id, // 关联到订单项 oi3Id
                            status = InventoryItemStatus.RESERVED
                        ))

                        // c. 扣减标准化库存 (筒灯)
                        val p5Stock = inventoryItemDao.findStandardStockByStoreAndProduct(storeId, p5Id)
                        if (p5Stock != null) {
                            inventoryItemDao.update(p5Stock.copy(quantity = p5Stock.quantity - 10))
                        }

                        // d. 创建预定的定制化库存项 (筒灯)
                        inventoryItemDao.insert(InventoryItem(
                            storeId = storeId,
                            productId = p5Id,
                            quantity = 10,
                            isStandardStock = false,
                            reservedForOrderItemId = oi4Id,
                            status = InventoryItemStatus.RESERVED
                        ))

                        Log.d("DatabaseModule", "Inventory items created and adjusted.")
                        Log.d("DatabaseModule", "Rich test data population finished.")
                    }
                }
            })
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

    @Provides
    @Singleton
    fun provideSupplierDao(database: AppDatabase): SupplierDao { // <-- 添加
        return database.supplierDao()
    }


    @Provides
    @Singleton
    fun provideInventoryItemDao(database: AppDatabase): InventoryItemDao {
        return database.inventoryItemDao()
    }

    @Provides
    @Singleton
    fun provideOrderItemStatusLogDao(database: AppDatabase): OrderItemStatusLogDao { // <-- **为 OrderItemStatusLogDao 添加 Provider**
        return database.orderItemStatusLogDao()
    }




}


