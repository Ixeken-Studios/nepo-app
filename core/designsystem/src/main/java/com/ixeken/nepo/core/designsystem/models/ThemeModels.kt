package com.ixeken.nepo.core.designsystem.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Libraries available for icons in the application themes.
 */
enum class IconLibrary {
    /** Lucide based icons. */
    LUCIDE_ICONS,
    /** Material Design based icons. */
    MATERIAL_ICONS
}

/**
 * Visual structural style types for the calculator design.
 */
enum class StructureStyleType {
    /** A flat, rustic theme look without gradients or transparency. */
    FLAT_RUSTIC,
    /** A modern glassmorphic look with semi-transparent blurs. */
    GLASSMORPHISM
}

/**
 * Metadata associated with a specific theme configuration.
 *
 * @property id Unique identifier of the theme.
 * @property displayName Human-readable name of the theme.
 * @property iconLibrary The icon library to use with this theme.
 * @property borderRadiusGlobal Global corner radius applied to components in this theme.
 */
data class ThemeMetadata(
    val id: String,
    val displayName: String,
    val iconLibrary: IconLibrary,
    val borderRadiusGlobal: Dp
)

/**
 * Color definitions for surfaces in the UI.
 *
 * @property appBackground Overall background color of the application screen.
 * @property bottomSheetBackground Background color of sheet/modal surfaces.
 * @property calculatorScreenBackground Background color of the calculator display screen.
 */
data class SurfaceColors(
    val appBackground: Color,
    val bottomSheetBackground: Color,
    val calculatorScreenBackground: Color
)

/**
 * Color definitions for typography elements.
 *
 * @property title Color for main screens or application headers.
 * @property headerAccent Color for highlighted header elements.
 * @property bodyPrimary Primary color for body text.
 * @property bodySecondary Secondary/subsidiary color for body text.
 * @property screenPrimary Primary color for text displayed on the calculator screen.
 * @property screenSecondary Secondary/helper color for text displayed on the calculator screen.
 */
data class TypographyColors(
    val title: Color,
    val headerAccent: Color,
    val bodyPrimary: Color,
    val bodySecondary: Color,
    val screenPrimary: Color,
    val screenSecondary: Color
)

/**
 * Reusable visual tokens for individual component states.
 *
 * @property foreground Content/Text color of the component.
 * @property background Fill/Background color of the component.
 */
data class ComponentVisualTokens(
    val foreground: Color,
    val background: Color
)

/**
 * Visual tokens for various interactive components like buttons.
 *
 * @property doneButton Tokens for the 'done' operation button.
 * @property closeButton Tokens for close/dismiss actions.
 * @property backButton Tokens for back navigation or delete actions.
 * @property confirmButton Tokens for confirming settings.
 * @property selectButton Tokens for selecting a theme or option.
 * @property operatorButton Tokens for standard arithmetic operator buttons.
 * @property numbersButton Tokens for numeric input buttons.
 * @property deleteButton Tokens for deletion or clear actions.
 */
data class InteractiveComponents(
    val doneButton: ComponentVisualTokens,
    val closeButton: ComponentVisualTokens,
    val backButton: ComponentVisualTokens,
    val confirmButton: ComponentVisualTokens,
    val selectButton: ComponentVisualTokens,
    val operatorButton: ComponentVisualTokens,
    val numbersButton: ComponentVisualTokens,
    val deleteButton: ComponentVisualTokens
)

/**
 * Colors for structural or decorative layout dividers.
 *
 * @property headerSeparator Divider color below headers.
 * @property itemSeparator Divider color between list/grid items.
 */
data class StructuralElements(
    val headerSeparator: Color,
    val itemSeparator: Color
)

/**
 * Complete set of colors defined in a theme.
 *
 * @property surfaces Core background and container surface colors.
 * @property typography Text and label colors.
 * @property interactiveComponents Button and control element colors.
 * @property structuralElements Divider and separator lines.
 */
data class NepoThemeColors(
    val surfaces: SurfaceColors,
    val typography: TypographyColors,
    val interactiveComponents: InteractiveComponents,
    val structuralElements: StructuralElements
)

data class SettingsStyle(
    val useCards: Boolean = true,
    val cardBorderRadius: Dp = 16.dp,
    val showMenuIcons: Boolean = true,
    val showDividers: Boolean = false,
    val showHeaderSeparators: Boolean = false
)

/**
 * Root theme styles containing both metadata, colors and structural look.
 *
 * Represents the final configuration of a dynamic theme.
 *
 * @property metadata General info and configuration settings.
 * @property colors All functional design colors.
 * @property structureStyle Visual layout style type (e.g. flat vs. glassmorphism).
 * @property settingsStyle Structural look and feel parameters for settings screens.
 */
data class NepoThemeStyles(
    val metadata: ThemeMetadata,
    val colors: NepoThemeColors,
    val structureStyle: StructureStyleType,
    val settingsStyle: SettingsStyle = SettingsStyle()
)
