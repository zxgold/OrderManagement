package com.example.manager.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import com.example.manager.ui.customer.CustomerListScreen // <-- Import your screen
import com.example.manager.ui.theme.ManagerTheme
import androidx.compose.runtime.getValue
import com.example.manager.ui.auth.LoginScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManagerTheme {
                AppNavigation() // <-- 调用我们的导航 Composable
            }
        }
    }
}
