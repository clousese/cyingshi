package com.cyingshi.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cyingshi.app.model.ApiSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "cys_prefs")
private val KEY_SOURCES = stringPreferencesKey("api_sources")

/** 接口列表持久化。纯净版：默认返回空列表，由用户自行添加。 */
class SourceStore(private val context: Context) {

    val sources: Flow<List<ApiSource>> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOURCES]?.let { fromJson(it) } ?: defaultSources()
    }

    suspend fun save(list: List<ApiSource>) {
        context.dataStore.edit { it[KEY_SOURCES] = toJson(list) }
    }

    fun defaultSources(): List<ApiSource> = emptyList()

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
    }
}
