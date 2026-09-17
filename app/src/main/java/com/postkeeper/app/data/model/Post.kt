package com.postkeeper.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val platform: Platform,
    val mediaType: MediaType,
    val mediaUrl: String,
    val thumbnailUrl: String? = null,
    val title: String? = null,
    val author: String? = null,
    val downloadPath: String? = null,
    val isDownloaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val downloadedAt: Long? = null
)

enum class Platform {
    INSTAGRAM,
    TWITTER,
    UNKNOWN
}

enum class MediaType {
    IMAGE,
    VIDEO,
    UNKNOWN
}
