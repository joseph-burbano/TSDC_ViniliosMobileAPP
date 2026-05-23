package com.uniandes.vinilos.ui.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniandes.vinilos.util.Constants.ALBUM_GENRES
import com.uniandes.vinilos.util.Constants.ALBUM_RECORD_LABELS
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlbumScreen(
    viewModel: CreateAlbumViewModel,
    onSuccess: () -> Unit,
    onDiscard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val cover by viewModel.cover.collectAsStateWithLifecycle()
    val releaseDate by viewModel.releaseDate.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val recordLabel by viewModel.recordLabel.collectAsStateWithLifecycle()

    val nameError by viewModel.nameError.collectAsStateWithLifecycle()
    val releaseDateError by viewModel.releaseDateError.collectAsStateWithLifecycle()
    val genreError by viewModel.genreError.collectAsStateWithLifecycle()
    val recordLabelError by viewModel.recordLabelError.collectAsStateWithLifecycle()
    val descriptionError by viewModel.descriptionError.collectAsStateWithLifecycle()

    // Navegar al éxito
    LaunchedEffect(uiState) {
        if (uiState is CreateAlbumUiState.Success) {
            onSuccess()
            viewModel.resetState()
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground
    val accent = Color(0xFF8B2E1A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .testTag("create_album_screen")
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Eyebrow
        Text(
            text = "CATALOGING SYSTEM",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = accent,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Crear Álbum",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Añade una nueva pieza a tu archivo personal. Documenta la historia, el sonido y la estética del prensado.",
            fontSize = 14.sp,
            color = onBackground.copy(alpha = 0.6f),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Cover URL field (sustituye el uploader del mockup)
        OutlinedTextField(
            value = cover,
            onValueChange = { viewModel.cover.value = it },
            label = { Text("URL de portada (opcional)") },
            placeholder = { Text("https://...", fontStyle = FontStyle.Italic) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_cover")
                .semantics { contentDescription = "input_cover" },
            singleLine = true,
            colors = vinilosTextFieldColors()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Título
        VinilosField(
            label = "TÍTULO DEL ÁLBUM",
            value = name,
            onValueChange = { viewModel.name.value = it },
            placeholder = "Ej. Kind of Blue",
            error = nameError,
            testTag = "input_name"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Año de lanzamiento
        VinilosField(
            label = "AÑO DE LANZAMIENTO",
            value = releaseDate,
            onValueChange = { viewModel.releaseDate.value = it },
            placeholder = "1959",
            error = releaseDateError,
            keyboardType = KeyboardType.Number,
            testTag = "input_release_date"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Género dropdown
        VinilosDropdown(
            label = "GÉNERO",
            selected = genre,
            options = ALBUM_GENRES,
            onSelected = { viewModel.genre.value = it },
            error = genreError,
            testTag = "dropdown_genre"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Sello discográfico dropdown
        VinilosDropdown(
            label = "SELLO DISCOGRÁFICO",
            selected = recordLabel,
            options = ALBUM_RECORD_LABELS,
            onSelected = { viewModel.recordLabel.value = it },
            error = recordLabelError,
            testTag = "dropdown_record_label"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Descripción / notas
        Column {
            Text(
                text = "NOTAS DEL ARCHIVISTA",
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                color = onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                placeholder = {
                    Text(
                        "Describe el estado del vinilo, la calidad del prensado o anécdotas sobre su adquisición...",
                        color = onBackground.copy(alpha = 0.35f),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("input_description")
                    .semantics { contentDescription = "input_description" },
                isError = descriptionError != null,
                colors = vinilosTextFieldColors(),
                maxLines = 6
            )
            if (descriptionError != null) {
                Text(
                    text = descriptionError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Error global
        if (uiState is CreateAlbumUiState.Error) {
            Text(
                text = (uiState as CreateAlbumUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("create_album_error")
            )
        }

        // Botón primario
        Button(
            onClick = { viewModel.submitAlbum() },
            enabled = uiState !is CreateAlbumUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_album")
                .semantics { contentDescription = "btn_submit_album" },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = onBackground,
                contentColor = backgroundColor
            )
        ) {
            if (uiState is CreateAlbumUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = backgroundColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "ARCHIVAR ÁLBUM →",
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón secundario
        TextButton(
            onClick = onDiscard,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_discard_album")
        ) {
            Text(
                text = "DESCARTAR BORRADOR",
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                color = onBackground.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Footer ref
        Text(
            text = "REF. AV-2024-HU07",
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = onBackground.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Componentes internos ──────────────────────────────────────────────────────

@Composable
private fun VinilosField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    testTag: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = onBackground.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    fontStyle = FontStyle.Italic,
                    color = onBackground.copy(alpha = 0.35f)
                )
            },
            isError = error != null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .semantics { contentDescription = testTag },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = vinilosTextFieldColors()
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VinilosDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    error: String?,
    testTag: String
) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = onBackground.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.testTag(testTag)
        ) {
            OutlinedTextField(
                value = selected.ifEmpty { "Seleccionar" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                isError = error != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .semantics { contentDescription = testTag },
                colors = vinilosTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                        modifier = Modifier.testTag("option_$option")
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun vinilosTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
    cursorColor = MaterialTheme.colorScheme.onBackground
)
