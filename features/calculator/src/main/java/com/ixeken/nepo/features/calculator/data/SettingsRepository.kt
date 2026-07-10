package com.ixeken.nepo.features.calculator.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository to persist application-wide preferences (theme, fonts, number formatting)
 * using [SharedPreferences].
 *
 * @param context Android application context.
 */
class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nepo_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "active_theme_id"
        private const val KEY_FONT = "active_font_name"
        private const val KEY_THOUSANDS_SEPARATOR = "thousands_separator"
        private const val KEY_DECIMAL_PLACES = "decimal_places"
        private const val KEY_CHECK_UPDATE_ON_START = "check_update_on_start"
        private const val KEY_CALCULATOR_MODE = "active_calculator_mode"
        private const val KEY_SOUND_FEEDBACK = "sound_feedback"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_CONVERTER_CATEGORY = "converter_category"
        private const val KEY_CONVERTER_SOURCE_PREFIX = "converter_source_"
        private const val KEY_CONVERTER_TARGET_PREFIX = "converter_target_"
        private const val KEY_CONVERTER_LAYOUT = "converter_layout"
        private const val KEY_ENABLE_CURRENCY = "enable_currency"
    }

    /**
     * Gets whether automatically checking for updates on app start is enabled.
     * Defaults to true.
     */
    fun isCheckUpdateOnStartEnabled(): Boolean {
        return prefs.getBoolean(KEY_CHECK_UPDATE_ON_START, true)
    }

    /**
     * Sets whether automatically checking for updates on app start is enabled.
     */
    fun setCheckUpdateOnStartEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CHECK_UPDATE_ON_START, enabled).apply()
    }

    /**
     * Gets the current theme ID. Defaults to "rustic_digital".
     */
    fun getThemeId(): String {
        return prefs.getString(KEY_THEME, "rustic_digital") ?: "rustic_digital"
    }

    /**
     * Sets the active theme ID.
     */
    fun setThemeId(themeId: String) {
        prefs.edit().putString(KEY_THEME, themeId).apply()
    }

    /**
     * Gets the decimal precision limit. Defaults to 11 (Max).
     */
    fun getDecimalPlaces(): Int {
        return prefs.getInt(KEY_DECIMAL_PLACES, 11)
    }

    /**
     * Sets the decimal precision limit.
     */
    fun setDecimalPlaces(places: Int) {
        prefs.edit().putInt(KEY_DECIMAL_PLACES, places).apply()
    }

    /**
     * Registers a shared preference change listener.
     */
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregisters a shared preference change listener.
     */
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Gets the active font family name. Defaults to "DM Mono".
     */
    fun getFontName(): String {
        return prefs.getString(KEY_FONT, "Theme default") ?: "Theme default"
    }

    /**
     * Sets the active font family name.
     */
    fun setFontName(fontName: String) {
        prefs.edit().putString(KEY_FONT, fontName).apply()
    }

    /**
     * Gets the thousands separator configuration. Defaults to "Comma".
     *
     * Options: "Comma", "Space", "Single quote".
     */
    fun getThousandsSeparator(): String {
        return prefs.getString(KEY_THOUSANDS_SEPARATOR, "Comma") ?: "Comma"
    }

    /**
     * Sets the thousands separator preference.
     */
    fun setThousandsSeparator(separatorOption: String) {
        prefs.edit().putString(KEY_THOUSANDS_SEPARATOR, separatorOption).apply()
    }

    /**
     * Formats a double value applying the thousands separator and formatting rules.
     *
     * @param value Double value to format.
     * @return Formatted string representation.
     */
    fun formatNumber(value: Double): String {
        val separator = when (getThousandsSeparator()) {
            "Comma" -> ","
            "Space" -> " "
            "Single quote" -> "'"
            else -> ","
        }

        val isInt = value % 1 == 0.0
        if (isInt) {
            val longVal = value.toLong()
            return formatWithSeparator(longVal.toString(), separator)
        }

        val maxDecimals = getDecimalPlaces()
        if (maxDecimals <= 10) {
            val bd = try {
                java.math.BigDecimal(value.toString()).setScale(maxDecimals, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
            } catch (e: Exception) {
                java.math.BigDecimal.valueOf(value).setScale(maxDecimals, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
            }
            val plainStr = bd.toPlainString()
            val parts = plainStr.split(".")
            val intPart = parts[0]
            val decPart = parts.getOrNull(1) ?: ""
            val formattedInt = formatWithSeparator(intPart, separator)
            return if (decPart.isNotEmpty()) "$formattedInt.$decPart" else formattedInt
        } else {
            val rawStr = value.toString()
            val parts = rawStr.split(".")
            val intPart = parts[0]
            val decPart = parts.getOrNull(1) ?: ""
            val formattedInt = formatWithSeparator(intPart, separator)
            return "$formattedInt.$decPart"
        }
    }

    private fun formatWithSeparator(intStr: String, separator: String): String {
        val sb = StringBuilder()
        var count = 0
        val isNegative = intStr.startsWith("-")
        val cleanStr = if (isNegative) intStr.substring(1) else intStr

        for (i in cleanStr.length - 1 downTo 0) {
            sb.append(cleanStr[i])
            count++
            if (count == 3 && i > 0) {
                sb.append(separator)
                count = 0
            }
        }
        val reversed = sb.reverse().toString()
        return if (isNegative) "-$reversed" else reversed
    }

    /**
     * Gets the saved active calculator mode ("BASIC" or "SCIENTIFIC").
     * Defaults to "BASIC".
     */
    fun getCalculatorMode(): String {
        return prefs.getString(KEY_CALCULATOR_MODE, "BASIC") ?: "BASIC"
    }

    /**
     * Sets the active calculator mode.
     */
    fun setCalculatorMode(mode: String) {
        prefs.edit().putString(KEY_CALCULATOR_MODE, mode).apply()
    }

    /**
     * Gets whether tap sound feedback is enabled. Defaults to false.
     */
    fun isSoundFeedbackEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_FEEDBACK, false)
    }

    /**
     * Sets whether tap sound feedback is enabled.
     */
    fun setSoundFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_FEEDBACK, enabled).apply()
    }

    /**
     * Gets whether haptic vibration feedback is enabled. Defaults to false.
     */
    fun isHapticFeedbackEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTIC_FEEDBACK, false)
    }

    /**
     * Sets whether haptic vibration feedback is enabled.
     */
    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    /**
     * Gets the saved converter category name. Defaults to "LENGTH".
     */
    fun getSelectedCategory(): String {
        return prefs.getString(KEY_CONVERTER_CATEGORY, "LENGTH") ?: "LENGTH"
    }

    /**
     * Sets the active converter category name.
     */
    fun setSelectedCategory(category: String) {
        prefs.edit().putString(KEY_CONVERTER_CATEGORY, category).apply()
    }

    /**
     * Gets the saved source unit ID for a given category.
     */
    fun getSourceUnit(category: String): String {
        val defaultUnit = when (category) {
            "LENGTH" -> "LENGTH_M"
            "WEIGHT" -> "WEIGHT_KG"
            "ENERGY" -> "ENERGY_J"
            "DATA" -> "DATA_MB"
            "VOLUME" -> "VOLUME_L"
            "AREA" -> "AREA_M2"
            "SPEED" -> "SPEED_KMH"
            "TIME" -> "TIME_S"
            "TEMPERATURE" -> "TEMP_C"
            "CURRENCY" -> "CURRENCY_USD"
            else -> "LENGTH_M"
        }
        return prefs.getString(KEY_CONVERTER_SOURCE_PREFIX + category, defaultUnit) ?: defaultUnit
    }

    /**
     * Sets the active source unit ID for a given category.
     */
    fun setSourceUnit(category: String, unitId: String) {
        prefs.edit().putString(KEY_CONVERTER_SOURCE_PREFIX + category, unitId).apply()
    }

    /**
     * Gets the saved target unit ID for a given category.
     */
    fun getTargetUnit(category: String): String {
        val defaultUnit = when (category) {
            "LENGTH" -> "LENGTH_CM"
            "WEIGHT" -> "WEIGHT_LB"
            "ENERGY" -> "ENERGY_CAL"
            "DATA" -> "DATA_GB"
            "VOLUME" -> "VOLUME_ML"
            "AREA" -> "AREA_FT2"
            "SPEED" -> "SPEED_MPH"
            "TIME" -> "TIME_MIN"
            "TEMPERATURE" -> "TEMP_F"
            "CURRENCY" -> "CURRENCY_MXN"
            else -> "LENGTH_CM"
        }
        return prefs.getString(KEY_CONVERTER_TARGET_PREFIX + category, defaultUnit) ?: defaultUnit
    }

    /**
     * Sets the active target unit ID for a given category.
     */
    fun setTargetUnit(category: String, unitId: String) {
        prefs.edit().putString(KEY_CONVERTER_TARGET_PREFIX + category, unitId).apply()
    }

    /**
     * Gets the saved unit converter layout style ("OUTSIDE", "INSIDE_SOLID", or "INSIDE_OUTLINE").
     * Defaults to "OUTSIDE".
     */
    fun getConverterLayout(): String {
        return prefs.getString(KEY_CONVERTER_LAYOUT, "OUTSIDE") ?: "OUTSIDE"
    }

    /**
     * Sets the active unit converter layout style.
     */
    fun setConverterLayout(layout: String) {
        prefs.edit().putString(KEY_CONVERTER_LAYOUT, layout).apply()
    }

    /**
     * Gets whether the currency converter category is enabled.
     * Defaults to true.
     */
    fun isCurrencyEnabled(): Boolean {
        return prefs.getBoolean(KEY_ENABLE_CURRENCY, true)
    }

    /**
     * Sets whether the currency converter category is enabled.
     */
    fun setCurrencyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLE_CURRENCY, enabled).apply()
    }
}
