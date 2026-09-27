package com.postkeeper.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.postkeeper.app.data.model.MediaType
import com.postkeeper.app.data.model.Platform
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.ui.theme.SpacingExtraSmall
import com.postkeeper.app.ui.theme.SpacingMedium
import com.postkeeper.app.ui.theme.CornerRadiusMedium

@Composable
fun PostCard(
    post: Post,
    onDownloadClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(24.dp),
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
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
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
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(SpacingExtraSmall.dp))
                        Text(if (post.isDownloaded) "Saved" else "Download")
                    }
                    
                    IconButton(
                        onClick = { onDeleteClick(post) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
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
        shape = RoundedCornerShape(100.dp),
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
        shape = RoundedCornerShape(100.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SpacingMedium.dp, vertical = SpacingExtraSmall.dp),
            horizontalArrangement = Arrangement.spacedBy(SpacingExtraSmall.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (mediaType) {
                    MediaType.VIDEO -> Icons.Default.PlayArrow
                    MediaType.IMAGE -> Icons.Default.Photo
                    MediaType.UNKNOWN -> Icons.Default.Photo
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
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
