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

class PostRepository(private val postDao: PostDao, private val context: Context) {
    
    val allPosts: Flow<List<Post>> = postDao.getAllPosts()
    
    suspend fun getPostById(id: Long): Post? = postDao.getPostById(id)
    
    suspend fun getPostByUrl(url: String): Post? = postDao.getPostByUrl(url)
    
    suspend fun insertPost(post: Post): Long = postDao.insert(post)
    
    suspend fun updatePost(post: Post) = postDao.update(post)
    
    suspend fun deletePost(post: Post) = postDao.delete(post)
    
    suspend fun processSharedUrl(url: String): ProcessResult {
        // Parse the URL to detect platform and media type
        val parsedResult = UrlParser.parseUrl(url)
        
        if (!parsedResult.isValid) {
            return ProcessResult.Error("Unsupported platform or invalid URL")
        }
        
        // Check if post already exists
        val existingPost = postDao.getPostByUrl(url)
        if (existingPost != null) {
            return ProcessResult.Exists(existingPost)
        }
        
        // Extract media info based on platform
        val mediaInfo = when (parsedResult.platform) {
            Platform.INSTAGRAM -> InstagramExtractor.extractMediaInfo(url)
                ?.let { MediaInfo(it.mediaUrl, it.thumbnailUrl, it.mediaType, it.title, it.author) }
            Platform.TWITTER -> TwitterExtractor.extractMediaInfo(url)
                ?.let { MediaInfo(it.mediaUrl, it.thumbnailUrl, it.mediaType, it.title, it.author) }
            Platform.UNKNOWN -> null
        }
        
        if (mediaInfo == null) {
            return ProcessResult.Error("Failed to extract media information. Make sure the post is public.")
        }
        
        // Create post entity
        val post = Post(
            url = url,
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
    
    suspend fun downloadPost(postId: Long): DownloadResult {
        val post = postDao.getPostById(postId) ?: return DownloadResult.Error("Post not found")
        
        if (post.isDownloaded) {
            return DownloadResult.Error("Post already downloaded")
        }
        
        val downloader = MediaDownloader(context)
        val result = downloader.downloadMedia(post.mediaUrl, post.mediaType)
        
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
