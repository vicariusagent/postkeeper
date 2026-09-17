package com.postkeeper.app.data.dao

import androidx.room.*
import com.postkeeper.app.data.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Long): Post?
    
    @Query("SELECT * FROM posts WHERE url = :url")
    suspend fun getPostByUrl(url: String): Post?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: Post): Long
    
    @Update
    suspend fun update(post: Post)
    
    @Delete
    suspend fun delete(post: Post)
    
    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE posts SET isDownloaded = 1, downloadPath = :path, downloadedAt = :downloadedAt WHERE id = :id")
    suspend fun markAsDownloaded(id: Long, path: String, downloadedAt: Long)
}
