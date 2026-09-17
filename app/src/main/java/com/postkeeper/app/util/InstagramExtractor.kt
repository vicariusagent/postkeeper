package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * Instagram media extractor using Jsoup for parsing public pages.
 * Note: For production use, consider using official Instagram API or 
 * dedicated libraries like instagram-scraper-api.
 */
object InstagramExtractor {
    
    suspend fun extractMediaInfo(url: String): InstagramMediaInfo? = withContext(Dispatchers.IO) {
        try {
            // Parse the Instagram page
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .followRedirects(true)
                .timeout(30000)
                .get()
            
            // Try to find video URL first
            val videoUrl = doc.select("meta[property=\"og:video:secure_url\"]").attr("content")
                ?: doc.select("meta[property=\"og:video:url\"]").attr("content")
                ?: doc.select("video[src]").attr("src")
            
            // Get image URL as fallback or thumbnail
            val imageUrl = doc.select("meta[property=\"og:image\"]").attr("content")
            
            // Get title/description
            val description = doc.select("meta[property=\"og:description\"]").attr("content")
            
            // Get author
            val author = doc.select("meta[property=\"og:site_name\"]").attr("content")
                ?: "Instagram"
            
            if (videoUrl.isNotEmpty()) {
                return@withContext InstagramMediaInfo(
                    mediaUrl = videoUrl,
                    thumbnailUrl = imageUrl.ifEmpty { null },
                    mediaType = MediaType.VIDEO,
                    title = description.ifEmpty { null },
                    author = author
                )
            } else if (imageUrl.isNotEmpty()) {
                return@withContext InstagramMediaInfo(
                    mediaUrl = imageUrl,
                    thumbnailUrl = null,
                    mediaType = MediaType.IMAGE,
                    title = description.ifEmpty { null },
                    author = author
                )
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class InstagramMediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)
