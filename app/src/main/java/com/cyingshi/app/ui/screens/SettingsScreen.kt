package com.cyingshi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cyingshi.app.ui.GreenDeep
import com.cyingshi.app.ui.GreenGradient
import com.cyingshi.app.ui.MainViewModel

/** 设置页：源管理 / 分类屏蔽管理 / 清除历史 / 关于 */
@Composable
fun SettingsScreen(nav: NavController, vm: MainViewModel) {
    var showAbout by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenGradient)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "设置",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        SettingItem(title = "接口管理", sub = "添加 / 删除视频接口") {
            nav.navigate("sources")
        }
        SettingItem(title = "分类屏蔽管理", sub = "屏蔽不看的分类，可随时恢复") {
            nav.navigate("blocks")
        }
        SettingItem(title = "清除观看历史", sub = "删除全部历史记录") {
            confirmClear = true
        }
        SettingItem(title = "关于", sub = "c影视 v4 · OK绿版") {
            showAbout = true
        }
        Spacer(modifier = Modifier.height(92.dp))
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于") },
            text = { Text("c影视 v4\nOK绿版界面\n纯净版：接口由用户自行添加") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("确定") }
            }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清除历史") },
            text = { Text("确定删除全部观看历史吗？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearHistory()
                    confirmClear = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SettingItem(title: String, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GreenDeep)
            Text(sub, fontSize = 12.sp, color = Color(0xFF3D5A4C))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF3D5A4C))
    }
}
