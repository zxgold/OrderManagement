package com.example.manager // 确保包名正确


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items// 导入 LazyListScope.items
//import androidx.compose.foundation.text.KeyboardActions// <-- **修正: 导入正确的 KeyboardActions**
//import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction// <-- **修正: 导入正确的 ImeAction**
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.manager.repository.OrderRepository
import com.example.manager.viewmodel.OrderViewModel
import com.example.manager.viewmodel.OrderViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.NumberFormat
import java.util.Locale

// --- 预览用的假 DAO ---


// 确保导入你的 Order 类 和 OrderDao 接口
// import com.example.manager.data.Order
// import com.example.manager.data.OrderDao

class FakeOrderDao : OrderDao { // 确保实现了你的 OrderDao 接口

    // 内部存储，注意 ID 类型现在与接口匹配 (假设 Order 的 id 是 Int 或可以处理 Int)
    // 如果 Order 的 id 必须是 Long，你需要在比较时转换
    private val orders = mutableListOf(
        // 示例数据，确保 ID 类型与 Order 定义一致，并能与 Int 比较
        Order(id = 1, orderNumber = "PREVIEW001", customerName = "Preview Customer A", amount = 99.99), // 假设 id 是 Int
        Order(id = 2, orderNumber = "PREVIEW002", customerName = "Preview Customer B", amount = 120.50)
    )
    private var nextId = orders.maxOfOrNull { it.id }?.plus(1) ?: 1 // 模拟自增 ID (Int)


    override suspend fun insert(order: Order) {
        // 如果 Order 的 id 是 Int 且自动生成
        val orderToInsert = if (order.id == 0) { // 假设 0 表示新订单
            order.copy(id = nextId++)
        } else {
            // 处理冲突替换逻辑 (如果需要严格模拟 REPLACE)
            orders.removeAll { it.id == order.id }
            order // 使用传入的 id
        }
        orders.add(orderToInsert)
        println("FakeDao Inserted: $orderToInsert")
    }

    override suspend fun update(order: Order) {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            orders[index] = order
            println("FakeDao Updated: $order")
        } else {
            println("FakeDao Update Warning: Order with id ${order.id} not found.")
        }
    }

    override suspend fun delete(order: Order) {
        val removed = orders.removeAll { it.id == order.id }
        if (removed) {
            println("FakeDao Deleted: $order")
        } else {
            println("FakeDao Delete Warning: Order with id ${order.id} not found.")
        }
    }

    // **修正:** 匹配接口签名 (id: Int, return: Flow<Order>)
    override fun getOrderByID(id: Int): Flow<Order> {
        // Room 的 Flow 在找不到时通常不会发出 null，而是可能 Flow 不发出值或完成。
        // 模拟这种行为，如果找不到，我们让 Flow 不发出值 (或者根据 Room 实际行为调整)。
        // 注意： .first() 如果找不到会抛异常，更接近某些 Room 配置。
        // find() 返回 null，需要处理。
        val foundOrder = orders.find { it.id == id }

        return if (foundOrder != null) {
            flowOf(foundOrder) // 找到了，发出非空 Order
        } else {
            // 找不到，返回一个空的 Flow，模拟 Room 找不到记录的情况
            // 或者如果你确定 Room 在这种情况下会抛异常，可以模拟抛异常
            flowOf() // 返回一个不发射任何元素的 Flow
            // 或者模拟异常: flow { throw NoSuchElementException("Order with id $id not found") }
        }
    }

    // **修正:** 确保返回当前列表的副本
    override fun getAllOrders(): Flow<List<Order>> {
        return flowOf(orders.toList()) // 返回当前列表状态的副本 Flow
    }
}


// 导入其他需要的 Compose 组件

// !! 主题相关 - 你的项目可能有一个 AppTheme Composable，如果没有，可以先用基础的
//import com.example.manager.ui.theme.OrderManagerTheme // !! 替换成你项目的主题 Composable

