package com.ixeken.nepo.features.calculator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.Lucide
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.features.calculator.presentation.CalculatorUserEvent

/**
 * Calculator keyboard layout.
 *
 * Organizes numeric, operator, and scientific keys in a responsive grid. Consumes theme visual tokens
 * and sends [CalculatorUserEvent] instances on key presses.
 *
 * @param onEvent Callback triggered when a key is pressed.
 * @param modifier Modifier applied to the outer layout container.
 */
@Composable
fun CalculatorKeyboard(
    onEvent: (CalculatorUserEvent) -> Unit,
    mode: String,
    isDegreeMode: Boolean,
    isInversedMode: Boolean,
    onToggleDegreeMode: () -> Unit,
    onToggleInversedMode: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val theme = LocalNepoTheme.current
    val tokens = theme.colors.interactiveComponents
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val spaceBetween = if (isLandscape) 4.dp else 8.dp
    val buttonModifier = if (isLandscape) Modifier.fillMaxHeight() else {
        if (mode == "SCIENTIFIC" || mode == "CONVERTER") Modifier.aspectRatio(1.22f)
        else Modifier.aspectRatio(1f)
    }
 
    val currentTypography = MaterialTheme.typography
    val customTypography = currentTypography.copy(
        titleLarge = currentTypography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = if (isLandscape && mode == "SCIENTIFIC") 18.sp else currentTypography.titleLarge.fontSize
        )
    )
 
    MaterialTheme(typography = customTypography) {
        Column(
            modifier = modifier
                .then(if (isLandscape) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
                .padding(if (isLandscape) 4.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
        // Row 0: Scientific operators as plain text floating labels (Only in Portrait mode)
        if (!isLandscape && mode == "SCIENTIFIC") {
            // Row 0.1: INV, RAD/DEG, (, ), π
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spaceBetween)
            ) {
                ScientificTextButton(
                    text = "INV",
                    onClick = { onToggleInversedMode() },
                    textColor = if (isInversedMode) theme.colors.typography.headerAccent else theme.colors.typography.bodyPrimary,
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = if (isDegreeMode) "DEG" else "RAD",
                    onClick = { onToggleDegreeMode() },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "(",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("(")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = ")",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress(")")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "π",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("π")) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Row 0.2: sin/asin, cos/acos, tan/atan, ^, √
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spaceBetween)
            ) {
                val sinText = if (isInversedMode) "asin" else "sin"
                ScientificTextButton(
                    text = sinText,
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("$sinText(")) },
                    modifier = Modifier.weight(1f)
                )
                val cosText = if (isInversedMode) "acos" else "cos"
                ScientificTextButton(
                    text = cosText,
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("$cosText(")) },
                    modifier = Modifier.weight(1f)
                )
                val tanText = if (isInversedMode) "atan" else "tan"
                ScientificTextButton(
                    text = tanText,
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("$tanText(")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "^",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("^")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "√",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("√(")) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Row 0.3: ln/exp, log/10^x, e, cbrt, abs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spaceBetween)
            ) {
                val lnText = if (isInversedMode) "eˣ" else "ln"
                val lnPress = if (isInversedMode) "exp(" else "ln("
                ScientificTextButton(
                    text = lnText,
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress(lnPress)) },
                    modifier = Modifier.weight(1f)
                )
                val logText = if (isInversedMode) "10ˣ" else "log"
                val logPress = if (isInversedMode) "10^(" else "log("
                ScientificTextButton(
                    text = logText,
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress(logPress)) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "e",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("e")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "³√",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("cbrt(")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "|x|",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("abs(")) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!isLandscape && mode == "BASIC") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spaceBetween)
            ) {
                ScientificTextButton(
                    text = "²√",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("²√(")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "π",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("π")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "^",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("^")) },
                    modifier = Modifier.weight(1f)
                )
                ScientificTextButton(
                    text = "()",
                    onClick = { onEvent(CalculatorUserEvent.OnKeyPress("()")) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 1: Scientific (if landscape) + Backspace (Icon), AC, %, ÷
        Row(
            modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            if (isLandscape) {
                if (mode == "SCIENTIFIC") {
                    ScientificTextButton(
                        text = "INV",
                        onClick = { onToggleInversedMode() },
                        textColor = if (isInversedMode) theme.colors.typography.headerAccent else theme.colors.typography.bodyPrimary,
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    ScientificTextButton(
                        text = if (isDegreeMode) "DEG" else "RAD",
                        onClick = { onToggleDegreeMode() },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    ScientificTextButton(
                        text = "()",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("()")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                } else {
                    ScientificTextButton(
                        text = "()",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("()")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                }
            }
            NepoButton(
                text = "C",
                onClick = { onEvent(CalculatorUserEvent.OnDeleteSingle) },
                visualTokens = tokens.deleteButton,
                icon = Lucide.Delete,
                iconBold = true,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "AC",
                onClick = { onEvent(CalculatorUserEvent.OnClearAll) },
                visualTokens = tokens.deleteButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "%",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("%")) },
                visualTokens = tokens.operatorButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "/",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("/")) },
                visualTokens = tokens.operatorButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
        }

        // Row 2: Scientific (if landscape) + 7, 8, 9, ×
        Row(
            modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            if (isLandscape) {
                if (mode == "SCIENTIFIC") {
                    val sinText = if (isInversedMode) "asin" else "sin"
                    ScientificTextButton(
                        text = sinText,
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("$sinText(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    val cosText = if (isInversedMode) "acos" else "cos"
                    ScientificTextButton(
                        text = cosText,
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("$cosText(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    val tanText = if (isInversedMode) "atan" else "tan"
                    ScientificTextButton(
                        text = tanText,
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("$tanText(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                } else {
                    ScientificTextButton(
                        text = "^",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("^")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                }
            }
            NepoButton(
                text = "7",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("7")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "8",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("8")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "9",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("9")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "×",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("×")) },
                visualTokens = tokens.operatorButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
        }

        // Row 3: Scientific (if landscape, empty spacer) + 4, 5, 6, -
        Row(
            modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            if (isLandscape) {
                if (mode == "SCIENTIFIC") {
                    val lnText = if (isInversedMode) "eˣ" else "ln"
                    val lnPress = if (isInversedMode) "exp(" else "ln("
                    ScientificTextButton(
                        text = lnText,
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress(lnPress)) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    val logText = if (isInversedMode) "10ˣ" else "log"
                    val logPress = if (isInversedMode) "10^(" else "log("
                    ScientificTextButton(
                        text = logText,
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress(logPress)) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    ScientificTextButton(
                        text = "^",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("^")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                } else {
                    // Empty spacer for alignment
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                }
            }
            NepoButton(
                text = "4",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("4")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "5",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("5")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "6",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("6")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "-",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("-")) },
                visualTokens = tokens.operatorButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
        }

        // Row 4: Scientific (if landscape) + 1, 2, 3, +
        Row(
            modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            if (isLandscape) {
                if (mode == "SCIENTIFIC") {
                    ScientificTextButton(
                        text = "e",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("e")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    ScientificTextButton(
                        text = "π",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("π")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    ScientificTextButton(
                        text = "√",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("√(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                } else {
                    ScientificTextButton(
                        text = "π",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("π")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                }
            }
            NepoButton(
                text = "1",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("1")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "2",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("2")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "3",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("3")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "+",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("+")) },
                visualTokens = tokens.operatorButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
        }

        // Row 5: Scientific (if landscape) + ±, 0, ., =
        Row(
            modifier = if (isLandscape) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            if (isLandscape) {
                if (mode == "SCIENTIFIC") {
                    ScientificTextButton(
                        text = "³√",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("cbrt(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    ScientificTextButton(
                        text = "|x|",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("abs(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                } else {
                    ScientificTextButton(
                        text = "√",
                        onClick = { onEvent(CalculatorUserEvent.OnKeyPress("²√(")) },
                        modifier = Modifier.weight(1f).then(buttonModifier)
                    )
                }
            }
            NepoButton(
                text = "±",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("±")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "Ø",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress("0")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = ".",
                onClick = { onEvent(CalculatorUserEvent.OnKeyPress(".")) },
                visualTokens = tokens.numbersButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
            NepoButton(
                text = "=",
                onClick = { onEvent(CalculatorUserEvent.OnEvaluate) },
                visualTokens = tokens.operatorButton,
                modifier = Modifier.weight(1f).then(buttonModifier),
                hazeState = hazeState
            )
        }
    }
}
}

@Composable
private fun ScientificTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: androidx.compose.ui.graphics.Color? = null
) {
    val theme = LocalNepoTheme.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val height = if (isLandscape) 30.dp else 36.dp
    val resolvedColor = textColor ?: theme.colors.typography.scientificOperators

    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { com.ixeken.nepo.features.calculator.data.SettingsRepository(context) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val view = androidx.compose.ui.platform.LocalView.current

    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (settingsRepository.isHapticFeedbackEnabled()) {
                        try {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        } catch (e: Exception) {}
                    }
                    if (settingsRepository.isSoundFeedbackEnabled()) {
                        try {
                            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                        } catch (e: Exception) {}
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = resolvedColor,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = LocalNepoFontFamily.current,
                fontSize = if (isLandscape) 13.sp else 15.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
