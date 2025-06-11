package com.example.manager.ui.work

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.manager.ui.customer.CustomerListScreen // 直接嵌入客户列表
import com.example.manager.ui.navigation.AppScreenRoutes // 导入路由定义 (如果需要从这里导航)

@OptIn(ExperimentalMaterial3Api::class) // 如果用到 Material3 组件
@Composable
fun WorkScreen(
    // 这个 navController 是 MainScreen 中 bottomSheetNavController 传递过来的
    // 它负责 WorkScreen 内部可能的子导航，或者 MainScreen 的 navController
    // 如果 WorkScreen 的入口都直接是全屏切换，则可能不需要它
    // 我们先假设 CustomerListScreen 是 WorkScreen 的一部分
    // mainScreenNavController: NavController // 从 MainScreen 传递过来的 NavController
) {
    // 我们可以使用一个 Column 来组织 WorkScreen 的内容
    // 或者，如果想让 CustomerListScreen 直接占据整个 WorkScreen 区域，可以简化
    // 这里我们先展示一种包含多个入口的布局方式

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "工作台",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- 客户管理栏 ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("客户管理", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                WorkScreenNavItem("客户") {
                    // TODO: 导航到 CustomerListScreen (如果它不是直接嵌入)
                    // 目前，我们可以考虑将 CustomerListScreen 直接作为工作台的一个主要部分
                    // 或者，如果想通过点击进入，则需要 mainScreenNavController.navigate(...)
                    // 为简单起见，我们先不在这里做导航，而是考虑直接嵌入或后续添加
                    // mainScreenNavController.navigate(AppScreenRoutes.CUSTOMER_LIST_ROUTE_IN_WORK) // 需要新的路由
                    // 暂时先不实现点击，因为 CustomerListScreen 会直接显示在下方
                }
                WorkScreenNavItem("客户跟进 (TODO)") { /* TODO */ }
                WorkScreenNavItem("销售单 (TODO)") { /* TODO */ }
                WorkScreenNavItem("回款 (TODO)") { /* TODO */ }
                WorkScreenNavItem("工单 (TODO)") { /* TODO */ }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 内部管理栏 ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("内部管理", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                WorkScreenNavItem("账本 (TODO)") { /* TODO */ }
                // ... 其他内部管理入口 ...
            }
        }

        // --- 或者，如果想让客户列表直接显示在工作台 ---
        // Spacer(modifier = Modifier.height(16.dp))
        // Text("近期客户", style = MaterialTheme.typography.titleMedium)
        // Box(modifier = Modifier.weight(1f)) { // 占据剩余空间
        //     CustomerListScreen() // 直接嵌入客户列表屏幕
        // }
        // 这种直接嵌入的方式，CustomerListScreen 需要能适应在部分屏幕区域显示

        // **更简单的初始版本：WorkScreen 直接就是 CustomerListScreen**
        // 如果 "工作" 标签页的主要内容就是客户列表，可以这样做：
        // CustomerListScreen()
        // **但根据你的描述，工作台是有多个入口的，所以上面的 Card 布局更合适。**
        // **我们需要决定点击 "客户" 按钮后如何展示 CustomerListScreen。**

        // **方案A：WorkScreen 内部导航 (使用 MainScreen 的 bottomSheetNavController)**
        // 这意味着 CustomerListScreen 也成为 MainScreen 内部 NavHost 的一个目标。
        // WorkScreen 内部的按钮点击后调用 bottomSheetNavController.navigate("customer_list_from_work")

        // **方案B：WorkScreen 就是一个入口列表，点击后进行全局导航 (使用 AppNavigation 的 navController)**
        // 这需要将 AppNavigation 的 navController 传递给 MainScreen，再传递给 WorkScreen。
        // 点击 "客户" 按钮后调用 mainNavController.navigate(AppScreenRoutes.CUSTOMER_LIST_ROUTE)

        // **为了简单起步和快速看到效果，我们先假设“工作”页的主要内容就是客户列表，**
        // **后续再改成带有多个入口的界面。**
        // **所以，我们暂时让 WorkScreen 直接显示 CustomerListScreen。**
        // **请注意，这只是一个临时简化，后续我们会根据你的需求构建带多个入口的 WorkScreen。**
    }
}

// WorkScreen 的子组件，用于创建导航项
@Composable
private fun WorkScreenNavItem(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}