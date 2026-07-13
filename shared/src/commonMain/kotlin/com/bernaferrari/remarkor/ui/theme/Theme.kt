@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.remarkor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import kotlinx.coroutines.flow.Flow

private val BlueSeedColor = Color(0xFF0D73F6)
private val RedSeedColor = Color(0xFFDF5353)
private val OrangeSeedColor = Color(0xFFED7F2A)
private val PurpleSeedColor = Color(0xFF8E4BFF)
private val GreenSeedColor = Color(0xFF30B67A)
private val AmberSeedColor = Color(0xFFFFB84D)
private val TealSeedColor = Color(0xFF17A6A6)
private val CyanSeedColor = Color(0xFF00BCD4)
private val PinkSeedColor = Color(0xFFEE5588)
private val IndigoSeedColor = Color(0xFF5C6BC0)
private val LimeSeedColor = Color(0xFF9CBF37)

enum class ThemePaletteOption(val token: String, val seedColor: Color?) {
    DYNAMIC("markor", null),
    BLUE("blue", BlueSeedColor),
    RED("red", RedSeedColor),
    ORANGE("orange", OrangeSeedColor),
    PURPLE("purple", PurpleSeedColor),
    GREEN("green", GreenSeedColor),
    AMBER("amber", AmberSeedColor),
    TEAL("teal", TealSeedColor),
    CYAN("cyan", CyanSeedColor),
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
            "teal" -> TEAL
            "cyan" -> CYAN
            "pink" -> PINK
            "indigo" -> INDIGO
            "lime" -> LIME
            else -> null
        }
    }
}

/** Settings swatches ordered warm → cool by hue. */
val ThemeSwatchOptions: List<ThemePaletteOption> =
    listOf(
        ThemePaletteOption.DYNAMIC,
        ThemePaletteOption.RED,
        ThemePaletteOption.ORANGE,
        ThemePaletteOption.AMBER,
        ThemePaletteOption.LIME,
        ThemePaletteOption.GREEN,
        ThemePaletteOption.TEAL,
        ThemePaletteOption.CYAN,
        ThemePaletteOption.BLUE,
        ThemePaletteOption.INDIGO,
        ThemePaletteOption.PURPLE,
        ThemePaletteOption.PINK,
    )

/** Swatches visible on the current platform (dynamic is Android-only). */
fun themeSwatchOptions(): List<ThemePaletteOption> =
    if (supportsDynamicTheme()) {
        ThemeSwatchOptions
    } else {
        ThemeSwatchOptions.filter { it != ThemePaletteOption.DYNAMIC }
    }

fun resolveThemePalette(palette: ThemePaletteOption): ThemePaletteOption =
    if (palette == ThemePaletteOption.DYNAMIC && !supportsDynamicTheme()) {
        ThemePaletteOption.TEAL
    } else {
        palette
    }

/**
 * Theme mode options.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

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
    val mode: ThemeMode,
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

    return ThemeSelection(
        palette = resolveThemePalette(palette),
        mode = mode,
    )
}

private fun seedColorFor(palette: ThemePaletteOption): Color =
    when (palette) {
        ThemePaletteOption.DYNAMIC -> Seed
        else -> requireNotNull(palette.seedColor)
    }

@Composable
fun MarkorTheme(
    darkTheme: Boolean? = null,
    appTheme: Flow<String>? = null,
    themeMode: Flow<String>? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val appThemeValue = appTheme?.collectAsState(initial = "markor")?.value
    val selection = appThemeValue?.let(::parseThemeSelection)

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

    val palette = resolveThemePalette(selection?.palette ?: ThemePaletteOption.DYNAMIC)
    val wallpaperScheme =
        if (dynamicColor && palette == ThemePaletteOption.DYNAMIC && supportsDynamicTheme()) {
            dynamicColorScheme(effectiveDarkTheme)
        } else {
            null
        }

    val colorScheme =
        wallpaperScheme ?: dynamicColorScheme(
            seedColor = seedColorFor(palette),
            isDark = effectiveDarkTheme,
            style = PaletteStyle.TonalSpot,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
        )

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalElevation provides Elevation(),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
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