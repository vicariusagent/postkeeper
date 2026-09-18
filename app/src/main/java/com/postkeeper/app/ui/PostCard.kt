package com.postkeeper.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Image
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
import com.postkeeper.app.ui.theme.CornerRadiusMedium
import com.postkeeper.app.ui.theme.CornerRadiusLarge
import com.postkeeper.app.ui.theme.SpacingSmall
import com.postkeeper.app.ui.theme.SpacingMedium

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
            .padding(horizontal = SpacingMedium.dp, vertical = SpacingSmall.dp),
        shape = RoundedCornerShape(CornerRadiusMedium.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.thumbnailUrl ?: post.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = post.title ?: "Post thumbnail",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = CornerRadiusMedium.dp, topEnd = CornerRadiusMedium.dp)),
                    contentScale = ContentScale.Crop
                )
                
                PlatformBadge(
                    platform = post.platform,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingSmall.dp)
                )
                
                MediaTypeBadge(
                    mediaType = post.mediaType,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(SpacingSmall.dp)
                )
                
                if (post.isDownloaded) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(SpacingSmall.dp)
                    ) {
                        Text("Downloaded", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Column(
                modifier = Modifier.padding(SpacingMedium.dp)
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
                        text = author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingMedium.dp),
                    horizontalArrangement = Arrangement.spacedBy(SpacingMedium.dp)
                ) {
                    Button(
                        onClick = { onDownloadClick(post) },
                        modifier = Modifier.weight(1f),
                        enabled = !post.isDownloaded,
                        shape = RoundedCornerShape(CornerRadiusLarge.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (post.isDownloaded) "Downloaded" else "Download")
                    }
                    
                    IconButton(
                        onClick = { onDeleteClick(post) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadiusLarge.dp),
        color = when (platform) {
            Platform.INSTAGRAM -> Color(0xFFE1306C)
            Platform.TWITTER -> Color(0xFF1DA1F2)
            Platform.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = 2.dp
    ) {
        Icon(
            imageVector = when (platform) {
                Platform.INSTAGRAM -> Icons.Default.Image
                Platform.TWITTER -> Icons.Default.PlayArrow
                Platform.UNKNOWN -> Icons.Default.Image
            },
            contentDescription = platform.name,
            tint = Color.White,
            modifier = Modifier.padding(4.dp).size(20.dp)
        )
    }
}

@Composable
private fun MediaTypeBadge(mediaType: MediaType, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadiusLarge.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Icon(
            imageVector = when (mediaType) {
                MediaType.VIDEO -> Icons.Default.PlayArrow
                MediaType.IMAGE -> Icons.Default.Image
                MediaType.UNKNOWN -> Icons.Default.Image
            },
            contentDescription = mediaType.name,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(4.dp).size(20.dp)
        )
    }
}
