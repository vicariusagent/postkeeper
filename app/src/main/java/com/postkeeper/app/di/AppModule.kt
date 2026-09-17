package com.postkeeper.app.di

import android.content.Context
import com.postkeeper.app.data.AppDatabase
import com.postkeeper.app.data.dao.PostDao
import com.postkeeper.app.data.repository.PostRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    @Singleton
    fun providePostRepository(postDao: PostDao, @ApplicationContext context: Context): PostRepository {
        return PostRepository(postDao, context)
    }
}
