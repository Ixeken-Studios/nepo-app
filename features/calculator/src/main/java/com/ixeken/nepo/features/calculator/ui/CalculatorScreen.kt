package com.ixeken.nepo.features.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.ixeken.nepo.core.designsystem.models.StructureStyleType
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.animation.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.InterceptPlatformTextInput
import kotlinx.coroutines.awaitCancellation
import com.composables.icons.lucide.*
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.core.designsystem.theme.LocalOriginalDensity
import com.ixeken.nepo.core.designsystem.theme.NepoTheme
import com.ixeken.nepo.core.designsystem.theme.ThemeParser
import com.ixeken.nepo.features.calculator.R
import com.ixeken.nepo.features.calculator.data.HistoryEntry
import com.ixeken.nepo.features.calculator.data.HistoryRepository
import com.ixeken.nepo.features.calculator.data.SettingsRepository
import com.ixeken.nepo.features.calculator.presentation.CalculatorUiState
import com.ixeken.nepo.features.calculator.presentation.CalculatorViewModel
import com.ixeken.nepo.features.calculator.ui.components.CalculatorKeyboard
import com.ixeken.nepo.features.calculator.ui.components.HistoryBottomSheet
import com.ixeken.nepo.features.calculator.domain.UnitCategory
import com.ixeken.nepo.features.calculator.domain.ConversionUnit
import com.ixeken.nepo.features.calculator.domain.ConverterRegistry
import com.ixeken.nepo.features.calculator.ui.components.UnitSelectorRow
import com.ixeken.nepo.features.calculator.ui.components.UnitSelectionBottomSheet
import androidx.compose.ui.text.input.TextFieldValue



private fun copyToClipboard(context: android.content.Context, text: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Nepo", text)
    clipboard?.setPrimaryClip(clip)
}

/**
 * Main Calculator screen.
 *
 * Implements a reactive layout with an output screen area, a settings configuration sheet,
 * and the calculation keyboard.
 *
 * @param viewModel Presentation [CalculatorViewModel] instance.
 * @param modifier Modifier applied to the outer layout container.
 */
