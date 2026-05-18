package com.uniandes.vinilos.ui.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrackScreen(
    albumId: Int,
    albumName: String,
    viewModel: CreateTrackViewModel,
    onSuccess: () -> Unit,
    onDiscard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()

    val nameError by viewModel.nameError.collectAsStateWithLifecycle()
    val durationError by viewModel.durationError.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is CreateTrackUiState.Success) {
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
            .testTag(AddTrackTestTags.SCREEN)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TRACK ASSOCIATION",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = accent,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Asociar canción",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Agrega una canción al álbum \"$albumName\". Documenta el orden, la duración exacta y mantén el archivo curado.",
            fontSize = 14.sp,
            color = onBackground.copy(alpha = 0.6f),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        TrackField(
            label = "NOMBRE DE LA CANCIÓN",
            value = name,
            onValueChange = { viewModel.name.value = it },
            placeholder = "Ej. So What",
            error = nameError,
            testTag = AddTrackTestTags.INPUT_NAME
        )

        Spacer(modifier = Modifier.height(20.dp))

        TrackField(
            label = "DURACIÓN (mm:ss)",
            value = duration,
            onValueChange = { viewModel.duration.value = it },
            placeholder = "3:45",
            error = durationError,
            keyboardType = KeyboardType.Number,
            testTag = AddTrackTestTags.INPUT_DURATION
        )

        Spacer(modifier = Modifier.height(36.dp))

        if (uiState is CreateTrackUiState.Error) {
            Text(
                text = (uiState as CreateTrackUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag(AddTrackTestTags.ERROR_GLOBAL)
            )
        }

        Button(
            onClick = { viewModel.submitTrack(albumId) },
            enabled = uiState !is CreateTrackUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(AddTrackTestTags.BTN_SUBMIT)
                .semantics { contentDescription = AddTrackTestTags.BTN_SUBMIT },
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = onBackground,
                contentColor = backgroundColor
            )
        ) {
            if (uiState is CreateTrackUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = backgroundColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "ASOCIAR CANCIÓN →",
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onDiscard,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AddTrackTestTags.BTN_DISCARD)
        ) {
            Text(
                text = "DESCARTAR",
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                color = onBackground.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "REF. AV-2024-HU08",
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = onBackground.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TrackField(
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = onBackground,
                unfocusedBorderColor = onBackground.copy(alpha = 0.25f),
                focusedLabelColor = onBackground,
                cursorColor = onBackground
            )
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("${testTag}_error")
            )
        }
    }
}

object AddTrackTestTags {
    const val SCREEN         = "add_track_screen"
    const val INPUT_NAME     = "input_track_name"
    const val INPUT_DURATION = "input_track_duration"
    const val BTN_SUBMIT     = "btn_submit_track"
    const val BTN_DISCARD    = "btn_discard_track"
    const val ERROR_GLOBAL   = "add_track_error"
}
