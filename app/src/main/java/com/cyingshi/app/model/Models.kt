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

/** 视频详情（含剧集列表） */
data class VideoDetail(val video: Video, val episodes: List<Episode>)
