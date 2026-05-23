package com.uniandes.vinilos.ui.prizes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Prize
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.components.VinilosTopBar

object PrizeAssociateTestTags {
    const val SCREEN              = "prize_associate_screen"
    const val LOADING             = "loading_indicator"
    const val ERROR               = "error_message"
    const val LIST                = "prize_associate_list"
    const val PRIZE_ITEM_PREFIX   = "prize_item_"
    const val TOGGLE_NEW          = "prize_associate_toggle_new"
    const val FIELD_NAME          = "prize_associate_field_name"
    const val FIELD_DESCRIPTION   = "prize_associate_field_description"
    const val FIELD_ORGANIZATION  = "prize_associate_field_organization"
    const val FIELD_DATE          = "prize_associate_field_date"
    const val SUBMIT              = "prize_associate_submit"
    const val SUCCESS             = "prize_associate_success"
    const val EMPTY               = "prize_associate_empty"
}

@Composable
fun PrizeAssociateScreen(
    artist: Performer?,
    viewModel: PrizeViewModel,
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAssociated: () -> Unit = {},
    userRole: UserRole? = null
) {
    val prizes by viewModel.prizes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val success by viewModel.associationSuccess.collectAsStateWithLifecycle()

    var selectedPrizeId by remember { mutableStateOf<Int?>(null) }
    var showNewForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newOrganization by remember { mutableStateOf("") }
    var premiationDate by remember { mutableStateOf("") }

    LaunchedEffect(success) {
        if (success != null) {
            viewModel.consumeAssociationSuccess()
            onAssociated()
        }
    }

    Scaffold(
        topBar = {
            VinilosTopBar(
                title = "Asociar premio",
                showBack = true,
                onBack = onBack,
                onMenuClick = onMenuClick,
                userRole = userRole
            )
        }
    ) { padding ->
        when {
            artist == null -> EmptyArtistState(padding)
            isLoading && prizes.isEmpty() -> LoadingState(padding)
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .testTag(PrizeAssociateTestTags.SCREEN)
            ) {
                HeroHeader(artist = artist)
                SectionHeader(eyebrow = "PASO 1", title = "Elige un premio")
                PrizeList(
                    prizes = prizes,
                    selectedPrizeId = selectedPrizeId,
                    isFormOpen = showNewForm,
                    onSelect = {
                        selectedPrizeId = it
                        showNewForm = false
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                NewPrizeToggle(
                    isOpen = showNewForm,
                    onClick = {
                        showNewForm = !showNewForm
                        if (showNewForm) selectedPrizeId = null
                    }
                )
                if (showNewForm) {
                    NewPrizeFields(
                        name = newName,
                        onNameChange = { newName = it },
                        description = newDescription,
                        onDescriptionChange = { newDescription = it },
                        organization = newOrganization,
                        onOrganizationChange = { newOrganization = it }
                    )
                }
                SectionHeader(eyebrow = "PASO 2", title = "Fecha de premiación")
                DateField(
                    value = premiationDate,
                    onChange = { premiationDate = it }
                )
                ErrorBanner(message = error)
                SubmitButton(
                    isSubmitting = isSubmitting,
                    onClick = {
                        viewModel.submitAssociation(
                            performerId = artist.id,
                            isMusician = artist.birthDate != null,
                            premiationDate = premiationDate,
                            selectedPrizeId = selectedPrizeId,
                            newPrizeName = if (showNewForm) newName else null,
                            newPrizeDescription = if (showNewForm) newDescription else null,
                            newPrizeOrganization = if (showNewForm) newOrganization else null
                        )
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LoadingState(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .testTag(PrizeAssociateTestTags.LOADING),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyArtistState(padding: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Artista no disponible",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(PrizeAssociateTestTags.EMPTY)
        )
    }
}

@Composable
private fun HeroHeader(artist: Performer) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "NUEVO RECONOCIMIENTO",
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Para ${artist.name}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            lineHeight = 32.sp
        )
    }
}

@Composable
private fun SectionHeader(eyebrow: String, title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        Text(
            text = eyebrow,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun PrizeList(
    prizes: List<Prize>,
    selectedPrizeId: Int?,
    isFormOpen: Boolean,
    onSelect: (Int) -> Unit
) {
    if (prizes.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag(PrizeAssociateTestTags.LIST)
    ) {
        prizes.forEach { prize ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("${PrizeAssociateTestTags.PRIZE_ITEM_PREFIX}${prize.id}"),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.background,
                onClick = { onSelect(prize.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPrizeId == prize.id && !isFormOpen,
                        onClick = { onSelect(prize.id) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = prize.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (prize.organization.isNotBlank()) {
                            Text(
                                text = prize.organization,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

@Composable
private fun NewPrizeToggle(isOpen: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .testTag(PrizeAssociateTestTags.TOGGLE_NEW)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = if (isOpen) "Cancelar nuevo premio" else "Crear un nuevo premio",
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NewPrizeFields(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    organization: String,
    onOrganizationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del premio") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PrizeAssociateTestTags.FIELD_NAME)
        )
        OutlinedTextField(
            value = organization,
            onValueChange = onOrganizationChange,
            label = { Text("Organización") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PrizeAssociateTestTags.FIELD_ORGANIZATION)
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción") },
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PrizeAssociateTestTags.FIELD_DESCRIPTION)
        )
    }
}

@Composable
private fun DateField(value: String, onChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text("Fecha (YYYY-MM-DD)") },
            singleLine = true,
            placeholder = { Text("2024-01-15") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PrizeAssociateTestTags.FIELD_DATE)
        )
    }
}

@Composable
private fun ErrorBanner(message: String?) {
    if (message.isNullOrBlank()) return
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag(PrizeAssociateTestTags.ERROR),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SubmitButton(isSubmitting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSubmitting,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(52.dp)
            .testTag(PrizeAssociateTestTags.SUBMIT)
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Asociar premio",
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}
