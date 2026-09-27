package com.postkeeper.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.vicart.compose.material.symbols.MaterialSymbol
import com.postkeeper.app.data.model.Post
import com.postkeeper.app.ui.PostCard
import com.postkeeper.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<Post>,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDownloadClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onAddUrl: (String) -> Unit,
    processMessage: String? = null,
    downloadMessage: String? = null,
    isAdding: Boolean = false,
    onDismissMessage: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(processMessage, downloadMessage) {
        (downloadMessage ?: processMessage)?.let {
            snackbarHostState.showSnackbar(it)
            onDismissMessage()
        }
    }

    LaunchedEffect(processMessage) {
        if (processMessage == "Post saved to your collection." ||
            processMessage == "This post is already in your collection."
        ) {
            showAddDialog = false
            urlInput = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Postkeeper", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("YOUR PERSONAL ARCHIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Text("${posts.size} SAVED", style = MaterialTheme.typography.labelMedium)
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Appearance: ${themeMode.label}"
                            }
                        ) {
                            MaterialSymbol.Filled(icon = "contrast", size = 22.dp)
                        }
                        DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
                            ThemeMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label) },
                                    onClick = { onThemeModeChange(mode); showThemeMenu = false },
                                    leadingIcon = {
                                        if (mode == themeMode) MaterialSymbol.Filled(icon = "check", size = 18.dp)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { MaterialSymbol.Filled(icon = "add", size = 24.dp) },
                text = { Text("Add link") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RectangleShape
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (posts.isEmpty()) {
            EmptyState(Modifier.fillMaxSize().padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text("Your collection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("Links and media you’ve kept close.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(posts, key = { it.id }) { post ->
                    PostCard(post = post, onDownloadClick = onDownloadClick, onDeleteClick = onDeleteClick)
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; urlInput = "" },
            title = { Text("Save a link", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Paste a public Instagram or X post link.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Post link") },
                        placeholder = { Text("https://…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onAddUrl(urlInput.trim()) }, enabled = urlInput.isNotBlank() && !isAdding, shape = RectangleShape) {
                    if (isAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isAdding) "Adding…" else "Save link")
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false; urlInput = "" }) { Text("Cancel") } },
            shape = RectangleShape
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = RectangleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(88.dp)) {
            Box(contentAlignment = Alignment.Center) {
                MaterialSymbol.Filled(icon = "archive", size = 34.dp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Keep what matters.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Share a post from Instagram or X, or add its link to start your personal archive.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
