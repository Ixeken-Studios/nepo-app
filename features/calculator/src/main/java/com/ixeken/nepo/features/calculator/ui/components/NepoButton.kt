package com.ixeken.nepo.features.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.ixeken.nepo.core.designsystem.models.StructureStyleType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue

/**
 * A themed customizable button component for the calculator.
 *
 * Automatically consumes the dynamic style configuration from [LocalNepoTheme.current].
 * When the active theme is GLASSMORPHISM and a [hazeState] is provided, applies a real
 * backdrop blur effect (frosted glass) instead of blurring its own content.
 *
 * @param text The button text label (or fallback content description for icons).
 * @param onClick Callback triggered when the button is clicked.
 * @param visualTokens Color configuration for the text (foreground) and background.
 * @param modifier Modifier applied to the outer layout container.
 * @param icon Optional vector icon to display instead of text.
 * @param hazeState Optional shared Haze state for backdrop blur in glassmorphism mode.
 */
@Composable
fun NepoButton(
    text: String,
    onClick: () -> Unit,
    visualTokens: ComponentVisualTokens,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(12.dp),
    iconBold: Boolean = false,
    isKeyboardKey: Boolean = true,
    hazeState: HazeState? = null
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    val shape = if (isKeyboardKey) {
        when (theme.calculatorStyle.buttonShapeType) {
            "CIRCLE" -> androidx.compose.foundation.shape.CircleShape
            "SQUARE" -> RoundedCornerShape(0.dp)
            "SQUIRCLE", "ROUNDED_RECTANGLE" -> RoundedCornerShape(theme.calculatorStyle.buttonBorderRadius)
            else -> RoundedCornerShape(theme.metadata.borderRadiusGlobal)
        }
    } else {
        RoundedCornerShape(theme.metadata.borderRadiusGlobal)
    }
    val isGlass = theme.structureStyle == StructureStyleType.GLASSMORPHISM

    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { com.ixeken.nepo.features.calculator.data.SettingsRepository(context) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val view = androidx.compose.ui.platform.LocalView.current

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = 2250f
        ),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (isGlass && hazeState != null) {
                    Modifier
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = visualTokens.background,
                                blurRadius = 20.dp,
                                tint = HazeTint(visualTokens.background.copy(alpha = 0.4f))
                            )
                        )
                        .border(width = 0.5.dp, color = Color(0x20FFFFFF), shape = shape)
                } else {
                    Modifier
                }
            )
            .background(visualTokens.background)
            .clickable(
                interactionSource = interactionSource,
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
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            if (iconBold) {
                Box(contentAlignment = Alignment.Center) {
                    val offset = 0.5.dp
                    Icon(imageVector = icon, contentDescription = null, tint = visualTokens.foreground, modifier = Modifier.offset(x = -offset, y = -offset))
                    Icon(imageVector = icon, contentDescription = null, tint = visualTokens.foreground, modifier = Modifier.offset(x = offset, y = -offset))
                    Icon(imageVector = icon, contentDescription = null, tint = visualTokens.foreground, modifier = Modifier.offset(x = -offset, y = offset))
                    Icon(imageVector = icon, contentDescription = null, tint = visualTokens.foreground, modifier = Modifier.offset(x = offset, y = offset))
                    Icon(imageVector = icon, contentDescription = text, tint = visualTokens.foreground)
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = visualTokens.foreground
                )
            }
        } else {
            Text(
                text = text,
                color = visualTokens.foreground,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = fontFamily,
                    fontSize = if (fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) fontSize else MaterialTheme.typography.titleLarge.fontSize
                )
            )
        }
    }
}
