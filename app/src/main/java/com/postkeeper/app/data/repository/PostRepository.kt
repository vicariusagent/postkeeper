package com.postkeeper.app.data.repository

import android.content.Context
import com.postkeeper.app.data.dao.PostDao
import com.postkeeper.app.data.model.MediaType
import com.postkeeper.app.data.model.Platform
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.util.DownloadResult
import com.postkeeper.app.util.InstagramExtractor
import com.postkeeper.app.util.MediaDownloader
import com.postkeeper.app.util.TwitterExtractor
import com.postkeeper.app.util.UrlParser
import kotlinx.coroutines.flow.Flow
import java.net.URI

class PostRepository(private val postDao: PostDao, private val context: Context) {
    
    val allPosts: Flow<List<Post>> = postDao.getAllPosts()
    
    suspend fun getPostById(id: Long): Post? = postDao.getPostById(id)
    
    suspend fun getPostByUrl(url: String): Post? = postDao.getPostByUrl(url)
    
    suspend fun insertPost(post: Post): Long = postDao.insert(post)
    
    suspend fun updatePost(post: Post) = postDao.update(post)
    
    suspend fun deletePost(post: Post) = postDao.delete(post)
    
    suspend fun processSharedUrl(url: String): ProcessResult {
        val normalizedUrl = url.trim().let { raw ->
            Regex("https?://[^\\s<>\\\"']+").find(raw)?.value ?: raw
        }.trimEnd('.', ',', ')', ']', '}', '>', '\"', '\'')
        val uri = runCatching { URI(normalizedUrl) }.getOrNull()
        val host = uri?.host?.lowercase()?.removePrefix("www.")
        val validHost = host in setOf("instagram.com", "instagr.am", "x.com", "twitter.com")
        if (uri == null || uri.scheme !in setOf("http", "https") || !validHost) {
            return ProcessResult.Error("Enter a valid Instagram or X post link.")
        }
        if (uri.path.orEmpty().substringAfterLast('/').contains('.')) {
            return processDirectMediaUrl(normalizedUrl)
        }

        val parsedResult = UrlParser.parseUrl(normalizedUrl)
        
        if (!parsedResult.isValid) {
            return ProcessResult.Error("Unsupported platform or invalid URL")
        }
        
        // Check if post already exists
        val existingPost = postDao.getPostByUrl(normalizedUrl)
        if (existingPost != null) {
            return ProcessResult.Exists(existingPost)
        }
        
        // Extract media info based on platform
        val mediaInfo = when (parsedResult.platform) {
            Platform.INSTAGRAM -> InstagramExtractor.extractMediaInfo(normalizedUrl)
                ?.let { MediaInfo(it.mediaUrl, it.thumbnailUrl, it.mediaType, it.title, it.author) }
            Platform.TWITTER -> TwitterExtractor.extractMediaInfo(normalizedUrl)
                ?.let { MediaInfo(it.mediaUrl, it.thumbnailUrl, it.mediaType, it.title, it.author) }
            Platform.UNKNOWN -> null
        }
        
        if (mediaInfo == null) {
            return ProcessResult.Error("Failed to extract media information. Make sure the post is public.")
        }
        
        // Create post entity
        val post = Post(
            url = normalizedUrl,
            platform = parsedResult.platform,
            mediaType = mediaInfo.mediaType,
            mediaUrl = mediaInfo.mediaUrl,
            thumbnailUrl = mediaInfo.thumbnailUrl,
            title = mediaInfo.title,
            author = mediaInfo.author
        )
        
        // Insert into database
        val postId = postDao.insert(post)
        val savedPost = post.copy(id = postId)
        
        return ProcessResult.Success(savedPost)
    }

    private suspend fun processDirectMediaUrl(url: String): ProcessResult {
        val uri = runCatching { URI(url) }.getOrNull()
            ?: return ProcessResult.Error("Enter a valid media link.")
        if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            return ProcessResult.Error("Enter a valid media link.")
        }
        val path = uri.path.orEmpty().lowercase()
        val type = when {
            path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(".webm") -> MediaType.VIDEO
            path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".webp") || path.endsWith(".gif") -> MediaType.IMAGE
            else -> return ProcessResult.Error("This direct link must point to an image or video file.")
        }
        val existing = postDao.getPostByUrl(url)
        if (existing != null) return ProcessResult.Exists(existing)
        val platform = UrlParser.detectPlatform(url)
        val post = Post(url = url, platform = platform, mediaType = type, mediaUrl = url, title = uri.host)
        val id = postDao.insert(post)
        return ProcessResult.Success(post.copy(id = id))
    }
    
    suspend fun downloadPost(postId: Long): DownloadResult {
        val post = postDao.getPostById(postId) ?: return DownloadResult.Error("Post not found")
        
        if (post.isDownloaded) {
            return DownloadResult.Error("Post already downloaded")
        }
        
        val downloader = MediaDownloader(context)
        val result = downloader.downloadMedia(post.mediaUrl, post.mediaType, postId)
        
        if (result is DownloadResult.Success) {
            postDao.markAsDownloaded(postId, result.filePath, System.currentTimeMillis())
        }
        
        return result
    }
}

data class MediaInfo(
    val mediaUrl: String,
    val thumbnailUrl: String?,
    val mediaType: MediaType,
    val title: String?,
    val author: String?
)

sealed class ProcessResult {
    data class Success(val post: Post) : ProcessResult()
    data class Exists(val post: Post) : ProcessResult()
    data class Error(val message: String) : ProcessResult()
}
