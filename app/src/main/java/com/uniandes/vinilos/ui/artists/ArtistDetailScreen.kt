package com.uniandes.vinilos.ui.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.UserRole
import androidx.compose.material.icons.filled.Menu
import com.uniandes.vinilos.ui.components.VinilosTopBar

// ── TestTags ──────────────────────────────────────────────────────────────────
object ArtistDetailTestTags {
    const val SCREEN          = "artist_detail_screen"
    const val LOADING         = "loading_indicator"
    const val NAME            = "artist_detail_name"
    const val IMAGE           = "artist_detail_image"
    const val ALBUMS          = "artist_detail_albums"
    const val BACK            = "top_bar_back_button"
    const val STATS           = "artist_detail_stats"
    const val PRIZES_SECTION  = "artist_detail_prizes_section"
    const val PRIZES_CTA      = "artist_detail_prizes_cta"
}

// ── Pantalla principal ────────────────────────────────────────────────────────
@Composable
fun ArtistDetailScreen(
    artistId: Int,
    viewModel: ArtistViewModel,
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAssociatePrize: (Int) -> Unit = {},
    userRole: UserRole? = null
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val performer = viewModel.findById(artistId)

    // Mientras la lista no haya cargado, performer es null
    when {
        isLoading || performer == null -> ArtistDetailLoading()
        else -> ArtistDetailContent(
            performer = performer,
            onBack = onBack,
            onMenuClick = onMenuClick,
            onAssociatePrize = onAssociatePrize,
            userRole = userRole
        )
    }
}

// ── Estado de carga ───────────────────────────────────────────────────────────
@Composable
private fun ArtistDetailLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(ArtistDetailTestTags.LOADING),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ── Contenido cuando hay datos ────────────────────────────────────────────────
@Composable
private fun ArtistDetailContent(
        performer: Performer,
        onBack: () -> Unit,
        onMenuClick: () -> Unit,
        onAssociatePrize: (Int) -> Unit = {},
        userRole: UserRole? = null
    ) {
    Scaffold(
        topBar = {
            VinilosTopBar(
                title = "Artistas",
                showBack = true,
                onBack = onBack,
                onMenuClick = onMenuClick,
                userRole = userRole
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .testTag(ArtistDetailTestTags.SCREEN)
        ) {
            HeroSection(
                performer = performer,
                onBack = onBack    // ya no necesita el botón flotante
            )
            DescriptionSection(performer = performer)
            GenreChipsSection(albums = performer.albums)
            StatsSection(performer = performer)
            DiscographySection(albums = performer.albums)
            if (userRole == UserRole.COLLECTOR) {
                PrizesSection(
                    onAssociatePrize = { onAssociatePrize(performer.id) }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Sección 1: Hero (foto + nombre superpuesto) ───────────────────────────────
@Composable
private fun HeroSection(performer: Performer, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        // Foto del artista (full width)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(performer.image)
                .crossfade(true)
                .build(),
            contentDescription = performer.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            modifier = Modifier
                .fillMaxSize()
                .testTag(ArtistDetailTestTags.IMAGE)
        )

        // Gradiente oscuro de abajo hacia arriba para legibilidad del texto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        startY = 120f
                    )
                )
        )
        // Texto superpuesto sobre la foto (parte inferior)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "ARTISTA DESTACADO",
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = performer.name,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = 42.sp,
                modifier = Modifier.testTag(ArtistDetailTestTags.NAME)
            )
        }
    }
}

// ── Sección 2: Tipo de artista + descripción ──────────────────────────────────
@Composable
private fun DescriptionSection(performer: Performer) {
    // Determina si es músico o banda según el campo que tenga fecha
    val typeLabel = if (performer.birthDate != null) "EL MÚSICO" else "LA BANDA"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = typeLabel,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = performer.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = performer.description,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
    }
}

// ── Sección 3: Chips de géneros ───────────────────────────────────────────────
@Composable
private fun GenreChipsSection(albums: List<Album>) {
    val genres = albums.map { it.genre }.distinct()
    if (genres.isEmpty()) return

    // FlowRow acomoda chips en múltiples líneas si no caben en una sola
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = genre.uppercase(),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                },
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

// ── Sección 4: Stats card ─────────────────────────────────────────────────────
@Composable
private fun StatsSection(performer: Performer) {
    val dateLabel = if (performer.birthDate != null) "NACIMIENTO" else "FORMACIÓN"
    val dateValue = (performer.birthDate ?: performer.creationDate)?.take(4) ?: "—"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
            .testTag(ArtistDetailTestTags.STATS),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow(label = "ARTISTA ID", value = "#${performer.id}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)
            StatRow(label = "ÁLBUMES", value = "${performer.albums.size} en discografía")
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)
            StatRow(label = dateLabel, value = dateValue)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Sección 5: Discografía (scroll horizontal) ────────────────────────────────
@Composable
private fun DiscographySection(albums: List<Album>) {
    if (albums.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header de la sección
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "LA COLECCIÓN",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Álbumes Esenciales",
                    fontSize = 26.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scroll horizontal de portadas
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ArtistDetailTestTags.ALBUMS),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(albums) { album ->
                AlbumCardSmall(album = album)
            }
        }
    }
}

// ── Sección 6: Galardones (HU-13) — sólo visible para COLLECTOR ───────────────
@Composable
private fun PrizesSection(onAssociatePrize: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .testTag(ArtistDetailTestTags.PRIZES_SECTION)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "LOS GALARDONES",
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Premios y Reconocimientos",
                fontSize = 26.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Asocia un premio existente o crea uno nuevo para este artista.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAssociatePrize,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .testTag(ArtistDetailTestTags.PRIZES_CTA)
        ) {
            Text(
                text = "Asociar premio",
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun AlbumCardSmall(album: Album) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(album.cover)
                .crossfade(true)
                .build(),
            contentDescription = album.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_menu_gallery),
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = album.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 16.sp
        )
        Text(
            text = album.releaseDate.take(4),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
