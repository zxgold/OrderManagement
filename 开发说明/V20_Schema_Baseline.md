# V20 Room Schema 基线（阶段 0）

本文件归档的是当前 V20 Room 结构基线，而非真实业务数据。唯一来源为 [20.json](../app/schemas/com.example.manager.data.db.AppDatabase/20.json)。

- Room database：`com.example.manager.data.db.AppDatabase`
- schema version：`20`
- identity hash：`24de24a4e618c44353484a67f5b1dee0`
- schema 文件 SHA-256：`C6E35FECC41951857816904AE8348238ED689316DB394B8DDFCD9A8227B8DC97`
- 归档日期：2026-08-16

## 表清单（13 个）

`stores`、`suppliers`、`staff`、`customers`、`products`、`orders`、`order_items`、`payments`、`follow_ups`、`ledger_entries`、`action_logs`、`order_item_status_logs`、`inventory_items`。

## 关键旧结构事实

- 员工角色为 `staff.role` 单值枚举（BOSS / STAFF / SHAREHOLDER）。
- 金额字段为 `REAL`：产品价格、订单金额、订单项金额、回款、账本。
- `orders.responsible_staff_ids` 为文本列表；没有规范化的订单人员分配表。
- Customer 没有负责人字段。
- `action_logs.staff_id` 旧外键为 `ON DELETE CASCADE`，与 V1 审计保护要求冲突。
- 旧库存表同时保存标准库存和订单预留库存；V1 将改为余额加不可变流水。

此文档与 schema JSON 一起构成阶段 0 的 V20 结构基线。后续不得用真实 V20 业务数据库替换或补充测试数据。
