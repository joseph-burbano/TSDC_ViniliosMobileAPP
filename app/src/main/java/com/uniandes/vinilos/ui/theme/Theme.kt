package com.uniandes.vinilos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.uniandes.vinilos.model.ColorBlindMode

private val DarkColorScheme = darkColorScheme(
    primary = RedAccent,
    secondary = DarkGrayText,
    tertiary = GrayMedium,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = WhiteSurface,
    onBackground = WhiteSurface,
    onSurface = WhiteSurface
)

private val LightColorScheme = lightColorScheme(
    primary = RedAccent,
    secondary = GrayMedium,
    tertiary = GrayLight,
    background = Cream,
    surface = WhiteSurface,
    onPrimary = WhiteSurface,
    onBackground = BlackPrimary,
    onSurface = BlackPrimary
)

// Okabe-Ito-inspired palettes swap red-green hues for CVD-safe blues and
// vermillion. The light scheme leans on deep blue for primary so it stays
// distinguishable from any accidental greens in the rest of the UI.
private val LightDaltonicColorScheme = lightColorScheme(
    primary = DaltonicBlue,
    secondary = DaltonicVermillion,
    tertiary = DaltonicSkyBlue,
    background = Cream,
    surface = WhiteSurface,
    onPrimary = WhiteSurface,
    onSecondary = WhiteSurface,
    onBackground = BlackPrimary,
    onSurface = BlackPrimary,
    error = DaltonicError,
    onError = WhiteSurface
)

private val DarkDaltonicColorScheme = darkColorScheme(
    primary = DaltonicSkyBlue,
    secondary = DaltonicOrange,
    tertiary = DaltonicVermillion,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = BlackPrimary,
    onSecondary = BlackPrimary,
    onBackground = WhiteSurface,
    onSurface = WhiteSurface,
    error = DaltonicError,
    onError = BlackPrimary
)

@Composable
fun VinilosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    dynamicColor: Boolean = false,  // ← desactivado
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        colorBlindMode != ColorBlindMode.NONE && darkTheme -> DarkDaltonicColorScheme
        colorBlindMode != ColorBlindMode.NONE -> LightDaltonicColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
