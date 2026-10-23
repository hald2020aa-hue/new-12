package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppColorTheme

data class MangaZzCustomColors(
    val primary: Color,
    val secondary: Color,
    val glow: Color,
    val cardBackground: Color = BgCardDark,
    val cardGlass: Color = BgCardGlass,
    val border: Color,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary
)

val LocalMangaZzColors = staticCompositionLocalOf {
    MangaZzCustomColors(
        primary = ThemeBlueSecondary,
        secondary = ThemeBluePrimary,
        glow = ThemeBlueGlow,
        border = BorderDark
    )
}

@Composable
fun MangaZzTheme(
    selectedTheme: AppColorTheme = AppColorTheme.BLUE,
    content: @Composable () -> Unit
) {
    val (targetPrimary, targetSecondary, targetGlow) = when (selectedTheme) {
        AppColorTheme.BLUE -> Triple(ThemeBlueSecondary, ThemeBluePrimary, ThemeBlueGlow)
        AppColorTheme.RED -> Triple(ThemeRedPrimary, ThemeRedSecondary, ThemeRedGlow)
        AppColorTheme.GREEN -> Triple(ThemeGreenPrimary, ThemeGreenSecondary, ThemeGreenGlow)
        AppColorTheme.PURPLE -> Triple(ThemePurplePrimary, ThemePurpleSecondary, ThemePurpleGlow)
        AppColorTheme.GOLD -> Triple(ThemeGoldPrimary, ThemeGoldSecondary, ThemeGoldGlow)
    }

    // Smooth color animation when switching theme
    val animatedPrimary by animateColorAsState(targetValue = targetPrimary, animationSpec = tween(300), label = "primary")
    val animatedSecondary by animateColorAsState(targetValue = targetSecondary, animationSpec = tween(300), label = "secondary")
    val animatedGlow by animateColorAsState(targetValue = targetGlow, animationSpec = tween(300), label = "glow")

    val customColors = MangaZzCustomColors(
        primary = animatedPrimary,
        secondary = animatedSecondary,
        glow = animatedGlow,
        border = animatedPrimary.copy(alpha = 0.35f),
        cardBackground = BgCardDark,
        cardGlass = BgCardGlass,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary
    )

    val colorScheme = darkColorScheme(
        primary = animatedPrimary,
        secondary = animatedSecondary,
        background = BgDark,
        surface = BgCardDark,
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )

    CompositionLocalProvider(
        LocalMangaZzColors provides customColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
