package com.postkeeper.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.postkeeper.app.ui.theme.ThemeMode
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.data.repository.PostRepository
import com.postkeeper.app.data.repository.ProcessResult
import com.postkeeper.app.util.DownloadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val preferences = context.getSharedPreferences("postkeeper_preferences", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        ThemeMode.fromPreference(preferences.getString("theme_mode", ThemeMode.SYSTEM.name))
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    
    private val _downloadResult = MutableStateFlow<DownloadResult?>(null)
    val downloadResult: StateFlow<DownloadResult?> = _downloadResult.asStateFlow()
    
    private val _processResult = MutableStateFlow<ProcessResult?>(null)
    val processResult: StateFlow<ProcessResult?> = _processResult.asStateFlow()

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()
    
    init {
        observePosts()
    }
    
    private fun observePosts() {
        viewModelScope.launch {
            repository.allPosts.collect { postList ->
                _posts.value = postList
            }
        }
    }
    
    fun processSharedUrl(url: String) {
        if (url.isBlank()) return
        
        viewModelScope.launch {
            _isAdding.value = true
            try {
                _processResult.value = repository.processSharedUrl(url)
            } finally {
                _isAdding.value = false
            }
        }
    }
    
    fun downloadPost(postId: Long) {
        viewModelScope.launch {
            val result = repository.downloadPost(postId)
            _downloadResult.value = result
        }
    }
    
    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }
    
    fun resetProcessResult() {
        _processResult.value = null
    }

    fun clearMessages() {
        _processResult.value = null
        _downloadResult.value = null
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }
}
