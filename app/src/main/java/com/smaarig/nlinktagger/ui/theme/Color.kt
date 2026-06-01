package com.smaarig.nlinktagger.ui.theme

import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Grey = Color(0xFF757575)
val LightGrey = Color(0xFFE0E0E0)
val DarkGrey = Color(0xFF121212)
val FunkyRed = Color(0xFFFF3D00)
val FunkyBlue = Color(0xFF2979FF)
val FunkyYellow = Color(0xFFFFEA00)
val FunkyGreen = Color(0xFF00E676)
val FunkyWhite = Color(0xFFF5F5F5)
val FunkyBlack = Color(0xFF212121)

fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
