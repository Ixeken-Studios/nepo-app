package com.ixeken.nepo

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.core.designsystem.theme.NepoTheme
import com.ixeken.nepo.core.designsystem.theme.ThemeParser
import com.ixeken.nepo.core.mathematics.di.MathModule
import com.ixeken.nepo.features.calculator.data.SettingsRepository
import com.ixeken.nepo.features.calculator.presentation.CalculatorViewModel
import com.ixeken.nepo.features.calculator.ui.CalculatorScreen
import com.ixeken.nepo.features.calculator.ui.SettingsScreen
import com.ixeken.nepo.features.calculator.ui.checkGitHubUpdate
import com.ixeken.nepo.features.calculator.ui.UpdateResult
import com.ixeken.nepo.features.calculator.ui.NepoUpdateDialog
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.ixeken.nepo.features.calculator.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun getGoogleFontFamily(name: String): FontFamily {
    if (name == "System default") {
        return FontFamily.Default
    }
    return try {
        FontFamily(
            Font(
                googleFont = GoogleFont(name),
                fontProvider = fontProvider
            )
        )
    } catch (e: Exception) {
        FontFamily.Monospace
    }
}

/**
 * Main entrance activity of the Nepo calculation platform.
 *
 * Implements a bottom navigation layout displaying the themed Calculator
 * and Unit Converter screens side-by-side using the shared [NepoTheme].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mathEngine = MathModule.provideMathEngine()
        val settingsRepository = SettingsRepository(this)

        setContent {
            val context = LocalContext.current
            var themeUpdateTrigger by remember { mutableStateOf(0) }

            // Listen to theme or font preference changes and force UI recomposition
            DisposableEffect(settingsRepository) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "active_theme_id" || key == "active_font_name") {
                        themeUpdateTrigger++
                    }
                }
                settingsRepository.registerListener(listener)
                onDispose {
                    settingsRepository.unregisterListener(listener)
                }
            }

            val activeTheme = remember(themeUpdateTrigger) {
                try {
                    val themeId = settingsRepository.getThemeId()
                    val themeFile = if (themeId == "glassy_premium") "themes/glassy.json" else "themes/rustic_digital.json"
                    ThemeParser.parseTheme(context, themeFile)
                } catch (e: Exception) {
                    try {
                        ThemeParser.parseTheme(context, "themes/rustic_digital.json")
                    } catch (ex: Exception) {
                        // Construct a minimal fallback style in memory if asset loading fails completely
                        com.ixeken.nepo.core.designsystem.models.NepoThemeStyles(
                            metadata = com.ixeken.nepo.core.designsystem.models.ThemeMetadata(
                                id = "fallback",
                                displayName = "Fallback",
                                iconLibrary = com.ixeken.nepo.core.designsystem.models.IconLibrary.LUCIDE_ICONS,
                                borderRadiusGlobal = 16.dp
                            ),
                            colors = com.ixeken.nepo.core.designsystem.models.NepoThemeColors(
                                surfaces = com.ixeken.nepo.core.designsystem.models.SurfaceColors(
                                    appBackground = androidx.compose.ui.graphics.Color(0xFF0F1016),
                                    bottomSheetBackground = androidx.compose.ui.graphics.Color(0xFF1A1B23),
                                    calculatorScreenBackground = androidx.compose.ui.graphics.Color(0xFF15FFFFFF)
                                ),
                                typography = com.ixeken.nepo.core.designsystem.models.TypographyColors(
                                    title = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                                    headerAccent = androidx.compose.ui.graphics.Color(0xFF8AB4F8),
                                    bodyPrimary = androidx.compose.ui.graphics.Color(0xFFE3E2E6),
                                    bodySecondary = androidx.compose.ui.graphics.Color(0xFF919094),
                                    screenPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                                    screenSecondary = androidx.compose.ui.graphics.Color(0xAAFFFFFF)
                                ),
                                interactiveComponents = com.ixeken.nepo.core.designsystem.models.InteractiveComponents(
                                    doneButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFF000000), androidx.compose.ui.graphics.Color(0xFF8AB4F8)),
                                    closeButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFFFFFFFF), androidx.compose.ui.graphics.Color(0xFF208AB4F8)),
                                    backButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFFFFFFFF), androidx.compose.ui.graphics.Color(0xFF208AB4F8)),
                                    confirmButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFF000000), androidx.compose.ui.graphics.Color(0xFF8AB4F8)),
                                    selectButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFF000000), androidx.compose.ui.graphics.Color(0xFF8AB4F8)),
                                    operatorButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFF8AB4F8), androidx.compose.ui.graphics.Color(0xFF208AB4F8)),
                                    numbersButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFFFFFFFF), androidx.compose.ui.graphics.Color(0x0AFFFFFF)),
                                    deleteButton = com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(androidx.compose.ui.graphics.Color(0xFFE3E2E6), androidx.compose.ui.graphics.Color(0x1AFFFFFF))
                                ),
                                structuralElements = com.ixeken.nepo.core.designsystem.models.StructuralElements(
                                    headerSeparator = androidx.compose.ui.graphics.Color(0x25FFFFFF),
                                    itemSeparator = androidx.compose.ui.graphics.Color(0x15FFFFFF)
                                )
                            ),
                            structureStyle = com.ixeken.nepo.core.designsystem.models.StructureStyleType.FLAT_RUSTIC
                        )
                    }
                }
            }

            val activeFontFamily = remember(themeUpdateTrigger) {
                val fontName = settingsRepository.getFontName()
                getGoogleFontFamily(fontName)
            }

            NepoTheme(currentStyles = activeTheme, fontFamily = activeFontFamily) {
                val calcViewModel = remember { CalculatorViewModel(mathEngine) }
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.CALCULATOR) }

                var showStartUpdateDialog by remember { mutableStateOf(false) }
                var startUpdateResult by remember { mutableStateOf<UpdateResult?>(null) }

                LaunchedEffect(Unit) {
                    if (settingsRepository.isCheckUpdateOnStartEnabled()) {
                        val result = checkGitHubUpdate(context)
                        if (result is UpdateResult.NewVersion) {
                            startUpdateResult = result
                            showStartUpdateDialog = true
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == AppScreen.SETTINGS || targetState == AppScreen.CONVERTER) {
                            (slideInHorizontally(animationSpec = tween(225), initialOffsetX = { it }) + fadeIn(animationSpec = tween(190)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(225), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(190)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(225), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(190)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(225), targetOffsetX = { it }) + fadeOut(animationSpec = tween(190)))
                        }
                    },
                    label = "screen_navigation"
                ) { screen ->
                    when (screen) {
                        AppScreen.CALCULATOR -> {
                            CalculatorScreen(
                                viewModel = calcViewModel,
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                onNavigateToConverter = {},
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settingsRepository = settingsRepository,
                                onClose = { currentScreen = AppScreen.CALCULATOR },
                                onThemeChanged = { themeUpdateTrigger++ },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppScreen.CONVERTER -> {
                            com.ixeken.nepo.features.converter.ui.ConverterScreen(
                                onNavigateToCalculator = { currentScreen = AppScreen.CALCULATOR },
                                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                currentMode = settingsRepository.getCalculatorMode(),
                                onModeChanged = { newMode ->
                                    settingsRepository.setCalculatorMode(newMode)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                NepoUpdateDialog(
                    showDialog = showStartUpdateDialog,
                    checkingUpdates = false,
                    updateResult = startUpdateResult,
                    onDismissRequest = { showStartUpdateDialog = false },
                    onRetryClick = {}
                )
                }
            }
        }
    }
}

private enum class AppScreen {
    CALCULATOR,
    SETTINGS,
    CONVERTER
}