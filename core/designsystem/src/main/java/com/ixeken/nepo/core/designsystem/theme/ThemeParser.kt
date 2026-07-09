package com.ixeken.nepo.core.designsystem.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.ixeken.nepo.core.designsystem.models.*
import java.io.InputStreamReader

/**
 * Utility parser that reads theme definitions from JSON assets and compiles
 * them into strongly typed [NepoThemeStyles] instances.
 *
 * Supports both camelCase and snake_case naming structures dynamically.
 */
object ThemeParser {
    private data class JsonThemeMetadata(
        val id: String,
        @SerializedName(value = "display_name", alternate = ["displayName"])
        val display_name: String,
        @SerializedName(value = "icons_library", alternate = ["iconLibrary"])
        val icons_library: String,
        @SerializedName(value = "border_radius_global", alternate = ["borderRadiusGlobal"])
        val border_radius_global: String
    )
    
    private data class JsonSurfaceColors(
        @SerializedName(value = "app_background", alternate = ["appBackground"])
        val app_background: String,
        @SerializedName(value = "bottom_sheet_background", alternate = ["bottomSheetBackground"])
        val bottom_sheet_background: String,
        @SerializedName(value = "calculator_screen_background", alternate = ["calculatorScreenBackground"])
        val calculator_screen_background: String
    )
    
    private data class JsonTypographyColors(
        val title: String,
        @SerializedName(value = "header_accent", alternate = ["headerAccent"])
        val header_accent: String,
        @SerializedName(value = "body_primary", alternate = ["bodyPrimary"])
        val body_primary: String,
        @SerializedName(value = "body_secondary", alternate = ["bodySecondary"])
        val body_secondary: String,
        @SerializedName(value = "screen_primary", alternate = ["screenPrimary"])
        val screen_primary: String,
        @SerializedName(value = "screen_secondary", alternate = ["screenSecondary"])
        val screen_secondary: String
    )
    
    private data class JsonComponentTokens(
        val icon: String?,
        val text: String?,
        val foreground: String?,
        val background: String
    )
    
    private data class JsonInteractiveComponents(
        @SerializedName(value = "done_button", alternate = ["doneButton"])
        val done_button: JsonComponentTokens?,
        @SerializedName(value = "close_button", alternate = ["closeButton"])
        val close_button: JsonComponentTokens?,
        @SerializedName(value = "back_button", alternate = ["backButton"])
        val back_button: JsonComponentTokens?,
        @SerializedName(value = "confirm_button", alternate = ["confirmButton"])
        val confirm_button: JsonComponentTokens?,
        @SerializedName(value = "select_button", alternate = ["selectButton"])
        val select_button: JsonComponentTokens?,
        @SerializedName(value = "operator_button", alternate = ["operatorButton"])
        val operator_button: JsonComponentTokens?,
        @SerializedName(value = "numbers_button", alternate = ["numbersButton"])
        val numbers_button: JsonComponentTokens?,
        @SerializedName(value = "delete_button", alternate = ["deleteButton"])
        val delete_button: JsonComponentTokens?
    )
    
    private data class JsonStructuralElements(
        @SerializedName(value = "header_separator", alternate = ["headerSeparator"])
        val header_separator: String,
        @SerializedName(value = "item_separator", alternate = ["itemSeparator"])
        val item_separator: String
    )
    
    private data class JsonThemeColors(
        val surfaces: JsonSurfaceColors,
        val typography: JsonTypographyColors,
        @SerializedName(value = "interactive_components", alternate = ["interactiveComponents"])
        val interactive_components: JsonInteractiveComponents,
        @SerializedName(value = "structural_elements", alternate = ["structuralElements"])
        val structural_elements: JsonStructuralElements
    )
    
    private data class JsonSettingsStyle(
        @SerializedName(value = "use_cards", alternate = ["useCards"])
        val use_cards: Boolean?,
        @SerializedName(value = "card_border_radius", alternate = ["cardBorderRadius"])
        val card_border_radius: String?,
        @SerializedName(value = "show_menu_icons", alternate = ["showMenuIcons"])
        val show_menu_icons: Boolean?,
        @SerializedName(value = "show_dividers", alternate = ["showDividers"])
        val show_dividers: Boolean?,
        @SerializedName(value = "show_header_separators", alternate = ["showHeaderSeparators"])
        val show_header_separators: Boolean?
    )

    private data class JsonThemeStyles(
        val metadata: JsonThemeMetadata,
        @SerializedName(value = "structure_style", alternate = ["structureStyle"])
        val structure_style: String?,
        @SerializedName(value = "settings_style", alternate = ["settingsStyle"])
        val settings_style: JsonSettingsStyle?,
        val colors: JsonThemeColors
    )

    private fun parseColor(hex: String): Color {
        return Color(android.graphics.Color.parseColor(hex))
    }

    private fun parseDp(dpStr: String): androidx.compose.ui.unit.Dp {
        val numeric = dpStr.replace("px", "").replace("dp", "").trim()
        return numeric.toIntOrNull()?.dp ?: numeric.toFloatOrNull()?.toInt()?.dp ?: 0.dp
    }

