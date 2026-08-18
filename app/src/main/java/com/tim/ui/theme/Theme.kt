package com.tim.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCatOrange,
    onPrimary = OnPrimaryOrange,
    primaryContainer = PrimaryContainerOrange,
    onPrimaryContainer = Color(0xFF6E2D00),
    secondary = SecondaryMint,
    onSecondary = OnSecondaryMint,
    secondaryContainer = SecondaryContainerMint,
    onSecondaryContainer = Color(0xFF003731),
    tertiary = TertiaryAmber,
    tertiaryContainer = TertiaryContainerAmber,
    background = BackgroundWarmCream,
    surface = SurfaceWarmWhite,
    surfaceVariant = SurfaceVariantWarm,
    onBackground = Color(0xFF231A14),
    onSurface = Color(0xFF231A14)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryCatOrange,
    onPrimary = Color(0xFF4A1A00),
    primaryContainer = Color(0xFF6E2D00),
    onPrimaryContainer = PrimaryContainerOrange,
    secondary = SecondaryMint,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFEDE0D8),
    onSurface = Color(0xFFEDE0D8)
)

@Composable
fun CatCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use custom cheerful theme by default
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
