package com.ixeken.nepo.features.calculator.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.activity.compose.BackHandler
import com.composables.icons.lucide.*
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.features.calculator.data.HistoryEntry
import com.ixeken.nepo.features.calculator.ui.components.NepoButton
import com.ixeken.nepo.features.calculator.data.SettingsRepository
import com.ixeken.nepo.features.calculator.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

/**
 * Bottom Sheet displaying calculation history.
 *
 * Implements MVI-aligned logic with a Normal mode and Edit mode (supporting multi-selection,
 * clear all, and bulk deletion).
 *
 * @param entries List of calculated history logs.
 * @param onRowClicked Callback triggered in Normal mode to load an expression into the calculator.
 * @param onDeleteEntries Callback to delete selected entries.
 * @param onClearAll Callback to wipe out the whole database.
 * @param onDismissRequest Callback to dismiss the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    entries: List<HistoryEntry>,
    settingsRepository: SettingsRepository,
    onRowClicked: (String) -> Unit,
    onDeleteEntries: (Set<String>) -> Unit,
    onClearAll: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val animateDismiss = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
        Unit
    }
    
    val blockNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return available
            }
        }
    }
    
    var isEditMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateOf(setOf<String>()) }

    BackHandler(enabled = isEditMode) {
        isEditMode = false
        selectedIds.value = emptySet()
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = theme.colors.surfaces.bottomSheetBackground,
        shape = RoundedCornerShape(
            topStart = theme.settingsStyle.cardBorderRadius,
            topEnd = theme.settingsStyle.cardBorderRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .then(
                    if (isLandscape) Modifier.safeDrawingPadding()
                    else Modifier
                )
                .padding(horizontal = if (isLandscape) 8.dp else 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isEditMode) {
                    // Edit Mode Button (Capsule shaped, matching mockup)
                    val editButtonTokens = remember(theme) {
                        com.ixeken.nepo.core.designsystem.models.ComponentVisualTokens(
                            foreground = theme.colors.typography.bodyPrimary,
                            background = theme.colors.interactiveComponents.confirmButton.background
                        )
                    }
                    NepoButton(
                        text = stringResource(id = R.string.history_btn_edit),
                        onClick = { isEditMode = true },
                        visualTokens = editButtonTokens,
                        fontSize = 14.sp,
                        padding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        isKeyboardKey = false,
                        modifier = Modifier
                            .width(72.dp)
                            .height(36.dp)
                    )

                    // Close Button
                    NepoButton(
                        text = stringResource(id = R.string.settings_dialog_btn_close),
                        onClick = animateDismiss,
                        visualTokens = theme.colors.interactiveComponents.closeButton,
                        icon = Lucide.X,
                        iconBold = true,
                        isKeyboardKey = false,
                        modifier = Modifier
                            .width(36.dp)
                            .height(36.dp)
                    )
                } else {
                    // Done Button (Circular, theme visual done style, checkmark icon)
                    NepoButton(
                        text = stringResource(id = R.string.history_btn_done),
                        onClick = {
                            isEditMode = false
                            selectedIds.value = emptySet()
                        },
                        visualTokens = theme.colors.interactiveComponents.doneButton,
                        icon = Lucide.Check,
                        iconBold = true,
                        isKeyboardKey = false,
                        modifier = Modifier
                            .width(36.dp)
                            .height(36.dp)
                    )

                    // Clear All / Delete (N) dynamic pill button
                    val selectedCount = selectedIds.value.size
                    val clearAllText = stringResource(id = R.string.history_btn_clear_all)
                    val deleteQtyText = stringResource(id = R.string.history_btn_delete_quantity, selectedCount)
                    val actionText = if (selectedCount == 0) clearAllText else deleteQtyText
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(theme.colors.interactiveComponents.confirmButton.background.copy(alpha = 0.8f))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                if (selectedCount == 0) {
                                    onClearAll()
                                } else {
                                    onDeleteEntries(selectedIds.value)
                                    selectedIds.value = emptySet()
                                }
                                isEditMode = false
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = actionText,
                            color = theme.colors.typography.headerAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (entries.isEmpty()) {
                // Empty History State with fixed height to display correctly in partially expanded state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Lucide.History,
                        contentDescription = "Empty history icon",
                        tint = theme.colors.typography.bodySecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.history_title_no_history),
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 16.sp,
                        fontFamily = fontFamily
                    )
                }
            } else {
                // Cronological History List
                val grouped = remember(entries) { groupHistory(entries) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(blockNestedScroll)
                ) {
                    grouped.forEach { (groupKey, groupItems) ->
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                val groupTitle = when (groupKey) {
                                    "LATEST" -> stringResource(id = R.string.history_group_latest)
                                    "PAST_30_DAYS" -> stringResource(id = R.string.history_group_past_30_days)
                                    "OLDER" -> stringResource(id = R.string.history_group_older)
                                    else -> groupKey
                                }
                                Text(
                                    text = groupTitle,
                                    color = theme.colors.typography.headerAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(
                                    color = theme.colors.structuralElements.headerSeparator,
                                    thickness = 1.dp
                                )
                            }
                        }

                        items(groupItems, key = { it.id }) { item ->
                            val isSelected = selectedIds.value.contains(item.id)
                            val paddingOffset by animateDpAsState(targetValue = if (isEditMode) 40.dp else 0.dp)

                            Row(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (isEditMode) {
                                            selectedIds.value = if (isSelected) {
                                                selectedIds.value - item.id
                                            } else {
                                                selectedIds.value + item.id
                                            }
                                        } else {
                                            onRowClicked(item.expression)
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditMode) {
                                    // Selection circle
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    theme.colors.interactiveComponents.selectButton.background
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = theme.colors.typography.bodySecondary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            androidx.compose.material3.Icon(
                                                imageVector = Lucide.Check,
                                                contentDescription = "Selected",
                                                tint = theme.colors.interactiveComponents.selectButton.foreground,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.expression,
                                        color = theme.colors.typography.bodySecondary,
                                        fontSize = 14.sp,
                                        fontFamily = fontFamily,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val formattedResult = remember(item.result) {
                                        try {
                                            settingsRepository.formatNumber(item.result.toDouble())
                                        } catch (e: Exception) {
                                            item.result
                                        }
                                    }
                                    Text(
                                        text = formattedResult,
                                        color = theme.colors.typography.bodyPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFamily,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            HorizontalDivider(
                                color = theme.colors.structuralElements.itemSeparator,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun groupHistory(entries: List<HistoryEntry>): Map<String, List<HistoryEntry>> {
    val now = System.currentTimeMillis()
    val oneDay = 24 * 60 * 60 * 1000L
    val thirtyDays = 30 * 24 * 60 * 60 * 1000L
    
    val groups = mutableMapOf<String, MutableList<HistoryEntry>>()
    groups["LATEST"] = mutableListOf()
    groups["PAST_30_DAYS"] = mutableListOf()
    groups["OLDER"] = mutableListOf()
    
    for (entry in entries) {
        val diff = now - entry.timestamp
        when {
            diff < oneDay -> groups["LATEST"]?.add(entry)
            diff < thirtyDays -> groups["PAST_30_DAYS"]?.add(entry)
            else -> groups["OLDER"]?.add(entry)
        }
    }
    return groups.filterValues { it.isNotEmpty() }
}