    /**
     * Deserializes a theme JSON configuration file from assets into [NepoThemeStyles].
     *
     * @param context Android context to access assets directory.
     * @param assetPath The file name/path of the JSON inside the assets folder.
     * @return Formatted [NepoThemeStyles] configuration.
     */
    fun parseTheme(context: Context, assetPath: String): NepoThemeStyles {
        val inputStream = context.assets.open(assetPath)
        val reader = InputStreamReader(inputStream)
        val jsonStyles = Gson().fromJson(reader, JsonThemeStyles::class.java)
        
        val metadata = ThemeMetadata(
            id = jsonStyles.metadata.id,
            displayName = jsonStyles.metadata.display_name,
            iconLibrary = if (jsonStyles.metadata.icons_library.contains("LUCIDE", ignoreCase = true)) {
                IconLibrary.LUCIDE_ICONS
            } else {
                IconLibrary.MATERIAL_ICONS
            },
            borderRadiusGlobal = try {
                parseDp(jsonStyles.metadata.border_radius_global)
            } catch (e: Exception) {
                16.dp
            }
        )
        
        val surfaces = SurfaceColors(
            appBackground = parseColor(jsonStyles.colors.surfaces.app_background),
            bottomSheetBackground = parseColor(jsonStyles.colors.surfaces.bottom_sheet_background),
            calculatorScreenBackground = parseColor(jsonStyles.colors.surfaces.calculator_screen_background)
        )
        
        val typography = TypographyColors(
            title = parseColor(jsonStyles.colors.typography.title),
            headerAccent = parseColor(jsonStyles.colors.typography.header_accent),
            bodyPrimary = parseColor(jsonStyles.colors.typography.body_primary),
            bodySecondary = parseColor(jsonStyles.colors.typography.body_secondary),
            screenPrimary = parseColor(jsonStyles.colors.typography.screen_primary),
            screenSecondary = parseColor(jsonStyles.colors.typography.screen_secondary)
        )
        
        val numbersBtn = jsonStyles.colors.interactive_components.numbers_button!!
        val operatorBtn = jsonStyles.colors.interactive_components.operator_button!!
        val confirmBtn = jsonStyles.colors.interactive_components.confirm_button!!
        val deleteBtn = jsonStyles.colors.interactive_components.delete_button!!
        
        fun resolveToken(btn: JsonComponentTokens?, defaultFallback: JsonComponentTokens): ComponentVisualTokens {
            val target = btn ?: defaultFallback
            val fg = target.foreground ?: target.icon ?: target.text ?: "#FFFFFF"
            return ComponentVisualTokens(
                foreground = parseColor(fg),
                background = parseColor(target.background)
            )
        }

        val interactiveComponents = InteractiveComponents(
            doneButton = resolveToken(jsonStyles.colors.interactive_components.done_button, confirmBtn),
            closeButton = resolveToken(jsonStyles.colors.interactive_components.close_button, operatorBtn),
            backButton = resolveToken(jsonStyles.colors.interactive_components.back_button, operatorBtn),
            confirmButton = resolveToken(jsonStyles.colors.interactive_components.confirm_button, confirmBtn),
            selectButton = resolveToken(jsonStyles.colors.interactive_components.select_button, confirmBtn),
            operatorButton = resolveToken(jsonStyles.colors.interactive_components.operator_button, operatorBtn),
            numbersButton = resolveToken(jsonStyles.colors.interactive_components.numbers_button, numbersBtn),
            deleteButton = resolveToken(jsonStyles.colors.interactive_components.delete_button, deleteBtn)
        )
        
        val structuralElements = StructuralElements(
            headerSeparator = parseColor(jsonStyles.colors.structural_elements.header_separator),
            itemSeparator = parseColor(jsonStyles.colors.structural_elements.item_separator)
        )
        
        val colors = NepoThemeColors(
            surfaces = surfaces,
            typography = typography,
            interactiveComponents = interactiveComponents,
            structuralElements = structuralElements
        )
        
        val structureStyle = if (jsonStyles.structure_style?.equals("GLASSMORPHISM", ignoreCase = true) == true) {
            StructureStyleType.GLASSMORPHISM
        } else {
            StructureStyleType.FLAT_RUSTIC
        }
        
        val jsonSettingsStyle = jsonStyles.settings_style
        val settingsStyle = if (jsonSettingsStyle != null) {
            SettingsStyle(
                useCards = jsonSettingsStyle.use_cards ?: true,
                cardBorderRadius = try {
                    jsonSettingsStyle.card_border_radius?.let { parseDp(it) } ?: 16.dp
                } catch (e: Exception) {
                    16.dp
                },
                showMenuIcons = jsonSettingsStyle.show_menu_icons ?: true,
                showDividers = jsonSettingsStyle.show_dividers ?: false,
                showHeaderSeparators = jsonSettingsStyle.show_header_separators ?: false
            )
        } else {
            SettingsStyle()
        }
        
        return NepoThemeStyles(
            metadata = metadata,
            colors = colors,
            structureStyle = structureStyle,
            settingsStyle = settingsStyle
        )
    }
}
