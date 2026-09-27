package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * Instagram media extractor using Jsoup to parse public post/reel/story pages.
 * Strategy:
 *  1. Try the page's Open Graph meta tags (works for many public posts).
 *  2. Fall back to embedded JSON in the HTML ("video_url", display_urls) —
 *     Instagram serves this inside <script type="application/json"> blocks.
 * Note: Private accounts and rate-limited requests cannot be scraped; consider
 * an official API or a self-hosted scraper service for production scale.
 */
object InstagramExtractor {

    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    suspend fun extractMediaInfo(url: String): InstagramMediaInfo? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent(UA)
                .header("Accept-Language", "en-US,en;q=0.9")
                .followRedirects(true)
                .timeout(30_000)
                .get()

            val html = doc.html()

            // --- 1. Embedded JSON: highest-quality video URL (reels/videos) ---
            Regex("\"video_url\"\\s*:\\s*\"(https?:\\/\\/[^\"]+)\"")
                .find(html)
                ?.groupValues?.getOrNull(1)
                ?.let { raw ->
                    val clean = raw.replace("\\u0026", "&").replace("\\/", "/")
                    return@withContext InstagramMediaInfo(
                        mediaUrl = clean,
                        thumbnailUrl = findThumbnail(doc),
                        mediaType = MediaType.VIDEO,
                        title = findCaption(doc, html),
                        author = findAuthor(doc, html)
                    )
                }

            // --- 2. Open Graph tags ---
            val ogVideo = firstNonBlank(
                doc.select("meta[property=og:video:secure_url]").attr("content"),
                doc.select("meta[property=og:video:url]").attr("content"),
                doc.select("meta[property=og:video]").attr("content")
            )
            val ogImage = firstNonBlank(
                doc.select("meta[property=og:image]").attr("content"),
                doc.select("meta[name=twitter:image]").attr("content")
            )

            if (ogVideo != null) {
                return@withContext InstagramMediaInfo(
                    mediaUrl = ogVideo,
                    thumbnailUrl = ogImage,
                    mediaType = MediaType.VIDEO,
                    title = doc.select("meta[property=og:description]").attr("content").ifBlank { null },
                    author = findAuthor(doc, html)
                )
            }

            if (ogImage != null) {
                return@withContext InstagramMediaInfo(
                    mediaUrl = ogImage,
                    thumbnailUrl = null,
                    mediaType = MediaType.IMAGE,
                    title = doc.select("meta[property=og:description]").attr("content").ifBlank { null },
                    author = findAuthor(doc, html)
                )
            }

            // --- 3. Story images via graphimage URL pattern ---
            Regex("(https?:\\/\\/[^\"]*graphimage[^\"]*)").find(html)?.groupValues?.getOrNull(1)?.let {
                return@withContext InstagramMediaInfo(
                    mediaUrl = it.replace("\\u0026", "&").replace("\\/", "/"),
                    thumbnailUrl = null,
                    mediaType = MediaType.IMAGE,
                    title = null,
                    author = findAuthor(doc, html)
                )
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun findThumbnail(doc: org.jsoup.nodes.Document): String? = firstNonBlank(
        doc.select("meta[property=og:image]").attr("content"),
        doc.select("meta[name=twitter:image]").attr("content")
    )

    private fun findCaption(doc: org.jsoup.nodes.Document, html: String): String? = firstNonBlank(
        doc.select("meta[property=og:description]").attr("content"),
        Regex("\"edge_media_to_caption\"\\s*:\\s*\\{\\s*\"text\"\\s*:\\s*\"([^\"]{1,300})\"").find(html)
            ?.groupValues?.getOrNull(1)
    )

    private fun findAuthor(doc: org.jsoup.nodes.Document, html: String): String? = firstNonBlank(
        Regex("\"username\"\\s*:\\s*\"([A-Za-z0-9._]{1,40})\"").find(html)?.groupValues?.getOrNull(1),
        doc.title().substringAfter("by ").takeIf { doc.title().contains("by ") }
    ) ?: "Instagram"

    private fun firstNonBlank(vararg values: String?): String? =
        values.firstOrNull { !it.isNullOrBlank() && it != "null" }
}

data class InstagramMediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)
