package com.uniandes.vinilos.ui.prizes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.Prize
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.theme.VinilosTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrizeAssociateScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val musician = Performer(
        id = 1,
        name = "Rubén Blades",
        image = "",
        description = "Cantautor",
        birthDate = "1948-07-16"
    )

    private val samplePrizes = listOf(
        Prize(10, "Grammy", "Music award", "NARAS"),
        Prize(20, "Latin Grammy", "Latin music award", "LARAS")
    )

    private fun mockViewModel(
        prizes: List<Prize> = samplePrizes,
        isLoading: Boolean = false,
        isSubmitting: Boolean = false,
        error: String? = null
    ): PrizeViewModel {
        val vm = mockk<PrizeViewModel>(relaxed = true)
        every { vm.prizes } returns MutableStateFlow(prizes)
        every { vm.isLoading } returns MutableStateFlow(isLoading)
        every { vm.isSubmitting } returns MutableStateFlow(isSubmitting)
        every { vm.error } returns MutableStateFlow(error)
        every { vm.associationSuccess } returns MutableStateFlow(null)
        return vm
    }

    @Test
    fun pantalla_muestra_lista_de_premios_existentes() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                PrizeAssociateScreen(
                    artist = musician,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(PrizeAssociateTestTags.SCREEN))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.LIST).assertExists()
        composeTestRule.onNodeWithText("Grammy").assertExists()
        composeTestRule.onNodeWithText("Latin Grammy").assertExists()
    }

    @Test
    fun spinner_visible_cuando_isLoading_true_y_lista_vacia() {
        val vm = mockViewModel(prizes = emptyList(), isLoading = true)

        composeTestRule.setContent {
            VinilosTheme {
                PrizeAssociateScreen(
                    artist = musician,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PrizeAssociateTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun toggle_abre_el_formulario_para_crear_nuevo_premio() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                PrizeAssociateScreen(
                    artist = musician,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(PrizeAssociateTestTags.TOGGLE_NEW))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.TOGGLE_NEW).performClick()
        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.FIELD_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.FIELD_ORGANIZATION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.FIELD_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun submit_invoca_submitAssociation_con_los_valores_del_formulario() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                PrizeAssociateScreen(
                    artist = musician,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(PrizeAssociateTestTags.FIELD_DATE))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Selecciona un premio existente y escribe la fecha
        composeTestRule.onNodeWithTag("${PrizeAssociateTestTags.PRIZE_ITEM_PREFIX}10").performClick()
        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.FIELD_DATE).performTextInput("2024-05-01")
        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.SUBMIT).performClick()

        verify {
            vm.submitAssociation(
                performerId = 1,
                isMusician = true,
                premiationDate = "2024-05-01",
                selectedPrizeId = 10,
                newPrizeName = null,
                newPrizeDescription = null,
                newPrizeOrganization = null
            )
        }
    }

    @Test
    fun mensaje_de_error_visible_cuando_viewmodel_expone_error() {
        val vm = mockViewModel(error = "Sin conexión. Revisa tu red e inténtalo de nuevo.")

        composeTestRule.setContent {
            VinilosTheme {
                PrizeAssociateScreen(
                    artist = musician,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(PrizeAssociateTestTags.ERROR))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.ERROR).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin conexión", substring = true).assertExists()
    }

    @Test
    fun artista_nulo_muestra_estado_vacio() {
        val vm = mockViewModel()

        composeTestRule.setContent {
            VinilosTheme {
                PrizeAssociateScreen(
                    artist = null,
                    viewModel = vm,
                    userRole = UserRole.COLLECTOR
                )
            }
        }

        composeTestRule.onNodeWithTag(PrizeAssociateTestTags.EMPTY).assertIsDisplayed()
    }
}
