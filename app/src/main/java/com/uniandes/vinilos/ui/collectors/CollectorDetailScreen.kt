package com.uniandes.vinilos.ui.collectors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.CollectorAlbum
import com.uniandes.vinilos.model.Performer
import androidx.compose.runtime.LaunchedEffect
import com.uniandes.vinilos.ui.components.VinilosTopBar
import com.uniandes.vinilos.model.UserRole

object CollectorDetailTestTags {
    const val SCREEN = "collector_detail_screen"
    const val LOADING = "collector_detail_loading"
    const val NAME = "collector_detail_name"
    const val IMAGE = "collector_detail_image"
    const val BACK    = "top_bar_back_button" 
    const val STATS = "collector_detail_stats"
    const val VAULT = "collector_detail_vault"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDetailScreen(
    collectorId: Int,
    viewModel: CollectorViewModel = viewModel(
        factory = CollectorViewModel.factory(LocalContext.current)
    ),
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAddFavoriteArtist: () -> Unit = {},
    userRole: UserRole? = null
) {
    LaunchedEffect(collectorId) {
        viewModel.loadCollector(collectorId)
    }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val collectors by viewModel.collectors.collectAsStateWithLifecycle()
    val collector = collectors.find { it.id == collectorId }

    if (isLoading || collector == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { testTag = CollectorDetailTestTags.LOADING },
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        return
    }

    Scaffold(
        modifier = Modifier.semantics { testTag = CollectorDetailTestTags.SCREEN },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VinilosTopBar(
                title = "Coleccionistas",
                showBack = true,
                onBack = onBack,
                onMenuClick = onMenuClick,
                userRole = userRole
            )
        },
        floatingActionButton = {
            if (userRole == UserRole.COLLECTOR) {
                FloatingActionButton(
                    onClick = onAddFavoriteArtist,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.semantics {
                        contentDescription = "Agregar artista favorito"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Agregar artista favorito"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            HeroSection(collector)
            Spacer(Modifier.height(24.dp))
            StatsSection(collector)
            Spacer(Modifier.height(32.dp))
            FavoriteArtistsSection(
                favorites = collector.favoritePerformers,
                onAddFavoriteArtist = onAddFavoriteArtist,
                userRole = userRole
            )
            Spacer(Modifier.height(32.dp))
            VaultSection(collector.collectorAlbums)
            Spacer(Modifier.height(24.dp))
            SeeMoreButton()
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HeroSection(collector: Collector) {
    val context = LocalContext.current
    Column {
        AsyncImage(
            // Bitmap decodificado al tamaño real (96dp), no al tamaño nativo del servidor.
            model = remember(collector.image) {
                ImageRequest.Builder(context)
                    .data(collector.image?.ifBlank { null })
                    .crossfade(true)
                    .scale(Scale.FILL)
                    .build()
            },
            contentDescription = collector.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .semantics { testTag = CollectorDetailTestTags.IMAGE }
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                "ELITE CURATOR",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        if (!collector.email.isNullOrBlank()) {
            Text(
                collector.email!!,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            collector.name,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { testTag = CollectorDetailTestTags.NAME }
        )
        if (!collector.description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                collector.description!!,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StatsSection(collector: Collector) {
    val albums = collector.collectorAlbums
    // remember evita recalcular el grouping/eachCount en cada recomposition.
    // Solo se recomputa cuando cambia la lista subyacente.
    val avgGrade = remember(albums) {
        albums.mapNotNull { it.status.takeIf { s -> s.isNotBlank() } }
            .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "—"
    }

    Column(modifier = Modifier.semantics { testTag = CollectorDetailTestTags.STATS }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("COLLECTION", albums.size.toString(), "LPs", Modifier.weight(1f))
            StatCard(
                "WANTLIST",
                collector.favoritePerformers.size.toString(),
                "Rarities",
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("AVG. GRADE", avgGrade, "Condition", Modifier.weight(1f))
            StatCard(
                "FOLLOWERS",
                collector.comments.size.toString(),
                "Audience",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(suffix, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun VaultSection(albums: List<CollectorAlbum>) {
    Column(modifier = Modifier.semantics { testTag = CollectorDetailTestTags.VAULT }) {
        Text(
            "The Vault",
            fontSize = 28.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Rare pressings and personal milestones.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                "BROWSE ALL",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        if (albums.isEmpty()) {
            Text(
                "Sin álbumes en la colección todavía.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            albums.forEach { item ->
                VaultAlbumCard(item)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun VaultAlbumCard(item: CollectorAlbum) {
    val album = item.album
    val context = LocalContext.current
    // remember evita reconstruir el string de referencia en cada recomposition.
    val ref = remember(album?.id, album?.releaseDate) {
        "${album?.id?.toString()?.padStart(2, '0') ?: "—"}-${album?.releaseDate?.take(4) ?: "----"}"
    }
    Column {
        Box {
            AsyncImage(
                // Bitmap downsampled al ancho × 280dp en vez del tamaño original del cover.
                model = remember(album?.cover) {
                    ImageRequest.Builder(context)
                        .data(album?.cover?.ifBlank { null })
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .build()
                },
                contentDescription = album?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Icon(
                imageVector = if (item.status == "NM") Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            ref,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            album?.name ?: "—",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            album?.performers?.firstOrNull()?.name ?: "Artista desconocido",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun SeeMoreButton() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.clickable { /* Navegación pendiente */ }
        ) {
            Text(
                "VER MÁS",
                color = MaterialTheme.colorScheme.background,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun FavoriteArtistsSection(
    favorites: List<Performer>,
    onAddFavoriteArtist: () -> Unit,
    userRole: UserRole?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Artistas Favoritos",
                fontSize = 28.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (userRole == UserRole.COLLECTOR) {
                TextButton(onClick = onAddFavoriteArtist) {
                    Text(
                        "GESTIONAR",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Músicos y bandas que definen el gusto del coleccionista.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(16.dp))
        if (favorites.isEmpty()) {
            Text(
                "Sin artistas favoritos todavía.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            favorites.forEach { performer ->
                FavoriteArtistChip(performer = performer)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun FavoriteArtistChip(performer: Performer) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = remember(performer.image) {
                ImageRequest.Builder(context)
                    .data(performer.image?.ifBlank { null })
                    .crossfade(true)
                    .scale(Scale.FILL)
                    .build()
            },
            contentDescription = performer.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = performer.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (performer.isMusician) "Músico" else "Banda",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

