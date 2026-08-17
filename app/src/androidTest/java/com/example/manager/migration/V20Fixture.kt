package com.example.manager.migration

import androidx.sqlite.db.SupportSQLiteDatabase

/** Completely fictional V20 data. It deliberately avoids all real business data. */
object V20Fixture {
    const val databaseName = "v20-migration-fixture.db"

    fun insertInto(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            database.execSQL("INSERT INTO stores (id, store_name, address, phone, created_at, updated_at, owner_staff_id) VALUES (1, '虚构卫浴体验店', '测试街 1 号', '010-00000000', 1700000000000, 1700000000000, NULL)")

            database.execSQL("INSERT INTO staff (id, store_id, name, role, username, password_hash, is_active, created_at, updated_at) VALUES (1, 1, '虚构老板', 'BOSS', 'fixture_boss', 'fixture-only', 1, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO staff (id, store_id, name, role, username, password_hash, is_active, created_at, updated_at) VALUES (2, 1, '虚构销售', 'STAFF', 'fixture_sales', 'fixture-only', 1, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO staff (id, store_id, name, role, username, password_hash, is_active, created_at, updated_at) VALUES (3, 1, '虚构股东', 'SHAREHOLDER', 'fixture_shareholder', 'fixture-only', 1, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO staff (id, store_id, name, role, username, password_hash, is_active, created_at, updated_at) VALUES (4, 1, '虚构停用员工', 'STAFF', 'fixture_disabled', 'fixture-only', 0, 1700000000000, 1700000000000)")
            database.execSQL("UPDATE stores SET owner_staff_id = 1 WHERE id = 1")

            database.execSQL("INSERT INTO suppliers (id, store_id, name, contact_person, phone, address, remark, created_at) VALUES (1, 1, '虚构供应商甲', '甲联系人', '010-00000001', NULL, NULL, 1700000000000)")
            database.execSQL("INSERT INTO suppliers (id, store_id, name, contact_person, phone, address, remark, created_at) VALUES (2, 1, '虚构供应商乙', '乙联系人', '010-00000002', NULL, NULL, 1700000000000)")
            database.execSQL("INSERT INTO products (id, supplier_id, category, name, model, default_price, description, specifications, is_active, created_at, updated_at) VALUES (1, 1, '马桶', '虚构智能马桶', 'FX-T1', 2999.995, NULL, NULL, 1, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO products (id, supplier_id, category, name, model, default_price, description, specifications, is_active, created_at, updated_at) VALUES (2, 1, '花洒', '虚构恒温花洒', 'FX-S1', 800.00, NULL, NULL, 1, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO products (id, supplier_id, category, name, model, default_price, description, specifications, is_active, created_at, updated_at) VALUES (3, 2, '浴室柜', '虚构浴室柜', 'FX-C1', 12500.50, NULL, NULL, 1, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO products (id, supplier_id, category, name, model, default_price, description, specifications, is_active, created_at, updated_at) VALUES (4, 2, '配件', '虚构下架配件', 'FX-A1', 0.01, NULL, NULL, 0, 1700000000000, 1700000000000)")

            database.execSQL("INSERT INTO customers (id, store_id, name, phone, address, remark, created_at, updated_at) VALUES (1, 1, '虚构客户甲', '13800000001', '虚构小区 1-101', NULL, 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO customers (id, store_id, name, phone, address, remark, created_at, updated_at) VALUES (2, 1, '虚构客户乙', '13800000002', '虚构小区 2-202', '负责人不可由备注推断', 1700000000000, 1700000000000)")
            database.execSQL("INSERT INTO customers (id, store_id, name, phone, address, remark, created_at, updated_at) VALUES (3, 1, '虚构客户丙', '13800000003', NULL, NULL, 1700000000000, 1700000000000)")

            database.execSQL("INSERT INTO orders (id, store_id, order_number, customer_id, order_date, total_amount, discount, final_amount, down_payment, status, completion_date, responsible_staff_ids, notes, created_at, updated_at, creating_staff_id) VALUES (1, 1, 'FIX-001', 1, 1700000000000, 10000.005, 0.005, 10000.000, 2000.005, 'PROCESSING', NULL, '[2,3]', NULL, 1700000000000, 1700000000000, 2)")
            database.execSQL("INSERT INTO orders (id, store_id, order_number, customer_id, order_date, total_amount, discount, final_amount, down_payment, status, completion_date, responsible_staff_ids, notes, created_at, updated_at, creating_staff_id) VALUES (2, 1, 'FIX-002', 2, 1700000000000, 800.00, 0.00, 800.00, 0.00, 'COMPLETED', 1700100000000, '[2]', NULL, 1700000000000, 1700100000000, 2)")
            database.execSQL("INSERT INTO orders (id, store_id, order_number, customer_id, order_date, total_amount, discount, final_amount, down_payment, status, completion_date, responsible_staff_ids, notes, created_at, updated_at, creating_staff_id) VALUES (3, 1, 'FIX-003', NULL, 1700000000000, 0.01, 0.00, 0.01, 0.00, 'DRAFT', NULL, NULL, '无客户草稿', 1700000000000, 1700000000000, 4)")
            database.execSQL("INSERT INTO order_items (id, order_id, product_id, product_category, product_name, product_model, dimensions, color, quantity, actual_unit_price, item_total_amount, status, status_last_update_staff_id, status_last_update_at, ordered_vendor_at, arrived_stock_at, installed_at, notes, is_customized) VALUES (1, 1, 1, '马桶', '虚构智能马桶', 'FX-T1', NULL, NULL, 2, 2999.995, 5999.990, 'ORDERED', 2, 1700000000100, 1700000000100, NULL, NULL, NULL, 0)")
            database.execSQL("INSERT INTO order_items (id, order_id, product_id, product_category, product_name, product_model, dimensions, color, quantity, actual_unit_price, item_total_amount, status, status_last_update_staff_id, status_last_update_at, ordered_vendor_at, arrived_stock_at, installed_at, notes, is_customized) VALUES (2, 1, 3, '浴室柜', '虚构浴室柜', 'FX-C1', NULL, NULL, 1, 4000.015, 4000.015, 'NOT_ORDERED', 3, 1700000000200, NULL, NULL, NULL, NULL, 1)")
            database.execSQL("INSERT INTO order_items (id, order_id, product_id, product_category, product_name, product_model, dimensions, color, quantity, actual_unit_price, item_total_amount, status, status_last_update_staff_id, status_last_update_at, ordered_vendor_at, arrived_stock_at, installed_at, notes, is_customized) VALUES (3, 2, 2, '花洒', '虚构恒温花洒', 'FX-S1', NULL, NULL, 1, 800.00, 800.00, 'INSTALLED', 2, 1700100000000, 1700001000000, 1700002000000, 1700100000000, NULL, 0)")
            database.execSQL("INSERT INTO order_items (id, order_id, product_id, product_category, product_name, product_model, dimensions, color, quantity, actual_unit_price, item_total_amount, status, status_last_update_staff_id, status_last_update_at, ordered_vendor_at, arrived_stock_at, installed_at, notes, is_customized) VALUES (4, 3, 4, '配件', '虚构下架配件', 'FX-A1', NULL, NULL, 1, 0.01, 0.01, 'NOT_ORDERED', 4, 1700000000300, NULL, NULL, NULL, NULL, 0)")

            database.execSQL("INSERT INTO payments (id, store_id, order_id, customer_id, amount, payment_date, payment_method, staff_id, notes, created_at) VALUES (1, 1, 1, 1, 2000.005, 1700000001000, 'CASH', 2, '虚构首付款', 1700000001000)")
            database.execSQL("INSERT INTO payments (id, store_id, order_id, customer_id, amount, payment_date, payment_method, staff_id, notes, created_at) VALUES (2, 1, 2, 2, 800.00, 1700100000000, 'CARD', 2, '虚构尾款', 1700100000000)")
            database.execSQL("INSERT INTO payments (id, store_id, order_id, customer_id, amount, payment_date, payment_method, staff_id, notes, created_at) VALUES (3, 1, 1, 1, -0.005, 1700200000000, 'CASH', 3, '虚构退款式历史记录', 1700200000000)")

            database.execSQL("INSERT INTO follow_ups (id, customer_id, order_id, follow_up_date, notes, staff_id, is_planned, status, scheduled_date, next_action_date, next_action_note, created_at) VALUES (1, 1, 1, 1700000002000, '虚构已跟进', 2, 0, 'DONE', NULL, 1700300000000, '回访', 1700000002000)")
            database.execSQL("INSERT INTO follow_ups (id, customer_id, order_id, follow_up_date, notes, staff_id, is_planned, status, scheduled_date, next_action_date, next_action_note, created_at) VALUES (2, 2, 2, 1700000003000, '虚构计划跟进', 2, 1, 'PLANNED', 1700300000000, NULL, NULL, 1700000003000)")
            database.execSQL("INSERT INTO follow_ups (id, customer_id, order_id, follow_up_date, notes, staff_id, is_planned, status, scheduled_date, next_action_date, next_action_note, created_at) VALUES (3, 3, NULL, 1700000004000, '停用员工历史跟进', 4, 0, 'DONE', NULL, NULL, NULL, 1700000004000)")
            database.execSQL("INSERT INTO ledger_entries (id, store_id, entry_type, amount, entry_date, description, related_order_id, related_customer_id, payment_id, staff_id, notes, created_at) VALUES (1, 1, 'INCOME', 2000.005, 1700000001000, '虚构回款收入', 1, 1, 1, 2, NULL, 1700000001000)")
            database.execSQL("INSERT INTO ledger_entries (id, store_id, entry_type, amount, entry_date, description, related_order_id, related_customer_id, payment_id, staff_id, notes, created_at) VALUES (2, 1, 'EXPENSE', 99.995, 1700000005000, '虚构采购支出', NULL, NULL, NULL, 3, NULL, 1700000005000)")
            database.execSQL("INSERT INTO ledger_entries (id, store_id, entry_type, amount, entry_date, description, related_order_id, related_customer_id, payment_id, staff_id, notes, created_at) VALUES (3, 1, 'INCOME', 50.00, 1700000006000, '虚构其他收入', NULL, NULL, NULL, 1, NULL, 1700000006000)")
            database.execSQL("INSERT INTO action_logs (id, action_time, staff_id, action_type, target_entity_type, target_entity_id, details) VALUES (1, 1700000007000, 1, 'LOGIN', 'STAFF', 1, 'fixture')")
            database.execSQL("INSERT INTO action_logs (id, action_time, staff_id, action_type, target_entity_type, target_entity_id, details) VALUES (2, 1700000007001, 2, 'CREATE_ORDER', 'ORDER', 1, 'fixture')")
            database.execSQL("INSERT INTO action_logs (id, action_time, staff_id, action_type, target_entity_type, target_entity_id, details) VALUES (3, 1700000007002, 3, 'CREATE_LEDGER', 'LEDGER', 2, 'fixture')")
            database.execSQL("INSERT INTO action_logs (id, action_time, staff_id, action_type, target_entity_type, target_entity_id, details) VALUES (4, 1700000007003, 4, 'DISABLED_STAFF_ACTION', 'CUSTOMER', 3, 'fixture')")
            database.execSQL("INSERT INTO order_item_status_logs (id, order_item_id, status, staff_id, timestamp) VALUES (1, 1, 'NOT_ORDERED', 2, 1700000000000)")
            database.execSQL("INSERT INTO order_item_status_logs (id, order_item_id, status, staff_id, timestamp) VALUES (2, 1, 'ORDERED', 2, 1700000000100)")
            database.execSQL("INSERT INTO order_item_status_logs (id, order_item_id, status, staff_id, timestamp) VALUES (3, 3, 'INSTALLED', 2, 1700100000000)")
            database.execSQL("INSERT INTO inventory_items (id, store_id, product_id, is_standard_stock, quantity, customization_details, reserved_for_order_item_id, status, last_updated_at, location_in_warehouse) VALUES (1, 1, 1, 1, 8, NULL, NULL, 'AVAILABLE', 1700000008000, 'A-01')")
            database.execSQL("INSERT INTO inventory_items (id, store_id, product_id, is_standard_stock, quantity, customization_details, reserved_for_order_item_id, status, last_updated_at, location_in_warehouse) VALUES (2, 1, 2, 1, 15, NULL, NULL, 'AVAILABLE', 1700000008000, 'A-02')")
            database.execSQL("INSERT INTO inventory_items (id, store_id, product_id, is_standard_stock, quantity, customization_details, reserved_for_order_item_id, status, last_updated_at, location_in_warehouse) VALUES (3, 1, 2, 0, 1, 'legacy reservation', 3, 'RESERVED', 1700000008000, 'A-02')")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}
