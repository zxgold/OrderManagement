package com.example.manager.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.manager.ui.customer.CustomerListScreen // <-- Import your screen
import com.example.manager.ui.theme.ManagerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent {
            ManagerTheme { // Apply your theme
                CustomerListScreen() // <-- Display your customer list screen

            }
        }
    }
}