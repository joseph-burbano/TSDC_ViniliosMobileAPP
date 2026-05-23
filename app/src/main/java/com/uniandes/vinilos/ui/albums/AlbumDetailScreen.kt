package com.uniandes.vinilos.ui.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.ui.components.AlbumCover
import com.uniandes.vinilos.ui.theme.VinilosTheme
import com.uniandes.vinilos.ui.components.VinilosTopBar
import androidx.compose.foundation.background
import com.uniandes.vinilos.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Int,
    viewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.factory(LocalContext.current)),
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAddTrack: (Int) -> Unit = {},
    userRole: UserRole? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is AlbumsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.testTag(AlbumDetailTestTags.LOADING))
            }
            return
        }
        is AlbumsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            }
            return
        }
        is AlbumsUiState.Success -> Unit
    }

    val album = viewModel.findById(albumId)
    if (album == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Álbum no encontrado", modifier = Modifier.padding(24.dp))
        }
        return
    }
    val coverColor = albumCoverColor(albumId)

    Scaffold(
        modifier = Modifier.testTag(AlbumDetailTestTags.SCREEN),
        topBar = {
            VinilosTopBar(
                title = "Albumes",
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
        ) {
            AlbumCover(
                coverUrl = album.cover,
                fallbackColor = coverColor,
                contentDescription = album.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                iconSize = 128.dp
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text(
                    text = album.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(coverColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = album.genre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = coverColor
                        )
                    }
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = album.releaseDate,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = album.recordLabel,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = album.description,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (album.performers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ARTISTAS",
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    album.performers.forEach { performer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(coverColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = performer.name.first().uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = coverColor,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = performer.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = performer.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                val isCollector = userRole == UserRole.COLLECTOR
                if (album.tracks.isNotEmpty() || isCollector) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CANCIONES",
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (isCollector) {
                            TextButton(
                                onClick = { onAddTrack(albumId) },
                                modifier = Modifier
                                    .testTag(AlbumDetailTestTags.BTN_ADD_TRACK)
                                    .semantics { contentDescription = AlbumDetailTestTags.BTN_ADD_TRACK }
                            ) {
                                Text(
                                    text = "+ AGREGAR",
                                    fontSize = 11.sp,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (album.tracks.isEmpty()) {
                        Text(
                            text = "Aún no hay canciones asociadas a este álbum.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .testTag(AlbumDetailTestTags.TRACKS_EMPTY)
                        )
                    } else {
                        album.tracks.forEachIndexed { index, track ->
                            TrackRow(index = index + 1, track = track)
                            if (index < album.tracks.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 0.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TrackRow(index: Int, track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp)
        )
        Text(
            text = track.name,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = track.duration,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

object AlbumDetailTestTags {
    const val SCREEN         = "album_detail_screen"
    const val LOADING        = "album_detail_loading"
    const val BACK           = "top_bar_back_button"
    const val BTN_ADD_TRACK  = "album_detail_btn_add_track"
    const val TRACKS_EMPTY   = "album_detail_tracks_empty"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AlbumDetailScreenPreview() {
    VinilosTheme {
        AlbumDetailScreen(albumId = 5)
    }
}
