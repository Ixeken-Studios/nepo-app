package com.ixeken.nepo.features.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import kotlinx.coroutines.launch
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.composables.icons.lucide.*
import com.ixeken.nepo.core.designsystem.theme.LocalNepoTheme
import com.ixeken.nepo.core.designsystem.theme.LocalNepoFontFamily
import com.ixeken.nepo.features.calculator.R
import com.ixeken.nepo.features.calculator.data.SettingsRepository
import com.ixeken.nepo.features.calculator.ui.components.FormattingWheelPicker
import com.ixeken.nepo.features.calculator.ui.components.NepoButton
private val previewFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun getPreviewFontFamily(name: String): FontFamily {
    if (name == "System default") {
        return FontFamily.Default
    }
    return try {
        FontFamily(
            Font(
                googleFont = GoogleFont(name),
                fontProvider = previewFontProvider
            )
        )
    } catch (e: Exception) {
        FontFamily.Monospace
    }
}

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onClose: () -> Unit,
    onThemeChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalNepoTheme.current
    val navController = rememberNavController()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
 
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val canNavigateBack = navController.previousBackStackEntry != null
    BackHandler(enabled = true) {
        if (canNavigateBack) {
            navController.popBackStack()
        } else {
            onClose()
        }
    }
 
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.colors.surfaces.appBackground)
            .then(
                if (isLandscape) Modifier.safeDrawingPadding()
                else Modifier.statusBarsPadding()
            )
    ) {
        androidx.compose.material3.ProvideTextStyle(
            value = androidx.compose.ui.text.TextStyle(fontFamily = LocalNepoFontFamily.current)
        ) {
            NavHost(
                navController = navController,
                startDestination = "root",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = if (isLandscape) 8.dp else 24.dp,
                        vertical = 8.dp
                    ),
                enterTransition = { slideInHorizontally(animationSpec = tween(225), initialOffsetX = { it }) + fadeIn(animationSpec = tween(190)) },
                exitTransition = { slideOutHorizontally(animationSpec = tween(225), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(190)) },
                popEnterTransition = { slideInHorizontally(animationSpec = tween(225), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(190)) },
                popExitTransition = { slideOutHorizontally(animationSpec = tween(225), targetOffsetX = { it }) + fadeOut(animationSpec = tween(190)) }
            ) {
                composable("root") {
                    SettingsRootScreen(navController, settingsRepository, onClose)
                }
                composable("appearance") {
                    AppearanceScreen(navController, settingsRepository, onThemeChanged)
                }
                composable("formatting") {
                    FormattingScreen(navController, settingsRepository)
                }
                composable("about") {
                    AboutScreen(navController, settingsRepository)
                }
                composable("converter") {
                    ConverterSettingsScreen(navController, settingsRepository)
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    breadcrumbParent: String?,
    activeNode: String,
    onBackClick: () -> Unit,
    isRoot: Boolean = false
) {
    val theme = LocalNepoTheme.current
    val headerTextColor = theme.colors.typography.headerAccent
    val breadcrumbColor = theme.colors.typography.headerAccent.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (breadcrumbParent != null) {
                Text(
                    text = breadcrumbParent,
                    color = breadcrumbColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ">",
                    color = breadcrumbColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = activeNode,
                color = headerTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        // Action button (X close or back arrow)
        NepoButton(
            text = if (isRoot) stringResource(id = R.string.settings_dialog_btn_close) else stringResource(id = R.string.settings_btn_back),
            onClick = onBackClick,
            visualTokens = if (isRoot) {
                theme.colors.interactiveComponents.closeButton
            } else {
                theme.colors.interactiveComponents.backButton
            },
            icon = if (isRoot) Lucide.X else Lucide.ArrowLeft,
            iconBold = true,
            isKeyboardKey = false,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = theme.colors.typography.headerAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        if (config.showHeaderSeparators) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                color = theme.colors.structuralElements.headerSeparator,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    if (config.useCards) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(config.cardBorderRadius))
                .background(theme.colors.interactiveComponents.numbersButton.background)
                .padding(horizontal = 16.dp),
            content = content
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun MenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (icon != null && config.showMenuIcons) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.colors.typography.headerAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                color = theme.colors.typography.bodyPrimary,
                fontSize = 16.sp
            )
        }
        androidx.compose.material3.Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = "Navigate to $title",
            tint = theme.colors.typography.bodySecondary,
            modifier = Modifier.size(16.dp)
        )
    }
    if (config.showDividers && !isLast) {
        HorizontalDivider(
            color = theme.colors.structuralElements.itemSeparator,
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun SelectMenuItem(
    title: String,
    isSelected: Boolean,
    fontFamily: FontFamily? = null,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = theme.colors.typography.bodyPrimary,
            fontFamily = fontFamily,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(theme.colors.interactiveComponents.selectButton.background),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Lucide.Check,
                    contentDescription = "Selected",
                    tint = theme.colors.interactiveComponents.selectButton.foreground,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
    if (config.showDividers && !isLast) {
        HorizontalDivider(
            color = theme.colors.structuralElements.itemSeparator,
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun SettingsRootScreen(
    navController: NavController,
    settingsRepository: SettingsRepository,
    onClose: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var showPrivacy by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsHeader(breadcrumbParent = null, activeNode = stringResource(id = R.string.settings_nav_root), onBackClick = onClose, isRoot = true)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_section_general))
            SettingsCard {
                MenuItem(title = stringResource(id = R.string.settings_option_converter), icon = Lucide.ArrowLeftRight, isLast = true) {
                    navController.navigate("converter")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(stringResource(id = R.string.settings_section_preferences))
            SettingsCard {
                MenuItem(title = stringResource(id = R.string.settings_option_appearance), icon = Lucide.Palette) {
                    navController.navigate("appearance")
                }
                MenuItem(title = stringResource(id = R.string.settings_option_formatting), icon = Lucide.Binary, isLast = true) {
                    navController.navigate("formatting")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(stringResource(id = R.string.settings_section_about))
            SettingsCard {
                AboutMenuItem(
                    title = stringResource(id = R.string.app_name_about),
                    subtitle = stringResource(id = R.string.app_version_about),
                    iconResId = R.drawable.ic_action_name,
                    onClick = {
                        navController.navigate("about")
                    }
                )
                MenuItem(
                    title = stringResource(id = R.string.settings_option_privacy_notice),
                    icon = Lucide.Shield,
                    onClick = { showPrivacy = true }
                )
                CreditsMenuItem(
                    title = stringResource(id = R.string.settings_option_created_by),
                    subtitle = stringResource(id = R.string.settings_pill_made_in_mexico),
                    icon = Lucide.Heart,
                    isLast = true,
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/Ixeken-Studios")
                        } catch (e: Exception) {
                        }
                    }
                )
            }
        }
    }

    if (showPrivacy) {
        SettingsBottomSheet(
            title = stringResource(id = R.string.settings_sheet_privacy_title),
            onDismissRequest = { showPrivacy = false }
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_sheet_privacy_title))
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_privacy_paragraph_1),
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 14.sp,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.settings_privacy_paragraph_2),
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 13.sp,
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalNepoTheme.current
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
        androidx.compose.material3.ProvideTextStyle(
            value = androidx.compose.ui.text.TextStyle(fontFamily = LocalNepoFontFamily.current)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isLandscape) 48.dp else 24.dp)
                    .padding(bottom = if (isLandscape) 12.dp else 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    NepoButton(
                        text = stringResource(id = R.string.settings_dialog_btn_close),
                        onClick = animateDismiss,
                        visualTokens = theme.colors.interactiveComponents.closeButton,
                        icon = Lucide.X,
                        iconBold = true,
                        isKeyboardKey = false,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .nestedScroll(blockNestedScroll)
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun AppearanceScreen(
    navController: NavController,
    settingsRepository: SettingsRepository,
    onThemeChanged: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    var showThemesSheet by remember { mutableStateOf(false) }
    var showFontsSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsHeader(
            breadcrumbParent = stringResource(id = R.string.settings_nav_root),
            activeNode = stringResource(id = R.string.settings_nav_appearance),
            onBackClick = { navController.popBackStack() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_section_appearance))
            SettingsCard {
                MenuItem(title = stringResource(id = R.string.settings_option_themes), icon = Lucide.Sparkles) {
                    showThemesSheet = true
                }
                MenuItem(title = stringResource(id = R.string.settings_option_fonts), icon = Lucide.Type, isLast = true) {
                    showFontsSheet = true
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val fontScaleOptions = remember { listOf(0.8f, 0.9f, 1.0f, 1.2f) }
            val fontScaleLabels = listOf(
                stringResource(id = R.string.font_scale_very_small),
                stringResource(id = R.string.font_scale_small),
                stringResource(id = R.string.font_scale_default),
                stringResource(id = R.string.font_scale_big)
            )
            var fontScale by remember { mutableStateOf(settingsRepository.getFontScaleMultiplier()) }
            val currentIndex = remember(fontScale) {
                val index = fontScaleOptions.indexOf(fontScale)
                if (index != -1) index else 2
            }
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (theme.settingsStyle.showMenuIcons) {
                            androidx.compose.material3.Icon(
                                imageVector = Lucide.Type,
                                contentDescription = null,
                                tint = theme.colors.typography.headerAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.settings_option_font_scale),
                                color = theme.colors.typography.bodyPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(id = R.string.settings_desc_font_scale),
                                color = theme.colors.typography.bodySecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = fontScaleLabels[currentIndex],
                        color = theme.colors.typography.headerAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.Slider(
                        value = currentIndex.toFloat(),
                        onValueChange = { value ->
                            val targetIndex = value.toInt().coerceIn(0, 3)
                            val targetScale = fontScaleOptions[targetIndex]
                            fontScale = targetScale
                            settingsRepository.setFontScaleMultiplier(targetScale)
                            onThemeChanged()
                        },
                        valueRange = 0f..3f,
                        steps = 2,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            activeTrackColor = theme.colors.typography.headerAccent,
                            thumbColor = theme.colors.typography.headerAccent,
                            inactiveTrackColor = theme.colors.surfaces.appBackground,
                            activeTickColor = theme.colors.surfaces.appBackground,
                            inactiveTickColor = theme.colors.typography.bodySecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(stringResource(id = R.string.settings_section_keyboard_feedback))
            var isSoundEnabled by remember { mutableStateOf(settingsRepository.isSoundFeedbackEnabled()) }
            var isHapticEnabled by remember { mutableStateOf(settingsRepository.isHapticFeedbackEnabled()) }
            SettingsCard {
                ToggleMenuItem(
                    title = stringResource(id = R.string.settings_option_sound_feedback),
                    description = stringResource(id = R.string.settings_desc_sound_feedback),
                    checked = isSoundEnabled,
                    onCheckedChange = { isChecked ->
                        isSoundEnabled = isChecked
                        settingsRepository.setSoundFeedbackEnabled(isChecked)
                    }
                )
                ToggleMenuItem(
                    title = stringResource(id = R.string.settings_option_haptic_feedback),
                    description = stringResource(id = R.string.settings_desc_haptic_feedback),
                    checked = isHapticEnabled,
                    isLast = true,
                    onCheckedChange = { isChecked ->
                        isHapticEnabled = isChecked
                        settingsRepository.setHapticFeedbackEnabled(isChecked)
                    }
                )
            }
        }
    }

    if (showThemesSheet) {
        val currentTheme = settingsRepository.getThemeId()
        SettingsBottomSheet(
            title = stringResource(id = R.string.settings_nav_themes),
            onDismissRequest = { showThemesSheet = false }
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_label_select_theme))
            SettingsCard {
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_rustic_digital),
                    isSelected = currentTheme == "rustic_digital",
                    onClick = {
                        settingsRepository.setThemeId("rustic_digital")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_glassy_premium),
                    isSelected = currentTheme == "glassy_premium",
                    onClick = {
                        settingsRepository.setThemeId("glassy_premium")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_revolution_hue),
                    isSelected = currentTheme == "revolution_hue",
                    onClick = {
                        settingsRepository.setThemeId("revolution_hue")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_square_grape),
                    isSelected = currentTheme == "square_grape",
                    onClick = {
                        settingsRepository.setThemeId("square_grape")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_yellow_harmony),
                    isSelected = currentTheme == "yellow_harmony",
                    onClick = {
                        settingsRepository.setThemeId("yellow_harmony")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_bubble_tea),
                    isSelected = currentTheme == "bubble_tea",
                    onClick = {
                        settingsRepository.setThemeId("bubble_tea")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_monochromatic_elegance),
                    isSelected = currentTheme == "monochromatic_elegance",
                    onClick = {
                        settingsRepository.setThemeId("monochromatic_elegance")
                        onThemeChanged()
                    }
                )
                SelectMenuItem(
                    title = stringResource(id = R.string.settings_theme_ocean_blue),
                    isSelected = currentTheme == "ocean_blue",
                    isLast = true,
                    onClick = {
                        settingsRepository.setThemeId("ocean_blue")
                        onThemeChanged()
                    }
                )
            }
        }
    }
    if (showFontsSheet) {
        val activeFont = settingsRepository.getFontName()
        SettingsBottomSheet(
            title = stringResource(id = R.string.settings_nav_fonts),
            onDismissRequest = { showFontsSheet = false }
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_font_section_default))
            SettingsCard {
                val defaults = listOf("Theme default", "System default")
                defaults.forEachIndexed { index, fontName ->
                    val isSelected = fontName == activeFont
                    val fontFamily = remember(fontName, theme) {
                        if (fontName == "Theme default") {
                            getPreviewFontFamily(theme.metadata.defaultFontName)
                        } else {
                            getPreviewFontFamily(fontName)
                        }
                    }
                    val titleText = when (fontName) {
                        "Theme default" -> stringResource(id = R.string.settings_font_theme_default)
                        "System default" -> stringResource(id = R.string.settings_font_system_default)
                        else -> fontName
                    }
                    SelectMenuItem(
                        title = titleText,
                        isSelected = isSelected,
                        fontFamily = fontFamily,
                        isLast = index == defaults.lastIndex,
                        onClick = {
                            settingsRepository.setFontName(fontName)
                            showFontsSheet = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(stringResource(id = R.string.settings_font_section_fonts))
            SettingsCard {
                val actualFonts = listOf(
                    "Courier Prime",
                    "DM Mono",
                    "Doto",
                    "Fira Code",
                    "Google Sans Code",
                    "Google Sans Flex",
                    "JetBrains Mono",
                    "Josefin Sans",
                    "Kode Mono",
                    "Outfit",
                    "Roboto Mono",
                    "Share Tech Mono",
                    "Space Mono",
                    "VT323"
                )
                actualFonts.forEachIndexed { index, fontName ->
                    val isSelected = fontName == activeFont
                    val fontFamily = remember(fontName, theme) {
                        getPreviewFontFamily(fontName)
                    }
                    SelectMenuItem(
                        title = fontName,
                        isSelected = isSelected,
                        fontFamily = fontFamily,
                        isLast = index == actualFonts.lastIndex,
                        onClick = {
                            settingsRepository.setFontName(fontName)
                            showFontsSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormattingScreen(
    navController: NavController,
    settingsRepository: SettingsRepository
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    var selectedSeparator by remember { mutableStateOf(settingsRepository.getThousandsSeparator()) }
    var decimalPlaces by remember { mutableStateOf(settingsRepository.getDecimalPlaces()) }

    // Test value for formatting preview: 123456.789
    val previewValue = 123456.789
    val formattedPreview = remember(selectedSeparator, decimalPlaces) {
        settingsRepository.formatNumber(previewValue)
    }

    val isGlass = theme.structureStyle == com.ixeken.nepo.core.designsystem.models.StructureStyleType.GLASSMORPHISM
    val unselectedBg = theme.colors.surfaces.appBackground
    val unselectedText = theme.colors.typography.headerAccent
    val selectedBg = if (isGlass) Color.White else theme.colors.surfaces.calculatorScreenBackground
    val selectedText = if (isGlass) Color(0xFF0F1016) else theme.colors.interactiveComponents.numbersButton.background

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsHeader(
            breadcrumbParent = stringResource(id = R.string.settings_nav_root),
            activeNode = stringResource(id = R.string.settings_nav_formatting),
            onBackClick = { navController.popBackStack() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_section_number_formatting))

            // 1. Preview Card
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_label_preview),
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 12.sp,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = formattedPreview,
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Thousands Separator Selector Card
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_label_thousands_separator),
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val separatorOptions = listOf(
                            "Comma" to stringResource(id = R.string.settings_separator_comma),
                            "Space" to stringResource(id = R.string.settings_separator_space),
                            "Single quote" to stringResource(id = R.string.settings_separator_quote)
                        )

                        separatorOptions.forEach { (key, label) ->
                            val isSelected = selectedSeparator == key
                            val bg = if (isSelected) selectedBg else unselectedBg
                            val textCol = if (isSelected) selectedText else unselectedText

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedSeparator = key
                                        settingsRepository.setThousandsSeparator(key)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = textCol,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Precision Slider Card
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (theme.settingsStyle.showMenuIcons) {
                                androidx.compose.material3.Icon(
                                    imageVector = Lucide.SlidersHorizontal,
                                    contentDescription = null,
                                    tint = theme.colors.typography.headerAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(id = R.string.settings_label_precision),
                                    color = theme.colors.typography.bodyPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(id = R.string.settings_desc_precision),
                                    color = theme.colors.typography.bodySecondary,
                                    fontSize = 12.sp
                                )
                           }
                        }
                        Text(
                            text = if (decimalPlaces > 10) stringResource(id = R.string.settings_label_max) else decimalPlaces.toString(),
                            color = theme.colors.typography.bodyPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.Slider(
                        value = decimalPlaces.toFloat(),
                        onValueChange = { value ->
                            val target = value.toInt()
                            decimalPlaces = target
                            settingsRepository.setDecimalPlaces(target)
                        },
                        valueRange = 1f..11f,
                        steps = 9,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            activeTrackColor = theme.colors.typography.headerAccent,
                            thumbColor = theme.colors.typography.headerAccent,
                            inactiveTrackColor = theme.colors.surfaces.appBackground,
                            activeTickColor = theme.colors.surfaces.appBackground,
                            inactiveTickColor = theme.colors.typography.bodySecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutMenuItem(
    title: String,
    subtitle: String,
    iconResId: Int,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    val fontFamily = LocalNepoFontFamily.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (config.showMenuIcons) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = iconResId),
                    contentDescription = null,
                    tint = theme.colors.typography.headerAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = theme.colors.typography.bodyPrimary,
                    fontSize = 16.sp,
                    fontFamily = fontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.colors.surfaces.appBackground)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = subtitle,
                        color = theme.colors.typography.headerAccent,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        androidx.compose.material3.Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = "Navigate to $title info",
            tint = theme.colors.typography.bodySecondary,
            modifier = Modifier.size(16.dp)
        )
    }
    if (config.showDividers && !isLast) {
        HorizontalDivider(
            color = theme.colors.structuralElements.itemSeparator,
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun AboutScreen(
    navController: NavController,
    settingsRepository: SettingsRepository
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    var showChangelog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var checkingUpdates by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    var showInternetConfirmDialog by remember { mutableStateOf(false) }
    var pendingCheckOnStart by remember { mutableStateOf<Boolean?>(null) }
    
    var checkOnStart by remember { mutableStateOf(settingsRepository.isCheckUpdateOnStartEnabled()) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsHeader(
            breadcrumbParent = stringResource(id = R.string.settings_nav_root),
            activeNode = stringResource(id = R.string.settings_section_about),
            onBackClick = { navController.popBackStack() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_section_info_updates))
            
            SettingsCard {
                MenuItem(
                    title = stringResource(id = R.string.settings_option_view_repository),
                    icon = Lucide.Github,
                    onClick = {
                        try {
                            uriHandler.openUri("https://github.com/Ixeken-Studios/nepo-app")
                        } catch (e: Exception) {
                        }
                    }
                )

                MenuItem(
                    title = stringResource(id = R.string.settings_option_view_changelog),
                    icon = Lucide.ScrollText,
                    onClick = { showChangelog = true }
                )
                
                MenuItem(
                    title = stringResource(id = R.string.settings_option_check_updates),
                    icon = Lucide.RefreshCw,
                    onClick = {
                        showInternetConfirmDialog = true
                    }
                )
                
                ToggleMenuItem(
                    title = stringResource(id = R.string.settings_option_check_update_on_start),
                    description = stringResource(id = R.string.settings_desc_check_update_on_start),
                    checked = checkOnStart,
                    onCheckedChange = { isChecked ->
                        pendingCheckOnStart = isChecked
                    },
                    icon = Lucide.Sparkles,
                    isLast = true
                )
            }
        }
    }

    if (showChangelog) {
        SettingsBottomSheet(
            title = stringResource(id = R.string.settings_sheet_changelog),
            onDismissRequest = { showChangelog = false }
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_sheet_version_history))
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_sheet_version_latest),
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val points = listOf(
                        stringResource(id = R.string.changelog_point_1),
                        stringResource(id = R.string.changelog_point_2),
                        stringResource(id = R.string.changelog_point_3),
                        stringResource(id = R.string.changelog_point_4)
                    )
                    points.forEach { point ->
                        Text(
                            text = point,
                            color = theme.colors.typography.bodySecondary,
                            fontSize = 13.sp,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }
    }

    if (pendingCheckOnStart != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingCheckOnStart = null },
            containerColor = theme.colors.surfaces.bottomSheetBackground,
            title = {
                Text(
                    text = stringResource(id = R.string.settings_dialog_internet_confirm_title),
                    color = theme.colors.typography.bodyPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.settings_dialog_internet_confirm_desc),
                    color = theme.colors.typography.bodySecondary,
                    fontSize = 14.sp,
                    fontFamily = fontFamily
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NepoButton(
                        text = stringResource(id = R.string.settings_dialog_internet_btn_proceed),
                        onClick = {
                            val targetState = pendingCheckOnStart ?: false
                            checkOnStart = targetState
                            settingsRepository.setCheckUpdateOnStartEnabled(targetState)
                            pendingCheckOnStart = null
                        },
                        visualTokens = theme.colors.interactiveComponents.confirmButton,
                        fontSize = 14.sp,
                        padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        isKeyboardKey = false
                    )
                    NepoButton(
                        text = stringResource(id = R.string.settings_dialog_btn_close),
                        onClick = { pendingCheckOnStart = null },
                        visualTokens = theme.colors.interactiveComponents.closeButton,
                        fontSize = 14.sp,
                        padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        isKeyboardKey = false
                    )
                }
            }
        )
    }

    if (showInternetConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showInternetConfirmDialog = false },
            containerColor = theme.colors.surfaces.bottomSheetBackground,
            title = {
                Text(
                    text = stringResource(id = R.string.settings_dialog_internet_confirm_title),
                    color = theme.colors.typography.bodyPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.settings_dialog_internet_confirm_desc),
                    color = theme.colors.typography.bodySecondary,
                    fontSize = 14.sp,
                    fontFamily = fontFamily
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NepoButton(
                        text = stringResource(id = R.string.settings_dialog_internet_btn_proceed),
                        onClick = {
                            showInternetConfirmDialog = false
                            checkingUpdates = true
                            showUpdateDialog = true
                            updateResult = null
                            coroutineScope.launch {
                                updateResult = checkGitHubUpdate(context)
                                checkingUpdates = false
                            }
                        },
                        visualTokens = theme.colors.interactiveComponents.confirmButton,
                        fontSize = 14.sp,
                        padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        isKeyboardKey = false
                    )
                    NepoButton(
                        text = stringResource(id = R.string.settings_dialog_btn_close),
                        onClick = { showInternetConfirmDialog = false },
                        visualTokens = theme.colors.interactiveComponents.closeButton,
                        fontSize = 14.sp,
                        padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        isKeyboardKey = false
                    )
                }
            }
        )
    }

    NepoUpdateDialog(
        showDialog = showUpdateDialog,
        checkingUpdates = checkingUpdates,
        updateResult = updateResult,
        onDismissRequest = { showUpdateDialog = false },
        onRetryClick = {
            checkingUpdates = true
            updateResult = null
            coroutineScope.launch {
                updateResult = checkGitHubUpdate(context)
                checkingUpdates = false
            }
        }
    )
}

@Composable
private fun ToggleMenuItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isLast: Boolean = false
) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    val fontFamily = LocalNepoFontFamily.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null && config.showMenuIcons) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.colors.typography.headerAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = theme.colors.typography.bodyPrimary,
                    fontSize = 16.sp,
                    fontFamily = fontFamily
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 12.sp,
                        fontFamily = fontFamily
                    )
                }
            }
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = theme.colors.typography.headerAccent,
                uncheckedThumbColor = theme.colors.typography.bodySecondary,
                uncheckedTrackColor = theme.colors.surfaces.appBackground,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = theme.colors.structuralElements.itemSeparator
            ),
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        )
    }
    if (config.showDividers && !isLast) {
        HorizontalDivider(
            color = theme.colors.structuralElements.itemSeparator,
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun CreditsMenuItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val theme = LocalNepoTheme.current
    val config = theme.settingsStyle
    val fontFamily = LocalNepoFontFamily.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (config.showMenuIcons) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.colors.typography.headerAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = theme.colors.typography.bodyPrimary,
                    fontSize = 16.sp,
                    fontFamily = fontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.colors.surfaces.appBackground)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = subtitle,
                        color = theme.colors.typography.headerAccent,
                        fontSize = 11.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        androidx.compose.material3.Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = "Open $title link",
            tint = theme.colors.typography.bodySecondary,
            modifier = Modifier.size(16.dp)
        )
    }
    if (config.showDividers && !isLast) {
        HorizontalDivider(
            color = theme.colors.structuralElements.itemSeparator,
            thickness = 0.5.dp
        )
    }
}

