package com.example.manager.data.model.enums

/**
 * 员工角色
 */
enum class StaffRole  {
    BOSS,       //老板
    STAFF,      //普通店员
    SHAREHOLDER //股东
}

/**
 * 订单总体状态 (V3)
 */
enum class OrderStatus {
    PENDING,     // 待处理/草稿 (刚创建，可能还未完成所有信息填写)
    PROCESSING,  // 处理中 (订单确认，产品在不同阶段流转)
    COMPLETED,   // 已完成 (所有产品已安装)
    CANCELLED    // 已取消
}

/**
 * 订单项/产品进度状态
 */
enum class OrderItemStatus {
    NOT_ORDERED,   // 未向厂家下单
    ORDERED,       // 已向厂家下单
    IN_TRANSIT,    // 物流中
    IN_STOCK,      // 已到库 (待安装)
    INSTALLING,    // 安装中 (可选，如果需要更细致跟踪)
    INSTALLED      // 已安装
}

/**
 * 回访计划/记录状态
 */
enum class FollowUpStatus {
    PENDING,     // 待执行
    COMPLETED,   // 已完成
    SKIPPED      // 已跳过/取消本次
}

/**
 * 日记账条目类型
 */
enum class LedgerEntryType {
    INCOME,     // 收入
    EXPENSE     // 支出
}

/**
 * 操作日志类型 (示例，可以根据需要增删)
 */
enum class ActionLogType {
    // 用户与系统
    LOGIN_SUCCESS,
    LOGIN_FAIL,
    STAFF_CREATED,
    STAFF_UPDATED,
    STAFF_DEACTIVATED,

    // 客户管理
    CUSTOMER_CREATED,
    CUSTOMER_UPDATED,
    CUSTOMER_DELETED,

    // 产品目录
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DEACTIVATED,

    // 订单管理
    ORDER_CREATED,
    ORDER_UPDATED, // 如修改备注、负责人
    ORDER_STATUS_CHANGED, // PENDING -> PROCESSING -> COMPLETED / CANCELLED
    ORDER_COMPLETED, // 标记完成，触发首次回访
    ORDER_CANCELLED,

    // 订单项管理
    ORDER_ITEM_ADDED,
    ORDER_ITEM_UPDATED, // 如修改数量、价格、备注
    ORDER_ITEM_REMOVED,
    ORDER_ITEM_STATUS_CHANGED, // NOT_ORDERED -> ORDERED -> ... -> INSTALLED

    // 收款管理
    PAYMENT_RECEIVED,
    PAYMENT_DELETED, // 是否允许删除？需谨慎

    // 回访管理
    FOLLOWUP_SCHEDULED, // 回访计划生成
    FOLLOWUP_COMPLETED, // 回访完成
    FOLLOWUP_SKIPPED,   // 回访跳过
    FOLLOWUP_UPDATED,   // 如修改计划日期或备注

    // 账目管理
    LEDGER_ENTRY_CREATED,
    LEDGER_ENTRY_UPDATED, // 通常不建议修改，除非修正错误
    LEDGER_ENTRY_DELETED  // 同上
}