@Composable
fun CalculatorScreen(
    viewModel: com.ixeken.nepo.features.calculator.presentation.CalculatorViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Repositories
    val settingsRepository = remember { SettingsRepository(context) }
    val historyRepository = remember { HistoryRepository(context) }

    // Dynamic Theme state
    var themeUpdateTrigger by remember { mutableStateOf(0) }
    val defaultTheme = LocalNepoTheme.current
    val activeTheme = remember(themeUpdateTrigger) {
        try {
            val themeId = settingsRepository.getThemeId()
            val themeFile = when (themeId) {
                "glassy_premium" -> "themes/glassy.json"
                "rustic_digital" -> "themes/rustic_digital.json"
                else -> "themes/$themeId.json"
            }
            try {
                ThemeParser.parseTheme(context, themeFile)
            } catch (ex: Exception) {
                ThemeParser.parseTheme(context, "themes/rustic_digital.json")
            }
        } catch (e: Exception) {
            // Fallback in case of parse failure
            defaultTheme
        }
    }

    val fontFamily = LocalNepoFontFamily.current

    // Navigation and sheets visibility state
    var showHistorySheet by remember { mutableStateOf(false) }
    var historyEntries by remember { mutableStateOf(emptyList<HistoryEntry>()) }
    var currentMode by remember { mutableStateOf(settingsRepository.getCalculatorMode()) }
    val converterLayout = remember(themeUpdateTrigger) { settingsRepository.getConverterLayout() }
    val isCurrencyEnabled = remember(themeUpdateTrigger) { settingsRepository.isCurrencyEnabled() }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    val isDegreeMode by viewModel.isDegreeMode.collectAsState()

    // Converter States
    var activeCategory by remember { mutableStateOf(UnitCategory.valueOf(settingsRepository.getSelectedCategory())) }
    var sourceUnit by remember(activeCategory) {
        mutableStateOf(ConverterRegistry.getUnitById(settingsRepository.getSourceUnit(activeCategory.name))
            ?: ConverterRegistry.getDefaultSourceUnit(activeCategory))
    }
    var targetUnit by remember(activeCategory) {
        mutableStateOf(ConverterRegistry.getUnitById(settingsRepository.getTargetUnit(activeCategory.name))
            ?: ConverterRegistry.getDefaultTargetUnit(activeCategory))
    }
    
    LaunchedEffect(isCurrencyEnabled) {
        if (!isCurrencyEnabled && activeCategory == UnitCategory.CURRENCY) {
            activeCategory = UnitCategory.LENGTH
            settingsRepository.setSelectedCategory(UnitCategory.LENGTH.name)
            sourceUnit = ConverterRegistry.getDefaultSourceUnit(UnitCategory.LENGTH)
            targetUnit = ConverterRegistry.getDefaultTargetUnit(UnitCategory.LENGTH)
        }
        com.ixeken.nepo.features.calculator.domain.CurrencyRatesManager.fetchLatestRates(isCurrencyEnabled)
    }
    var showSourceUnitSheet by remember { mutableStateOf(false) }
    var showTargetUnitSheet by remember { mutableStateOf(false) }

    val visorLayoutState = remember(uiState) {
        when (val state = uiState) {
            is CalculatorUiState.Empty -> VisorLayoutState.Empty
            is CalculatorUiState.WhileTyping -> VisorLayoutState.Typing(state.expression, state.partialResult)
            is CalculatorUiState.AfterEqual -> VisorLayoutState.Result(state.originalExpression, state.finalResult)
        }
    }

    // Persist successful calculations to local history
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is CalculatorUiState.AfterEqual && currentMode != "CONVERTER") {
            val exprText = state.originalExpression.text
            val resultText = state.finalResult
            if (exprText.isNotEmpty() && resultText.isNotEmpty() && resultText != "Error") {
                historyRepository.addEntry(exprText, resultText)
            }
        }
    }

    NepoTheme(currentStyles = activeTheme, fontFamily = fontFamily) {
        val theme = LocalNepoTheme.current
        val hazeState = rememberHazeState()
        val isGlass = theme.structureStyle == StructureStyleType.GLASSMORPHISM

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(theme.colors.surfaces.appBackground)
        ) {
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isWindowLandscape = maxWidth > maxHeight
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE || isWindowLandscape
            val isCompactHeight = maxHeight < 580.dp
            val isUltraCompact = maxHeight < 460.dp

            val maxResultSize = if (isLandscape) 36.sp else if (isUltraCompact) 26.sp else if (isCompactHeight) 38.sp else 54.sp
            val maxExpressionSize = if (isLandscape) 28.sp else if (isUltraCompact) 20.sp else if (isCompactHeight) 26.sp else 36.sp
            val maxPartialSize = if (isLandscape) 18.sp else if (isUltraCompact) 14.sp else if (isCompactHeight) 16.sp else 20.sp

            val displayCard = @Composable { cardModifier: Modifier ->
                val originalDensity = LocalOriginalDensity.current
                val body = @Composable {
                    val showCard = theme.calculatorStyle.visorShowCard
                val displayShape = RoundedCornerShape(
                    topStart = if (showCard) theme.calculatorStyle.visorCardBorderRadiusTop else 0.dp,
                    topEnd = if (showCard) theme.calculatorStyle.visorCardBorderRadiusTop else 0.dp,
                    bottomStart = if (showCard) theme.calculatorStyle.visorCardBorderRadiusBottom else 0.dp,
                    bottomEnd = if (showCard) theme.calculatorStyle.visorCardBorderRadiusBottom else 0.dp
                )
                Column(
                    modifier = cardModifier
                        .then(
                            if (showCard) {
                                Modifier.background(
                                    color = theme.colors.surfaces.calculatorScreenBackground,
                                    shape = displayShape
                                )
                            } else Modifier
                        )
                        .then(
                            if (theme.calculatorStyle.outerCardPadding == 0.dp) {
                                Modifier.statusBarsPadding()
                            } else Modifier
                        )
                        .padding(if (isUltraCompact) 4.dp else if (isCompactHeight) 8.dp else if (currentMode == "CONVERTER") 12.dp else 16.dp)
                ) {
                // Top control row inside the display card (Only when not ultra compact)
                if (!isUltraCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History Icon Trigger (Left corner) wrapped in a glassy button if isGlass is true
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (isGlass) Modifier.background(Color(0x20FFFFFF), shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "bubble_tea") Modifier.background(theme.colors.surfaces.appBackground, shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "monochromatic_elegance") Modifier.background(theme.colors.typography.headerAccent, shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "ocean_blue") Modifier.background(theme.colors.interactiveComponents.confirmButton.background, shape = androidx.compose.foundation.shape.CircleShape)
                                else Modifier
                            )
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { showHistorySheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.History,
                            contentDescription = "Open history log",
                            tint = theme.colors.typography.screenIcons,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Modes Menu Icon Trigger (Center) wrapped in a glassy button if isGlass is true
                    var showModeMenu by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (isGlass) Modifier.background(Color(0x20FFFFFF), shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "bubble_tea") Modifier.background(theme.colors.surfaces.appBackground, shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "monochromatic_elegance") Modifier.background(theme.colors.typography.headerAccent, shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "ocean_blue") Modifier.background(theme.colors.interactiveComponents.confirmButton.background, shape = androidx.compose.foundation.shape.CircleShape)
                                else Modifier
                            )
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { showModeMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.SquareMenu,
                            contentDescription = "Change calculator mode",
                            tint = theme.colors.typography.screenIcons,
                            modifier = Modifier.size(20.dp)
                        )

                        if (showModeMenu) {
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { isVisible = true }

                            val scale by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0.85f,
                                animationSpec = tween(190, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            )
                            val alpha by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0f,
                                animationSpec = tween(165, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                            )

                            Popup(
                                alignment = Alignment.TopCenter,
                                offset = IntOffset(0, with(androidx.compose.ui.platform.LocalDensity.current) { 44.dp.roundToPx() }),
                                onDismissRequest = { showModeMenu = false },
                                properties = PopupProperties(focusable = true)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            alpha = alpha,
                                            transformOrigin = TransformOrigin(0.5f, 0f)
                                        )
                                        .width(220.dp)
                                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal))
                                        .background(
                                            color = theme.colors.surfaces.bottomSheetBackground,
                                            shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal)
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = theme.colors.structuralElements.itemSeparator,
                                            shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Option: Basic
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    currentMode = "BASIC"
                                                    settingsRepository.setCalculatorMode("BASIC")
                                                    showModeMenu = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Lucide.Diff,
                                                    contentDescription = null,
                                                    tint = theme.colors.typography.headerAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = stringResource(id = com.ixeken.nepo.core.designsystem.R.string.settings_mode_basic),
                                                    color = theme.colors.typography.bodyPrimary,
                                                    fontSize = 14.sp,
                                                    fontFamily = fontFamily
                                                )
                                            }
                                            if (currentMode == "BASIC") {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(theme.colors.interactiveComponents.selectButton.background),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Lucide.Check,
                                                        contentDescription = "Selected",
                                                        tint = theme.colors.interactiveComponents.selectButton.foreground,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Option: Scientific
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    currentMode = "SCIENTIFIC"
                                                    settingsRepository.setCalculatorMode("SCIENTIFIC")
                                                    showModeMenu = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Lucide.FlaskConical,
                                                    contentDescription = null,
                                                    tint = theme.colors.typography.headerAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = stringResource(id = com.ixeken.nepo.core.designsystem.R.string.settings_mode_scientific),
                                                    color = theme.colors.typography.bodyPrimary,
                                                    fontSize = 14.sp,
                                                    fontFamily = fontFamily
                                                )
                                            }
                                            if (currentMode == "SCIENTIFIC") {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(theme.colors.interactiveComponents.selectButton.background),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Lucide.Check,
                                                        contentDescription = "Selected",
                                                        tint = theme.colors.interactiveComponents.selectButton.foreground,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(
                                            color = theme.colors.structuralElements.itemSeparator,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )

                                        // Option: Unit converter
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    currentMode = "CONVERTER"
                                                    settingsRepository.setCalculatorMode("CONVERTER")
                                                    showModeMenu = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Lucide.ArrowUpDown,
                                                    contentDescription = null,
                                                    tint = theme.colors.typography.headerAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = stringResource(id = com.ixeken.nepo.core.designsystem.R.string.settings_mode_converter),
                                                    color = theme.colors.typography.bodyPrimary,
                                                    fontSize = 14.sp,
                                                    fontFamily = fontFamily
                                                )
                                            }
                                            if (currentMode == "CONVERTER") {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(theme.colors.interactiveComponents.selectButton.background),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Lucide.Check,
                                                        contentDescription = "Selected",
                                                        tint = theme.colors.interactiveComponents.selectButton.foreground,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Settings Icon Trigger (Right corner) wrapped in a glassy button if isGlass is true
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (isGlass) Modifier.background(Color(0x20FFFFFF), shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "bubble_tea") Modifier.background(theme.colors.surfaces.appBackground, shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "monochromatic_elegance") Modifier.background(theme.colors.typography.headerAccent, shape = androidx.compose.foundation.shape.CircleShape)
                                else if (theme.metadata.id == "ocean_blue") Modifier.background(theme.colors.interactiveComponents.confirmButton.background, shape = androidx.compose.foundation.shape.CircleShape)
                                else Modifier
                            )
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.SlidersHorizontal,
                            contentDescription = "Open settings configuration",
                            tint = theme.colors.typography.screenIcons,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                }

                // Output text stack, aligned bottom-right
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    if (currentMode == "SCIENTIFIC") {
                        Text(
                            text = if (isDegreeMode) "Deg" else "Rad",
                            color = theme.colors.typography.screenSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    } else {
                        Spacer(modifier = Modifier.height(1.dp))
                    }

                    if (currentMode == "CONVERTER") {
                        val currentExpr = when (val state = visorLayoutState) {
                            is VisorLayoutState.Empty -> TextFieldValue("")
                            is VisorLayoutState.Typing -> state.expression
                            is VisorLayoutState.Result -> state.originalExpression
                        }
                        val isReadOnly = when (visorLayoutState) {
                            is VisorLayoutState.Result -> true
                            else -> false
                        }
                        val (srcText, destText) = getConverterValues(visorLayoutState, sourceUnit, targetUnit, settingsRepository)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.End
                        ) {
                            // Top: Editable / Non-editable Expression
                            AutoResizeTextField(
                                value = currentExpr,
                                onValueChange = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnExpressionValueChanged(it)) },
                                readOnly = isReadOnly,
                                maxFontSize = 15.sp,
                                minFontSize = 12.sp,
                                color = if (isReadOnly) theme.colors.typography.screenSecondary else theme.colors.typography.screenPrimary,
                                fontFamily = fontFamily,
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(theme.colors.typography.screenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            // Source value
                            AutoResizeText(
                                text = srcText.ifEmpty { "0" },
                                maxFontSize = 26.sp,
                                minFontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.colors.typography.screenPrimary,
                                fontFamily = fontFamily,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = stringResource(id = sourceUnit.nameResId).lowercase(),
                                color = theme.colors.typography.screenPrimary,
                                fontSize = 11.sp,
                                fontFamily = fontFamily
                            )
                            
                            HorizontalDivider(
                                color = theme.colors.structuralElements.itemSeparator,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                            
                            // Destination value
                            AutoResizeText(
                                text = destText.ifEmpty { "0" },
                                maxFontSize = 38.sp,
                                minFontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.colors.typography.screenPrimary,
                                fontFamily = fontFamily,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (destText.isNotEmpty()) {
                                            copyToClipboard(context, destText)
                                            android.widget.Toast.makeText(context, context.getString(R.string.result_copied_toast), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            )
                            Text(
                                text = stringResource(id = targetUnit.nameResId).lowercase(),
                                color = theme.colors.typography.screenPrimary,
                                fontSize = 12.sp,
                                fontFamily = fontFamily
                            )
                        }
                    } else {
                        AnimatedContent(
                            targetState = visorLayoutState,
                            transitionSpec = {
                                val isTransitionBetweenTypingAndResult =
                                    (initialState is VisorLayoutState.Result && targetState is VisorLayoutState.Typing) ||
                                    (initialState is VisorLayoutState.Typing && targetState is VisorLayoutState.Result) ||
                                    (initialState is VisorLayoutState.Empty && targetState is VisorLayoutState.Result) ||
                                    (initialState is VisorLayoutState.Result && targetState is VisorLayoutState.Empty)

                                val isBetweenEmptyAndTyping =
                                    (initialState is VisorLayoutState.Empty && targetState is VisorLayoutState.Typing) ||
                                    (initialState is VisorLayoutState.Typing && targetState is VisorLayoutState.Empty)

                                if (isTransitionBetweenTypingAndResult) {
                                    val animSpec = androidx.compose.animation.core.tween<Float>(durationMillis = 225)
                                    val intOffsetSpec = androidx.compose.animation.core.tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 225)
                                    (slideInVertically(animationSpec = intOffsetSpec) { height -> height } + fadeIn(animationSpec = animSpec)).togetherWith(
                                        slideOutVertically(animationSpec = intOffsetSpec) { height -> -height } + fadeOut(animationSpec = animSpec)
                                    )
                                } else if (isBetweenEmptyAndTyping) {
                                    fadeIn(animationSpec = androidx.compose.animation.core.tween(165)).togetherWith(
                                        fadeOut(animationSpec = androidx.compose.animation.core.tween(165))
                                    )
                                } else {
                                    EnterTransition.None togetherWith ExitTransition.None
                                }
                            },
                            label = "visor_transition",
                            modifier = Modifier.fillMaxWidth()
                        ) { state ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.End
                            ) {
                                when (state) {
                                    is VisorLayoutState.Result -> {
                                        // Top: Operation (original expression) is small (without '=' as shown in mockup)
                                        AutoResizeTextField(
                                            value = state.originalExpression,
                                            onValueChange = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnExpressionValueChanged(it)) },
                                            readOnly = true,
                                            maxFontSize = maxPartialSize,
                                            minFontSize = 10.sp,
                                            color = theme.colors.typography.screenSecondary,
                                            fontFamily = fontFamily,
                                            cursorBrush = androidx.compose.ui.graphics.SolidColor(theme.colors.typography.screenSecondary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        // Bottom: Final result (total) is large
                                        val formattedResult = try {
                                            settingsRepository.formatNumber(state.finalResult.toDouble())
                                        } catch (e: Exception) {
                                            state.finalResult
                                        }
                                        AutoResizeText(
                                            text = formattedResult,
                                            maxFontSize = maxResultSize,
                                            minFontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.colors.typography.screenPrimary,
                                            fontFamily = fontFamily,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    if (formattedResult.isNotEmpty()) {
                                                        copyToClipboard(context, formattedResult)
                                                        android.widget.Toast.makeText(context, context.getString(R.string.result_copied_toast), android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        )
                                    }
                                    is VisorLayoutState.Typing -> {
                                        // Top: running total (partial result) is small
                                        val formattedPartial = if (state.partialResult.isNotEmpty()) {
                                            try {
                                                settingsRepository.formatNumber(state.partialResult.toDouble())
                                            } catch (e: Exception) {
                                                state.partialResult
                                            }
                                        } else {
                                            ""
                                        }
                                        AutoResizeText(
                                            text = formattedPartial,
                                            maxFontSize = maxPartialSize,
                                            minFontSize = 10.sp,
                                            color = theme.colors.typography.screenSecondary,
                                            fontFamily = fontFamily,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        // Bottom: Operation (expression) is large, editable as a text box with cursor
                                        AutoResizeTextField(
                                            value = state.expression,
                                            onValueChange = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnExpressionValueChanged(it)) },
                                            readOnly = false,
                                            maxFontSize = maxExpressionSize,
                                            minFontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.colors.typography.screenPrimary,
                                            fontFamily = fontFamily,
                                            cursorBrush = androidx.compose.ui.graphics.SolidColor(theme.colors.typography.screenPrimary),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    is VisorLayoutState.Empty -> {
                                        // Empty state shows slashed zero 'Ø' aligned top-right with AutoResizeText
                                        AutoResizeText(
                                            text = "0",
                                            maxFontSize = if (isLandscape) 42.sp else if (isUltraCompact) 28.sp else if (isCompactHeight) 38.sp else 64.sp,
                                            minFontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.colors.typography.screenPrimary,
                                            fontFamily = fontFamily,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (currentMode == "CONVERTER" && activeCategory == UnitCategory.CURRENCY) {
                    val statusText = if (com.ixeken.nepo.features.calculator.domain.CurrencyRatesManager.ratesDate == "offline") {
                        stringResource(id = R.string.currency_rates_offline)
                    } else {
                        com.ixeken.nepo.features.calculator.domain.CurrencyRatesManager.ratesDate
                    }
                    Text(
                        text = stringResource(id = R.string.currency_rates_status, statusText),
                        color = theme.colors.typography.screenSecondary,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )
                }

                if (currentMode == "CONVERTER" && converterLayout != "OUTSIDE") {
                    Spacer(modifier = Modifier.height(12.dp))
                    UnitSelectorRow(
                        sourceUnit = sourceUnit,
                        targetUnit = targetUnit,
                        onSourceClick = { showSourceUnitSheet = true },
                        onTargetClick = { showTargetUnitSheet = true },
                        onSwapClick = {
                            val temp = sourceUnit
                            sourceUnit = targetUnit
                            targetUnit = temp
                            settingsRepository.setSourceUnit(activeCategory.name, sourceUnit.id)
                            settingsRepository.setTargetUnit(activeCategory.name, targetUnit.id)
                        },
                        layout = converterLayout
                    )
                }
            }
            }
            if (originalDensity != null) {
                CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides originalDensity) {
                    body()
                }
            } else {
                body()
            }
        }

        val isInversedMode by viewModel.isInversedMode.collectAsState()
        val keyboardContent = @Composable { keyboardModifier: Modifier ->
            val originalDensity = LocalOriginalDensity.current
            val body = @Composable {
                val showKeyboardCard = theme.calculatorStyle.keyboardShowCard
            if (showKeyboardCard) {
                val keyboardShape = RoundedCornerShape(
                    topStart = theme.calculatorStyle.keyboardCardBorderRadiusTop,
                    topEnd = theme.calculatorStyle.keyboardCardBorderRadiusTop,
                    bottomStart = theme.calculatorStyle.keyboardCardBorderRadiusBottom,
                    bottomEnd = theme.calculatorStyle.keyboardCardBorderRadiusBottom
                )
                val isOceanBlue = theme.metadata.id == "ocean_blue"
                Box(
                    modifier = keyboardModifier
                        .then(
                            if (isOceanBlue) {
                                Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)
                            } else Modifier
                        )
                        .background(
                            color = theme.calculatorStyle.keyboardCardBackground,
                            shape = keyboardShape
                        )
                        .then(
                            if (theme.calculatorStyle.outerCardPadding == 0.dp) {
                                Modifier.navigationBarsPadding()
                            } else Modifier
                        )
                        .padding(8.dp)
                ) {
                    CalculatorKeyboard(
                        onEvent = viewModel::onEvent,
                        mode = currentMode,
                        isDegreeMode = isDegreeMode,
                        isInversedMode = isInversedMode,
                        onToggleDegreeMode = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnToggleDegreeMode) },
                        onToggleInversedMode = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnToggleInversedMode) },
                        modifier = if (isLandscape || isCompactHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
                        isCompactHeight = isCompactHeight,
                        isUltraCompact = isUltraCompact,
                        hazeState = if (isGlass) hazeState else null
                    )
                }
            } else {
                CalculatorKeyboard(
                    onEvent = viewModel::onEvent,
                    mode = currentMode,
                    isDegreeMode = isDegreeMode,
                    isInversedMode = isInversedMode,
                    onToggleDegreeMode = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnToggleDegreeMode) },
                    onToggleInversedMode = { viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnToggleInversedMode) },
                    modifier = keyboardModifier
                        .then(
                            if (theme.metadata.id == "ocean_blue") {
                                Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)
                            } else Modifier
                        )
                        .then(
                            if (theme.calculatorStyle.outerCardPadding == 0.dp) {
                                Modifier.navigationBarsPadding()
                            } else Modifier
                        ),
                    isCompactHeight = isCompactHeight,
                    isUltraCompact = isUltraCompact,
                    hazeState = if (isGlass) hazeState else null
                )
            }
            }
            if (originalDensity != null) {
                CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides originalDensity) {
                    body()
                }
            } else {
                body()
            }
        }

        val hasOuterCard = theme.calculatorStyle.outerCardPadding > 0.dp || theme.calculatorStyle.outerCardBackground != Color.Transparent || theme.calculatorStyle.outerCardBorderWidth > 0.dp

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(theme.colors.surfaces.appBackground)
                .then(
                    if (isGlass) Modifier.hazeSource(state = hazeState)
                    else Modifier
                )
                .then(
                    if (theme.calculatorStyle.outerCardPadding > 0.dp) {
                        Modifier.safeDrawingPadding()
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (hasOuterCard) theme.calculatorStyle.outerCardPadding else 0.dp)
                    .then(
                        if (hasOuterCard && theme.calculatorStyle.outerCardBackground != Color.Transparent) {
                            Modifier.background(
                                color = theme.calculatorStyle.outerCardBackground,
                                shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal)
                            )
                        } else Modifier
                    )
                    .then(
                        if (hasOuterCard && theme.calculatorStyle.outerCardBorderWidth > 0.dp && theme.calculatorStyle.outerCardBorderColor != Color.Transparent) {
                            Modifier.border(
                                width = theme.calculatorStyle.outerCardBorderWidth,
                                color = theme.calculatorStyle.outerCardBorderColor,
                                shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal)
                            )
                        } else Modifier
                    )
                    .padding(if (hasOuterCard) 8.dp else theme.calculatorStyle.outerCardPadding)
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val visorWeight = if (currentMode == "SCIENTIFIC") 0.35f else 0.45f
                        val keyboardWeight = if (currentMode == "SCIENTIFIC") 0.65f else 0.55f
                        Column(
                            modifier = Modifier.weight(visorWeight).fillMaxHeight()
                        ) {
                            displayCard(Modifier.weight(1f).fillMaxWidth())
                            if (currentMode == "CONVERTER" && converterLayout == "OUTSIDE") {
                                Spacer(modifier = Modifier.height(4.dp))
                                UnitSelectorRow(
                                    sourceUnit = sourceUnit,
                                    targetUnit = targetUnit,
                                    onSourceClick = { showSourceUnitSheet = true },
                                    onTargetClick = { showTargetUnitSheet = true },
                                    onSwapClick = {
                                        val temp = sourceUnit
                                        sourceUnit = targetUnit
                                        targetUnit = temp
                                        settingsRepository.setSourceUnit(activeCategory.name, sourceUnit.id)
                                        settingsRepository.setTargetUnit(activeCategory.name, targetUnit.id)
                                    }
                                )
                            }
                        }
                        keyboardContent(Modifier.weight(keyboardWeight).fillMaxHeight())
                    }
                } else {
                    if (isUltraCompact) {
                        displayCard(Modifier.fillMaxWidth().weight(0.24f))
                        if (currentMode == "CONVERTER" && converterLayout == "OUTSIDE") {
                            Spacer(modifier = Modifier.height(2.dp))
                            UnitSelectorRow(
                                sourceUnit = sourceUnit,
                                targetUnit = targetUnit,
                                onSourceClick = { showSourceUnitSheet = true },
                                onTargetClick = { showTargetUnitSheet = true },
                                onSwapClick = {
                                    val temp = sourceUnit
                                    sourceUnit = targetUnit
                                    targetUnit = temp
                                    settingsRepository.setSourceUnit(activeCategory.name, sourceUnit.id)
                                    settingsRepository.setTargetUnit(activeCategory.name, targetUnit.id)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        keyboardContent(Modifier.fillMaxWidth().weight(0.76f))
                    } else if (isCompactHeight) {
                        val visorWeight = if (currentMode == "SCIENTIFIC") 0.30f else 0.35f
                        val keyboardWeight = if (currentMode == "SCIENTIFIC") 0.70f else 0.65f
                        displayCard(Modifier.fillMaxWidth().weight(visorWeight))
                        if (currentMode == "CONVERTER" && converterLayout == "OUTSIDE") {
                            Spacer(modifier = Modifier.height(4.dp))
                            UnitSelectorRow(
                                sourceUnit = sourceUnit,
                                targetUnit = targetUnit,
                                onSourceClick = { showSourceUnitSheet = true },
                                onTargetClick = { showTargetUnitSheet = true },
                                onSwapClick = {
                                    val temp = sourceUnit
                                    sourceUnit = targetUnit
                                    targetUnit = temp
                                    settingsRepository.setSourceUnit(activeCategory.name, sourceUnit.id)
                                    settingsRepository.setTargetUnit(activeCategory.name, targetUnit.id)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        keyboardContent(Modifier.fillMaxWidth().weight(keyboardWeight))
                    } else {
                        displayCard(Modifier.fillMaxWidth().weight(1.0f))
                        if (currentMode == "CONVERTER" && converterLayout == "OUTSIDE") {
                            Spacer(modifier = Modifier.height(8.dp))
                            UnitSelectorRow(
                                sourceUnit = sourceUnit,
                                targetUnit = targetUnit,
                                onSourceClick = { showSourceUnitSheet = true },
                                onTargetClick = { showTargetUnitSheet = true },
                                onSwapClick = {
                                    val temp = sourceUnit
                                    sourceUnit = targetUnit
                                    targetUnit = temp
                                    settingsRepository.setSourceUnit(activeCategory.name, sourceUnit.id)
                                    settingsRepository.setTargetUnit(activeCategory.name, targetUnit.id)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        keyboardContent(Modifier.fillMaxWidth())
                    }
            }
        }
    }

            // History Bottom Sheet overlay
            if (showHistorySheet) {
                LaunchedEffect(showHistorySheet) {
                    historyEntries = historyRepository.getHistory()
                }

                HistoryBottomSheet(
                    entries = historyEntries,
                    settingsRepository = settingsRepository,
                    onRowClicked = { expression ->
                        // Load selected expression back to the calculator and close
                        for (char in expression) {
                            viewModel.onEvent(com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent.OnKeyPress(char.toString()))
                        }
                        showHistorySheet = false
                    },
                    onDeleteEntries = { ids ->
                        historyRepository.deleteEntries(ids)
                        historyEntries = historyRepository.getHistory()
                    },
                    onClearAll = {
                        historyRepository.clearAll()
                        historyEntries = historyRepository.getHistory()
                    },
                    onDismissRequest = { showHistorySheet = false }
                )
            }

            // Source Unit selection bottom sheet
            if (showSourceUnitSheet) {
                UnitSelectionBottomSheet(
                    title = stringResource(id = R.string.units_from),
                    selectedUnit = sourceUnit,
                    currentCategory = activeCategory,
                    onUnitSelected = { unit ->
                        if (unit.category != activeCategory) {
                            activeCategory = unit.category
                            settingsRepository.setSelectedCategory(unit.category.name)
                            targetUnit = ConverterRegistry.getUnitById(settingsRepository.getTargetUnit(unit.category.name))
                                ?: ConverterRegistry.getDefaultTargetUnit(unit.category)
                        }
                        sourceUnit = unit
                        settingsRepository.setSourceUnit(activeCategory.name, unit.id)
                    },
                    onCategorySelected = { category ->
                        activeCategory = category
                        settingsRepository.setSelectedCategory(category.name)
                        sourceUnit = ConverterRegistry.getUnitById(settingsRepository.getSourceUnit(category.name))
                            ?: ConverterRegistry.getDefaultSourceUnit(category)
                        targetUnit = ConverterRegistry.getUnitById(settingsRepository.getTargetUnit(category.name))
                            ?: ConverterRegistry.getDefaultTargetUnit(category)
                    },
                    onDismissRequest = { showSourceUnitSheet = false },
                    isCurrencyEnabled = isCurrencyEnabled
                )
            }

            // Target Unit selection bottom sheet
            if (showTargetUnitSheet) {
                UnitSelectionBottomSheet(
                    title = stringResource(id = R.string.units_to),
                    selectedUnit = targetUnit,
                    currentCategory = activeCategory,
                    onUnitSelected = { unit ->
                        if (unit.category != activeCategory) {
                            activeCategory = unit.category
                            settingsRepository.setSelectedCategory(unit.category.name)
                            sourceUnit = ConverterRegistry.getUnitById(settingsRepository.getSourceUnit(unit.category.name))
                                ?: ConverterRegistry.getDefaultSourceUnit(unit.category)
                        }
                        targetUnit = unit
                        settingsRepository.setTargetUnit(activeCategory.name, unit.id)
                    },
                    onCategorySelected = { category ->
                        activeCategory = category
                        settingsRepository.setSelectedCategory(category.name)
                        sourceUnit = ConverterRegistry.getUnitById(settingsRepository.getSourceUnit(category.name))
                            ?: ConverterRegistry.getDefaultSourceUnit(category)
                        targetUnit = ConverterRegistry.getUnitById(settingsRepository.getTargetUnit(category.name))
                            ?: ConverterRegistry.getDefaultTargetUnit(category)
                    },
                    onDismissRequest = { showTargetUnitSheet = false },
                    isCurrencyEnabled = isCurrencyEnabled
                )
            }
        }
    }
}

@Composable
private fun AutoResizeText(
    text: String,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.Default,
    textAlign: TextAlign = TextAlign.End
) {
    var fontSize by remember(text) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        fontFamily = fontFamily,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                if (readyToDraw) drawContent()
            },
        onTextLayout = { result ->
            if ((result.didOverflowWidth || result.didOverflowHeight) && fontSize.value > minFontSize.value) {
                fontSize = (fontSize.value - 1).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun AutoResizeTextField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.Default,
    textAlign: TextAlign = TextAlign.End,
    readOnly: Boolean = true,
    cursorBrush: androidx.compose.ui.graphics.Brush
) {
    var fontSize by remember(value.text) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(value.text) { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    InterceptPlatformTextInput(
        interceptor = { _, _ -> awaitCancellation() }
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = true,
            maxLines = 1,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color,
                fontFamily = fontFamily,
                textAlign = textAlign
            ),
            cursorBrush = cursorBrush,
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .drawWithContent {
                    if (readyToDraw) drawContent()
                },
            onTextLayout = { result ->
                if ((result.didOverflowWidth || result.didOverflowHeight) && fontSize.value > minFontSize.value) {
                    fontSize = (fontSize.value - 1).sp
                } else {
                    readyToDraw = true
                }
            }
        )
    }

    LaunchedEffect(value.text) {
        try {
            kotlinx.coroutines.delay(20)
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore focus failures
        }
    }
}

sealed interface VisorLayoutState {
    data object Empty : VisorLayoutState
    data class Typing(
        val expression: androidx.compose.ui.text.input.TextFieldValue,
        val partialResult: String
    ) : VisorLayoutState
    data class Result(
        val originalExpression: androidx.compose.ui.text.input.TextFieldValue,
        val finalResult: String
    ) : VisorLayoutState
}

@Composable
private fun getConverterValues(
    state: VisorLayoutState,
    sourceUnit: ConversionUnit,
    targetUnit: ConversionUnit,
    settingsRepository: SettingsRepository
): Pair<String, String> {
    val sourceValStr = when (state) {
        is VisorLayoutState.Empty -> "0"
        is VisorLayoutState.Typing -> {
            if (state.partialResult.isNotEmpty()) {
                state.partialResult
            } else {
                val raw = state.expression.text
                if (raw.toDoubleOrNull() != null) raw else ""
            }
        }
        is VisorLayoutState.Result -> {
            if (state.finalResult == "Error") "" else state.finalResult
        }
    }
    
    if (sourceValStr.isEmpty()) {
        return Pair("", "")
    }
    
    val sourceDouble = sourceValStr.toDoubleOrNull()
    if (sourceDouble == null) {
        return Pair(sourceValStr, "")
    }
    
    val convertedDouble = ConverterRegistry.convert(sourceDouble, sourceUnit, targetUnit)
    
    val formattedSource = try {
        settingsRepository.formatNumber(sourceDouble)
    } catch (e: Exception) {
        sourceValStr
    }
    
    val formattedTarget = try {
        settingsRepository.formatNumber(convertedDouble)
    } catch (e: Exception) {
        convertedDouble.toString()
    }
    
    return Pair(formattedSource, formattedTarget)
}
