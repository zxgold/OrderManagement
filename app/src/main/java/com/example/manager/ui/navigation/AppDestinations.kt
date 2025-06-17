package com.example.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 统一管理应用的所有导航路由和目的地信息
 */
object AppDestinations {

    // --- Top Level Navigation (由最外层的 NavController 控制) ---
    const val LOADING_ROUTE = "loading_screen"
    const val LOGIN_ROUTE = "login_screen"
    const val REGISTRATION_ROUTE = "registration_screen"
    const val MAIN_APP_HOST_ROUTE = "main_app_host_screen" // 主应用框架

    // --- Bottom Navigation Tab Root Routes (由 MainScreen 的内部 NavController 控制) ---
    const val WORK_TAB_ROOT_ROUTE = "work_tab"
    const val ME_TAB_ROOT_ROUTE = "me_tab"

    // --- Routes accessible from Work Tab (工作台内部导航) ---
    const val CUSTOMER_LIST_ROUTE = "customer_list_screen"
    const val SUPPLIER_PRODUCT_ROUTE = "supplier_product_screen"
    const val WORK_ORDER_LIST_ROUTE = "work_order_list_screen"
    const val ORDER_LIST_ROUTE = "order_list_screen"
    const val INVENTORY_ROUTE = "inventory_screen"
    // TODO: 为其他 WorkScreen 的仪表盘入口定义路由...

    // --- Detail/Add/Edit Screen Routes (通常带参数) ---
    const val CUSTOMER_DETAIL_ARG_ID = "customerId"
    const val CUSTOMER_DETAIL_ROUTE_TEMPLATE = "customer_detail_screen/{$CUSTOMER_DETAIL_ARG_ID}"
    fun customerDetailRoute(customerId: Long): String = "customer_detail_screen/$customerId"

    const val ORDER_DETAIL_ARG_ID = "orderId"
    const val ORDER_DETAIL_ROUTE_TEMPLATE = "order_detail_screen/{$ORDER_DETAIL_ARG_ID}"
    fun orderDetailRoute(orderId: Long): String = "order_detail_screen/$orderId"

    const val WORK_ORDER_DETAIL_ARG_ID = "orderItemId"
    const val WORK_ORDER_DETAIL_ROUTE_TEMPLATE = "work_order_detail_screen/{$WORK_ORDER_DETAIL_ARG_ID}"
    fun workOrderDetailRoute(orderItemId: Long): String = "work_order_detail_screen/$orderItemId"

    const val ADD_EDIT_ORDER_ARG_ID = "orderId"
    const val ADD_EDIT_ORDER_ROUTE_TEMPLATE = "add_edit_order_screen?orderId={$ADD_EDIT_ORDER_ARG_ID}"
    fun addOrderRoute(): String = "add_edit_order_screen"
    fun editOrderRoute(orderId: Long): String = "add_edit_order_screen?orderId=$orderId"

    // **用于“即时添加客户”的专门路由**
    const val ADD_CUSTOMER_ARG_NAME = "defaultName"
    const val ADD_CUSTOMER_ROUTE_TEMPLATE = "add_customer_screen?${ADD_CUSTOMER_ARG_NAME}={${ADD_CUSTOMER_ARG_NAME}}"
    const val NEW_CUSTOMER_ID_RESULT_KEY = "new_customer_id"
    fun addCustomerRoute(defaultName: String = ""): String = "add_customer_screen?${ADD_CUSTOMER_ARG_NAME}=$defaultName"

    // --- Routes accessible from Me Tab  ---
    const val STAFF_MANAGEMENT_ROUTE = "staff_management_screen"
}

// --- Bottom Navigation Item Definitions ---
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Work : BottomNavItem(AppDestinations.WORK_TAB_ROOT_ROUTE, Icons.Filled.Work, "工作")
    object Me : BottomNavItem(AppDestinations.ME_TAB_ROOT_ROUTE, Icons.Filled.AccountCircle, "我的")
}