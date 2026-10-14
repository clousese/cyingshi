package com.cyingshi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/** 分类屏蔽管理：列出各源已屏蔽的分类，可取消屏蔽 */
@Composable
fun BlockManageScreen(nav: NavController, vm: MainViewModel) {
    val sources by vm.sources.collectAsState()
    val blockedMap by vm.blockedMap.collectAsState()

    val entries = blockedMap.entries.filter { it.value.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenGradient)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Text(
                "分类屏蔽管理",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "暂无屏蔽的分类\n在首页长按分类胶囊即可屏蔽",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                entries.forEach { (url, cats) ->
                    item(key = url) {
                        val sname = sources.find { it.url == url }?.name ?: url
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.92f))
                                .padding(14.dp)
                        ) {
                            Text(
                                sname,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenDeep
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            cats.forEach { (id, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name.ifBlank { id },
                                        fontSize = 14.sp,
                                        color = Color(0xFF1A2E24),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { vm.unblockCategory(url, id) }) {
                                        Text("取消屏蔽", color = GreenDeep)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