sealed interface UpdateResult {
    data object UpToDate : UpdateResult
    data class NewVersion(val version: String, val downloadUrl: String, val changelog: String?) : UpdateResult
    data class FutureVersion(val version: String, val latestVersion: String) : UpdateResult
    data object Error : UpdateResult
}

private data class GitHubRelease(
    @com.google.gson.annotations.SerializedName("tag_name") val tag_name: String = "",
    @com.google.gson.annotations.SerializedName("html_url") val html_url: String = "",
    @com.google.gson.annotations.SerializedName("body") val body: String? = null,
    @com.google.gson.annotations.SerializedName("assets") val assets: List<GitHubAsset>? = null
)

private data class GitHubAsset(
    @com.google.gson.annotations.SerializedName("name") val name: String = "",
    @com.google.gson.annotations.SerializedName("browser_download_url") val browser_download_url: String = ""
)

private fun isNewerVersion(current: String, latest: String): Boolean {
    val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
    val cleanLatest = latest.removePrefix("v").removePrefix("V").trim()
    if (cleanCurrent == cleanLatest) return false
    
    val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
    val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
    
    val length = maxOf(currentParts.size, latestParts.size)
    for (i in 0 until length) {
        val currVal = currentParts.getOrElse(i) { 0 }
        val latVal = latestParts.getOrElse(i) { 0 }
        if (latVal > currVal) return true
        if (latVal < currVal) return false
    }
    return false
}

