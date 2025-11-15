package com.tradingapp.scalper.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00897B),
    onSecondaryContainer = Color.White,

    tertiary = Pink80,
    onTertiary = Color.Black,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color.White,

    outline = Border,
    outlineVariant = Divider,

    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    inversePrimary = PrimaryBlueDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color.Black,

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color.Black,

    tertiary = Pink40,
    onTertiary = Color.White,

    background = Color.White,
    onBackground = Color.Black,

    surface = Color(0xFFFAFAFA),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF424242),

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color.Black,

    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD)
)

@Composable
fun TradingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
