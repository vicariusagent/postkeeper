package com.postkeeper.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.postkeeper.app.data.model.MediaType
import com.postkeeper.app.data.model.Platform
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.ui.theme.SpacingExtraSmall
import com.postkeeper.app.ui.theme.SpacingMedium
import dev.vicart.compose.material.symbols.MaterialSymbol

@Composable
fun PostCard(
    post: Post,
    onDownloadClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember(post.id) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.thumbnailUrl ?: post.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = post.title ?: "Post thumbnail",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RectangleShape),
                    contentScale = ContentScale.Crop
                )
                
                PlatformBadge(
                    platform = post.platform,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
                
                MediaTypeBadge(
                    mediaType = post.mediaType,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
                
                if (post.isDownloaded) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Saved") },
                        leadingIcon = {
                            MaterialSymbol.Filled(icon = "check", size = 16.dp)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Text(
                    text = post.title ?: "Untitled Post",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                post.author?.let { author ->
                    Text(
                        text = "@$author",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onDownloadClick(post) },
                        modifier = Modifier.weight(1f),
                        enabled = !post.isDownloaded,
                        shape = RectangleShape
                    ) {
                        MaterialSymbol.Filled(icon = "download", size = 18.dp)
                        Spacer(Modifier.width(SpacingExtraSmall.dp))
                        Text(if (post.isDownloaded) "Saved" else "Download")
                    }
                    
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.wrapContentSize().semantics { contentDescription = "Delete post" }
                    ) {
                        MaterialSymbol.Filled(icon = "delete", size = 24.dp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete saved post?") },
            text = { Text("This removes the post from your Postkeeper collection.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirmation = false; onDeleteClick(post) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            },
            shape = RectangleShape
        )
    }
}

@Composable
private fun PlatformBadge(platform: Platform, modifier: Modifier = Modifier) {
    val platformColors = when (platform) {
        Platform.INSTAGRAM -> Brush.linearGradient(
            colors = listOf(Color(0xFF222222), Color(0xFF222222))
        )
        Platform.TWITTER -> Brush.linearGradient(
            colors = listOf(Color(0xFF222222), Color(0xFF222222))
        )
        Platform.UNKNOWN -> Brush.linearGradient(
            colors = listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outlineVariant)
        )
    }
    
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(platformColors)
                .padding(horizontal = 12.dp, vertical = SpacingExtraSmall.dp)
        ) {
            Text(
                text = when (platform) {
                    Platform.INSTAGRAM -> "Instagram"
                    Platform.TWITTER -> "X"
                    Platform.UNKNOWN -> "Unknown"
                },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MediaTypeBadge(mediaType: MediaType, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SpacingMedium.dp, vertical = SpacingExtraSmall.dp),
            horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialSymbol.Filled(
                icon = if (mediaType == MediaType.VIDEO) "play_arrow" else "image",
                size = 16.dp,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (mediaType) {
                    MediaType.VIDEO -> "Video"
                    MediaType.IMAGE -> "Image"
                    MediaType.UNKNOWN -> "Unknown"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
