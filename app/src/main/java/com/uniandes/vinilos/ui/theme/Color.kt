package com.uniandes.vinilos.ui.theme

import androidx.compose.ui.graphics.Color

// Vinilos palette
val Cream = Color(0xFFF5F0E8)
val BlackPrimary = Color(0xFF1A1A1A)
val RedAccent = Color(0xFF8B2020)
val GrayMedium = Color(0xFF6B6B6B)
val GrayLight = Color(0xFFD4D0C8)
val WhiteSurface = Color(0xFFFFFFFF)

// Dark theme
val DarkBackground = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2C2C2C)
val DarkGrayText = Color(0xFFB0AEA8)

// Daltonic palette — based on Okabe & Ito (2008) "Color Universal Design".
// These hues stay distinguishable under deuteranopia, protanopia and tritanopia,
// and replace the brand RedAccent which is the riskiest hue for red-green
// color blindness (the most common form, ~8% of men).
val DaltonicBlue       = Color(0xFF0072B2) // primary — distinguishable across all CVD types
val DaltonicVermillion = Color(0xFFD55E00) // secondary — high-contrast warm accent
val DaltonicSkyBlue    = Color(0xFF56B4E9) // tertiary / dark-mode primary
val DaltonicOrange     = Color(0xFFE69F00) // dark-mode secondary
val DaltonicError      = Color(0xFFCC79A7) // reddish-purple — clearly different from primary in CVD