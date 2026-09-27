package com.postkeeper.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.postkeeper.app.ui.screens.home.HomeScreen
import com.postkeeper.app.ui.screens.home.HomeViewModel
import com.postkeeper.app.ui.theme.PostkeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: HomeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PostkeeperTheme {
                val posts = viewModel.posts.collectAsStateWithLifecycle()
                val processResult = viewModel.processResult.collectAsStateWithLifecycle()
                val downloadResult = viewModel.downloadResult.collectAsStateWithLifecycle()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        posts = posts.value,
                        onDownloadClick = { post ->
                            viewModel.downloadPost(post.id)
                        },
                        onDeleteClick = { post ->
                            viewModel.deletePost(post)
                        },
                        onAddUrl = { url ->
                            viewModel.processSharedUrl(url)
                        },
                        processMessage = processResult.value?.let { result ->
                            when (result) {
                                is com.postkeeper.app.data.repository.ProcessResult.Success -> "Post saved to your collection."
                                is com.postkeeper.app.data.repository.ProcessResult.Exists -> "This post is already in your collection."
                                is com.postkeeper.app.data.repository.ProcessResult.Error -> result.message
                            }
                        },
                        downloadMessage = downloadResult.value?.let { result ->
                            when (result) {
                                is com.postkeeper.app.util.DownloadResult.Success -> "Media saved to your device."
                                is com.postkeeper.app.util.DownloadResult.Error -> result.message
                            }
                        },
                        onDismissMessage = viewModel::clearMessages
                    )
                }
            }
        }
        
        handleShareIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }
    
    private fun handleShareIntent(intent: Intent?) {
        intent?.let {
            val sharedText = it.getStringExtra(Intent.EXTRA_TEXT)
            val sharedUri = it.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            
            sharedText?.let { text ->
                viewModel.processSharedUrl(text.trim())
            }
            
            sharedUri?.let { uri ->
                viewModel.processSharedUrl(uri.toString())
            }
        }
    }
}
