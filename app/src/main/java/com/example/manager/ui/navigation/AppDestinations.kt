package com.example.manager.ui.navigation


object AppDestinations {
    const val LOADING_SCREEN_ROUTE = "loading_screen" // 新增加载屏幕路由
    const val LOGIN_ROUTE = "login"
    const val REGISTRATION_ROUTE = "registration"
    const val MAIN_APP_ROUTE = "main_app"         // 主应用框架路由

    const val CUSTOMER_LIST_ROUTE = "customer_list_for_work" // 客户页面路由
    const val CUSTOMER_DETAIL_ROUTE_TEMPLATE = "customer_detail_screen/{customerId}" // 客户详情模板
    fun customerDetailRoute(customerId: Long): String { // 构建客户详情路由的辅助函数
        return "customer_detail_screen/$customerId"
    }

    const val ORDER_LIST_ROUTE = "order_list_screen" // <-- 新增：订单列表路由
    const val ORDER_DETAIL_ROUTE_TEMPLATE = "order_detail_screen/{orderId}" // <-- 新增：订单详情
    fun orderDetailRoute(orderId: Long) = "order_detail_screen/$orderId"
    const val ADD_EDIT_ORDER_ROUTE = "add_edit_order_screen" // <-- 新增：添加/编辑订单
    // 如果编辑也需要ID: const val ADD_EDIT_ORDER_ROUTE_TEMPLATE = "add_edit_order_screen?orderId={orderId}"

    // 使用可选参数来处理新增和编辑
    const val ADD_EDIT_ORDER_ROUTE_TEMPLATE = "add_edit_order_screen?orderId={orderId}"
    fun addOrderRoute(): String = "add_edit_order_screen" // 新增订单时，不传 orderId
    fun editOrderRoute(orderId: Long): String = "add_edit_order_screen?orderId=$orderId" // 编辑时传递 orderId

    const val SUPPLIER_PRODUCT_ROUTE = "supplier_product_screen"

    const val WORK_ORDER_LIST_ROUTE = "work_order_list_screen" // 新增工单列表路由

    const val WORK_ORDER_DETAIL_ARG_ID = "orderItemId" // 将参数名也定义为常量，避免拼写错误
    const val WORK_ORDER_DETAIL_ROUTE_TEMPLATE = "work_order_detail_screen/{$WORK_ORDER_DETAIL_ARG_ID}"
    fun workOrderDetailRoute(orderItemId: Long): String {// 辅助函数，用于构建带实际 ID 的路由字符串
        return "work_order_detail_screen/$orderItemId"
    }

}