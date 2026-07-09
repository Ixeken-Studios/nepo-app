package com.ixeken.nepo.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import com.ixeken.nepo.core.designsystem.models.NepoThemeStyles

/**
 * CompositionLocal used to propagate the current [NepoThemeStyles] down the Compose tree.
 *
 * This provides static access to UI tokens and styles. It throws an error if accessed
 * without an active [NepoTheme] provider wrapper.
 */
val LocalNepoTheme = staticCompositionLocalOf<NepoThemeStyles> {
    error("Inyección fallida: No se ha detectado ninguna configuración de NepoTheme activa.")
}

/**
 * CompositionLocal used to propagate the active [FontFamily] (e.g. Google Fonts) down the Compose tree.
 */
val LocalNepoFontFamily = staticCompositionLocalOf<FontFamily> {
    FontFamily.Default
}

/**
 * Theme provider wrapper that exposes the provided [currentStyles] to nested Composable content.
 *
 * Use this wrapper at the root of your layout hierarchy to enable dynamic styling.
 *
 * @param currentStyles The active [NepoThemeStyles] definition to inject.
 * @param fontFamily The active [FontFamily] definition to inject.
 * @param content The Composable children hierarchy that will consume the theme styles.
 */
@Composable
fun NepoTheme(
    currentStyles: NepoThemeStyles,
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalNepoTheme provides currentStyles,
        LocalNepoFontFamily provides fontFamily,
        content = content
    )
}
