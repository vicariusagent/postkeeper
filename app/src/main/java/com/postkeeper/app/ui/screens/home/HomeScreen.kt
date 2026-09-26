package com.postkeeper.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.ui.PostCard
import com.postkeeper.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<Post>,
    onDownloadClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onAddUrl: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }
    
    // Split posts by platform
    val instagramPosts = remember(posts) { posts.filter { it.platform == "instagram" } }
    val xPosts = remember(posts) { posts.filter { it.platform == "x" } }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Postkeeper",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Post") }
            )
        }
    ) { paddingValues ->
        if (posts.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = SpacingMedium.dp,
                    end = SpacingMedium.dp,
                    top = SpacingSmall.dp,
                    bottom = SpacingXXL.dp
                ),
                verticalArrangement = Arrangement.spacedBy(SpacingLarge.dp)
            ) {
                // Instagram Section
                if (instagramPosts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Instagram",
                            count = instagramPosts.size,
                            platformColor = InstagramPrimary
                        )
                    }
                    items(instagramPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onDownloadClick = onDownloadClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
                
                // X Section
                if (xPosts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "X (Twitter)",
                            count = xPosts.size,
                            platformColor = XBlack
                        )
                    }
                    items(xPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onDownloadClick = onDownloadClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddUrlDialog(
            urlInput = urlInput,
            onUrlChange = { urlInput = it },
            onDismiss = { 
                showAddDialog = false
                urlInput = ""
            },
            onConfirm = {
                if (urlInput.isNotBlank()) {
                    onAddUrl(urlInput.trim())
                    showAddDialog = false
                    urlInput = ""
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    platformColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingMedium.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = SpacingLarge.dp)
                .background(platformColor, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.width(SpacingSmall.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = platformColor.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "$count posts",
                style = MaterialTheme.typography.labelSmall,
                color = platformColor,
                modifier = Modifier.padding(horizontal = SpacingSmall.dp, vertical = SpacingExtraSmall.dp)
            )
        }
    }
}

@Composable
private fun AddUrlDialog(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { 
            Text(
                "Add Post URL",
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column {
                Text(
                    "Paste an Instagram or X/Twitter post URL:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(SpacingMedium.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    label = { Text("URL") },
                    placeholder = { 
                        Text(
                            "https://instagram.com/p/... or https://x.com/...",
                            style = MaterialTheme.typography.bodySmall
                        ) 
                    },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = urlInput.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Add Post")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Cancel")
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingXXL.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(SpacingLarge.dp))
        Text(
            text = "No posts yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(SpacingSmall.dp))
        Text(
            text = "Share a link from Instagram or X\nto get started!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
