package com.cyingshi.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cyingshi.app.data.ImportParser
import com.cyingshi.app.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManageScreen(nav: NavController, vm: MainViewModel = viewModel()) {
    val sources by vm.sources.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("接口管理") },
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) { Text("返回") }
                },
                actions = {
                    TextButton(onClick = { showImport = true }) { Text("批量导入") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("＋") }
        }
    ) { pad ->
        if (sources.isEmpty()) {
            Box(
                modifier = Modifier.padding(pad).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无接口\n点击右下角 ＋ 添加，或使用右上角批量导入",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(pad).fillMaxSize()) {
                items(sources, key = { it.url }) { s ->
                    ListItem(
                        headlineContent = { Text(s.name) },
                        supportingContent = {
                            Text(
                                s.url,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        trailingContent = {
                            TextButton(onClick = { vm.deleteSource(s.url) }) { Text("删除") }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAdd) {
        AddSourceDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, url ->
                vm.addSource(name, url)
                showAdd = false
            }
        )
    }

    if (showImport) {
        BatchImportDialog(
            onDismiss = { showImport = false },
            onImport = { text ->
                scope.launch {
                    val r = vm.importSources(text)
                    showImport = false
                    snackbar.showSnackbar("成功导入 ${r.imported.size} 个，跳过 ${r.skipped} 个")
                }
            }
        )
    }
}

@Composable
fun AddSourceDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加接口") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("接口地址") },
                    placeholder = { Text("https://…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                err?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val u = url.trim()
                if (name.isBlank()) {
                    err = "名称不能为空"
                    return@TextButton
                }
                if (!u.startsWith("http://", ignoreCase = true) && !u.startsWith("https://", ignoreCase = true)) {
                    err = "接口地址须以 http 开头"
                    return@TextButton
                }
                onConfirm(name.trim(), u)
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/** 批量导入对话框：一键粘贴多行文本或 JSON 数组 */
@Composable
fun BatchImportDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("批量导入接口") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "每行一个，格式：名称|接口地址",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "也支持：名称,接口地址 或 名称 接口地址；\n或粘贴 JSON 数组：[{\"name\":\"…\",\"url\":\"…\"}]",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("在此粘贴接口列表…") },
                    minLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 320.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(text) },
                enabled = text.isNotBlank()
            ) { Text("导入") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { text = ImportParser.SAMPLE_TEMPLATE }) {
                    Text("载入示例模板")
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}
