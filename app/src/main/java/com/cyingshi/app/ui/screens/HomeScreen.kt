package com.cyingshi.app.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cyingshi.app.model.Category
import com.cyingshi.app.model.Video
import com.cyingshi.app.ui.FavRed
import com.cyingshi.app.ui.GreenDeep
import com.cyingshi.app.ui.GreenGradient
import com.cyingshi.app.ui.MainViewModel
import com.cyingshi.app.ui.StatusGreen
import com.cyingshi.app.ui.YearBlue
import com.cyingshi.app.ui.enc

/** OK绿版首页：渐变背景 + 顶栏 + 分类胶囊 + 三列海报墙 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(nav: NavController, vm: MainViewModel) {
    val sources by vm.sources.collectAsState()
    val currentSource by vm.currentSource.collectAsState()
    val categories by vm.categories.collectAsState()
    val currentCategory by vm.currentCategory.collectAsState()
    val videos by vm.videos.collectAsState()
    val page by vm.page.collectAsState()
    val pageCount by vm.pageCount.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val isSearchMode by vm.isSearchMode.collectAsState()
    val blockedMap by vm.blockedMap.collectAsState()
    val favorites by vm.favorites.collectAsState()

    var keyword by remember { mutableStateOf("") }

    // 横屏时海报多列显示，避免大片空白
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var blockTarget by remember { mutableStateOf<Category?>(null) }

    val blockedIds = currentSource?.let { blockedMap[it.url]?.keys } ?: emptySet()
    val visibleCats = remember(categories, blockedIds) {
        categories.filterNot { blockedIds.contains(it.id) }
    }

    // 若当前分类被屏蔽（或不可见），自动切到第一个可见分类
    val curCat = currentCategory
    LaunchedEffect(visibleCats, curCat?.id) {
        if (!isSearchMode && curCat != null && visibleCats.isNotEmpty() &&
            visibleCats.none { it.id == curCat.id }
        ) {
            vm.selectCategory(visibleCats[0])
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenGradient)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                keyword = keyword,
                onKeyword = { keyword = it },
                onSearch = { if (keyword.isNotBlank()) vm.search(keyword) },
                searchEnabled = currentSource != null,
                onSwitchSource = { nav.navigate("sources") },
                onFavorites = { nav.navigate("favorites") },
                onHistory = { nav.navigate("history") }
            )

            if (isSearchMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "搜索中，点击返回分类浏览",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { vm.exitSearch() }) {
                        Text("退出搜索", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (currentSource != null) {
                if (visibleCats.isEmpty()) {
                    Text(
                        text = "分类已全部屏蔽，可在设置 → 分类屏蔽管理中恢复",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visibleCats, key = { it.id }) { c ->
                            CatChip(
                                text = c.name,
                                selected = curCat?.id == c.id,
                                onClick = { vm.selectCategory(c) },
                                onLongClick = { blockTarget = c }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    sources.isEmpty() -> EmptySourceCard { nav.navigate("sources") }
                    loading && videos.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Color.White) }

                    videos.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "暂无数据",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }

                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(if (isLandscape) 6 else 3),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(videos, key = { it.vodId }) { v ->
                            val srcUrl = currentSource?.url
                            VideoCard(
                                v = v,
                                isFav = srcUrl != null && favorites.any {
                                    it.apiUrl == srcUrl && it.vodId == v.vodId
                                },
                                onFav = { srcUrl?.let { vm.toggleFavorite(v, it) } },
                                onClick = {
                                    srcUrl?.let {
                                        nav.navigate("detail?apiUrl=${enc(it)}&vodId=${enc(v.vodId)}")
                                    }
                                }
                            )
                        }
                    }
                }
                if (loading && videos.isNotEmpty()) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(28.dp)
                    )
                }
            }

            if (pageCount > 1 && !isSearchMode && videos.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PageButton("上一页", enabled = page > 1 && !loading) { vm.loadPage(page - 1) }
                    Text(
                        text = " $page / $pageCount ",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    PageButton("下一页", enabled = page < pageCount && !loading) { vm.loadPage(page + 1) }
                }
            }
            Spacer(modifier = Modifier.height(92.dp))
        }

    }

    // 长按分类 → 屏蔽确认
    val bt = blockTarget
    if (bt != null) {
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text("屏蔽分类") },
            text = { Text("以后不再显示「${bt.name}」吗？可在设置 → 分类屏蔽管理中恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    currentSource?.let { vm.blockCategory(it.url, bt) }
                    blockTarget = null
                }) { Text("屏蔽") }
            },
            dismissButton = {
                TextButton(onClick = { blockTarget = null }) { Text("取消") }
            }
        )
    }
}

/** 顶栏：换源立方体 + 胶囊搜索框 + 收藏星 + 历史 */
@Composable
private fun TopBar(
    keyword: String,
    onKeyword: (String) -> Unit,
    onSearch: () -> Unit,
    searchEnabled: Boolean,
    onSwitchSource: () -> Unit,
    onFavorites: () -> Unit,
    onHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSwitchSource, modifier = Modifier.size(42.dp)) {
            CubeIcon(tint = Color.White)
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = keyword,
                onValueChange = onKeyword,
                singleLine = true,
                enabled = searchEnabled,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { inner ->
                    if (keyword.isEmpty()) {
                        Text(
                            "搜索视频...",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 15.sp
                        )
                    }
                    inner()
                },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onSearch,
                enabled = searchEnabled,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "搜索", tint = Color.White)
            }
        }
        IconButton(onClick = onFavorites, modifier = Modifier.size(42.dp)) {
            Icon(Icons.Filled.Star, contentDescription = "收藏", tint = Color.White)
        }
        IconButton(onClick = onHistory, modifier = Modifier.size(42.dp)) {
            Icon(Icons.Filled.History, contentDescription = "历史", tint = Color.White)
        }
    }
}

