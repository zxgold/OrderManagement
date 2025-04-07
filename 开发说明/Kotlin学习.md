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
