package com.ixeken.nepo.features.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * A drum-style vertical wheel picker with snapping and auto-adjusting highlight.
 *
 * Utilized for selecting formatting preferences such as decimal precision or separators.
 *
 * @param options List of text options to display.
 * @param selectedOption Current selected option.
 * @param onOptionSelected Callback triggered when a new option is highlighted or clicked.
 * @param modifier Modifier applied to the outer layout container.
 */
@Composable
fun FormattingWheelPicker(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNepoTheme.current
    val listState = rememberLazyListState()

    // Determine initial index to scroll to
    val initialIndex = options.indexOf(selectedOption).coerceAtLeast(0)

    LaunchedEffect(key1 = initialIndex) {
        listState.scrollToItem(initialIndex)
    }

    // Monitor scroll state changes to auto-adjust selection to the center-most item
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> options.getOrNull(index) }
            .distinctUntilChanged()
            .collect { item ->
                if (item != null && item != selectedOption) {
                    onOptionSelected(item)
                }
            }
    }

    Box(
        modifier = modifier
            .height(120.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.metadata.borderRadiusGlobal))
            .background(theme.colors.surfaces.bottomSheetBackground)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight bar in the middle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(theme.colors.interactiveComponents.selectButton.background.copy(alpha = 0.15f))
        )

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 40.dp), // centers the items vertically
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(options) { index, option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            onOptionSelected(option)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            theme.colors.typography.bodyPrimary
                        } else {
                            theme.colors.typography.bodySecondary
                        }
                    )
                }
            }
        }
    }
}
