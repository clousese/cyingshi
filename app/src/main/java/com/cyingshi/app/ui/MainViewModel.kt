package com.cyingshi.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cyingshi.app.data.ApiClient
import com.cyingshi.app.data.ImportParser
import com.cyingshi.app.data.ImportResult
import com.cyingshi.app.data.SourceStore
import com.cyingshi.app.model.ApiSource
import com.cyingshi.app.model.Category
import com.cyingshi.app.model.Video
import com.cyingshi.app.model.VideoDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SourceStore(app.applicationContext)

    private val _sources = MutableStateFlow<List<ApiSource>>(emptyList())
    val sources: StateFlow<List<ApiSource>> = _sources

    private val _currentSource = MutableStateFlow<ApiSource?>(null)
    val currentSource: StateFlow<ApiSource?> = _currentSource

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _currentCategory = MutableStateFlow<Category?>(null)
    val currentCategory: StateFlow<Category?> = _currentCategory

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos

    private val _detail = MutableStateFlow<VideoDetail?>(null)
    val detail: StateFlow<VideoDetail?> = _detail

    private val _page = MutableStateFlow(1)
    val page: StateFlow<Int> = _page

    private val _pageCount = MutableStateFlow(1)
    val pageCount: StateFlow<Int> = _pageCount

    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode

    private var searchKeyword = ""

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _sources.value = store.sources.first()
            if (_sources.value.isNotEmpty() && _currentSource.value == null) {
                selectSource(_sources.value[0])
            }
        }
    }

    fun selectSource(s: ApiSource) {
        _currentSource.value = s
        _currentCategory.value = null
        _videos.value = emptyList()
        _isSearchMode.value = false
        searchKeyword = ""
        _page.value = 1
        _pageCount.value = 1
        _total.value = 0
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            _error.value = null
            try {
                val cats = ApiClient.fetchCategories(s.url)
                _categories.value = cats
                if (cats.isNotEmpty()) {
                    _currentCategory.value = cats[0]
                    loadVideosInternal(1)
                } else {
                    _error.value = "该接口未返回分类"
                }
            } catch (e: Exception) {
                _error.value = "获取分类失败：${e.message}"
            }
            _loading.value = false
        }
    }

    fun selectCategory(c: Category) {
        _currentCategory.value = c
        _isSearchMode.value = false
        searchKeyword = ""
        loadPage(1)
    }

    fun loadPage(p: Int) {
        if (p < 1 || p > _pageCount.value) return
        if (_currentSource.value == null) return
        if (!_isSearchMode.value && _currentCategory.value == null) return
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            _error.value = null
            loadVideosInternal(p)
            _loading.value = false
        }
    }

    private suspend fun loadVideosInternal(p: Int) {
        val s = _currentSource.value ?: return
        _page.value = p
        try {
            val r = if (_isSearchMode.value) {
                ApiClient.searchVideos(s.url, searchKeyword, p)
            } else {
                ApiClient.fetchVideos(s.url, _currentCategory.value?.id ?: "", p)
            }
            _videos.value = r.list
            _pageCount.value = r.pageCount
            _total.value = r.total
            if (r.list.isEmpty()) _error.value = "暂无数据"
        } catch (e: Exception) {
            _error.value = "加载失败：${e.message}"
        }
    }

    fun search(keyword: String) {
        val k = keyword.trim()
        if (k.isEmpty() || _currentSource.value == null) return
        searchKeyword = k
        _isSearchMode.value = true
        _currentCategory.value = null
        loadPage(1)
    }

    fun exitSearch() {
        _isSearchMode.value = false
        searchKeyword = ""
        _videos.value = emptyList()
        _page.value = 1
        _pageCount.value = 1
        val c = _currentCategory.value
        if (c != null) loadPage(1)
    }

    fun loadDetail(apiUrl: String, vodId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            _error.value = null
            _detail.value = null
            try {
                _detail.value = ApiClient.fetchVideoDetail(apiUrl, vodId)
                if (_detail.value == null) _error.value = "获取详情失败"
            } catch (e: Exception) {
                _error.value = "获取详情失败：${e.message}"
            }
            _loading.value = false
        }
    }

    fun addSource(name: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cur = _sources.value.toMutableList()
            if (cur.none { it.url == url }) {
                cur.add(ApiSource(name, url))
                store.save(cur)
                _sources.value = cur
                if (_currentSource.value == null) {
                    selectSource(cur[0])
                }
            }
        }
    }

    fun deleteSource(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cur = _sources.value.filterNot { it.url == url }
            store.save(cur)
            _sources.value = cur
            if (_currentSource.value?.url == url) {
                _currentSource.value = null
                _categories.value = emptyList()
                _currentCategory.value = null
                _videos.value = emptyList()
            }
        }
    }

    /** 批量导入：返回（成功导入，跳过数） */
    suspend fun importSources(text: String): ImportResult = withContext(Dispatchers.IO) {
        val cur = _sources.value
        val res = ImportParser.parse(text, cur.map { it.url }.toSet())
        if (res.imported.isNotEmpty()) {
            val merged = cur + res.imported
            store.save(merged)
            _sources.value = merged
            if (_currentSource.value == null) {
                selectSource(merged[0])
            }
        }
        res
    }
}

