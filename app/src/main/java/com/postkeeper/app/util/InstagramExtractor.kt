package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

/** Extracts direct media links when Instagram exposes them on a public post page. */
object InstagramExtractor {
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    suspend fun extractMediaInfo(url: String): InstagramMediaInfo? = withContext(Dispatchers.IO) {
        val canonical = canonicalPostUrl(url)
        val candidates = listOf(canonical, canonical.trimEnd('/') + "/embed/captioned/").distinct()
        for (candidate in candidates) {
            val info = runCatching {
                val doc = Jsoup.connect(candidate)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.instagram.com/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .timeout(25_000)
                    .get()
                extractFromDocument(doc, url)
            }.onFailure {
                android.util.Log.w("InstagramExtractor", "Instagram page lookup failed", it)
            }.getOrNull()
            if (info != null) return@withContext info
        }
        null
    }

    private fun canonicalPostUrl(url: String): String {
        val uri = URI(url)
        val shortcodeMatch = Regex("/(p|reel|reels|tv)/([A-Za-z0-9_-]+)").find(uri.path.orEmpty())
        val (kind, shortcode) = shortcodeMatch?.let { it.groupValues[1] to it.groupValues[2] }
            ?: return url
        val normalizedKind = if (kind == "reels") "reel" else kind
        return "https://www.instagram.com/$normalizedKind/$shortcode/"
    }

    private fun extractFromDocument(doc: Document, originalUrl: String): InstagramMediaInfo? {
        val jsonLd = doc.select("script[type='application/ld+json']")
            .asSequence()
            .mapNotNull { script -> runCatching { JSONObject(script.data().ifBlank { script.html() }) }.getOrNull() }
            .firstOrNull()
        val jsonVideo = jsonLd?.let(::videoUrlFromJsonLd)
        val videoUrl = sequenceOf(
            jsonVideo,
            doc.select("meta[property='og:video:secure_url']").attr("content"),
            doc.select("meta[property='og:video:url']").attr("content"),
            doc.select("meta[property='og:video']").attr("content"),
            doc.select("meta[name='twitter:player:stream']").attr("content"),
            doc.select("video[src]").attr("src"),
            doc.select("video source[src]").attr("src")
        ).firstOrNull { isHttpMediaUrl(it) }

        val ogImage = sequenceOf(
            doc.select("meta[property='og:image:secure_url']").attr("content"),
            doc.select("meta[property='og:image']").attr("content"),
            doc.select("meta[name='twitter:image']").attr("content"),
            imageUrlFromJsonLd(jsonLd)
        ).firstOrNull { isHttpMediaUrl(it) }

        val description = sequenceOf(
            doc.select("meta[property='og:description']").attr("content"),
            doc.select("meta[name='description']").attr("content"),
            jsonLd?.optString("caption").orEmpty()
        ).firstOrNull(String::isNotBlank)
        val author = doc.select("meta[name='twitter:creator']").attr("content")
            .ifBlank { doc.select("meta[property='og:title']").attr("content") }
            .ifBlank { "Instagram" }
            .trimStart('@')

        if (videoUrl != null) {
            return InstagramMediaInfo(videoUrl, ogImage, MediaType.VIDEO, description, author)
        }

        val path = runCatching { URI(originalUrl).path.orEmpty().lowercase() }.getOrDefault("")
        val pageType = doc.select("meta[property='og:type']").attr("content").lowercase()
        val hasVideoMarkup = doc.select("video, meta[property^='og:video']").isNotEmpty()
        val isVideoPost = path.contains("/reel/") || path.contains("/reels/") || path.contains("/tv/") ||
            pageType.contains("video") || hasVideoMarkup || jsonLd?.optString("@type") == "VideoObject"

        // Do not mistake an Instagram video preview image for the video itself.
        if (isVideoPost || ogImage == null) return null
        return InstagramMediaInfo(ogImage, ogImage, MediaType.IMAGE, description, author)
    }

    private fun videoUrlFromJsonLd(json: JSONObject): String? {
        val graph = json.optJSONArray("@graph")
        val objects = (0 until (graph?.length() ?: 0)).mapNotNull { graph?.optJSONObject(it) } + json
        return objects.firstNotNullOfOrNull { item ->
            if (item.optString("@type").contains("Video", ignoreCase = true)) {
                sequenceOf(item.optString("contentUrl"), item.optString("videoUrl"))
                    .firstOrNull(::isHttpMediaUrl)
            } else null
        }
    }

    private fun imageUrlFromJsonLd(json: JSONObject?): String? {
        if (json == null) return null
        val image = json.opt("image")
        return when (image) {
            is String -> image
            is JSONObject -> image.optString("url").takeIf(String::isNotBlank)
            is JSONArray -> image.optString(0).takeIf(String::isNotBlank)
            else -> null
        }
    }

    private fun isHttpMediaUrl(value: String?): Boolean =
        !value.isNullOrBlank() && value.startsWith("https://", ignoreCase = true) && !value.startsWith("blob:", ignoreCase = true)
}

data class InstagramMediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)
