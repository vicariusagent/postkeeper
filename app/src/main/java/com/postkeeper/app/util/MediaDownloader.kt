package com.postkeeper.app.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.postkeeper.app.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
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
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext DownloadResult.Error("Failed to download: ${response.code}")
            }
            
            val body = response.body ?: return@withContext DownloadResult.Error("Empty response body")
            
            // Determine file extension and MIME type
            val contentType = response.header("Content-Type") ?: ""
            val (extension, mimeType) = getFileExtensionAndMimeType(mediaType, contentType)
            
            val fileName = "postkeeper_${System.currentTimeMillis()}.$extension"
            
            // Use MediaStore for Android 10+ (API 29+), legacy storage for older versions
            val savedPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStoreApi29Plus(fileName, mimeType, body.byteStream())
            } else {
                saveToLegacyStorage(fileName, body.byteStream())
            }
            
            savedPath
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
            }
            
            val resolver = context.contentResolver
            val uri = resolver.insert(collection, contentValues)
                ?: return DownloadResult.Error("Failed to create MediaStore entry")
            
            resolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            } ?: return DownloadResult.Error("Failed to open output stream")
            
            // Return the URI string as the saved path
            DownloadResult.Success(uri.toString())
        } catch (e: Exception) {
            DownloadResult.Error("MediaStore save failed: ${e.message}")
        }
    }
    
    /**
     * Save file using legacy storage (Android 9 and below)
     */
    private fun saveToLegacyStorage(fileName: String, inputStream: java.io.InputStream): DownloadResult {
        return try {
            val downloadDir = getLegacyDownloadDirectory()
            val file = File(downloadDir, fileName)
            
            FileOutputStream(file).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            
            DownloadResult.Success(file.absolutePath)
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
        if (!postkeeperDir.exists()) {
            postkeeperDir.mkdirs()
        }
        return postkeeperDir
    }
}

sealed class DownloadResult {
    data class Success(val filePath: String) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}
