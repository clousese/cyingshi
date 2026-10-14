package com.cyingshi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cyingshi.app.ui.FavRed
import com.cyingshi.app.ui.GreenGradient
import com.cyingshi.app.ui.MainViewModel
import com.cyingshi.app.ui.enc
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

/** 观看历史 */
@Composable
fun HistoryScreen(nav: NavController, vm: MainViewModel) {
    val history by vm.history.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenGradient)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "观看历史",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = { confirmClear = true }) {
                        Text("清空", color = Color.White)
                    }
                }
            }
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无观看历史", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(history, key = { it.apiUrl + it.vodId }) { h ->
                        MediaRow(
                            pic = h.vodPic,
                            name = h.vodName,
                            sub = timeFmt.format(Date(h.time)),
                            onClick = {
                                nav.navigate("detail?apiUrl=${enc(h.apiUrl)}&vodId=${enc(h.vodId)}")
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(92.dp))
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空历史") },
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

/** 我的收藏 */
@Composable
fun FavoriteScreen(nav: NavController, vm: MainViewModel) {
    val favorites by vm.favorites.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenGradient)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "我的收藏",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { nav.popBackStack() }) {
                    Text("返回", color = Color.White)
                }
            }
            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "还没有收藏\n在海报右下角点爱心即可收藏",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(favorites, key = { it.apiUrl + it.vodId }) { f ->
                        MediaRow(
                            pic = f.vodPic,
                            name = f.vodName,
                            sub = timeFmt.format(Date(f.time)),
                            onClick = {
                                nav.navigate("detail?apiUrl=${enc(f.apiUrl)}&vodId=${enc(f.vodId)}")
                            },
                            trailing = {
                                IconButton(
                                    onClick = { vm.removeFavorite(f.apiUrl, f.vodId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = "取消收藏",
                                        tint = FavRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(92.dp))
        }
    }
}

/** 海报缩略图 + 名称 + 副标题的行 */
@Composable
private fun MediaRow(
    pic: String,
    name: String,
    sub: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pic.isNotBlank()) {
            AsyncImage(
                model = pic,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp, 76.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp, 76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text("无图", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = name.ifBlank { "未知标题" },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        trailing?.invoke()
    }
}
