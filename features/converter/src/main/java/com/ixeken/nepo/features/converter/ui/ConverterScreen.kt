package com.ixeken.nepo.features.converter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import com.ixeken.nepo.core.designsystem.R
import com.composables.icons.lucide.*
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.core.designsystem.models.StructureStyleType

/**
 * Screen displaying unit converter.
 *
 * Consumes visual tokens from the dynamic [LocalNepoTheme] and applies glassmorphic modifiers
 * when [StructureStyleType.GLASSMORPHISM] is active.
 *
 * @param modifier Modifier applied to the outer layout container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onNavigateToCalculator: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentMode: String,
    onModeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNepoTheme.current
    val isGlass = theme.structureStyle == StructureStyleType.GLASSMORPHISM

    BackHandler {
        onNavigateToCalculator()
    }

    // Conversion states
    var inputValue by remember { mutableStateOf("1") }
    var isMetersToFeet by remember { mutableStateOf(true) }

    val numericInput = inputValue.toDoubleOrNull() ?: 0.0
    val outputValue = if (isMetersToFeet) {
        numericInput * 3.28084
    } else {
        numericInput * 2.20462
    }

    val fromUnit = if (isMetersToFeet) "Meters (m)" else "Kilograms (kg)"
    val toUnit = if (isMetersToFeet) "Feet (ft)" else "Pounds (lb)"
    val title = if (isMetersToFeet) "Length Converter" else "Weight Converter"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.colors.surfaces.appBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glassmorphic Card Container
        val cardShape = RoundedCornerShape(theme.metadata.borderRadiusGlobal)
        val cardModifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .then(
                if (isGlass) {
                    Modifier
                        .blur(12.dp)
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x25FFFFFF), cardShape)
                } else {
                    Modifier.background(theme.colors.surfaces.calculatorScreenBackground)
                }
            )
            .padding(24.dp)

        Column(
            modifier = cardModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top control row inside the converter card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // History Icon (Inactive in Converter, for symmetry)
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.History,
                        contentDescription = null,
                        tint = theme.colors.typography.bodySecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Modes Menu Icon Trigger (Center)
                var showModeMenu by remember { mutableStateOf(false) }
                val fontFamily = com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily.current

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .then(
                            if (isGlass) Modifier.background(Color(0x20FFFFFF), shape = androidx.compose.foundation.shape.CircleShape)
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
                        contentDescription = "Change mode",
                        tint = theme.colors.typography.screenPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    if (showModeMenu) {
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { isVisible = true }

                        val scale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0.85f,
                            animationSpec = androidx.compose.animation.core.tween(190, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
                        )
                        val alpha by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0f,
                            animationSpec = androidx.compose.animation.core.tween(165, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
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
                                                onModeChanged("BASIC")
                                                showModeMenu = false
                                                onNavigateToCalculator()
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
                                                 text = stringResource(id = R.string.settings_mode_basic),
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
                                                onModeChanged("SCIENTIFIC")
                                                showModeMenu = false
                                                onNavigateToCalculator()
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
                                                 text = stringResource(id = R.string.settings_mode_scientific),
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

                                    // Option: Calculator (return to Calculator)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                showModeMenu = false
                                                onNavigateToCalculator()
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
                                                imageVector = Lucide.Calculator,
                                                contentDescription = null,
                                                tint = theme.colors.typography.headerAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                 text = stringResource(id = R.string.settings_mode_calculator),
                                                 color = theme.colors.typography.bodyPrimary,
                                                 fontSize = 14.sp,
                                                 fontFamily = fontFamily
                                             )
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
                        tint = theme.colors.typography.screenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = title,
                color = theme.colors.typography.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Input Field
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text(text = fromUnit, color = theme.colors.typography.bodySecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = theme.colors.typography.bodyPrimary,
                    unfocusedTextColor = theme.colors.typography.bodyPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = theme.colors.typography.headerAccent,
                    unfocusedLabelColor = theme.colors.typography.bodySecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Unit Category Row
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.colors.interactiveComponents.deleteButton.background)
                    .clickable { isMetersToFeet = !isMetersToFeet }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.ArrowUpDown,
                    contentDescription = "Switch conversion category",
                    tint = theme.colors.interactiveComponents.deleteButton.foreground,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch Category",
                    color = theme.colors.interactiveComponents.deleteButton.foreground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Output Display Card
            val outputShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(outputShape)
                    .background(theme.colors.surfaces.appBackground.copy(alpha = 0.5f))
                    .border(0.5.dp, theme.colors.structuralElements.itemSeparator, outputShape)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = toUnit,
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.4f", outputValue),
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
