package com.smaarig.nlinktagger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FunkyColorScheme = lightColorScheme(
    primary = FunkyBlue,
    onPrimary = FunkyWhite,
    secondary = FunkyRed,
    onSecondary = FunkyWhite,
    tertiary = FunkyYellow,
    onTertiary = FunkyBlack,
    background = FunkyWhite,
    onBackground = FunkyBlack,
    surface = FunkyGreen,
    onSurface = FunkyBlack,
    outline = FunkyBlack
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Grey,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = DarkGrey,
    onSurfaceVariant = White,
    outline = Grey
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Grey,
    onSecondary = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = LightGrey,
    onSurfaceVariant = Black,
    outline = Grey
)

@Composable
fun NLinkTaggerTheme(
    appTheme: AppTheme = AppTheme.MINIMAL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.MINIMAL -> if (darkTheme) DarkColorScheme else LightColorScheme
        AppTheme.FUNKY -> FunkyColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = if (appTheme == AppTheme.FUNKY) FunkyTypography else Typography,
        content = content
    )
}
