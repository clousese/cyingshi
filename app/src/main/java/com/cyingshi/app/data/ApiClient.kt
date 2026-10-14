package com.cyingshi.app.data

import com.cyingshi.app.model.Category
import com.cyingshi.app.model.Video
import com.cyingshi.app.model.VideoDetail
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** 采集接口 JSON API 访问：分类 / 列表 / 搜索 / 详情 */
object ApiClient {

    private const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()

    data class VideoPage(val list: List<Video>, val pageCount: Int, val total: Int)

    fun get(url: String, retry: Int = 3): String? {
        repeat(retry) { i ->
            try {
                val req = Request.Builder().url(url)
                    .header("User-Agent", UA)
                    .header("Accept", "application/json")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Cache-Control", "no-cache")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrEmpty()) return body
                    }
                }
            } catch (_: Exception) {
            }
            if (i < retry - 1) Thread.sleep(1000)
        }
        return null
    }

    /** 拆出 base 地址和 query 参数（参数做 URL 解码） */
    fun parseApiUrl(url: String): Pair<String, MutableMap<String, String>> {
        val q = url.indexOf('?')
        val base = if (q >= 0) url.substring(0, q) else url
        val params = mutableMapOf<String, String>()
        if (q >= 0) {
            url.substring(q + 1).split('&').forEach { kv ->
                if (kv.isEmpty()) return@forEach
                val e = kv.indexOf('=')
                if (e >= 0) {
                    params[dec(kv.substring(0, e))] = dec(kv.substring(e + 1))
                } else {
                    params[dec(kv)] = ""
                }
            }
        }
        return base to params
    }

    fun buildUrl(base: String, params: Map<String, String>): String {
        if (params.isEmpty()) return base
        return base + "?" + params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
    }

    private fun dec(s: String): String = try {
        URLDecoder.decode(s, "UTF-8")
    } catch (_: Exception) {
        s
    }

    private fun js(o: JSONObject, k: String): String {
        if (!o.has(k) || o.isNull(k)) return ""
        return o.opt(k).toString()
    }

    private fun jint(o: JSONObject, k: String, def: Int): Int {
        return js(o, k).toIntOrNull() ?: def
    }

    private fun jsonToMap(o: JSONObject): Map<String, String> {
        val m = mutableMapOf<String, String>()
        val keys = o.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (!o.isNull(k)) m[k] = o.opt(k).toString()
        }
        return m
    }

    private fun mapToVideo(m: Map<String, String>) = Video(
        vodId = m["vod_id"] ?: "",
        vodName = m["vod_name"]?.ifEmpty { "未知标题" } ?: "未知标题",
        vodPic = m["vod_pic"] ?: "",
        vodYear = m["vod_year"] ?: "",
        vodArea = m["vod_area"] ?: "",
        vodRemarks = m["vod_remarks"] ?: "",
        vodActor = m["vod_actor"] ?: "",
        vodDirector = m["vod_director"] ?: "",
        vodContent = m["vod_content"] ?: ""
    )

    private fun mergeVideo(a: Video, m: Map<String, String>): Video {
        fun pick(old: String, k: String): String {
            val v = m[k]
            return if (!v.isNullOrEmpty()) v else old
        }
        return a.copy(
            vodName = pick(a.vodName, "vod_name"),
            vodPic = pick(a.vodPic, "vod_pic"),
            vodYear = pick(a.vodYear, "vod_year"),
            vodArea = pick(a.vodArea, "vod_area"),
            vodRemarks = pick(a.vodRemarks, "vod_remarks"),
            vodActor = pick(a.vodActor, "vod_actor"),
            vodDirector = pick(a.vodDirector, "vod_director"),
            vodContent = pick(a.vodContent, "vod_content")
        )
    }

    fun fetchCategories(apiUrl: String): List<Category> {
        val body = get(apiUrl) ?: return emptyList()
        val result = mutableListOf<Category>()
        try {
            val json = JSONObject(body)
            if (jint(json, "code", 0) != 1) return emptyList()
            val arr = json.optJSONArray("class") ?: return emptyList()
            data class C(val id: String, val name: String, val pid: Int)
            val all = mutableListOf<C>()
            val parents = mutableMapOf<Int, String>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val name = js(o, "type_name")
                if (name.isEmpty()) continue
                val id = js(o, "type_id")
                val pid = js(o, "type_pid").toIntOrNull() ?: 0
                all.add(C(id, name, pid))
                if (pid == 0) parents[id.toIntOrNull() ?: -1] = name
            }
            for (c in all.filter { it.pid == 0 }) result.add(Category(c.id, c.name))
            for (c in all.filter { it.pid != 0 }) {
                val pn = parents[c.pid] ?: "未分类"
                result.add(Category(c.id, "$pn > ${c.name}"))
            }
        } catch (_: Exception) {
        }
        return result
    }

    fun fetchVideos(apiUrl: String, categoryId: String, page: Int): VideoPage {
        val (base, params) = parseApiUrl(apiUrl)
        params["ac"] = "list"
        params["t"] = categoryId
        params["pg"] = page.toString()
        return fetchVideoList(buildUrl(base, params), apiUrl)
    }

    fun searchVideos(apiUrl: String, keyword: String, page: Int): VideoPage {
        val (base, params) = parseApiUrl(apiUrl)
        params["ac"] = "list"
        params["wd"] = keyword
        params["pg"] = page.toString()
        return fetchVideoList(buildUrl(base, params), apiUrl)
    }

    private fun fetchVideoList(listUrl: String, apiUrl: String): VideoPage {
        val body = get(listUrl) ?: return VideoPage(emptyList(), 1, 0)
        try {
            val json = JSONObject(body)
            if (jint(json, "code", 0) != 1) return VideoPage(emptyList(), 1, 0)
            val arr = json.optJSONArray("list") ?: return VideoPage(emptyList(), 1, 0)
            val ids = mutableListOf<String>()
            val videos = mutableListOf<Video>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val n = PlayUrlParser.normalizeVideoFields(jsonToMap(o))
                val id = n["vod_id"] ?: ""
                if (id.isNotEmpty()) {
                    ids.add(id)
                    videos.add(mapToVideo(n))
                }
            }
            // 批量取详情，把海报/简介等补全
            if (ids.isNotEmpty()) {
                val details = fetchDetailsBatch(apiUrl, ids)
                for (idx in videos.indices) {
                    details[videos[idx].vodId]?.let { videos[idx] = mergeVideo(videos[idx], it) }
                }
            }
            val pc = jint(json, "pagecount", 1).let { if (it < 1) 1 else it }
            return VideoPage(videos, pc, jint(json, "total", 0))
        } catch (_: Exception) {
            return VideoPage(emptyList(), 1, 0)
        }
    }

    private fun fetchDetailsBatch(apiUrl: String, ids: List<String>): Map<String, Map<String, String>> {
        val (base, params) = parseApiUrl(apiUrl)
        params["ac"] = "detail"
        params["ids"] = ids.joinToString(",")
        val body = get(buildUrl(base, params)) ?: return emptyMap()
        val map = mutableMapOf<String, Map<String, String>>()
        try {
            val json = JSONObject(body)
            if (jint(json, "code", 0) != 1) return emptyMap()
            val arr = json.optJSONArray("list") ?: return emptyMap()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val m = PlayUrlParser.normalizeVideoFields(jsonToMap(o))
                val id = m["vod_id"] ?: ""
                if (id.isNotEmpty()) map[id] = m
            }
        } catch (_: Exception) {
        }
        return map
    }

    fun fetchVideoDetail(apiUrl: String, vodId: String): VideoDetail? {
        val m = fetchDetailsBatch(apiUrl, listOf(vodId))[vodId] ?: return null
        return VideoDetail(
            video = mapToVideo(m),
            episodes = PlayUrlParser.parsePlayUrl(m),
            sources = PlayUrlParser.parsePlayUrlGrouped(m)
        )
    }
}
