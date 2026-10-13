package com.cyingshi.app.data

import com.cyingshi.app.model.ApiSource
import org.json.JSONArray

/** 批量导入解析结果 */
data class ImportResult(val imported: List<ApiSource>, val skipped: Int)

/**
 * 批量导入解析器。支持两种格式自动识别：
 * 1) 文本行格式：每行 `名称|URL`，同时兼容 `名称,URL` 与 `名称 URL`（取最后一个 token 为 URL，前面为名称）；
 * 2) JSON 数组格式：[{"name":"xxx","url":"https://..."}, ...]（去空格后以 [ 开头则按 JSON 解析）。
 *
 * 规则：跳过空行；跳过非法行（URL 不是 http 开头）；按 URL 去重（与已有重复的跳过）。
 */
object ImportParser {

    /** 示例模板：占位示例，不含任何真实接口地址 */
    const val SAMPLE_TEMPLATE =
        "示例接口1|https://example.com/api.php/provide/vod/?ac=list\n" +
            "示例接口2|https://example.com/api.php/provide/vod/?ac=list"

    fun parse(text: String, existingUrls: Set<String>): ImportResult {
        val t = text.trim()
        if (t.isEmpty()) return ImportResult(emptyList(), 0)
        return if (t.startsWith("[")) parseJson(t, existingUrls) else parseLines(t, existingUrls)
    }

    private fun parseLines(t: String, existingUrls: Set<String>): ImportResult {
        val imported = mutableListOf<ApiSource>()
        val seen = existingUrls.toMutableSet()
        var skipped = 0
        for (raw in t.lines()) {
            val line = raw.trim()
            if (line.isEmpty()) continue // 空行直接跳过，不计入
            val (name, url) = splitLine(line)
            if (!isHttp(url)) {
                skipped++
                continue
            }
            if (url in seen) {
                skipped++
                continue
            }
            imported.add(ApiSource(if (name.isEmpty()) url else name, url))
            seen.add(url)
        }
        return ImportResult(imported, skipped)
    }

    /** 拆分一行：最后一个 token 为 URL，前面为名称 */
    private fun splitLine(line: String): Pair<String, String> {
        val sep: String
        val parts: List<String> = when {
            '|' in line -> {
                sep = "|"
                line.split('|')
            }
            ',' in line -> {
                sep = ","
                line.split(',')
            }
            else -> {
                sep = " "
                line.split(Regex("\\s+"))
            }
        }
        if (parts.size == 1) return "" to parts[0].trim()
        val url = parts.last().trim()
        val name = parts.dropLast(1).joinToString(sep).trim()
        return name to url
    }

    private fun parseJson(t: String, existingUrls: Set<String>): ImportResult {
        try {
            val arr = JSONArray(t)
            val imported = mutableListOf<ApiSource>()
            val seen = existingUrls.toMutableSet()
            var skipped = 0
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i)
                val url = o?.optString("url")?.trim() ?: ""
                val name = o?.optString("name")?.trim() ?: ""
                if (!isHttp(url)) {
                    skipped++
                    continue
                }
                if (url in seen) {
                    skipped++
                    continue
                }
                imported.add(ApiSource(if (name.isEmpty()) url else name, url))
                seen.add(url)
            }
            return ImportResult(imported, skipped)
        } catch (_: Exception) {
            // JSON 解析失败，回退按行解析
            return parseLines(t, existingUrls)
        }
    }

    private fun isHttp(url: String): Boolean {
        val u = url.trim()
        return u.startsWith("http://", ignoreCase = true) || u.startsWith("https://", ignoreCase = true)
    }
}
