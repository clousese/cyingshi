package com.cyingshi.app.data

import com.cyingshi.app.model.Episode

/**
 * 播放地址解析：把采集接口返回的 vod_play_url 文本解析为剧集列表。
 * 规则：$$$ 分播放源，# 分集，$ 分"集名$地址"。
 */
object PlayUrlParser {

    private val excludePatterns = listOf(
        Regex("\\.jpg$", RegexOption.IGNORE_CASE),
        Regex("\\.png$", RegexOption.IGNORE_CASE),
        Regex("\\.gif$", RegexOption.IGNORE_CASE),
        Regex("\\.css$", RegexOption.IGNORE_CASE),
        Regex("\\.js$", RegexOption.IGNORE_CASE),
        Regex("\\.html?$", RegexOption.IGNORE_CASE),
        Regex("\\.php$", RegexOption.IGNORE_CASE),
        Regex("\\.asp$", RegexOption.IGNORE_CASE),
        Regex("\\.jsp$", RegexOption.IGNORE_CASE),
        Regex("/share/[a-f0-9]+$", RegexOption.IGNORE_CASE)
    )

    fun isValidM3U8Url(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val u = url.trim()
        if (!u.startsWith("http://", ignoreCase = true) && !u.startsWith("https://", ignoreCase = true)) return false
        if (!u.contains("m3u8", ignoreCase = true)) return false
        if (excludePatterns.any { it.containsMatchIn(u) }) return false
        return true
    }

    /** 不同接口字段名差异大，统一归一化 */
    fun normalizeVideoFields(src: Map<String, String>): Map<String, String> {
        val v = src.toMutableMap()
        fun firstOf(vararg keys: String): String {
            for (k in keys) {
                val x = v[k]
                if (!x.isNullOrEmpty() && x != "0") return x
            }
            return ""
        }
        v["vod_pic"] = firstOf("vod_pic", "pic", "vod_img", "img", "vod_cover", "cover", "vod_pic_url", "pic_url", "vod_thumb", "thumb")
        v["vod_year"] = firstOf("vod_year", "year", "vod_time", "time", "vod_pubdate", "pubdate")
        v["vod_area"] = firstOf("vod_area", "area", "vod_country", "country", "vod_region", "region", "vod_lang", "lang")
        v["vod_name"] = firstOf("vod_name", "title", "name", "vod_title")
        v["vod_id"] = firstOf("vod_id", "id")
        v["vod_remarks"] = firstOf("vod_remarks", "remarks", "vod_note", "note")
        return v
    }

    fun parsePlayUrl(vodInfo: Map<String, String>): List<Episode> {
        var content = ""
        for (f in listOf("vod_play_url", "vod_url", "play_url", "url")) {
            if (!vodInfo[f].isNullOrEmpty()) {
                content = vodInfo[f]!!
                break
            }
        }
        if (content.isEmpty()) return emptyList()

        val episodes = mutableListOf<Episode>()
        for (source in content.split("$$$")) {
            val s = source.trim()
            if (s.isEmpty()) continue
            for (part in s.split("#")) {
                val p = part.trim()
                if (p.isEmpty()) continue
                val idx = p.indexOf('$')
                if (idx >= 0) {
                    var name = p.substring(0, idx).trim()
                    val url = p.substring(idx + 1).trim()
                        .replace("#", "\n")
                        .replace("$", " ")
                        .replace(Regex("\\\\+"), "")
                    if (isValidM3U8Url(url)) {
                        if (name.isEmpty()) name = "第${episodes.size + 1}集"
                        episodes.add(Episode(name, url))
                    }
                } else if (isValidM3U8Url(p)) {
                    episodes.add(Episode("第${episodes.size + 1}集", p))
                }
            }
        }
        // 兜底：正则提取所有 m3u8 链接
        if (episodes.isEmpty()) {
            val re = Regex(
                "https?://[^\\s<>\"'{}|^`\\[\\]]+\\.m3u8[^\\s<>\"'{}|^`\\[\\]]*",
                RegexOption.IGNORE_CASE
            )
            re.findAll(content).forEachIndexed { i, m ->
                val url = m.value
                if (isValidM3U8Url(url)) episodes.add(Episode("第${i + 1}集", url))
            }
        }
        return episodes
    }
}
