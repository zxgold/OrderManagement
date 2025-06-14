我们来把这段关于**方法重写 (Method Overriding)** 和**多态 (Polymorphism)** 的内容解释得更清楚明白。

想象一下，你有一个**通用蓝图**（父类 SmartDevice）来制造各种智能设备。这个蓝图规定了所有智能设备都应该有“开机” (turnOn()) 和“关机” (turnOff()) 的功能。

但是，不同类型的智能设备（比如智能灯 SmartLightDevice 和智能电视 SmartTvDevice）在执行“开机”和“关机”时，具体的操作细节是不一样的。

- 开灯可能只是点亮灯泡并设置一个默认亮度。
- 开电视可能需要启动操作系统、显示欢迎画面、设置默认音量和频道。

这就是“**替换子类中的父类方法**”要解决的问题。

**核心概念：**

1. **父类提供通用接口 (但可能行为不够具体)：**

   - SmartDevice 类定义了 turnOn() 和 turnOff() 方法。这些是所有智能设备都应该具备的功能。
   - **open 关键字：** 为了允许子类修改这些方法的具体行为，父类中的这些方法必须用 open 关键字标记。这就像在蓝图上说：“这个部分（比如开机步骤）你们具体的工厂（子类）可以根据自己的产品特点进行修改。”

   > ```kotlin
   > open class SmartDevice(...) {
   >     open fun turnOn() { /* 通用开机逻辑，可能很简单或为空 */ }
   >     open fun turnOff() { /* 通用关机逻辑 */ }
   > }
   > ```

2. **子类提供特定实现 (方法重写)：**

   - SmartLightDevice 和 SmartTvDevice 是 SmartDevice 的子类。它们继承了 turnOn() 和 turnOff() 方法。

   - 但是，它们需要为这些方法提供自己独特的实现。这就是**方法重写 (Method Overriding)**。

   - **override 关键字：** 当子类要重写父类中标记为 open 的方法时，它必须在自己的方法声明前加上 override 关键字。这就像子工厂说：“关于蓝图上的‘开机步骤’，我们有自己的一套更详细、更适合我们产品的操作流程。”

     > ```kotlin
     > class SmartLightDevice(...) : SmartDevice(...) {
     >     override fun turnOn() {
     >         // 这是智能灯“开机”的具体操作
     >         deviceStatus = "on"
     >         brightnessLevel = 2 // 灯特有的行为
     >         println("$name turned on. The brightness level is $brightnessLevel.")
     >     }
     >     override fun turnOff() { /* ... */ }
     > }
     > 
     > class SmartTvDevice(...) : SmartDevice(...) {
     >     override fun turnOn() {
     >         // 这是智能电视“开机”的具体操作
     >         deviceStatus = "on"
     >         println("$name is turned on. Speaker volume is set to $speakerVolume and channel number is set to $channelNumber.") // 电视特有的行为
     >     }
     >     override fun turnOff() { /* ... */ }
     > }
     > ```

   - **“替换意味着要拦截操作，通常是手动控制。替换方法时，子类中的方法会中断父类中定义的方法的执行，并提供其自有的执行内容。”**

     - 这句话的意思是：当你在子类中 override 一个方法后，当你通过子类的对象调用这个方法时，执行的是子类中重写后的版本，而不是父类中的原始版本。子类的实现“接管”了父类的实现。

3. **多态 (Polymorphism) 的魔力：**

   - **“多态性”** 意味着“多种形态”。在面向对象编程中，它允许我们以一种通用的方式来处理不同类型的对象。

   - **代码中的体现：**

     > ```kotlin
     > fun main() {
     >     var smartDevice: SmartDevice // 声明一个变量，类型是父类 SmartDevice
     > 
     >     // 第一次：让 smartDevice 指向一个 SmartTvDevice 对象
     >     smartDevice = SmartTvDevice("Android TV", "Entertainment")
     >     smartDevice.turnOn() // 调用 turnOn()
     > 
     >     // 第二次：让同一个 smartDevice 变量指向一个 SmartLightDevice 对象
     >     smartDevice = SmartLightDevice("Google Light", "Utility")
     >     smartDevice.turnOn() // 再次调用 turnOn()
     > }
     > ```

   - **“代码会对 SmartDevice 类型的变量调用 turnOn() 方法，并能根据变量的实际值来执行 turnOn() 方法的不同实现。”**
     - 这就是多态的核心！你写的是 smartDevice.turnOn()，看起来是调用父类的方法，但实际执行的是具体子类根据自身特点定制的那个方法。这使得代码非常灵活，你可以用同样的方式操作不同类型的对象，而它们会各自表现出正确的行为。

