package com.cyingshi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cyingshi.app.model.ApiSource
import com.cyingshi.app.model.Video
import com.cyingshi.app.ui.MainViewModel
import com.cyingshi.app.ui.enc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: MainViewModel = viewModel()) {
    val sources by vm.sources.collectAsState()
    val currentSource by vm.currentSource.collectAsState()
    val categories by vm.categories.collectAsState()
    val currentCategory by vm.currentCategory.collectAsState()
    val videos by vm.videos.collectAsState()
    val page by vm.page.collectAsState()
    val pageCount by vm.pageCount.collectAsState()
    val total by vm.total.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val isSearchMode by vm.isSearchMode.collectAsState()
    var keyword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("c影视") },
                actions = {
                    IconButton(onClick = { nav.navigate("sources") }) {
                        Icon(Icons.Default.Settings, contentDescription = "接口管理")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            if (sources.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("暂无视频接口")
                        Text(
                            "请先在接口管理中添加接口地址",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { nav.navigate("sources") }) { Text("去添加接口") }
                    }
                }
            } else {
                SourcePicker(
                    sources = sources,
                    current = currentSource,
                    onSelect = { vm.selectSource(it) }
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    placeholder = { Text("搜索视频…") },
                    singleLine = true,
                    enabled = currentSource != null,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { vm.search(keyword) },
                    enabled = currentSource != null && keyword.isNotBlank()
                ) { Text("搜索") }
            }

            if (isSearchMode) {
                Text(
                    text = "搜索模式，点击返回分类浏览",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                OutlinedButton(
                    onClick = { vm.exitSearch() },
                    modifier = Modifier.padding(top = 4.dp)
                ) { Text("退出搜索") }
            }

            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { c ->
                    FilterChip(
                        selected = currentCategory?.id == c.id,
                        onClick = { vm.selectCategory(c) },
                        label = { Text(c.name) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(4.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(videos, key = { it.vodId }) { v ->
                    VideoCard(v) {
                        val s = currentSource ?: return@VideoCard
                        nav.navigate("detail?apiUrl=${enc(s.url)}&vodId=${enc(v.vodId)}")
                    }
                }
            }

            if (pageCount > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { vm.loadPage(page - 1) },
                        enabled = page > 1 && !loading
                    ) { Text("上一页") }
                    Text(
                        " 第 $page 页 / 共 $pageCount 页${if (total > 0) "（$total）" else ""} ",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    OutlinedButton(
                        onClick = { vm.loadPage(page + 1) },
                        enabled = page < pageCount && !loading
                    ) { Text("下一页") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcePicker(
    sources: List<ApiSource>,
    current: ApiSource?,
    onSelect: (ApiSource) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = current?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("当前接口") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sources.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.name) },
                    onClick = {
                        onSelect(s)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun VideoCard(v: Video, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Column {
            if (v.vodPic.isNotBlank()) {
                AsyncImage(
                    model = v.vodPic,
                    contentDescription = v.vodName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f),
                    contentAlignment = Alignment.Center
                ) { Text("无封面", style = MaterialTheme.typography.bodySmall) }
            }
            Column(Modifier.padding(6.dp)) {
                Text(
                    v.vodName.ifBlank { "未知标题" },
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (v.vodYear.isNotBlank()) {
                        Text(
                            v.vodYear,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (v.vodRemarks.isNotBlank()) {
                        Text(v.vodRemarks, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
