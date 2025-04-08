# Kotlin学习

## 二

将学习如何编写使用变量的代码，以便让程序的某些部分可以更改，而不必编写一套全新的指令

![image-20250329213619558](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250329213619558.png)

在变量名称后，需依次添加冒号、空格和变量的数据类型。如前所述，`String`、`Int`、`Double`、`Float,` 和 `Boolean` 是一些基本的 Kotlin 数据类型。本课程后面的内容会对这些数据类型进行详细介绍。请务必准确拼写数据类型，并以大写字母开头。

这些值称为“字面量”，因为它们是固定值或常量值（也就是保持不变的值）.

在 `count` 变量前面添加美元符号 `$`（即 `"You have $count unread messages."`）来修正您的程序。这是一个字符串模板，因为它包含模板表达式，在本例中为 `$count`。模板表达式是一种会将求得的值替换到字符串中的表达式。

如果您使用更复杂的表达式，就必须用大括号将该表达式括起来，并在大括号前添加 `$` 符号：`${unreadCount + readCount}`。用大括号括起来的表达式（即 `unreadCount + readCount`）的求值结果为 `105`。然后，`105` 这个值会替换到字符串字面量中。

利用类型推断，当 Kotlin 编译器可以推断（或确定）变量应属的数据类型时，您不必在代码中写入确切类型。这意味着，如果您为变量提供了初始值，就可以在变量声明中省略数据类型。Kotlin 编译器会查看初始值的数据类型，并假定变量会存储该类型的数据。

- `val` 关键字 - 预计变量值不会变化时使用。
- `var` 关键字 - 预计变量值会发生变化时使用。

在 Kotlin 中，建议尽量使用 `val` 关键字，而不是 `var` 关键字。

您可以使用 + 号将两个字符串加在一起（这种做法称为“串联”）。

```kotlin
fun main() {
    val nextMeeting = "Next meeting is:"
    val date = "January 1"
    val reminder = nextMeeting + date
    println(reminder)
}
```

本节编码规范：