class MainActivity : ComponentActivity() {

    // ViewModel Factory 仍然需要，因为 ViewModel 有构造函数参数
    // !! 依赖注入警告：直接在 Activity 创建依赖仍然是为了简化，推荐后续使用 Hilt。
    private val factory by lazy {
        val database = OrderDatabase.getDatabase(applicationContext)
        val repository = OrderRepository(database.orderDao() as FakeOrderDao)
        OrderViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 应用你的 Compose 主题 (通常在 ui/theme/Theme.kt 中定义)
            MaterialTheme { // !! 使用你的主题
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 调用主屏幕 Composable，并传入 Factory
                    OrderScreen(factory = factory)
                }
            }
        }
    }
}

// 主屏幕 Composable (稍后实现)
@Composable
fun OrderScreen(factory: OrderViewModelFactory, modifier: Modifier = Modifier) {

    // 1. 获取 ViewModel 实例
    // 使用 lifecycle-viewmodel-compose 提供的 viewModel() 函数
    val orderViewModel: OrderViewModel = viewModel(factory = factory)

    // 2. 获取订单列表状态
    // 使用 observeAsState 将 LiveData<List<Order>> 转换为 State<List<Order>?>
    // 当 LiveData 更新时，Compose 会自动重组使用此状态的部分
    val ordersState = orderViewModel.allOrders.observeAsState(initial = emptyList()) // 提供初始值

    // 3. 创建输入字段的状态变量
    // 使用 remember 和 mutableStateOf 来持有可变状态，并在重组间保持状态
    var orderNumber by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    // 4. 获取键盘和焦点控制器 (用于隐藏键盘)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 5. UI 布局 (使用 Column)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Add New Order", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        // 输入框
        OutlinedTextField(
            value = orderNumber,
            onValueChange = { orderNumber = it },
            label = { Text("Order Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next) // 输入法下一项
        )
        Spacer(modifier = Modifier.height(8.dp)) // 垂直间距

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal, // 数字键盘
                imeAction = ImeAction.Done // 输入法完成按钮
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = { // 点击完成按钮时触发
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 添加按钮
        Button(
            onClick = {
                // 调用 ViewModel 添加订单
                orderViewModel.insert(orderNumber, customerName, amount)
                // 清空输入框状态
                orderNumber = ""
                customerName = ""
                amount = ""
                // 隐藏键盘并清除焦点
                keyboardController?.hide()
                focusManager.clearFocus()
                // !! 可以考虑从 ViewModel 获取反馈 (如 Toast)
            },
            modifier = Modifier.align(Alignment.End) // 按钮右对齐
        ) {
            Text("Add Order")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider() // 分隔线
        Spacer(modifier = Modifier.height(16.dp))

        Text("Order List", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))

        // 6. 订单列表 (使用 LazyColumn)
        // ordersState.value 是 State<List<Order>?> 解包后的 List<Order>?
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = ordersState.value ?: emptyList(), key = { order -> order.id }) { order ->
                OrderItem(order = order)
                Divider() // 列表项之间的分隔线
            }
        }
    }
}



// --- 单个订单列表项 Composable ---
@Composable
fun OrderItem(order: Order, modifier: Modifier = Modifier) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) } // 优化格式化实例创建

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // 上下内边距
    ) {
        Text(
            text = "Order #${order.orderNumber}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Customer: ${order.customerName}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Amount: ${currencyFormat.format(order.amount)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// --- 预览 ---
@Preview(showBackground = true)
@Composable
fun OrderScreenPreview() {
    MaterialTheme {
        // 创建一个模拟的 Factory 用于预览，避免依赖真实数据库
        val fakeRepository = object : OrderRepository(FakeOrderDao()) {} // 使用假的 DAO
        val fakeFactory = OrderViewModelFactory(fakeRepository)
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            OrderScreen(factory = fakeFactory)
        }
    }
}

