package com.sinc.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import com.sinc.mobile.ui.theme.*

private val LightColors = lightColorScheme(
    primary = SincPrimary,
    onPrimary = SincOnPrimary,
    secondary = SincPrimaryDark,
    onSecondary = SincOnPrimary,
    tertiary = SincPrimaryLight,
    onTertiary = SincOnPrimary,
    error = SincError,
    onError = SincOnPrimary,
    background = SincBackground,
    onBackground = SincTextPrimary,
    surface = SincSurface,
    onSurface = SincTextPrimary,
    outline = SincDivider,
    primaryContainer = SincPrimaryLight,
    onPrimaryContainer = SincPrimary,
    secondaryContainer = SincBackground,
    onSecondaryContainer = SincTextSecondary,
    tertiaryContainer = SincBackground,
    onTertiaryContainer = SincTextSecondary,
    surfaceVariant = SincBackground,
    onSurfaceVariant = SincTextSecondary,
)


// private val DarkColors = darkColorScheme(
//    primary = md_theme_dark_primary,
//    onPrimary = md_theme_dark_onPrimary,
//    primaryContainer = md_theme_dark_primaryContainer,
//    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
//    secondary = md_theme_dark_secondary,
//    onSecondary = md_theme_dark_onSecondary,
//    secondaryContainer = md_theme_dark_secondaryContainer,
//    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
//    tertiary = md_theme_dark_tertiary,
//    onTertiary = md_theme_dark_onTertiary,
//    tertiaryContainer = md_theme_dark_tertiaryContainer,
//    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
//    error = md_theme_dark_error,
//    onError = md_theme_dark_onError,
//    background = md_theme_dark_background,
//    onSurface = md_theme_dark_onSurface,
//    surfaceVariant = md_theme_dark_surfaceVariant,
//    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
//    outline = md_theme_dark_outline,
// )

@Composable
fun SincMobileTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // darkTheme -> DarkColors // Commented out
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val grayBackgroundColor = Color(0xFFE0E0E0) // Gris claro estándar para las barras del sistema

            // Configurar la barra de estado (superior)
            window.statusBarColor = grayBackgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true // Iconos oscuros

            // Configurar la barra de navegación (inferior)
            window.navigationBarColor = grayBackgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true // Iconos oscuros
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
