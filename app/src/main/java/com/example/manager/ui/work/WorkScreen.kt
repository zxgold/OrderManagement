package com.example.manager.ui.work

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.* // 导入所有需要的图标
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.manager.ui.components.DashboardItem // 导入 DashboardItem
import com.example.manager.ui.navigation.AppDestinations
import com.example.manager.ui.navigation.AppDestinations.CUSTOMER_LIST_ROUTE
import com.example.manager.ui.navigation.AppDestinations.ORDER_LIST_ROUTE


data class DashboardActionItem(
    val label: String,
    val icon: ImageVector,
    val actionTag: String // 用于在 onClick 中识别是哪个项目被点击了
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // FlowRow 需要 ExperimentalLayoutApi
@Composable
fun WorkScreen(
    bottomNavController: NavController // 从 MainScreen 传递过来的
) {
    val customerManagementItems = listOf(
        DashboardActionItem("客户", Icons.Filled.People, "customer_list_for_work"),
        DashboardActionItem("跟进", Icons.Filled.Schedule, "follow_up"),
        DashboardActionItem("销售单", Icons.AutoMirrored.Filled.ReceiptLong, "order_list_screen"),
        DashboardActionItem("回款", Icons.Filled.Payment, "payments"),
        DashboardActionItem("工单", Icons.AutoMirrored.Filled.ListAlt, "work_orders")
    )

    val internalManagementItems = listOf(
        DashboardActionItem("审批", Icons.Filled.Approval, "approvals"),
        DashboardActionItem("资料云盘", Icons.Filled.Cloud, "cloud_storage"),
        DashboardActionItem("支出账本", Icons.Filled.AccountBalanceWallet, "ledger"),
        DashboardActionItem("供应商", Icons.Filled.Storefront, "supplier_product_screen")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 使整个屏幕可滚动
            .padding(vertical = 16.dp)
    ) {
        // 顶部的搜索/查询客户栏 (可以保留)
        OutlinedTextField(
            value = "", // 临时值，后续需要状态管理
            onValueChange = { /* TODO */ },
            label = { Text("全局搜索...") }, // 可以改为更通用的搜索
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "搜索") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp)) // 增加与下方卡片的间距

        // 客户管理栏
        SectionTitleWithHorizontalPadding("客户管理") // 使用带padding的标题
        HorizontalDashboardItemList( // 新的可复用组件
            items = customerManagementItems,
            onItemClick = { itemTag ->
                Log.d("WorkScreen", "客户管理项点击: $itemTag")
                if (itemTag == CUSTOMER_LIST_ROUTE) {
                    bottomNavController.navigate(CUSTOMER_LIST_ROUTE)
                }
                if (itemTag == ORDER_LIST_ROUTE) {
                    bottomNavController.navigate(ORDER_LIST_ROUTE)
                }


                // TODO: 处理其他导航
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 内部管理栏
        SectionTitleWithHorizontalPadding("内部管理")
        HorizontalDashboardItemList(
            items = internalManagementItems,
            onItemClick = { itemTag ->
                Log.d("WorkScreen", "内部管理项点击: $itemTag")
                // TODO: 处理导航
            }
        )
    }
}

@Composable
private fun SectionTitleWithHorizontalPadding(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(horizontal = 16.dp) // 给标题也加上水平padding
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun HorizontalDashboardItemList(
    items: List<DashboardActionItem>, // items 参数是一个列表
    onItemClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // items(items) { item -> // 如果 items 是参数名，直接这样写可能会有作用域问题
        items(items = items) { itemData -> // **明确指定 items 参数，并将 lambda 参数命名为 itemData (或其他)**
            DashboardItem(
                icon = itemData.icon,     // <-- 使用 itemData.icon
                label = itemData.label,   // <-- 使用 itemData.label
                onClick = { onItemClick(itemData.actionTag) } // <-- 使用 itemData.actionTag
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardGrid(
    items: List<DashboardActionItem>,
    onItemClick: (String) -> Unit // 传递 actionTag
) {
    // 使用 FlowRow 实现自适应换行的网格
    // 每行期望放置 3-4 个项目，可以根据 DashboardItem 的宽度自动调整
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp), // 项目之间的水平间距
        verticalArrangement = Arrangement.spacedBy(12.dp)   // 项目之间的垂直间距
        // maxItemsInEachRow = 4 // 如果想严格控制每行数量，但FlowRow更侧重流式布局
    ) {
        items.forEach { item ->
            // 为每个 DashboardItem 设置一个合适的宽度，让 FlowRow 能够排列它们
            // 你可以尝试不同的 Modifier.width() 或 Modifier.weight() (如果外层是 Row/Column)
            // 这里我们让 DashboardItem 自己 fillMaxWidth 然后 aspectRatio(1f) 来撑开单元格
            // FlowRow 会根据可用空间来排列。
            // 为了让每行能放下多个，我们可以给每个item一个近似的宽度，
            // 例如，如果一行想放3个，那么每个item大概是屏幕宽度的1/3减去间距。
            // 但更简单的方法是直接使用 FlowRow 的特性，让它自动排列。
            // 我们需要确保 DashboardItem 本身不要过大。
            Box(modifier = Modifier.weight(1f)) { // 尝试让每个项目在可用空间内平均分配
                // 注意：FlowRow 和 weight 的直接组合可能不如预期，
                // 通常 weight 用于 Row/Column 的直接子元素。
                // 一个更可靠的方式是计算宽度。
                // **或者，更简单的方式是直接不设置weight，依赖DashboardItem的aspectRatio和padding**
                DashboardItem(
                    icon = item.icon,
                    label = item.label,
                    onClick = { onItemClick(item.actionTag) }
                    // modifier = Modifier.width(90.dp) // 调整这个宽度看效果
                )
            }
        }
    }
}