package com.postkeeper.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.data.repository.PostRepository
import com.postkeeper.app.data.repository.ProcessResult
import com.postkeeper.app.util.DownloadResult
import com.postkeeper.app.util.MediaDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // One-shot events (snackbars). A Channel keeps events buffered until collected,
    // so share-intent results fired before the UI composes are never lost.
    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    /** Parse + save a shared link AND immediately download its media to local storage. */
    fun processSharedUrl(rawUrl: String) {
        // Users often paste captions/emoji alongside the link — extract the URL.
        val url = UrlMatcher.extractUrl(rawUrl)
        if (url == null) {
            viewModelScope.launch {
                _events.send(HomeEvent.Error("No Instagram or X link found in that text."))
            }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                when (val result = repository.processSharedUrl(url)) {
                    is ProcessResult.Success -> {
                        _events.send(HomeEvent.Info("Saved to library — downloading media…"))
                        // Auto-download right after saving: one tap from Share → file on device.
                        runDownload(result.post.id)
                    }
                    is ProcessResult.Exists -> {
                        if (result.post.isDownloaded) {
                            _events.send(HomeEvent.Info("Already in your library."))
                        } else {
                            _events.send(HomeEvent.Info("Already saved — downloading media…"))
                            runDownload(result.post.id)
                        }
                    }
                    is ProcessResult.Error ->
                        _events.send(HomeEvent.Error(result.message))
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /** Manual "Download" button on a post card. */
    fun downloadPost(postId: Long) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                runDownload(postId)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private suspend fun runDownload(postId: Long) {
        when (val result = repository.downloadPost(postId)) {
            is DownloadResult.Success ->
                _events.send(HomeEvent.Info("Media saved to your device 📁 ${MediaDownloader.describeSavedLocation(result.filePath)}"))
            is DownloadResult.Error ->
                _events.send(HomeEvent.Error(result.message))
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post)
            _events.send(HomeEvent.Info("Removed from library."))
        }
    }
}

sealed class HomeEvent {
    data class Info(val text: String) : HomeEvent()
    data class Error(val text: String) : HomeEvent()
}

/** Pulls the first http(s) URL out of arbitrary shared text. */
object UrlMatcher {
    private val regex = Regex("""https?://\S+""")
    fun extractUrl(text: String): String? =
        regex.find(text.trim())?.value?.trimEnd('.', ',', ')', ']', '"', '\'')
}
