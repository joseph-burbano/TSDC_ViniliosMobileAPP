package com.uniandes.vinilos.ui.accessibility

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.uniandes.vinilos.model.ColorBlindMode
import com.uniandes.vinilos.model.UserRole
import com.uniandes.vinilos.ui.components.AppSettingsDrawer
import com.uniandes.vinilos.ui.components.AppSettingsDrawerTestTags
import com.uniandes.vinilos.ui.theme.VinilosTheme
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso's AccessibilityChecks framework is enabled once for the class.
 * Every Compose `performClick()` ends up invoking Espresso under the hood,
 * which means each click automatically runs Google's a11y rule set
 * (touch-target size, contentDescription presence, contrast, etc.).
 */
@RunWith(AndroidJUnit4::class)
class AppSettingsDrawerAccessibilityTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun enableAccessibilityChecks() {
            AccessibilityChecks.enable().setRunChecksFromRootView(true)
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun theme_toggle_expone_descripcion_y_es_clickeable() {
        composeTestRule.setContent {
            VinilosTheme {
                AppSettingsDrawer(
                    userRole = UserRole.VISITOR,
                    isDarkTheme = false,
                    colorBlindMode = ColorBlindMode.NONE,
                    onToggleTheme = {},
                    onBecomeCollector = {},
                    onLeaveCollector = {},
                    onCloseDrawer = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Cambiar a tema oscuro")
            .assertExists()

        composeTestRule
            .onNodeWithTag(AppSettingsDrawerTestTags.THEME_TOGGLE)
            .assertHasClickAction()
            .assertIsDisplayed()
    }

    @Test
    fun toggle_daltonismo_es_visible_y_clickeable() {
        var toggled = false
        composeTestRule.setContent {
            VinilosTheme {
                AppSettingsDrawer(
                    userRole = UserRole.VISITOR,
                    isDarkTheme = false,
                    colorBlindMode = ColorBlindMode.NONE,
                    onToggleTheme = {},
                    onToggleColorBlind = { toggled = true },
                    onBecomeCollector = {},
                    onLeaveCollector = {},
                    onCloseDrawer = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Activar modo daltónico")
            .assertExists()

        composeTestRule
            .onNodeWithTag(AppSettingsDrawerTestTags.COLOR_BLIND_TOGGLE)
            .assertHasClickAction()
            .performClick()

        assertTrue(toggled)
    }

    @Test
    fun toggle_daltonismo_invierte_label_cuando_esta_activo() {
        composeTestRule.setContent {
            VinilosTheme(colorBlindMode = ColorBlindMode.DEUTERANOPIA) {
                AppSettingsDrawer(
                    userRole = UserRole.COLLECTOR,
                    isDarkTheme = false,
                    colorBlindMode = ColorBlindMode.DEUTERANOPIA,
                    onToggleTheme = {},
                    onToggleColorBlind = {},
                    onBecomeCollector = {},
                    onLeaveCollector = {},
                    onCloseDrawer = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Desactivar modo daltónico")
            .assertExists()
    }

    @Test
    fun accion_de_rol_expone_descripcion_diferenciada_por_rol() {
        composeTestRule.setContent {
            VinilosTheme {
                AppSettingsDrawer(
                    userRole = UserRole.VISITOR,
                    isDarkTheme = false,
                    colorBlindMode = ColorBlindMode.NONE,
                    onToggleTheme = {},
                    onBecomeCollector = {},
                    onLeaveCollector = {},
                    onCloseDrawer = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Cambiar al rol de coleccionista")
            .assertExists()
    }
}
