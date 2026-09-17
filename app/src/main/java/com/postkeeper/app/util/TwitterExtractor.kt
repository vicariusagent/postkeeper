package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * Twitter/X media extractor using Jsoup for parsing public pages.
 * Note: For production use, consider using official Twitter API v2 or
 * dedicated libraries like twitter-scraper.
 */
object TwitterExtractor {
    
    suspend fun extractMediaInfo(url: String): TwitterMediaInfo? = withContext(Dispatchers.IO) {
        try {
            // Parse the Twitter page
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .followRedirects(true)
                .timeout(30000)
                .get()
            
            // Try to find video URL from player iframe or meta tags
            val videoUrl = doc.select("meta[property=\"og:video:secure_url\"]").attr("content")
                ?: doc.select("meta[property=\"og:video:url\"]").attr("content")
                ?: doc.select("iframe[src*=\"/i/videos/\"]").attr("src")
            
            // Get image URL
            val imageUrl = doc.select("meta[property=\"og:image\"]").attr("content")
            
            // Get title/text
            val description = doc.select("meta[property=\"og:description\"]").attr("content")
            
            // Get author name
            val author = doc.select("meta[name=\"twitter:creator\"]").attr("content")
                ?: doc.select("meta[property=\"og:site_name\"]").attr("content")
                ?: "Twitter"
            
            if (videoUrl.isNotEmpty()) {
                return@withContext TwitterMediaInfo(
                    mediaUrl = videoUrl,
                    thumbnailUrl = imageUrl.ifEmpty { null },
                    mediaType = MediaType.VIDEO,
                    title = description.ifEmpty { null },
                    author = author.trimStart('@')
                )
            } else if (imageUrl.isNotEmpty()) {
                return@withContext TwitterMediaInfo(
                    mediaUrl = imageUrl,
                    thumbnailUrl = null,
                    mediaType = MediaType.IMAGE,
                    title = description.ifEmpty { null },
                    author = author.trimStart('@')
                )
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class TwitterMediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)
