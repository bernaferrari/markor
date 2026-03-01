package com.bernaferrari.remarkor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme as kolorDynamicColorScheme
import kotlinx.coroutines.flow.Flow

enum class ThemePaletteOption(val token: String, val seedColor: Color?) {
    DYNAMIC("markor", null),
    BLUE("blue", BlueSeedColor),
    RED("red", RedSeedColor),
    ORANGE("orange", OrangeSeedColor),
    PURPLE("purple", PurpleSeedColor),
    GREEN("green", GreenSeedColor),
    AMBER("amber", AmberSeedColor),
    TEAL("teal", TealSeedColor),
    PINK("pink", PinkSeedColor),
    INDIGO("indigo", IndigoSeedColor),
    LIME("lime", LimeSeedColor);

    companion object {
        fun fromToken(token: String): ThemePaletteOption? = when (token) {
            "markor", "default" -> DYNAMIC
            "blue" -> BLUE
            "red" -> RED
            "orange" -> ORANGE
            "purple" -> PURPLE
            "green" -> GREEN
            "amber" -> AMBER
            "teal", "cyan" -> TEAL
            "pink" -> PINK
            "indigo" -> INDIGO
            "lime" -> LIME
            else -> null
        }
    }
}

/**
 * Theme mode options.
 */
enum class ThemeMode {
    SYSTEM,  // Follow system setting
    LIGHT,   // Always light
    DARK;    // Always dark
    
    companion object {
        fun fromString(value: String): ThemeMode {
            return when (value.lowercase()) {
                "light" -> LIGHT
                "dark" -> DARK
                else -> SYSTEM
            }
        }
    }
}

private data class ThemeSelection(
    val palette: ThemePaletteOption,
    val mode: ThemeMode
)

private fun parseThemeSelection(rawValue: String): ThemeSelection {
    if (rawValue.isBlank()) return ThemeSelection(ThemePaletteOption.DYNAMIC, ThemeMode.SYSTEM)

    var palette = ThemePaletteOption.DYNAMIC
    var mode = ThemeMode.SYSTEM
    val tokens = rawValue
        .trim()
        .lowercase()
        .split(Regex("[\\s_\\-|]+"))
        .filter { it.isNotBlank() }

    tokens.forEach { token ->
        ThemePaletteOption.fromToken(token)?.let {
            palette = it
        }
        when (token) {
            "light" -> mode = ThemeMode.LIGHT
            "dark" -> mode = ThemeMode.DARK
            "system", "auto" -> mode = ThemeMode.SYSTEM
        }
    }

    return ThemeSelection(palette = palette, mode = mode)
}

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim
)

private val BlueSeedColor = Color(0xFF0D73F6)
private val RedSeedColor = Color(0xFFDF5353)
private val OrangeSeedColor = Color(0xFFED7F2A)
private val PurpleSeedColor = Color(0xFF8E4BFF)
private val GreenSeedColor = Color(0xFF30B67A)
private val AmberSeedColor = Color(0xFFFFB84D)
private val TealSeedColor = Color(0xFF17A6A6)
private val PinkSeedColor = Color(0xFFEE5588)
private val IndigoSeedColor = Color(0xFF5C6BC0)
private val LimeSeedColor = Color(0xFF9CBF37)

/**
 * Markor Material 3 Expressive Theme with dynamic color and motion support.
 *
 * Uses MaterialExpressiveTheme with expressive MotionScheme for enhanced
 * spring-based animations and micro-interactions following M3 Expressive guidelines.
 *
 * @param darkTheme Whether to use dark theme colors (overrides themeMode if provided)
 * @param themeMode Flow of the theme mode setting (System, Light, Dark)
 * @param dynamicColor Whether to use dynamic colors from the system wallpaper (Android 12+)
 * @param content The composable content to theme
 */
@Composable
fun MarkorTheme(
    darkTheme: Boolean? = null,
    appTheme: Flow<String>? = null,
    themeMode: Flow<String>? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val appThemeValue = appTheme?.collectAsState(initial = "markor")?.value
    val selection = appThemeValue?.let(::parseThemeSelection)

    // Determine dark theme based on settings or parameter
    val effectiveDarkTheme = when {
        darkTheme != null -> darkTheme
        selection != null -> {
            when (selection.mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDarkTheme
            }
        }
        themeMode != null -> {
            val mode by themeMode.collectAsState(initial = "system")
            when (ThemeMode.fromString(mode)) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDarkTheme
            }
        }
        else -> systemDarkTheme
    }

    val palette = selection?.palette ?: ThemePaletteOption.DYNAMIC
    val kolorSeed = when (palette) {
        ThemePaletteOption.DYNAMIC -> null
        else -> palette.seedColor
    }

    val staticColorScheme = when (palette) {
        ThemePaletteOption.DYNAMIC -> if (effectiveDarkTheme) DarkColorScheme else LightColorScheme
        else -> requireNotNull(kolorSeed).let { seed ->
            kolorDynamicColorScheme(
                seedColor = seed,
                isDark = effectiveDarkTheme
            )
        }
    }

    // Dynamic color should not override explicit accent palettes.
    val dynamicScheme = if (dynamicColor && palette == ThemePaletteOption.DYNAMIC) {
        dynamicColorScheme(effectiveDarkTheme)
    } else {
        null
    }
    val colorScheme = dynamicScheme ?: staticColorScheme

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalElevation provides Elevation()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object MarkorTheme {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val elevation: Elevation
        @Composable
        @ReadOnlyComposable
        get() = LocalElevation.current
}
