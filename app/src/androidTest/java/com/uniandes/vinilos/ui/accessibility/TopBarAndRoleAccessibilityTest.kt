package com.uniandes.vinilos.ui.accessibility

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.components.VinilosTopBar
import com.uniandes.vinilos.ui.role.RoleSelectionScreen
import com.uniandes.vinilos.ui.theme.VinilosTheme
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopBarAndRoleAccessibilityTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun enableAccessibilityChecks() {
            AccessibilityChecks.enable().setRunChecksFromRootView(true)
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── VinilosTopBar ─────────────────────────────────────────────────────────

    @Test
    fun topBar_titulo_es_un_heading_para_TalkBack() {
        composeTestRule.setContent {
            VinilosTheme {
                VinilosTopBar(title = "Álbumes", showBack = true)
            }
        }

        // El título debe estar marcado como heading semántico
        composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .fetchSemanticsNodes()
            .let { assert(it.isNotEmpty()) { "TopBar title should be marked as a heading" } }
    }

    @Test
    fun topBar_boton_back_expone_descripcion_Volver_y_es_clickeable() {
        composeTestRule.setContent {
            VinilosTheme {
                VinilosTopBar(title = "Detalle", showBack = true)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Volver")
            .assertHasClickAction()
    }

    @Test
    fun topBar_boton_menu_expone_descripcion_Abrir_menu() {
        composeTestRule.setContent {
            VinilosTheme {
                VinilosTopBar(title = "Vinilos")
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Abrir menú")
            .assertHasClickAction()
    }

    @Test
    fun topBar_muestra_chip_de_rol_cuando_userRole_no_es_null() {
        composeTestRule.setContent {
            VinilosTheme {
                VinilosTopBar(title = "Álbumes", userRole = UserRole.COLLECTOR)
            }
        }

        composeTestRule.onNodeWithText("Coleccionista").assertExists()
    }

    // ── RoleSelectionScreen ───────────────────────────────────────────────────

    @Test
    fun roleSelection_titulo_es_heading() {
        composeTestRule.setContent {
            VinilosTheme {
                RoleSelectionScreen(onRoleSelected = {})
            }
        }

        composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .fetchSemanticsNodes()
            .let { assert(it.isNotEmpty()) { "Welcome title should be a heading" } }
    }

    @Test
    fun roleSelection_tarjetas_tienen_descripcion_para_TalkBack() {
        composeTestRule.setContent {
            VinilosTheme {
                RoleSelectionScreen(onRoleSelected = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("Ingresar como Visitante").assertExists()
        composeTestRule.onNodeWithContentDescription("Ingresar como Coleccionista").assertExists()
    }
}
