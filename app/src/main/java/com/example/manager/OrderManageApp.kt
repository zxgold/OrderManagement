package com.example.manager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp // 导入 Hilt 的核心注解。

/*
 * @HiltAndroidApp注解是Hilt设置的起点，会触发Hilt的代码生成，
 * 包括一个附加到我的应用生命周期的依赖容器，、
 * 这个生成的Hilt组件会附加到Application对象的生命周期，并向其提供依赖项
 * 它也是应用中其他Hilt组件的父组件
 */
// 创建好这个文件后，Hilt就知道了我的应用入口，并准备好生成必要的代码了

@HiltAndroidApp
class OrderManageApp : Application() {
    // 目前这个类可以为空，
    // Hilt会利用@HiltAndroidApp注解自动生成所需的代码，
    // 未来可能在这里进行一些全局初始化操作，但现在不需要

    override fun onCreate() {
        super.onCreate()
        // 例如，这里可以初始化Timer日志库、设置主题等全局操作
        // Timer.plant(Timer.DebugTree()) // 如果使用Timer
    }
}