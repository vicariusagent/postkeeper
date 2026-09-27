package com.postkeeper.app.util

import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import kotlin.math.PI
import kotlin.math.floor

/** Extracts media URLs from public X/Twitter posts. */
object TwitterExtractor {
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    suspend fun extractMediaInfo(url: String): TwitterMediaInfo? = withContext(Dispatchers.IO) {
        val tweetId = UrlParser.extractPostId(url, UrlParser.detectPlatform(url))
        if (tweetId != null) {
            extractFromSyndication(tweetId)?.let { return@withContext it }
        }
        extractFromPage(url)
    }

    private fun extractFromSyndication(tweetId: String): TwitterMediaInfo? = runCatching {
        val token = syndicationToken(tweetId)
        val response = Jsoup.connect("https://cdn.syndication.twimg.com/tweet-result?id=$tweetId&lang=en&token=$token")
            .ignoreContentType(true)
            .userAgent("Googlebot")
            .referrer("https://x.com/")
            .timeout(20_000)
            .get()
        val tweet = JSONObject(response.text())
        val mediaItems = sequenceOf(tweet, tweet.optJSONObject("quoted_tweet"), tweet.optJSONObject("retweeted_status"))
            .filterNotNull()
            .flatMap { item ->
                val details = item.optJSONArray("mediaDetails")
                (0 until (details?.length() ?: 0)).mapNotNull { details?.optJSONObject(it) }.asSequence()
            }.toList()
        var imageUrl: String? = null
        var thumbnailUrl: String? = null

        if (mediaItems.isNotEmpty()) {
            for (item in mediaItems) {
                val type = item.optString("type")
                val preview = item.optString("media_url_https").takeIf(String::isNotBlank)
                if (type == "video" || type == "animated_gif") {
                    val variants = item.optJSONObject("video_info")?.optJSONArray("variants")
                    val mp4 = (0 until (variants?.length() ?: 0))
                        .mapNotNull { variants?.optJSONObject(it) }
                        .filter { it.optString("content_type") == "video/mp4" }
                        .maxByOrNull { it.optInt("bitrate", 0) }
                        ?.optString("url")
                        ?.takeIf(String::isNotBlank)
                        ?: continue
                    return@runCatching TwitterMediaInfo(
                        mediaUrl = mp4,
                        thumbnailUrl = preview,
                        mediaType = MediaType.VIDEO,
                        title = tweet.optString("text").takeIf(String::isNotBlank),
                        author = tweet.optJSONObject("user")?.optString("screen_name")?.takeIf(String::isNotBlank)
                    )
                }
                if (type == "photo" && imageUrl == null) imageUrl = preview
            }
        }

        val photos = tweet.optJSONArray("photos")
        if (imageUrl == null && photos != null && photos.length() > 0) {
            imageUrl = photos.optJSONObject(0)?.optString("url")?.takeIf(String::isNotBlank)
        }
        imageUrl?.let {
            TwitterMediaInfo(
                mediaUrl = it,
                thumbnailUrl = it,
                mediaType = MediaType.IMAGE,
                title = tweet.optString("text").takeIf(String::isNotBlank),
                author = tweet.optJSONObject("user")?.optString("screen_name")?.takeIf(String::isNotBlank)
            )
        }
    }.onFailure { android.util.Log.w("TwitterExtractor", "X syndication lookup failed", it) }.getOrNull()

    /** X computes this public endpoint token from the numeric tweet ID. */
    private fun syndicationToken(tweetId: String): String {
        val number = (tweetId.toDouble() / 1e15) * PI
        var fraction = number - floor(number)
        var integer = floor(number).toLong()
        val alphabet = "0123456789abcdefghijklmnopqrstuvwxyz"
        val whole = StringBuilder()
        do {
            whole.append(alphabet[(integer % 36).toInt()])
            integer /= 36
        } while (integer > 0)
        whole.reverse()

        // Emit the shortest base-36 representation that distinguishes this
        // floating-point value, matching JavaScript Number.toString(36).
        var delta = (Math.ulp(number) / 2.0).coerceAtLeast(Double.MIN_VALUE)
        val digits = StringBuilder()
        while (fraction >= delta && digits.length < 32) {
            delta *= 36.0
            val scaled = fraction * 36.0
            val digit = floor(scaled).toInt().coerceIn(0, 35)
            digits.append(alphabet[digit])
            fraction = scaled - digit
            val needsRounding = fraction > 0.5 || (fraction == 0.5 && digit % 2 == 1)
            if (needsRounding && fraction + delta > 1.0) {
                var carryIndex = digits.lastIndex
                while (carryIndex >= 0 && digits[carryIndex] == 'z') {
                    digits.deleteCharAt(carryIndex)
                    carryIndex--
                }
                if (carryIndex >= 0) {
                    digits.setCharAt(carryIndex, alphabet[alphabet.indexOf(digits[carryIndex]) + 1])
                } else {
                    integer = whole.toString().toLong(36) + 1
                    whole.clear()
                    var carried = integer
                    do {
                        whole.append(alphabet[(carried % 36).toInt()])
                        carried /= 36
                    } while (carried > 0)
                    whole.reverse()
                }
                break
            }
        }
        val raw = if (digits.isEmpty()) whole.toString() else "$whole.$digits"
        return raw.filterNot { it == '0' || it == '.' }
    }

    private fun extractFromPage(url: String): TwitterMediaInfo? = runCatching {
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .referrer("https://x.com/")
            .followRedirects(true)
            .timeout(25_000)
            .get()

        val videoUrl = sequenceOf(
            doc.select("meta[property='og:video:secure_url']").attr("content"),
            doc.select("meta[property='og:video:url']").attr("content"),
            doc.select("meta[property='og:video']").attr("content"),
            doc.select("meta[name='twitter:player:stream']").attr("content")
        ).firstOrNull { it.isNotBlank() && it.startsWith("https://") }
        val imageUrl = doc.select("meta[property='og:image']").attr("content")
        val description = doc.select("meta[property='og:description']").attr("content").takeIf(String::isNotBlank)
        val author = doc.select("meta[name='twitter:creator']").attr("content")
            .ifBlank { doc.select("meta[property='og:site_name']").attr("content") }
            .ifBlank { "X" }
            .trimStart('@')

        when {
            videoUrl != null -> TwitterMediaInfo(videoUrl, imageUrl.takeIf(String::isNotBlank), MediaType.VIDEO, description, author)
            imageUrl.isNotBlank() && !imageUrl.contains("ext_tw_video_thumb", ignoreCase = true) ->
                TwitterMediaInfo(imageUrl, imageUrl, MediaType.IMAGE, description, author)
            else -> null
        }
    }.onFailure { android.util.Log.w("TwitterExtractor", "Could not read public X media", it) }.getOrNull()
}

data class TwitterMediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)
