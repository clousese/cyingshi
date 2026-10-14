package com.cyingshi.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cyingshi.app.model.ApiSource
import com.cyingshi.app.model.Favorite
import com.cyingshi.app.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "cys_prefs")
private val KEY_SOURCES = stringPreferencesKey("api_sources")
private val KEY_BLOCKED = stringPreferencesKey("blocked_cats")
private val KEY_FAVORITES = stringPreferencesKey("favorites")
private val KEY_HISTORY = stringPreferencesKey("history_videos")

/** 接口列表持久化。纯净版：默认返回空列表，由用户自行添加。 */
class SourceStore(private val context: Context) {

    val sources: Flow<List<ApiSource>> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOURCES]?.let { fromJson(it) } ?: defaultSources()
    }

    suspend fun save(list: List<ApiSource>) {
        context.dataStore.edit { it[KEY_SOURCES] = toJson(list) }
    }

    fun defaultSources(): List<ApiSource> = emptyList()

    /** 被屏蔽的分类：sourceUrl -> (categoryId -> categoryName) */
    val blocked: Flow<Map<String, Map<String, String>>> = context.dataStore.data.map { prefs ->
        prefs[KEY_BLOCKED]?.let { blockedFromJson(it) } ?: emptyMap()
    }

    suspend fun setBlockedCategory(
        sourceUrl: String,
        categoryId: String,
        categoryName: String,
        blocked: Boolean
    ) {
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_BLOCKED]?.let { blockedFromJson(it) }
                ?.mapValues { it.value.toMutableMap() }
                ?.toMutableMap() ?: mutableMapOf()
            if (blocked) {
                cur.getOrPut(sourceUrl) { mutableMapOf() }[categoryId] = categoryName
            } else {
                cur[sourceUrl]?.remove(categoryId)
                if (cur[sourceUrl].isNullOrEmpty()) cur.remove(sourceUrl)
            }
            prefs[KEY_BLOCKED] = blockedToJson(cur)
        }
    }

    companion object {
        fun toJson(list: List<ApiSource>): String {
            val arr = JSONArray()
            for (s in list) arr.put(JSONObject().put("name", s.name).put("url", s.url))
            return arr.toString()
        }

        fun fromJson(str: String): List<ApiSource> {
            val out = mutableListOf<ApiSource>()
            try {
                val arr = JSONArray(str)
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val name = o.optString("name")
                    val url = o.optString("url")
                    if (url.isNotEmpty()) out.add(ApiSource(if (name.isEmpty()) url else name, url))
                }
            } catch (_: Exception) {
            }
            return out
        }

        private fun blockedToJson(map: Map<String, Map<String, String>>): String {
            val o = JSONObject()
            for ((url, cats) in map) {
                val inner = JSONObject()
                for ((id, name) in cats) inner.put(id, name)
                o.put(url, inner)
            }
            return o.toString()
        }

        private fun blockedFromJson(str: String): Map<String, Map<String, String>> {
            val out = mutableMapOf<String, Map<String, String>>()
            try {
                val o = JSONObject(str)
                val keys = o.keys()
                while (keys.hasNext()) {
                    val url = keys.next()
                    val inner = o.optJSONObject(url) ?: continue
                    val cats = mutableMapOf<String, String>()
                    val ik = inner.keys()
                    while (ik.hasNext()) {
                        val id = ik.next()
                        cats[id] = inner.optString(id)
                    }
                    out[url] = cats
                }
            } catch (_: Exception) {
            }
            return out
        }
    }
}

/** 收藏与观看历史（与接口共用同一 DataStore 文件） */
class UserDataStore(private val context: Context) {

    val favorites: Flow<List<Favorite>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES]?.let { favsFromJson(it) } ?: emptyList()
    }

    val history: Flow<List<HistoryEntry>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HISTORY]?.let { historyFromJson(it) } ?: emptyList()
    }

    /** 切换收藏，返回切换后是否为已收藏 */
    suspend fun toggleFavorite(f: Favorite): Boolean {
        var nowFav = false
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_FAVORITES]?.let { favsFromJson(it) }?.toMutableList()
                ?: mutableListOf()
            val idx = cur.indexOfFirst { it.apiUrl == f.apiUrl && it.vodId == f.vodId }
            if (idx >= 0) {
                cur.removeAt(idx)
                nowFav = false
            } else {
                cur.add(0, f)
                nowFav = true
            }
            prefs[KEY_FAVORITES] = favsToJson(cur)
        }
        return nowFav
    }

    suspend fun removeFavorite(apiUrl: String, vodId: String) {
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_FAVORITES]?.let { favsFromJson(it) }?.toMutableList()
                ?: mutableListOf()
            cur.removeAll { it.apiUrl == apiUrl && it.vodId == vodId }
            prefs[KEY_FAVORITES] = favsToJson(cur)
        }
    }

    suspend fun addHistory(e: HistoryEntry) {
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_HISTORY]?.let { historyFromJson(it) }?.toMutableList()
                ?: mutableListOf()
            cur.removeAll { it.apiUrl == e.apiUrl && it.vodId == e.vodId }
            cur.add(0, e)
            prefs[KEY_HISTORY] = historyToJson(cur.take(100))
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(KEY_HISTORY) }
    }

    companion object {
        private fun favsToJson(list: List<Favorite>): String {
            val arr = JSONArray()
            for (f in list) arr.put(
                JSONObject()
                    .put("apiUrl", f.apiUrl)
                    .put("vodId", f.vodId)
                    .put("vodName", f.vodName)
                    .put("vodPic", f.vodPic)
                    .put("time", f.time)
            )
            return arr.toString()
        }

        private fun favsFromJson(str: String): List<Favorite> {
            val out = mutableListOf<Favorite>()
            try {
                val arr = JSONArray(str)
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    out.add(
                        Favorite(
                            apiUrl = o.optString("apiUrl"),
                            vodId = o.optString("vodId"),
                            vodName = o.optString("vodName"),
                            vodPic = o.optString("vodPic"),
                            time = o.optLong("time")
                        )
                    )
                }
            } catch (_: Exception) {
            }
            return out
        }

        private fun historyToJson(list: List<HistoryEntry>): String {
            val arr = JSONArray()
            for (h in list) arr.put(
                JSONObject()
                    .put("apiUrl", h.apiUrl)
                    .put("vodId", h.vodId)
                    .put("vodName", h.vodName)
                    .put("vodPic", h.vodPic)
                    .put("time", h.time)
            )
            return arr.toString()
        }

        private fun historyFromJson(str: String): List<HistoryEntry> {
            val out = mutableListOf<HistoryEntry>()
            try {
                val arr = JSONArray(str)
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    out.add(
                        HistoryEntry(
                            apiUrl = o.optString("apiUrl"),
                            vodId = o.optString("vodId"),
                            vodName = o.optString("vodName"),
                            vodPic = o.optString("vodPic"),
                            time = o.optLong("time")
                        )
                    )
                }
            } catch (_: Exception) {
            }
            return out
        }
    }
}
