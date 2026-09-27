package com.postkeeper.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.postkeeper.app.ui.screens.home.HomeEvent
import com.postkeeper.app.ui.screens.home.HomeScreen
import com.postkeeper.app.ui.screens.home.HomeViewModel
import com.postkeeper.app.ui.screens.home.UiMessage
import com.postkeeper.app.ui.theme.PostkeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Share intents (ACTION_SEND) are handled here on cold start and in
        // onNewIntent when the app is already running.
        val startupShareUrl = shareUrlFromIntent(intent)

        setContent {
            val posts by viewModel.posts.collectAsStateWithLifecycle()
            val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
            var uiMessage by remember { mutableStateOf<UiMessage?>(null) }

            // One-shot ViewModel events → snackbar messages
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    uiMessage = when (event) {
                        is HomeEvent.Info -> UiMessage.Info(event.text)
                        is HomeEvent.Error -> UiMessage.Error(event.text)
                    }
                }
            }

            // Process a share-intent URL that arrived during/before composition
            LaunchedEffect(startupShareUrl) {
                startupShareUrl?.let { viewModel.processSharedUrl(it) }
            }

            PostkeeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        posts = posts,
                        isProcessing = isProcessing,
                        message = uiMessage,
                        onMessageShown = { uiMessage = null },
                        onDownloadClick = { post ->
                            viewModel.downloadPost(post.id)
                        },
                        onDeleteClick = { post ->
                            viewModel.deletePost(post)
                        },
                        onAddUrl = { url ->
                            viewModel.processSharedUrl(url)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shareUrlFromIntent(intent)?.let { viewModel.processSharedUrl(it) }
    }

    private fun shareUrlFromIntent(intent: Intent?): String? {
        intent ?: return null
        if (intent.action != Intent.ACTION_SEND) return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!text.isNullOrBlank()) return text
        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
        return uri?.toString()
    }
}
