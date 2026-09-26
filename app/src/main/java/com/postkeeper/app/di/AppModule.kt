package com.postkeeper.app.di

import android.content.Context
import com.postkeeper.app.data.AppDatabase
import com.postkeeper.app.data.dao.PostDao
import com.postkeeper.app.data.repository.PostRepository
import com.postkeeper.app.util.InstagramExtractor
import com.postkeeper.app.util.MediaDownloader
import com.postkeeper.app.util.TwitterExtractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao = database.postDao()
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)
    
    @Provides
    @Singleton
    fun providePostRepository(
        postDao: PostDao,
        @ApplicationContext context: Context
    ): PostRepository = PostRepository(postDao, context)
    
    @Provides
    @Singleton
    fun provideMediaDownloader(@ApplicationContext context: Context): MediaDownloader =
        MediaDownloader(context)
    
    @Provides
    @Singleton
    fun provideInstagramExtractor(): InstagramExtractor = InstagramExtractor
    
    @Provides
    @Singleton
    fun provideTwitterExtractor(): TwitterExtractor = TwitterExtractor
}
