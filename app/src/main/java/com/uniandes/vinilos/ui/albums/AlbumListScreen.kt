package com.uniandes.vinilos.ui.albums

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.ui.components.AlbumCover
import com.uniandes.vinilos.ui.theme.VinilosTheme
import com.uniandes.vinilos.ui.components.VinilosTopBar
import com.uniandes.vinilos.model.UserRole
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

private val coverColors = listOf(
    Color(0xFF1A237E),
    Color(0xFF4A148C),
    Color(0xFF880E4F),
    Color(0xFF1B5E20),
    Color(0xFFBF360C),
    Color(0xFF006064),
    Color(0xFF4E342E),
    Color(0xFF37474F),
)

internal fun albumCoverColor(albumId: Int): Color =
    coverColors[((albumId - 1).coerceAtLeast(0)) % coverColors.size]

@Composable
fun AlbumListScreen(
    viewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.factory(LocalContext.current)),
    onAlbumClick: (Int) -> Unit = {},
    onMenuClick: () -> Unit = {},
    userRole: UserRole? = null,
    onCreateAlbum: () -> Unit = {} 
) {
    // collectAsStateWithLifecycle suspende la recolección cuando la pantalla pasa a STOPPED,
    // evitando trabajo en background y previniendo escenarios pre-ANR si el ViewModel emite
    // mientras la app está en segundo plano.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val visibleAlbums by viewModel.visibleAlbums.collectAsStateWithLifecycle()
    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is AlbumsUiState.Loading -> SkeletonState()
        is AlbumsUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.refresh() }
        )
        is AlbumsUiState.Success -> AlbumListContent(
            albums = state.albums,
            visibleAlbums = visibleAlbums,
            hasMore = hasMore,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            onLoadMore = { viewModel.loadMore() },
            onAlbumClick = onAlbumClick,
            onMenuClick = onMenuClick,
            userRole = userRole,
            onCreateAlbum = onCreateAlbum
        )
    }
}

@Composable
private fun SkeletonState() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag(AlbumListTestTags.LOADING)
    ) {
        items(3) { SkeletonCard() }
    }
}

@Composable
private fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton-alpha"
    )
    val shimmer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f * alpha)

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(shimmer)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonBar(widthFraction = 0.6f, height = 24.dp, color = shimmer)
            SkeletonBar(widthFraction = 0.2f, height = 14.dp, color = shimmer)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBar(widthFraction = 0.4f, height = 16.dp, color = shimmer)
    }
}

@Composable
private fun SkeletonBar(widthFraction: Float, height: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(AlbumListTestTags.ERROR),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No se pudo cargar el catálogo",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumListContent(
    albums: List<Album>,
    visibleAlbums: List<Album>,
    hasMore: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onAlbumClick: (Int) -> Unit,
    onMenuClick: () -> Unit = {},
    userRole: UserRole? = null,
    onCreateAlbum: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf<String?>(null) }

    val allGenres = remember(albums) { albums.map { it.genre }.distinct().sorted() }
    
    val filteredAlbums = remember(albums, searchQuery, selectedGenre) {
        albums.filter { album ->
            val matchesSearch = searchQuery.isBlank() ||
                album.name.contains(searchQuery, ignoreCase = true) ||
                album.performers.any { it.name.contains(searchQuery, ignoreCase = true) }
            val matchesGenre = selectedGenre == null || album.genre == selectedGenre
            matchesSearch && matchesGenre
        }
    }

    val displayAlbums = if (searchQuery.isNotBlank() || selectedGenre != null) {
        filteredAlbums
    } else {
        visibleAlbums
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {  // ← Column exterior sin scroll

            VinilosTopBar(                            // ← fuera del grid, fijo
                userRole = userRole,
                onMenuClick = onMenuClick
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(AlbumListTestTags.LIST)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp)) {
                            Text(
                                text = "SELECCIÓN DE",
                                fontSize = 12.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F).copy(alpha = 0.7f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "Álbumes",
                                    fontSize = 48.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 56.sp
                                )
                                Text(
                                    text = "${filteredAlbums.size} encontrados",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar álbumes o artistas...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .testTag(AlbumListTestTags.SEARCH),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedGenre == null,
                                    onClick = { selectedGenre = null },
                                    label = { Text("Todos") }
                                )
                            }
                            // key estable también en chips de género para evitar recrear
                            // FilterChip al cambiar la selección.
                            items(allGenres, key = { it }) { genre ->
                                FilterChip(
                                    selected = selectedGenre == genre,
                                    onClick = { selectedGenre = if (selectedGenre == genre) null else genre },
                                    label = { Text(genre) }
                                )
                            }
                        }
                    }

                    if (displayAlbums.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.Album,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No se encontraron álbumes",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        // key = { album.id } reusa composables al filtrar/buscar y preserva el
                        // estado interno de cada AlbumCard (animaciones, eventos pendientes).
                        items(displayAlbums, key = { it.id }) { album ->
                            AlbumCard(album = album, onClick = { onAlbumClick(album.id) })
                        }
                        
                        if (hasMore && searchQuery.isBlank() && selectedGenre == null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                                    OutlinedButton(
                                        onClick = onLoadMore,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("load_more_button"),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "CARGAR MÁS",
                                            letterSpacing = 2.sp,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // FAB solo para Collector
        if (userRole == UserRole.COLLECTOR) {
            FloatingActionButton(
                onClick = onCreateAlbum,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("fab_create_album")
                    .semantics { contentDescription = "fab_create_album" },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Text("+", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    val coverColor = albumCoverColor(album.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag(AlbumListTestTags.cardFor(album.id))
    ) {
        AlbumCover(
            coverUrl = album.cover,
            fallbackColor = coverColor,
            contentDescription = album.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RectangleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = album.name,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${album.releaseDate} • ${album.genre.uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F).copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
        }

        if (album.performers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album.performers.first().name,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

object AlbumListTestTags {
    const val LIST = "album_list"
    const val SEARCH = "album_search"
    const val LOADING = "loading_indicator"
    const val ERROR = "error_message"
    fun cardFor(albumId: Int) = "album_item_$albumId"
}

@Preview(showBackground = true)
@Composable
fun AlbumListScreenPreview() {
    VinilosTheme {
        Surface(color = Color(0xFFFAF9F6)) { // Fondo tipo "eggshell" como el prototipo
            AlbumListContent(
                albums = com.uniandes.vinilos.util.FakeData.albums,
                visibleAlbums = com.uniandes.vinilos.util.FakeData.albums.take(2),
                hasMore = true,
                isRefreshing = false,
                onRefresh = {},
                onLoadMore = {},
                onAlbumClick = {}
            )
        }
    }
}
