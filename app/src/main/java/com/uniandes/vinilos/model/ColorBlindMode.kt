package com.uniandes.vinilos.model

/**
 * Active color-vision adaptation mode. NONE preserves the original Vinilos
 * palette; the other three switch to a daltonism-friendly palette tuned for
 * each common form of color blindness.
 */
enum class ColorBlindMode {
    NONE,
    DEUTERANOPIA,
    PROTANOPIA,
    TRITANOPIA
}
