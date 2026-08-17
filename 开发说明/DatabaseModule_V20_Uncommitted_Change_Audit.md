# DatabaseModule.kt 未提交修改审计（阶段 0）

审计对象：[DatabaseModule.kt](../app/src/main/java/com/example/manager/di/DatabaseModule.kt)。本文件只记录状态，不修改该生产文件。

## 检测到的未提交修改

相对 Git 基线，该文件新增了一个 `RoomDatabase.Callback` 初始化协程，并通过 `Provider<DAO>` 取得 Store、Staff、Supplier、Product、Customer、Order、OrderItem、OrderItemStatusLog 和 InventoryItem DAO，在 `onCreate` 写入演示数据。

演示数据包括一个店铺、BOSS/STAFF/SHAREHOLDER 员工、供应商、家具/照明产品、客户、订单、订单项、状态日志和包含“预留库存”的库存记录。数据使用旧单角色、明文 `passwordHash = "123456"`、Double 金额以及 V1 已排除的库存预留语义。

## 结论

不应作为 V1 生产数据库初始化逻辑保留。它与 V1 的虚构 Migration Fixture 目标重复，且会在首次安装时污染正式数据库、绕过后续 PBKDF2、权限与库存流水规则。

阶段 0 不删除或修改此改动，以保护现有工作区。阶段 7 Legacy Cleanup 前，应由产品确认后移除或改造为仅调试构建的 `DemoDataInitializer`，且不得自动运行于生产数据库。
