package com.postkeeper.app.util

import android.content.Context
import android.os.Environment
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
    
    suspend fun downloadMedia(mediaUrl: String, mediaType: MediaType): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val request = okhttp3.Request.Builder()
                .url(mediaUrl)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext DownloadResult.Error("Failed to download: ${response.code}")
            }
            
            val body = response.body ?: return@withContext DownloadResult.Error("Empty response body")
            
            val extension = when (mediaType) {
                MediaType.VIDEO -> "mp4"
                MediaType.IMAGE -> {
                    val contentType = response.header("Content-Type") ?: "image/jpeg"
                    when {
                        contentType.contains("png") -> "png"
                        contentType.contains("webp") -> "webp"
                        else -> "jpg"
                    }
                }
                MediaType.UNKNOWN -> "bin"
            }
            
            val fileName = "postkeeper_${System.currentTimeMillis()}.$extension"
            val downloadDir = getDownloadDirectory()
            val file = File(downloadDir, fileName)
            
            FileOutputStream(file).use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
            
            DownloadResult.Success(file.absolutePath)
        } catch (e: Exception) {
            DownloadResult.Error("Download failed: ${e.message}")
        }
    }
    
    private fun getDownloadDirectory(): File {
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
