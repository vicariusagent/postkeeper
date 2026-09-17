package com.postkeeper.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: PostRepository
) : ViewModel() {
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    
    private val _downloadResult = MutableStateFlow<DownloadResult?>(null)
    val downloadResult: StateFlow<DownloadResult?> = _downloadResult.asStateFlow()
    
    private val _processResult = MutableStateFlow<ProcessResult?>(null)
    val processResult: StateFlow<ProcessResult?> = _processResult.asStateFlow()
    
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
            val result = repository.processSharedUrl(url)
            _processResult.value = result
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
}
