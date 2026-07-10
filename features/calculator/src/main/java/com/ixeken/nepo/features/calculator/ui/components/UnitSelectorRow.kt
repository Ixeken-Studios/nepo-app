package com.ixeken.nepo.features.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeftRight
import com.composables.icons.lucide.Lucide
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.features.calculator.domain.ConversionUnit

@Composable
fun UnitSelectorRow(
    sourceUnit: ConversionUnit,
    targetUnit: ConversionUnit,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier,
    layout: String = "OUTSIDE"
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    
    val isInside = layout == "INSIDE_SOLID" || layout == "INSIDE_OUTLINE"
    
    val buttonBg = when (layout) {
        "INSIDE_OUTLINE" -> Color.Transparent
        else -> theme.colors.interactiveComponents.numbersButton.background
    }
    
    val buttonFg = when (layout) {
        "INSIDE_OUTLINE" -> theme.colors.typography.screenPrimary
        else -> theme.colors.interactiveComponents.numbersButton.foreground
    }
    
    val accentColor = if (isInside) {
        theme.colors.typography.screenPrimary
    } else {
        theme.colors.typography.headerAccent
    }
    
    val shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal)
    
    val borderModifier = if (layout == "INSIDE_OUTLINE") {
        Modifier.border(1.dp, theme.colors.typography.screenPrimary, shape)
    } else {
        Modifier
    }
    
    val buttonHeight = if (isInside) 40.dp else 48.dp
    val horizontalPadding = if (isInside) 4.dp else 16.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Source Unit Button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(buttonHeight)
                .clip(shape)
                .background(buttonBg)
                .then(borderModifier)
                .clickable { onSourceClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = sourceUnit.nameResId),
                color = buttonFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Swap Button
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSwapClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.ArrowLeftRight,
                contentDescription = "Swap units",
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Target Unit Button
        Box(
            modifier = Modifier
                .weight(1f)
                .height(buttonHeight)
                .clip(shape)
                .background(buttonBg)
                .then(borderModifier)
                .clickable { onTargetClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = targetUnit.nameResId),
                color = buttonFg,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
