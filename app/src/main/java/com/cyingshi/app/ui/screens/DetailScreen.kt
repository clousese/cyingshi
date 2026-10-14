package com.cyingshi.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cyingshi.app.ui.FavRed
import com.cyingshi.app.ui.GreenBottom
import com.cyingshi.app.ui.GreenDeep
import com.cyingshi.app.ui.GreenGradient
import com.cyingshi.app.ui.MainViewModel
import com.cyingshi.app.ui.enc

/** OK风详情页：海报横幅 + 信息 + 播放源分组 + 选集宫格 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    nav: NavController,
    vm: MainViewModel,
    apiUrl: String,
    vodId: String
) {
    val detail by vm.detail.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val favorites by vm.favorites.collectAsState()

    var srcIndex by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(apiUrl, vodId) {
        srcIndex = 0
        expanded = false
        vm.loadDetail(apiUrl, vodId)
    }

    val d = detail
    val isFav = d?.let { v ->
        favorites.any { it.apiUrl == apiUrl && it.vodId == v.video.vodId }
    } ?: false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenGradient)
            .statusBarsPadding()
    ) {
        if (loading && d == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        error?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        if (d != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 顶部海报横幅
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    if (d.video.vodPic.isNotBlank()) {
                        AsyncImage(
                            model = d.video.vodPic,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, GreenBottom.copy(alpha = 0.92f)),
                                    startY = 120f
                                )
                            )
                    )
                    IconButton(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier
                            .padding(12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                    Text(
                        text = d.video.vodName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val meta = listOf(
                            d.video.vodYear.trim().take(4)
                                .takeIf { it.isNotEmpty() && it[0].isDigit() },
                            d.video.vodArea.trim().takeIf { it.isNotEmpty() },
                            d.video.vodRemarks.trim().takeIf { it.isNotEmpty() }
                        ).filterNotNull()
                        Text(
                            text = if (meta.isEmpty()) "暂无信息" else meta.joinToString(" · "),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { vm.toggleFavorite(d.video, apiUrl) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (isFav) FavRed else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (d.video.vodActor.isNotBlank()) {
                        Text(
                            "主演：${d.video.vodActor}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (d.video.vodContent.isNotBlank()) {
                        Text("剧情简介", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = d.video.vodContent,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(
                                if (expanded) "收起" else "展开",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // 播放源分组
                    val groups = d.sources
                    if (groups.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("播放源", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(groups, key = { i, g -> "$i-${g.name}" }) { i, g ->
                                SrcChip(g.name, i == srcIndex, onClick = { srcIndex = i })
                            }
                        }
                    }

                    val eps = groups.getOrNull(srcIndex)?.episodes ?: d.episodes
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "选集（${eps.size}）",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (eps.isEmpty()) {
                        Text(
                            "暂无可播放剧集",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            eps.forEach { ep ->
                                EpisodeButton(ep.name) {
                                    vm.addHistory(d.video, apiUrl)
                                    nav.navigate(
                                        "player?url=${enc(ep.url)}&title=${enc(d.video.vodName + " - " + ep.name)}"
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SrcChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) GreenDeep else Color.White.copy(alpha = 0.20f))
            .combinedClickable(onClick = onClick, onLongClick = {})
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun EpisodeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
