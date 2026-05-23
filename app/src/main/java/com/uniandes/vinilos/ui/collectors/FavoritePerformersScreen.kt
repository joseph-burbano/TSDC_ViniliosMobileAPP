package com.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.components.VinilosTopBar

object FavoritePerformersTestTags {
    const val SCREEN = "favorite_performers_screen"
    const val LOADING = "favorite_performers_loading"
    const val ERROR = "favorite_performers_error"
    const val LIST = "favorite_performers_list"
    const val EMPTY = "favorite_performers_empty"
    const val ITEM_PREFIX = "favorite_performer_item_"
    const val TOGGLE_PREFIX = "favorite_performer_toggle_"
}

@Composable
fun FavoritePerformersScreen(
    collector: Collector?,
    viewModel: FavoritePerformersViewModel,
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    userRole: UserRole? = null
) {
    val collectorId = collector?.id ?: -1
    val initialFavorites = collector?.favoritePerformers ?: emptyList()

    LaunchedEffect(collectorId) {
        if (collectorId != -1) {
            viewModel.loadData(collectorId, initialFavorites)
        }
    }

    val allPerformers by viewModel.allPerformers.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isTogglingId by viewModel.isTogglingId.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.semantics { contentDescription = FavoritePerformersTestTags.SCREEN },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VinilosTopBar(
                title = "Artistas favoritos",
                showBack = true,
                onBack = onBack,
                onMenuClick = onMenuClick,
                userRole = userRole
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { padding ->
        when {
            collector == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Coleccionista no disponible",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag(FavoritePerformersTestTags.EMPTY)
                    )
                }
            }

            isLoading && allPerformers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .testTag(FavoritePerformersTestTags.LOADING),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            allPerformers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay artistas disponibles.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag(FavoritePerformersTestTags.EMPTY)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .testTag(FavoritePerformersTestTags.LIST),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        PerformerListHeader(
                            collectorName = collector.name,
                            total = allPerformers.size,
                            favoritesCount = favoriteIds.size
                        )
                    }

                    items(
                        items = allPerformers,
                        key = { it.id }
                    ) { performer ->
                        PerformerFavoriteItem(
                            performer = performer,
                            isFavorite = favoriteIds.contains(performer.id),
                            isToggling = isTogglingId == performer.id,
                            onToggle = {
                                viewModel.toggleFavorite(collectorId, performer)
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PerformerListHeader(
    collectorName: String,
    total: Int,
    favoritesCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "ARTISTAS FAVORITOS DE",
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = collectorName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$favoritesCount favoritos · $total artistas disponibles",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

@Composable
private fun PerformerFavoriteItem(
    performer: Performer,
    isFavorite: Boolean,
    isToggling: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val typeLabel = if (performer.isMusician) "Músico" else "Banda"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("${FavoritePerformersTestTags.ITEM_PREFIX}${performer.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = remember(performer.image) {
                ImageRequest.Builder(context)
                    .data(performer.image.ifBlank { null })
                    .crossfade(true)
                    .scale(Scale.FILL)
                    .build()
            },
            contentDescription = performer.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiary)
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = performer.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Surface(
                color = if (performer.isMusician)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = typeLabel.uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (performer.isMusician)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (isToggling) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .testTag("${FavoritePerformersTestTags.TOGGLE_PREFIX}${performer.id}")
                    .semantics {
                        contentDescription = if (isFavorite)
                            "Quitar ${performer.name} de favoritos"
                        else
                            "Agregar ${performer.name} a favoritos"
                    }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
