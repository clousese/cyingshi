package com.cyingshi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cyingshi.app.ui.AppNav
import com.cyingshi.app.ui.CYingShiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CYingShiTheme {
                AppNav()
            }
        }
    }
}
