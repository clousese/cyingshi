package com.cyingshi.app.model

/** 用户自配的视频接口 */
data class ApiSource(val name: String, val url: String)

/** 视频分类 */
data class Category(val id: String, val name: String)

/** 视频条目 */
data class Video(
    val vodId: String,
    val vodName: String,
    val vodPic: String,
    val vodYear: String,
    val vodArea: String,
    val vodRemarks: String,
    val vodActor: String,
    val vodDirector: String,
    val vodContent: String
)

/** 单集 */
data class Episode(val name: String, val url: String)

/** 播放源分组（vod_play_from $$$ 对应） */
data class PlaySource(val name: String, val episodes: List<Episode>)

/** 视频详情（含剧集列表与播放源分组） */
data class VideoDetail(
    val video: Video,
    val episodes: List<Episode>,
    val sources: List<PlaySource> = emptyList()
)

/** 收藏 */
data class Favorite(
    val apiUrl: String,
    val vodId: String,
    val vodName: String,
    val vodPic: String,
    val time: Long
)

/** 观看历史 */
data class HistoryEntry(
    val apiUrl: String,
    val vodId: String,
    val vodName: String,
    val vodPic: String,
    val time: Long
)
