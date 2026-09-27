package com.postkeeper.app.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class MediaDownloader(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun downloadMedia(mediaUrl: String, mediaType: MediaType, postId: Long): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder()
                .url(mediaUrl)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext DownloadResult.Error("The media server returned ${response.code}.")
                }
                val body = response.body ?: return@withContext DownloadResult.Error("The media server returned an empty file.")
                if (body.contentLength() == 0L) return@withContext DownloadResult.Error("The media file is empty.")
                val contentType = response.header("Content-Type")?.substringBefore(';')?.trim()?.lowercase().orEmpty()
                if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
                    return@withContext DownloadResult.Error("The link did not return an image or video file.")
                }
                val actualType = if (contentType.startsWith("video/")) MediaType.VIDEO else MediaType.IMAGE
                val (extension, mimeType) = getFileExtensionAndMimeType(actualType, contentType)
                val fileName = "postkeeper_${postId}_${System.currentTimeMillis()}.$extension"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveToMediaStoreApi29Plus(fileName, mimeType, body.byteStream())
                } else {
                    saveToLegacyStorage(fileName, mimeType, body.byteStream())
                }
            }
        } catch (e: Exception) {
            DownloadResult.Error("Download failed: ${e.message}")
        }
    }
    
    private fun getFileExtensionAndMimeType(mediaType: MediaType, contentType: String): Pair<String, String> {
        return when (mediaType) {
            MediaType.VIDEO -> Pair("mp4", "video/mp4")
            MediaType.IMAGE -> {
                when {
                    contentType.contains("png") -> Pair("png", "image/png")
                    contentType.contains("webp") -> Pair("webp", "image/webp")
                    contentType.contains("gif") -> Pair("gif", "image/gif")
                    contentType.contains("jpeg") || contentType.contains("jpg") -> Pair("jpg", "image/jpeg")
                    else -> Pair("jpg", "image/jpeg")
                }
            }
            MediaType.UNKNOWN -> Pair("bin", "application/octet-stream")
        }
    }
    
    /**
     * Save file using MediaStore API (Android 10+/API 29+)
     * This is the recommended approach for scoped storage
     */
    private fun saveToMediaStoreApi29Plus(fileName: String, mimeType: String, inputStream: java.io.InputStream): DownloadResult {
        return try {
            val collection = if (mimeType.startsWith("image/")) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if (mimeType.startsWith("video/")) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, getRelativePath(mimeType))
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            
            val resolver = context.contentResolver
            val uri = resolver.insert(collection, contentValues)
                ?: return DownloadResult.Error("Failed to create MediaStore entry")
            
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    inputStream.use { it.copyTo(outputStream) }
                } ?: throw IllegalStateException("Could not open output file")
                val completed = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                resolver.update(uri, completed, null, null)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            
            // Return the URI string as the saved path
            DownloadResult.Success(uri.toString())
        } catch (e: Exception) {
            DownloadResult.Error("MediaStore save failed: ${e.message}")
        }
    }
    
    /**
     * Save file using legacy storage (Android 9 and below)
     */
    private fun saveToLegacyStorage(fileName: String, mimeType: String, inputStream: java.io.InputStream): DownloadResult {
        return try {
            val resolver = context.contentResolver
            val collection = if (mimeType.startsWith("image/")) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.DATA, File(getLegacyDownloadDirectory(), fileName).absolutePath)
            }
            val uri = resolver.insert(collection, values) ?: return DownloadResult.Error("Could not create the saved media file.")
            resolver.openOutputStream(uri)?.use { output -> inputStream.use { it.copyTo(output) } }
                ?: return DownloadResult.Error("Could not write the saved media file.")
            DownloadResult.Success(uri.toString())
        } catch (e: Exception) {
            DownloadResult.Error("Legacy save failed: ${e.message}")
        }
    }
    
    /**
     * Get relative path for MediaStore based on MIME type
     * Files will be saved in: /storage/emulated/0/{relativePath}
     */
    private fun getRelativePath(mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> "${Environment.DIRECTORY_PICTURES}/Postkeeper"
            mimeType.startsWith("video/") -> "${Environment.DIRECTORY_MOVIES}/Postkeeper"
            else -> "${Environment.DIRECTORY_DOWNLOADS}/Postkeeper"
        }
    }
    
    /**
     * Get download directory for legacy storage (Android 9 and below)
     */
    private fun getLegacyDownloadDirectory(): File {
        val postkeeperDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Postkeeper"
        )
        if (!postkeeperDir.exists() && !postkeeperDir.mkdirs()) throw IllegalStateException("Could not create Postkeeper download folder")
        return postkeeperDir
    }
}

sealed class DownloadResult {
    data class Success(val filePath: String) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}