/** 立方体换源图标 */
@Composable
private fun CubeIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val sw = w * 0.075f
        val hex = Path().apply {
            moveTo(w * 0.50f, h * 0.06f)
            lineTo(w * 0.93f, h * 0.30f)
            lineTo(w * 0.93f, h * 0.72f)
            lineTo(w * 0.50f, h * 0.96f)
            lineTo(w * 0.07f, h * 0.72f)
            lineTo(w * 0.07f, h * 0.30f)
            close()
        }
        drawPath(hex, tint, style = Stroke(sw))
        val cx = w * 0.50f
        val cy = h * 0.51f
        drawLine(tint, Offset(cx, cy), Offset(w * 0.50f, h * 0.06f), strokeWidth = sw)
        drawLine(tint, Offset(cx, cy), Offset(w * 0.07f, h * 0.72f), strokeWidth = sw)
        drawLine(tint, Offset(cx, cy), Offset(w * 0.93f, h * 0.72f), strokeWidth = sw)
    }
}

/** 分类胶囊 chip（支持长按屏蔽） */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) GreenDeep else Color.White.copy(alpha = 0.20f))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

/** 海报卡片：圆角 / 年份角标 / 状态条 / 剧名条 / 收藏心 */
@Composable
private fun VideoCard(
    v: Video,
    isFav: Boolean,
    onFav: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .aspectRatio(3f / 4f)
    ) {
        if (v.vodPic.isNotBlank()) {
            AsyncImage(
                model = v.vodPic,
                contentDescription = v.vodName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("无封面", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
        // 左上年份角标
        val year = v.vodYear.trim().take(4)
        if (year.isNotEmpty() && year[0].isDigit()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(YearBlue)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(year, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        // 底部：状态条 + 剧名条
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            if (v.vodRemarks.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusGreen.copy(alpha = 0.78f))
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = v.vodRemarks,
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            ) {
                Text(
                    text = v.vodName.ifBlank { "未知标题" },
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 26.dp)
                )
            }
        }
        // 右下收藏心
        IconButton(
            onClick = onFav,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
                .size(32.dp)
        ) {
            Icon(
                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "收藏",
                tint = if (isFav) FavRed else Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PageButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.30f else 0.12f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun EmptySourceCard(onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("暂无视频接口", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "请先在接口管理中添加接口地址",
                fontSize = 13.sp,
                color = Color(0xFF3D5A4C),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = GreenDeep)
            ) { Text("去添加接口", color = Color.White) }
        }
    }
}