private fun isFutureVersion(current: String, latest: String): Boolean {
    val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
    val cleanLatest = latest.removePrefix("v").removePrefix("V").trim()
    if (cleanCurrent == cleanLatest) return false
    
    val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
    val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
    
    val length = maxOf(currentParts.size, latestParts.size)
    for (i in 0 until length) {
        val currVal = currentParts.getOrElse(i) { 0 }
        val latVal = latestParts.getOrElse(i) { 0 }
        if (currVal > latVal) return true
        if (currVal < latVal) return false
    }
    return false
}

suspend fun checkGitHubUpdate(context: android.content.Context): UpdateResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var connection: java.net.HttpURLConnection? = null
    try {
        val url = java.net.URL("https://api.github.com/repos/Ixeken-Studios/nepo-app/releases/latest")
        connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "Nepo-App")
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        
        val responseCode = connection.responseCode
        if (responseCode == 200) {
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val gson = com.google.gson.Gson()
            val release = gson.fromJson(responseText, GitHubRelease::class.java)
            
            val currentVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.2.1"
            } catch (e: Exception) {
                "1.2.1"
            }
            
            if (isNewerVersion(currentVersion, release.tag_name)) {
                val apkUrl = release.assets?.firstOrNull { it.name.endsWith(".apk") }?.browser_download_url ?: release.html_url
                UpdateResult.NewVersion(
                    version = release.tag_name,
                    downloadUrl = apkUrl,
                    changelog = release.body
                )
            } else if (isFutureVersion(currentVersion, release.tag_name)) {
                UpdateResult.FutureVersion(
                    version = currentVersion,
                    latestVersion = release.tag_name
                )
            } else {
                UpdateResult.UpToDate
            }
        } else {
            val errorText = try {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
            } catch (e: Exception) {
                "Failed to read error body"
            }
            android.util.Log.e("NepoUpdateCheck", "GitHub API returned status $responseCode: $errorText")
            UpdateResult.Error
        }
    } catch (e: Exception) {
        android.util.Log.e("NepoUpdateCheck", "Exception checking GitHub updates: ${e.message}", e)
        UpdateResult.Error
    } finally {
        connection?.disconnect()
    }
}