- 量名称应采用驼峰式大小写形式，并以小写字母开头。
- 在变量声明中指定数据类型时，应在冒号后面添加一个空格。
- 赋值运算符 (`=`)、加号 (`+`)、减号 (`-`)、乘号 (`*`)、除号 (`/`) 等运算符的前后应有空格。
- 如果是编写更为复杂的程序，[建议每行不要超过 100 个字符](https://developer.android.com/kotlin/style-guide?hl=zh-cn#line_wrapping)。这样一来，您无需水平滚动计算机屏幕，便可轻松阅读程序中的所有代码。

如果您想用一行超过 100 个字符的长注释来详细说明代码，不妨使用多行注释。具体方法为：使用由正斜杠 (`/`) 和星号 (`*`) 组成的 `/*` 来作为多行注释的开头，在注释的每个新行开头添加一个星号，最后使用由星号和正斜杠符号组成的 `*/` 作为结尾。

```kotlin
/*
 * This is a very long comment that can
 * take up multiple lines.
 */
```

## 三、创建和使用函数

![image-20250329215630487](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250329215630487.png)

声明具有返回值类型的函数的语法

![image-20250331125921134](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250331125921134.png)

默认情况下，如果不指定返回值类型，默认返回值类型是Unit，相当于其他语言的woid类型。

对于不返回任何内容或返回Unit的函数，不需要使用return语句。

![image-20250331130638580](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250331130638580.png)

每个形参均由变量名称和数据类型组成，以冒号和空格分隔。多个形参以英文逗号分隔。

与某些语言（例如在 Java 中，函数可以更改传递到形参中的值）不同，Kotlin 中的形参是不可变的。您不能在函数主体中重新分配形参的值。

函数签名：函数名称及其输入（形参）统称为“函数签名”。函数签名包含返回值类型前面的所有内容，如以下代码段所示。

```kotlin
fun birthdayGreeting(name: String, age: Int)
```

具名实参：使您更改了实参的顺序，系统也会为相同的形参传入相同的值。

![image-20250331132033752](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250331132033752.png)

如需尝试 Kotlin 语言的更多练习，请查看 JetBrains Academy 的 [Kotlin 基础知识课程](https://hyperskill.org/tracks/18)。如需跳转到特定主题，请前往[知识图谱](https://hyperskill.org/knowledge-map)，查看上述课程涵盖的主题列表。





# 第一单元 您的首个Android应用

## 1.1 Kotlin简介

## 1.2 设置Studio

## 1.3 构建基本布局

可组合函数是界面的基本构建块，在compose中，可组合函数：描述界面中的某一部分；不会返回任何内容；接受一些输入并生成屏幕上显示的内容。

注解是用于在代码中附加额外信息的方式。此类信息可以帮助Jetpack Compose编译器等工具和其他开发者理解应用的代码。

可组合函数带有 [`@Composable`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable?hl=zh-cn) 注解，所有可组合函数都必须带有此注解，此注解可告知compose编译器：此函数用于将数据转换为界面。

Pascal命名法：复合词中的每个单词的首字母大写，与驼峰命名法之间的区别在于：在Pascal命名法中，所有单词的首字母都大写，但在驼峰命名法中，首字母可以是大写或小写。

尾随lambda语法

更改字体大小：可缩放像素sp是字体带下的度量单位，sp的大小会根据用户在手机设置下的首选文本大小进行调整，需要导入androidx.compose.ui.unit.sp，以使用.sp扩展属性

一个可组合函数里面可以有多个Text()函数：一个可组合函数可能会描述多个界面元素，不过，如果没有提供如何排列这些元素的指导，Compose可能会以不适合的方式排列它们

```kotlin
@Composable
fun GreetingText(message: String, from: String, modifier: Modifier = Modifier) {
    Text(
        // ...
    )
    Text(
        text = from
    )
}
```

Compose中的三个标准布局元素是Column、Row、Box可组合项：

Row函数通过导入androidx.compose.foundation.layout.Row来使用。Column函数通过导入androidx.compose.foundation.layout.Column来使用

![image-20250409212635412](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250409212635412.png)

举例：

```kotlin
// Don't copy.
Row {
    Text("First Column")
    Text("Second Column")
}
```

尾随lambda语法：在上述代码段中，Row可组合函数使用的是花括号而不是圆括号，这称为尾随lambda语法。当最后一个形参是函数时，kotlin提供了这种特殊语法来将函数作为形参传递给函数。

比较下面两个代码段可以更好地体会到这个语法：

1. 非尾随lambda语法：

   ```kotlin
   Row(
       content = {
           Text("Some text")
           Text("Some more text")
           Text("Last text")
       }
   )
   ```

2. 尾随lambda语法

   ```kotlin
   Row {
       Text("Some text")
       Text("Some more text")
       Text("Last text")
   }
   ```

对预览效果感到满意后，就可以在设备或模拟器上向应用添加你家可组合项：

1. 在 `MainActivity.kt` 文件中，滚动到 `onCreate()` 函数。
2. 从 `Surface` 代码块调用 `GreetingText()` 函数。
3. 传递 `GreetingText()` 函数、您的生日祝福和签名。

完成后的 `onCreate()` 函数应该会如同下面的代码段所示：

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HappyBirthdayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingText(message = "Happy Birthday Sam!", from = "From Emma")
                }
            }
        }
    }
}
```

将问候语居中：

1. 需在屏幕中心对齐问候语，请添加一个名为 `verticalArrangement` 的形参，添加到Colunm函数的参数里面，并将其设置为 `Arrangement.Center`

   ```
   @Composable
   fun GreetingText(message: String, from: String, modifier: Modifier = Modifier) {
       Column(
           verticalArrangement = Arrangement.Center,
           modifier = modifier
       ) {
           // ...
       }
   }
   ```

2. 围绕列添加 `8.dp` 内边距。最好以 `4.dp` 为增量使用内边距值

   ```kotlin
   @Composable
   fun GreetingText(message: String, from: String, modifier: Modifier = Modifier) {
       Column(
           verticalArrangement = Arrangement.Center,
           modifier = modifier.padding(8.dp)
       ) {
           // ...
       }
   }
   ```

3. 给Text函数添加参数textAlign将文本居中对齐

   ```kotlin
   Text(
       text = message,
       fontSize = 100.sp,
       lineHeight = 116.sp,
       textAlign = TextAlign.Center
   )
   ```

4.  给Text函数添加参数modifier使文本添加内边距并使其右对齐

   ```kotlin
   Text(
       text = from,
       fontSize = 36.sp,
       modifier = Modifier
           .padding(16.dp)
           .align(alignment = Alignment.End)
   )
   ```

# 第二单元 构建应用界面

## 2.1 Kotlin基础知识

——详细了解Kotlin、面向对象的编程和lambda的基础知识

### 2.1.2 在Kotlin中编写条件

1. if

   <img src="C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416110516679.png" alt="image-20250416110516679" style="zoom:25%;" />

   <img src="C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416110950142.png" alt="image-20250416110950142" style="zoom: 33%;" />

2. when

   对分支数量更多的程序进行简化

   <img src="C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416111526020.png" alt="image-20250416111526020" style="zoom:50%;" />

   如果已经执行正文，程序会忽略后续的分支，并推出when语句

   ```kotlin
   fun main() {
       val trafficLightColor = "Black"
       
       when (trafficLightColor) {
           "Red" -> println("Stop")
           "Green" -> println("Go")
           "Yellow" -> println("Slow down")
           else -> println("Invalid traffic-light color")
       }
   }
   ```

   注意：when语句有一个变体，不接受任何形参，并且用于替换if/else链

   ```kotlin
   fun main() {
       val a = 5
       val b = 6
       
       when {
       	a > b -> println("a is greater than b")
       	a < b -> println("a is less than b")
       	else -> println("a is equal to b")
       }   
   }
   ```

3. 使用英文逗号处理多个条件

   ![image-20250416125002944](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416125002944.png)

4. 使用in关键字处理一系列条件

   ![image-20250416125110522](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416125110522.png)

   ```kotlin
   fun main() {
       val x = 3
   
       when (x) {
           2, 3, 5, 7 -> println("x is a prime number between 1 and 10.")
           in 1..10 -> println("x is a number between 1 and 10, but not a prime number.")
           else -> println("x isn't a prime number between 1 and 10.")
       }
   }
   ```

5. 使用is关键字作为条件，检查所评估值的数据类型

   ![image-20250416125239245](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416125239245.png)

6. 使用if/else 和 when 作为表达式

   ![image-20250416130323309](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416130323309.png)

   ![image-20250416130428477](C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416130428477.png)

   ```kotlin
   fun main() {
       val trafficLightColor = "Black"
   
       val message = 
         if (trafficLightColor == "Red") "Stop"
         else if (trafficLightColor == "Yellow") "Slow"
         else if (trafficLightColor == "Green") "Go"
         else "Invalid traffic-light color"
   }
   
   ```

   ```kotlin
   fun main() {
       val trafficLightColor = "Amber"
   
       val message = when(trafficLightColor) {
           "Red" -> "Stop"
           "Yellow", "Amber" -> "Slow"
           "Green" -> "Go"
           else -> "Invalid traffic-light color"
       }
       println(message)
   }
   ```

### 2.1.3 在Kotlin中使用可为null性

“可为null性”是许多编程语言中的常见概念，代表对变量不设置任何值，在kotlin中，系统会刻意处理可为null性，以实现null安全。

<img src="C:\Users\21117\AppData\Roaming\Typora\typora-user-images\image-20250416131008992.png" alt="image-20250416131008992" style="zoom: 80%;" />











# 埋坑

## 1. `Modifier` 和子元素

1.3中留下的坑

## 2. 字体大小

1.3.6 可缩放像素和密度无关像素

## 3. text函数的参数

text、fontSize、lineHeight

## 4. 居中对齐

了解Column的参数

## 5. 了解MainActivity的格式和组合









