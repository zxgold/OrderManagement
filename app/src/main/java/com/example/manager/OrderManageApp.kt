package com.example.manager


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OrderManageApp1 : Application() {
    // 目前这个类可以为空，
    // Hilt会利用@HiltAndroidApp注解自动生成所需的代码，
    // 未来可能在这里进行一些全局初始化操作，但现在不需要

    override fun onCreate() {
        super.onCreate()
        // 例如，这里可以初始化Timer日志库、设置主题等全局操作
        // Timer.plant(Timer.DebugTree()) // 如果使用Timer
    }
}