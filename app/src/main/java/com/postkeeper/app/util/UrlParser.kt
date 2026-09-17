package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import com.postkeeper.app.data.model.Platform

object UrlParser {
    
    fun parseUrl(url: String): ParsedUrlResult {
        val platform = detectPlatform(url)
        val mediaType = detectMediaType(url, platform)
        
        return ParsedUrlResult(
            url = url,
            platform = platform,
            mediaType = mediaType,
            isValid = platform != Platform.UNKNOWN
        )
    }
    
    fun detectPlatform(url: String): Platform {
        return when {
            url.contains("instagram.com", ignoreCase = true) -> Platform.INSTAGRAM
            url.contains("instagr.am", ignoreCase = true) -> Platform.INSTAGRAM
            url.contains("twitter.com", ignoreCase = true) -> Platform.TWITTER
            url.contains("x.com", ignoreCase = true) -> Platform.TWITTER
            else -> Platform.UNKNOWN
        }
    }
    
    fun detectMediaType(url: String, platform: Platform): MediaType {
        return when {
            url.contains("/video/", ignoreCase = true) || 
            url.contains("/reel/", ignoreCase = true) ||
            url.contains("/stories/", ignoreCase = true) ||
            url.endsWith(".mp4", ignoreCase = true) ||
            url.endsWith(".mov", ignoreCase = true) -> MediaType.VIDEO
            
            url.contains("/p/", ignoreCase = true) ||
            url.endsWith(".jpg", ignoreCase = true) ||
            url.endsWith(".jpeg", ignoreCase = true) ||
            url.endsWith(".png", ignoreCase = true) ||
            url.endsWith(".webp", ignoreCase = true) -> MediaType.IMAGE
            
            else -> MediaType.UNKNOWN
        }
    }
    
    fun extractPostId(url: String, platform: Platform): String? {
        return when (platform) {
            Platform.INSTAGRAM -> {
                val patterns = listOf(
                    Regex("/p/([A-Za-z0-9_-]+)"),
                    Regex("/reel/([A-Za-z0-9_-]+)"),
                    Regex("/reels/([A-Za-z0-9_-]+)"),
                    Regex("/stories/[A-Za-z0-9_.]+/([A-Za-z0-9_-]+)")
                )
                patterns.firstNotNullOfOrNull { pattern ->
                    pattern.find(url)?.groupValues?.get(1)
                }
            }
            Platform.TWITTER -> {
                val patterns = listOf(
                    Regex("/status/([0-9]+)"),
                    Regex("/statuses/([0-9]+)")
                )
                patterns.firstNotNullOfOrNull { pattern ->
                    pattern.find(url)?.groupValues?.get(1)
                }
            }
            Platform.UNKNOWN -> null
        }
    }
}

data class ParsedUrlResult(
    val url: String,
    val platform: Platform,
    val mediaType: MediaType,
    val isValid: Boolean
)
