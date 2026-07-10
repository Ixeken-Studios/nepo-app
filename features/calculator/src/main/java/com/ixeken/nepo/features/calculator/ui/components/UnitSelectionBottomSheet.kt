package com.ixeken.nepo.features.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.ixeken.nepo.core.designsystem.models.StructureStyleType
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.features.calculator.R
import com.ixeken.nepo.features.calculator.domain.ConversionUnit
import com.ixeken.nepo.features.calculator.domain.ConverterRegistry
import com.ixeken.nepo.features.calculator.domain.UnitCategory

import kotlinx.coroutines.launch
import com.ixeken.nepo.features.calculator.ui.components.NepoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectionBottomSheet(
    title: String,
    selectedUnit: ConversionUnit,
    currentCategory: UnitCategory,
    onUnitSelected: (ConversionUnit) -> Unit,
    onCategorySelected: (UnitCategory) -> Unit,
    onDismissRequest: () -> Unit,
    isCurrencyEnabled: Boolean = true
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
    
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = theme.colors.surfaces.bottomSheetBackground,
        shape = RoundedCornerShape(
            topStart = theme.settingsStyle.cardBorderRadius,
            topEnd = theme.settingsStyle.cardBorderRadius
        ),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = theme.colors.structuralElements.itemSeparator.copy(alpha = 0.5f)
            )
        }
    ) {
        androidx.compose.material3.ProvideTextStyle(
            value = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.units_title),
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 14.sp,
                        fontFamily = fontFamily
                    )
                }
                
                NepoButton(
                    text = stringResource(id = R.string.settings_dialog_btn_close),
                    onClick = animateDismiss,
                    visualTokens = theme.colors.interactiveComponents.closeButton,
                    icon = Lucide.X,
                    iconBold = true,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Search Bar
            var searchQuery by remember { mutableStateOf("") }
            val context = LocalContext.current
            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(text = stringResource(id = R.string.search_unit_placeholder), color = theme.colors.typography.bodySecondary) },
                singleLine = true,
                leadingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                imageVector = Lucide.ChevronLeft,
                                contentDescription = "Clear search",
                                tint = theme.colors.typography.bodySecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else null,
                trailingIcon = {
                    Icon(
                        imageVector = Lucide.Search,
                        contentDescription = null,
                        tint = theme.colors.typography.bodySecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = theme.colors.typography.bodyPrimary,
                    unfocusedTextColor = theme.colors.typography.bodyPrimary,
                    focusedBorderColor = theme.colors.typography.headerAccent,
                    unfocusedBorderColor = theme.colors.structuralElements.itemSeparator,
                    focusedContainerColor = theme.colors.interactiveComponents.numbersButton.background.copy(alpha = 0.5f),
                    unfocusedContainerColor = theme.colors.interactiveComponents.numbersButton.background.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(theme.metadata.borderRadiusGlobal),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Categories Carousel
            var activeCategory by remember { mutableStateOf(currentCategory) }
            val categories = remember(isCurrencyEnabled) {
                UnitCategory.values()
                    .filter { isCurrencyEnabled || it != UnitCategory.CURRENCY }
                    .sortedBy { context.getString(it.displayNameRes) }
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == activeCategory && searchQuery.isEmpty()
                    val buttonColor = if (isSelected) {
                        theme.colors.surfaces.calculatorScreenBackground
                    } else {
                        theme.colors.interactiveComponents.numbersButton.background
                    }
                    val textColor = if (isSelected) {
                        theme.colors.typography.screenPrimary
                    } else {
                        theme.colors.typography.headerAccent
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(theme.metadata.borderRadiusGlobal))
                            .background(buttonColor)
                            .clickable {
                                searchQuery = ""
                                activeCategory = category
                                onCategorySelected(category)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = category.displayNameRes),
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Units Card Container
            val filteredUnits = remember(searchQuery, activeCategory, isCurrencyEnabled) {
                val baseList = if (searchQuery.isEmpty()) {
                    ConverterRegistry.getUnitsForCategory(activeCategory)
                } else {
                    ConverterRegistry.units.filter {
                        context.getString(it.nameResId).contains(searchQuery, ignoreCase = true) ||
                        context.getString(it.abbrResId).contains(searchQuery, ignoreCase = true)
                    }
                }
                baseList.filter { isCurrencyEnabled || it.category != UnitCategory.CURRENCY }
            }
            
            val cardShape = RoundedCornerShape(theme.settingsStyle.cardBorderRadius)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.colors.interactiveComponents.numbersButton.background, shape = cardShape)
                    .then(
                        if (theme.structureStyle == StructureStyleType.GLASSMORPHISM) {
                            Modifier.border(0.5.dp, theme.colors.structuralElements.itemSeparator, cardShape)
                        } else Modifier
                    )
                    .padding(vertical = 4.dp)
            ) {
                if (filteredUnits.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Lucide.Info,
                            contentDescription = null,
                            tint = theme.colors.typography.bodySecondary.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.search_unit_no_results),
                            color = theme.colors.typography.bodySecondary,
                            fontSize = 14.sp,
                            fontFamily = fontFamily,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .nestedScroll(blockNestedScroll)
                    ) {
                        items(filteredUnits) { unit ->
                            val isSelected = unit.id == selectedUnit.id
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (unit.category != activeCategory) {
                                            activeCategory = unit.category
                                            onCategorySelected(unit.category)
                                        }
                                        onUnitSelected(unit)
                                        onDismissRequest()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(id = unit.nameResId),
                                        color = theme.colors.typography.bodyPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = fontFamily
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(id = unit.abbrResId),
                                        color = theme.colors.typography.bodySecondary,
                                        fontSize = 13.sp,
                                        fontFamily = fontFamily
                                    )
                                }
                                
                                if (isSelected) {
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
    }
}
}
