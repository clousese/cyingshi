package com.cyingshi.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cyingshi.app.ui.MainViewModel
import com.cyingshi.app.ui.enc

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    nav: NavController,
    apiUrl: String,
    vodId: String,
    vm: MainViewModel = viewModel()
) {
    val detail by vm.detail.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(apiUrl, vodId) {
        vm.loadDetail(apiUrl, vodId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.video?.vodName ?: "详情") },
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) { Text("返回") }
                }
            )
        }
    ) { pad ->
        val d = detail
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            if (loading && d == null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            if (d != null) {
                Row {
                    if (d.video.vodPic.isNotBlank()) {
                        AsyncImage(
                            model = d.video.vodPic,
                            contentDescription = d.video.vodName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.width(120.dp).height(160.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(d.video.vodName, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        if (d.video.vodYear.isNotBlank()) Text("年份：${d.video.vodYear}", style = MaterialTheme.typography.bodySmall)
                        if (d.video.vodArea.isNotBlank()) Text("地区：${d.video.vodArea}", style = MaterialTheme.typography.bodySmall)
                        if (d.video.vodActor.isNotBlank()) Text("演员：${d.video.vodActor}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        if (d.video.vodDirector.isNotBlank()) Text("导演：${d.video.vodDirector}", style = MaterialTheme.typography.bodySmall)
                        if (d.video.vodRemarks.isNotBlank()) Text("备注：${d.video.vodRemarks}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (d.video.vodContent.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("简介", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(d.video.vodContent, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "选集（${d.episodes.size}）",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                if (d.episodes.isEmpty()) {
                    Text("暂无可播放剧集", style = MaterialTheme.typography.bodySmall)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        d.episodes.forEach { ep ->
                            OutlinedButton(
                                onClick = {
                                    nav.navigate(
                                        "player?url=${enc(ep.url)}&title=${enc(d.video.vodName + " - " + ep.name)}"
                                    )
                                }
                            ) { Text(ep.name) }
                        }
                    }
                }
            }
        }
    }
}
