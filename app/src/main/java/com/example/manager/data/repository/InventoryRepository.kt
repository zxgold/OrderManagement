package com.example.manager.data.repository

import com.example.manager.data.model.uimodel.InventoryItemWithProductInfo
import kotlinx.coroutines.flow.Flow

/**
 * 库存数据的 Repository 接口
 */
interface InventoryRepository {

    /**
     * 获取某个店铺的所有库存项（包含产品信息），并以 Flow 的形式返回。
     */
    fun getInventoryItemsWithProductInfoFlow(storeId: Long): Flow<List<InventoryItemWithProductInfo>>

    /**
     * 增加指定产品的库存数量。如果库存项不存在，则会创建。
     */
    suspend fun increaseStock(storeId: Long, productId: Long, amount: Int)

    /**
     * 减少指定产品的库存数量。
     * @return Result<Unit> - 如果成功则返回 Success，如果库存不足则返回 Failure。
     */
    suspend fun decreaseStock(storeId: Long, productId: Long, amount: Int): Result<Unit>

    // 未来可能添加手动入库等其他方法
    // suspend fun manualStockIn(inventoryItem: InventoryItem): Result<Unit>
}