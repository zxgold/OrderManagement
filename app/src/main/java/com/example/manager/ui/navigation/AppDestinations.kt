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

}