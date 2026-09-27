package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * X (Twitter) media extractor.
 * Strategy: X.com requires JS for direct HTML scraping, so we use the
 * open-source FxTwitter (fixupx) / Nitter-style JSON endpoints which expose
 * tweet media as plain JSON. Order of attempts:
 *   1. api.fxtwitter.com — returns full tweet JSON incl. media URLs & variants
 *   2. Open Graph tags on x.com directly (fallback)
 */
object TwitterExtractor {

    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    data class TweetMedia(
        val url: String,
        val type: MediaType,
        val thumbnail: String?
    )

    suspend fun extractMediaInfo(url: String): TwitterMediaInfo? = withContext(Dispatchers.IO) {
        extractFromFxTwitter(url) ?: extractFromOpenGraph(url)
    }

    /** Preferred: FxTwitter JSON API (open source: github.com/FixTweet/FxTwitter) */
    private fun extractFromFxTwitter(originalUrl: String): TwitterMediaInfo? {
        return try {
            val matcher = Regex("x\\.com/([^/]+)/status/(\\d+)|twitter\\.com/([^/]+)/status/(\\d+)")
                .find(originalUrl) ?: return null
            val user = matcher.groupValues[1].ifEmpty { matcher.groupValues[3] }
            val id = matcher.groupValues[2].ifEmpty { matcher.groupValues[4] }

            val json = org.json.JSONObject(
                Jsoup.connect("https://api.fxtwitter.com/$user/status/$id")
                    .userAgent(UA)
                    .ignoreContentType(true)
                    .timeout(20_000)
                    .get()
                    .body()
                    .text()
            )

            val tweet = json.optJSONObject("tweet") ?: return null
            val mediaArray = tweet.optJSONObject("media")?.optJSONArray("all")

            // Pick the first video if present, else the first photo
            var chosen: TweetMedia? = null
            if (mediaArray != null) {
                for (i in 0 until mediaArray.length()) {
                    val m = mediaArray.getJSONObject(i)
                    val type = m.optString("type")
                    when {
                        type == "video" || type == "gif" -> {
                            // variants: pick highest bitrate mp4
                            val variants = m.optJSONArray("variants")
                            var best: String? = null
                            var bestBps = -1
                            if (variants != null) {
                                for (j in 0 until variants.length()) {
                                    val v = variants.getJSONObject(j)
                                    if (v.optString("content_type").startsWith("video/mp4")) {
                                        val bps = v.optInt("bitrate", 0)
                                        if (bps > bestBps) { bestBps = bps; best = v.getString("url") }
                                    }
                                }
                            }
                            val videoUrl = best ?: m.optString("url")
                            if (videoUrl.isNotBlank()) {
                                chosen = TweetMedia(videoUrl, MediaType.VIDEO, m.optString("thumbnail_url").ifBlank { null })
                                break
                            }
                        }
                        type == "photo" && chosen == null -> {
                            chosen = TweetMedia(m.getString("url"), MediaType.IMAGE, null)
                        }
                    }
                }
            }

            // Text-only tweets still "save" — use the author avatar as image? No: report no media.
            val media = chosen ?: return null

            TwitterMediaInfo(
                mediaUrl = media.url,
                thumbnailUrl = media.thumbnail ?: tweet.optString("quote")
                    .takeIf { false } // placeholder
                    ?: tweet.optJSONObject("author")?.optString("avatar_url").ifBlankToNull(),
                mediaType = media.type,
                title = tweet.optString("text").ifBlank { null },
                author = tweet.optJSONObject("author")?.optString("screen_name").ifBlankToNull()
            )
        } catch (e: Exception) {
            null
        }
    }

    /** Fallback: plain Open Graph scraping of the x.com page. */
    private fun extractFromOpenGraph(url: String): TwitterMediaInfo? {
        return try {
            val doc = Jsoup.connect(url)
                .userAgent(UA)
                .followRedirects(true)
                .timeout(30_000)
                .get()

            val videoUrl = firstNonBlank(
                doc.select("meta[property=og:video:secure_url]").attr("content"),
                doc.select("meta[property=og:video:url]").attr("content"),
                doc.select("meta[property=og:video]").attr("content")
            )
            val imageUrl = firstNonBlank(
                doc.select("meta[property=og:image]").attr("content"),
                doc.select("meta[name=twitter:image]").attr("content")
            )
            val description = doc.select("meta[property=og:description]").attr("content")
            val author = firstNonBlank(
                doc.select("meta[name=twitter:creator]").attr("content")
            )?.trimStart('@')

            when {
                videoUrl != null -> TwitterMediaInfo(videoUrl, imageUrl, MediaType.VIDEO, description.ifBlank { null }, author)
                imageUrl != null -> TwitterMediaInfo(imageUrl, null, MediaType.IMAGE, description.ifBlank { null }, author)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun String?.ifBlankToNull(): String? = this?.takeIf { it.isNotBlank() && it != "null" }

    private fun firstNonBlank(vararg values: String?): String? =
        values.firstOrNull { !it.isNullOrBlank() && it != "null" }
}

data class TwitterMediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)