@Composable
fun NepoUpdateDialog(
    showDialog: Boolean,
    checkingUpdates: Boolean,
    updateResult: UpdateResult?,
    onDismissRequest: () -> Unit,
    onRetryClick: () -> Unit
) {
    if (!showDialog) return
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    
    val currentVersion = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.2.1"
        } catch (e: Exception) {
            "1.2.1"
        }
    }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {
            if (!checkingUpdates) onDismissRequest()
        },
        containerColor = theme.colors.surfaces.bottomSheetBackground,
        title = {
            Text(
                text = stringResource(id = R.string.settings_dialog_update_check),
                color = theme.colors.typography.bodyPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (checkingUpdates) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = theme.colors.typography.headerAccent,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.settings_dialog_checking_updates),
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 14.sp,
                        fontFamily = fontFamily
                    )
                } else {
                    when (val result = updateResult) {
                        is UpdateResult.UpToDate -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.colors.typography.headerAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Lucide.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.settings_dialog_up_to_date, currentVersion),
                                color = theme.colors.typography.bodyPrimary,
                                fontSize = 14.sp,
                                fontFamily = fontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                        is UpdateResult.FutureVersion -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.colors.typography.headerAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Lucide.Sparkles,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.settings_dialog_future_version, result.version, result.latestVersion),
                                color = theme.colors.typography.bodyPrimary,
                                fontSize = 14.sp,
                                fontFamily = fontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                        is UpdateResult.NewVersion -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.colors.typography.headerAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Lucide.Sparkles,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.settings_dialog_update_available, result.version),
                                color = theme.colors.typography.bodyPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                textAlign = TextAlign.Center
                            )
                            if (!result.changelog.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp)
                                        .background(theme.colors.surfaces.appBackground, shape = RoundedCornerShape(8.dp))
                                        .verticalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = result.changelog,
                                        color = theme.colors.typography.bodySecondary,
                                        fontSize = 12.sp,
                                        fontFamily = fontFamily
                                    )
                                }
                            }
                        }
                        is UpdateResult.Error, null -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.colors.typography.bodySecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Lucide.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.settings_dialog_update_error),
                                color = theme.colors.typography.bodyPrimary,
                                fontSize = 14.sp,
                                fontFamily = fontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!checkingUpdates) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (val result = updateResult) {
                         is UpdateResult.NewVersion -> {
                            NepoButton(
                                text = stringResource(id = R.string.settings_dialog_btn_download),
                                onClick = {
                                    onDismissRequest()
                                    try {
                                        uriHandler.openUri(result.downloadUrl)
                                    } catch (e: Exception) {}
                                },
                                visualTokens = theme.colors.interactiveComponents.confirmButton,
                                fontSize = 14.sp,
                                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                isKeyboardKey = false
                            )
                            NepoButton(
                                text = stringResource(id = R.string.settings_dialog_btn_close),
                                onClick = onDismissRequest,
                                visualTokens = theme.colors.interactiveComponents.closeButton,
                                fontSize = 14.sp,
                                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                isKeyboardKey = false
                            )
                        }
                        is UpdateResult.Error, null -> {
                            NepoButton(
                                text = stringResource(id = R.string.settings_dialog_btn_retry),
                                onClick = onRetryClick,
                                visualTokens = theme.colors.interactiveComponents.confirmButton,
                                fontSize = 14.sp,
                                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                isKeyboardKey = false
                            )
                            NepoButton(
                                text = stringResource(id = R.string.settings_dialog_btn_close),
                                onClick = onDismissRequest,
                                visualTokens = theme.colors.interactiveComponents.closeButton,
                                fontSize = 14.sp,
                                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                isKeyboardKey = false
                            )
                        }
                        is UpdateResult.UpToDate, is UpdateResult.FutureVersion -> {
                            NepoButton(
                                text = stringResource(id = R.string.settings_dialog_btn_ok),
                                onClick = onDismissRequest,
                                visualTokens = theme.colors.interactiveComponents.confirmButton,
                                fontSize = 14.sp,
                                padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                isKeyboardKey = false
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ConverterSettingsScreen(
    navController: NavController,
    settingsRepository: SettingsRepository
) {
    val theme = LocalNepoTheme.current
    val fontFamily = LocalNepoFontFamily.current
    var isCurrencyEnabled by remember { mutableStateOf(settingsRepository.isCurrencyEnabled()) }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsHeader(
            breadcrumbParent = stringResource(id = R.string.settings_nav_root),
            activeNode = stringResource(id = R.string.settings_option_converter),
            onBackClick = { navController.popBackStack() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(id = R.string.settings_section_general))
            SettingsCard {
                ToggleMenuItem(
                    title = stringResource(id = R.string.settings_option_enable_currency),
                    description = stringResource(id = R.string.settings_desc_enable_currency),
                    checked = isCurrencyEnabled,
                    isLast = true,
                    onCheckedChange = { isChecked ->
                        isCurrencyEnabled = isChecked
                        settingsRepository.setCurrencyEnabled(isChecked)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(stringResource(id = R.string.settings_section_converter_layout))
            var selectedLayout by remember { mutableStateOf(settingsRepository.getConverterLayout()) }
            val isGlass = theme.structureStyle == com.ixeken.nepo.core.designsystem.models.StructureStyleType.GLASSMORPHISM
            val unselectedBg = theme.colors.surfaces.appBackground
            val unselectedText = theme.colors.typography.headerAccent
            val selectedBg = if (isGlass) Color.White else theme.colors.surfaces.calculatorScreenBackground
            val selectedText = if (isGlass) Color(0xFF0F1016) else theme.colors.interactiveComponents.numbersButton.background

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_label_converter_layout),
                        color = theme.colors.typography.bodyPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = fontFamily
                    )
                    Text(
                        text = stringResource(id = R.string.settings_desc_converter_layout),
                        color = theme.colors.typography.bodySecondary,
                        fontSize = 12.sp,
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val layoutOptions = listOf(
                            "OUTSIDE" to stringResource(id = R.string.settings_converter_layout_outside),
                            "INSIDE_SOLID" to stringResource(id = R.string.settings_converter_layout_inside_solid),
                            "INSIDE_OUTLINE" to stringResource(id = R.string.settings_converter_layout_inside_outline)
                        )

                        layoutOptions.forEach { (key, label) ->
                            val isSelected = selectedLayout == key
                            val bg = if (isSelected) selectedBg else unselectedBg
                            val textCol = if (isSelected) selectedText else unselectedText

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedLayout = key
                                        settingsRepository.setConverterLayout(key)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = textCol,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    minLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
