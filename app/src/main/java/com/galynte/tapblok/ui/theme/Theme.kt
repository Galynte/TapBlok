package com.galynte.tapblok.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealOnContainerDark,
    secondary = SlateContainer,
    onSecondary = SlateOnContainer,
    secondaryContainer = SlateSecondary,
    onSecondaryContainer = SlateContainer,
    tertiary = AmberContainer,
    onTertiary = AmberOnContainer,
    tertiaryContainer = AmberTertiary,
    onTertiaryContainer = AmberContainer,
    background = Night,
    onBackground = NightInk,
    surface = Night,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightOnSurfaceVariant,
    outline = NightOnSurfaceVariant,
    error = BlockedOnContainerDark,
    onError = BlockedContainerDark,
    errorContainer = BlockedContainerDark,
    onErrorContainer = BlockedOnContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = SlateSecondary,
    onSecondary = SlateOnSecondary,
    secondaryContainer = SlateContainer,
    onSecondaryContainer = SlateOnContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberOnContainer,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    error = Blocked,
    onError = TealOnPrimary,
    errorContainer = BlockedContainer,
    onErrorContainer = BlockedOnContainer
)

@Composable
fun TapOutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Fixed brand colors keep this fork visually distinct from the upstream
    // project and from whatever wallpaper palette the device happens to use.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
