package com.postkeeper.app.ui.home

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.postkeeper.app.R
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.databinding.ActivityHomeBinding
import com.postkeeper.app.util.DownloadResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: PostAdapter
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            processSharedUrl()
        } else {
            Toast.makeText(this, "Storage permission required to download media", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Check if app was launched via share intent
        handleShareIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }
    
    private fun setupRecyclerView() {
        adapter = PostAdapter(
            onItemClick = { post -> /* Navigate to detail */ },
            onDownloadClick = { post -> downloadPost(post) },
            onDeleteClick = { post -> deletePost(post) }
        )
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPosts.adapter = adapter
    }
    
    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            showAddPostDialog()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.posts.collectLatest { posts ->
                    adapter.submitList(posts)
                    binding.emptyState.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerViewPosts.visibility = if (posts.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.downloadResult.collectLatest { result ->
                when (result) {
                    is DownloadResult.Success -> {
                        Snackbar.make(binding.root, "Media saved successfully!", Snackbar.LENGTH_LONG).show()
                    }
                    is DownloadResult.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.processResult.collectLatest { result ->
                result?.let {
                    when (it) {
                        is com.postkeeper.app.data.repository.ProcessResult.Success -> {
                            Snackbar.make(binding.root, "Post added successfully!", Snackbar.LENGTH_SHORT).show()
                        }
                        is com.postkeeper.app.data.repository.ProcessResult.Exists -> {
                            Snackbar.make(binding.root, "Post already exists in your collection", Snackbar.LENGTH_SHORT).show()
                        }
                        is com.postkeeper.app.data.repository.ProcessResult.Error -> {
                            Snackbar.make(binding.root, it.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    viewModel.resetProcessResult()
                }
            }
        }
    }
    
    private fun handleShareIntent(intent: Intent?) {
        intent?.let {
            val sharedText = it.getStringExtra(Intent.EXTRA_TEXT)
            val sharedUri = it.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            
            sharedText?.let { text ->
                viewModel.processSharedUrl(text.trim())
            }
            
            sharedUri?.let { uri ->
                // Handle shared image/video URI
                viewModel.processSharedUrl(uri.toString())
            }
        }
    }
    
    private fun processSharedUrl() {
        // Process any URL from clipboard or user input
        // This would be called after permission is granted
    }
    
    private fun showAddPostDialog() {
        // Show dialog to paste URL
        val url = binding.editTextUrl.text.toString().trim()
        if (url.isNotEmpty()) {
            viewModel.processSharedUrl(url)
            binding.editTextUrl.text?.clear()
        } else {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun downloadPost(post: Post) {
        if (!post.isDownloaded) {
            viewModel.downloadPost(post.id)
        } else {
            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deletePost(post: Post) {
        viewModel.deletePost(post)
    }
}
