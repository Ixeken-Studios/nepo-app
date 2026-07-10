package com.ixeken.nepo.features.calculator.domain

import com.ixeken.nepo.features.calculator.R

enum class UnitCategory(val displayNameRes: Int) {
    LENGTH(R.string.category_length),
    WEIGHT(R.string.category_weight),
    ENERGY(R.string.category_energy),
    DATA(R.string.category_data),
    VOLUME(R.string.category_volume),
    AREA(R.string.category_area),
    SPEED(R.string.category_speed),
    TIME(R.string.category_time),
    TEMPERATURE(R.string.category_temperature),
    CURRENCY(R.string.category_currency)
}

data class ConversionUnit(
    val id: String,
    val nameResId: Int,
    val abbrResId: Int,
    val category: UnitCategory,
    val factorToBase: Double // factor to multiply to get the base unit
)

object ConverterRegistry {
    // Base units:
    // LENGTH: Meters (m)
    // WEIGHT: Kilograms (kg)
    // ENERGY: Joules (J)
    // DATA: Bytes (B)
    // VOLUME: Liters (L)
    // AREA: Square meters (m²)
    // SPEED: Meters per second (m/s)
    // TIME: Seconds (s)
    // TEMPERATURE: Celsius (°C)
    // CURRENCY: US Dollar (USD)

    val units = listOf(
        // Length Units
        ConversionUnit("LENGTH_MM", R.string.unit_millimeters, R.string.unit_millimeters_abbr, UnitCategory.LENGTH, 0.001),
        ConversionUnit("LENGTH_CM", R.string.unit_centimeters, R.string.unit_centimeters_abbr, UnitCategory.LENGTH, 0.01),
        ConversionUnit("LENGTH_M", R.string.unit_meters, R.string.unit_meters_abbr, UnitCategory.LENGTH, 1.0),
        ConversionUnit("LENGTH_KM", R.string.unit_kilometers, R.string.unit_kilometers_abbr, UnitCategory.LENGTH, 1000.0),
        ConversionUnit("LENGTH_IN", R.string.unit_inches, R.string.unit_inches_abbr, UnitCategory.LENGTH, 0.0254),
        ConversionUnit("LENGTH_FT", R.string.unit_feet, R.string.unit_feet_abbr, UnitCategory.LENGTH, 0.3048),
        ConversionUnit("LENGTH_YD", R.string.unit_yards, R.string.unit_yards_abbr, UnitCategory.LENGTH, 0.9144),
        ConversionUnit("LENGTH_MI", R.string.unit_miles, R.string.unit_miles_abbr, UnitCategory.LENGTH, 1609.344),
        ConversionUnit("LENGTH_PC", R.string.unit_parsecs, R.string.unit_parsecs_abbr, UnitCategory.LENGTH, 3.0856775814913673e16),

        // Weight Units
        ConversionUnit("WEIGHT_MG", R.string.unit_milligrams, R.string.unit_milligrams_abbr, UnitCategory.WEIGHT, 0.000001),
        ConversionUnit("WEIGHT_G", R.string.unit_grams, R.string.unit_grams_abbr, UnitCategory.WEIGHT, 0.001),
        ConversionUnit("WEIGHT_KG", R.string.unit_kilograms, R.string.unit_kilograms_abbr, UnitCategory.WEIGHT, 1.0),
        ConversionUnit("WEIGHT_LB", R.string.unit_pounds, R.string.unit_pounds_abbr, UnitCategory.WEIGHT, 0.45359237),
        ConversionUnit("WEIGHT_OZ", R.string.unit_ounces, R.string.unit_ounces_abbr, UnitCategory.WEIGHT, 0.028349523125),

        // Energy Units
        ConversionUnit("ENERGY_J", R.string.unit_joules, R.string.unit_joules_abbr, UnitCategory.ENERGY, 1.0),
        ConversionUnit("ENERGY_KJ", R.string.unit_kilojoules, R.string.unit_kilojoules_abbr, UnitCategory.ENERGY, 1000.0),
        ConversionUnit("ENERGY_CAL", R.string.unit_calories, R.string.unit_calories_abbr, UnitCategory.ENERGY, 4.184),
        ConversionUnit("ENERGY_KCAL", R.string.unit_kilocalories, R.string.unit_kilocalories_abbr, UnitCategory.ENERGY, 4184.0),
        ConversionUnit("ENERGY_WH", R.string.unit_watthours, R.string.unit_watthours_abbr, UnitCategory.ENERGY, 3600.0),
        ConversionUnit("ENERGY_KWH", R.string.unit_kilowatthours, R.string.unit_kilowatthours_abbr, UnitCategory.ENERGY, 3600000.0),

        // Data Units
        ConversionUnit("DATA_B", R.string.unit_bytes, R.string.unit_bytes_abbr, UnitCategory.DATA, 1.0),
        ConversionUnit("DATA_KB", R.string.unit_kilobytes, R.string.unit_kilobytes_abbr, UnitCategory.DATA, 1024.0),
        ConversionUnit("DATA_MB", R.string.unit_megabytes, R.string.unit_megabytes_abbr, UnitCategory.DATA, 1048576.0),
        ConversionUnit("DATA_GB", R.string.unit_gigabytes, R.string.unit_gigabytes_abbr, UnitCategory.DATA, 1073741824.0),
        ConversionUnit("DATA_TB", R.string.unit_terabytes, R.string.unit_terabytes_abbr, UnitCategory.DATA, 1099511627776.0),

        // Volume Units
        ConversionUnit("VOLUME_ML", R.string.unit_milliliters, R.string.unit_milliliters_abbr, UnitCategory.VOLUME, 0.001),
        ConversionUnit("VOLUME_L", R.string.unit_liters, R.string.unit_liters_abbr, UnitCategory.VOLUME, 1.0),
        ConversionUnit("VOLUME_M3", R.string.unit_cubic_meters, R.string.unit_cubic_meters_abbr, UnitCategory.VOLUME, 1000.0),
        ConversionUnit("VOLUME_FL_OZ", R.string.unit_fluid_ounces, R.string.unit_fluid_ounces_abbr, UnitCategory.VOLUME, 0.0295735295625),
        ConversionUnit("VOLUME_CUP", R.string.unit_cups, R.string.unit_cups_abbr, UnitCategory.VOLUME, 0.2365882365),
        ConversionUnit("VOLUME_PT", R.string.unit_pints, R.string.unit_pints_abbr, UnitCategory.VOLUME, 0.473176473),
        ConversionUnit("VOLUME_QT", R.string.unit_quarts, R.string.unit_quarts_abbr, UnitCategory.VOLUME, 0.946352946),
        ConversionUnit("VOLUME_GAL", R.string.unit_gallons, R.string.unit_gallons_abbr, UnitCategory.VOLUME, 3.785411784),
        ConversionUnit("VOLUME_TSP", R.string.unit_teaspoons, R.string.unit_teaspoons_abbr, UnitCategory.VOLUME, 0.00492892159375),
        ConversionUnit("VOLUME_TBSP", R.string.unit_tablespoons, R.string.unit_tablespoons_abbr, UnitCategory.VOLUME, 0.01478676478125),
        ConversionUnit("VOLUME_FT3", R.string.unit_cubic_feet, R.string.unit_cubic_feet_abbr, UnitCategory.VOLUME, 28.316846592),

        // Area Units
        ConversionUnit("AREA_MM2", R.string.unit_square_millimeters, R.string.unit_square_millimeters_abbr, UnitCategory.AREA, 0.000001),
        ConversionUnit("AREA_CM2", R.string.unit_square_centimeters, R.string.unit_square_centimeters_abbr, UnitCategory.AREA, 0.0001),
        ConversionUnit("AREA_M2", R.string.unit_square_meters, R.string.unit_square_meters_abbr, UnitCategory.AREA, 1.0),
        ConversionUnit("AREA_KM2", R.string.unit_square_kilometers, R.string.unit_square_kilometers_abbr, UnitCategory.AREA, 1000000.0),
        ConversionUnit("AREA_IN2", R.string.unit_square_inches, R.string.unit_square_inches_abbr, UnitCategory.AREA, 0.00064516),
        ConversionUnit("AREA_FT2", R.string.unit_square_feet, R.string.unit_square_feet_abbr, UnitCategory.AREA, 0.09290304),
        ConversionUnit("AREA_YD2", R.string.unit_square_yards, R.string.unit_square_yards_abbr, UnitCategory.AREA, 0.83612736),
        ConversionUnit("AREA_AC", R.string.unit_acres, R.string.unit_acres_abbr, UnitCategory.AREA, 4046.8564224),
        ConversionUnit("AREA_HA", R.string.unit_hectares, R.string.unit_hectares_abbr, UnitCategory.AREA, 10000.0),

        // Speed Units
        ConversionUnit("SPEED_MPS", R.string.unit_mps, R.string.unit_mps_abbr, UnitCategory.SPEED, 1.0),
        ConversionUnit("SPEED_KMH", R.string.unit_kmh, R.string.unit_kmh_abbr, UnitCategory.SPEED, 0.2777777777777778),
        ConversionUnit("SPEED_MPH", R.string.unit_mph, R.string.unit_mph_abbr, UnitCategory.SPEED, 0.44704),
        ConversionUnit("SPEED_FPS", R.string.unit_fps, R.string.unit_fps_abbr, UnitCategory.SPEED, 0.3048),
        ConversionUnit("SPEED_KNOT", R.string.unit_knots, R.string.unit_knots_abbr, UnitCategory.SPEED, 0.5144444444444445),

        // Time Units
        ConversionUnit("TIME_MS", R.string.unit_milliseconds, R.string.unit_milliseconds_abbr, UnitCategory.TIME, 0.001),
        ConversionUnit("TIME_S", R.string.unit_seconds, R.string.unit_seconds_abbr, UnitCategory.TIME, 1.0),
        ConversionUnit("TIME_MIN", R.string.unit_minutes, R.string.unit_minutes_abbr, UnitCategory.TIME, 60.0),
        ConversionUnit("TIME_H", R.string.unit_hours, R.string.unit_hours_abbr, UnitCategory.TIME, 3600.0),
        ConversionUnit("TIME_D", R.string.unit_days, R.string.unit_days_abbr, UnitCategory.TIME, 86400.0),
        ConversionUnit("TIME_WK", R.string.unit_weeks, R.string.unit_weeks_abbr, UnitCategory.TIME, 604800.0),
        ConversionUnit("TIME_YR", R.string.unit_years, R.string.unit_years_abbr, UnitCategory.TIME, 31536000.0),

        // Temperature Units
        ConversionUnit("TEMP_C", R.string.unit_celsius, R.string.unit_celsius_abbr, UnitCategory.TEMPERATURE, 1.0),
        ConversionUnit("TEMP_F", R.string.unit_fahrenheit, R.string.unit_fahrenheit_abbr, UnitCategory.TEMPERATURE, 1.0),
        ConversionUnit("TEMP_K", R.string.unit_kelvin, R.string.unit_kelvin_abbr, UnitCategory.TEMPERATURE, 1.0),

        // Currency Units
        ConversionUnit("CURRENCY_USD", R.string.unit_usd, R.string.unit_usd_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_EUR", R.string.unit_eur, R.string.unit_eur_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_MXN", R.string.unit_mxn, R.string.unit_mxn_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_ARS", R.string.unit_ars, R.string.unit_ars_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_CLP", R.string.unit_clp, R.string.unit_clp_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_COP", R.string.unit_cop, R.string.unit_cop_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_PEN", R.string.unit_pen, R.string.unit_pen_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_BRL", R.string.unit_brl, R.string.unit_brl_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_UYU", R.string.unit_uyu, R.string.unit_uyu_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_CRC", R.string.unit_crc, R.string.unit_crc_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_BOB", R.string.unit_bob, R.string.unit_bob_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_DOP", R.string.unit_dop, R.string.unit_dop_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_GTQ", R.string.unit_gtq, R.string.unit_gtq_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_GBP", R.string.unit_gbp, R.string.unit_gbp_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_JPY", R.string.unit_jpy, R.string.unit_jpy_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_CAD", R.string.unit_cad, R.string.unit_cad_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_CNY", R.string.unit_cny, R.string.unit_cny_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_RUB", R.string.unit_rub, R.string.unit_rub_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_KPW", R.string.unit_kpw, R.string.unit_kpw_abbr, UnitCategory.CURRENCY, 1.0),
        ConversionUnit("CURRENCY_IRR", R.string.unit_irr, R.string.unit_irr_abbr, UnitCategory.CURRENCY, 1.0)
    )

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return units.filter { it.category == category }
    }

    fun getUnitById(id: String): ConversionUnit? {
        return units.find { it.id == id }
    }

    fun getDefaultSourceUnit(category: UnitCategory): ConversionUnit {
        return when (category) {
            UnitCategory.LENGTH -> getUnitById("LENGTH_M")!!
            UnitCategory.WEIGHT -> getUnitById("WEIGHT_KG")!!
            UnitCategory.ENERGY -> getUnitById("ENERGY_J")!!
            UnitCategory.DATA -> getUnitById("DATA_MB")!!
            UnitCategory.VOLUME -> getUnitById("VOLUME_L")!!
            UnitCategory.AREA -> getUnitById("AREA_M2")!!
            UnitCategory.SPEED -> getUnitById("SPEED_KMH")!!
            UnitCategory.TIME -> getUnitById("TIME_S")!!
            UnitCategory.TEMPERATURE -> getUnitById("TEMP_C")!!
            UnitCategory.CURRENCY -> getUnitById("CURRENCY_USD")!!
        }
    }

    fun getDefaultTargetUnit(category: UnitCategory): ConversionUnit {
        return when (category) {
            UnitCategory.LENGTH -> getUnitById("LENGTH_CM")!!
            UnitCategory.WEIGHT -> getUnitById("WEIGHT_LB")!!
            UnitCategory.ENERGY -> getUnitById("ENERGY_CAL")!!
            UnitCategory.DATA -> getUnitById("DATA_GB")!!
            UnitCategory.VOLUME -> getUnitById("VOLUME_ML")!!
            UnitCategory.AREA -> getUnitById("AREA_FT2")!!
            UnitCategory.SPEED -> getUnitById("SPEED_MPH")!!
            UnitCategory.TIME -> getUnitById("TIME_MIN")!!
            UnitCategory.TEMPERATURE -> getUnitById("TEMP_F")!!
            UnitCategory.CURRENCY -> getUnitById("CURRENCY_MXN")!!
        }
    }

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        if (from.category != to.category) return 0.0
        
        if (from.category == UnitCategory.TEMPERATURE) {
            // Convert 'from' to base (Celsius)
            val celsiusValue = when (from.id) {
                "TEMP_C" -> value
                "TEMP_F" -> (value - 32.0) / 1.8
                "TEMP_K" -> value - 273.15
                else -> value
            }
            
            // Convert base (Celsius) to 'to'
            return when (to.id) {
                "TEMP_C" -> celsiusValue
                "TEMP_F" -> celsiusValue * 1.8 + 32.0
                "TEMP_K" -> celsiusValue + 273.15
                else -> celsiusValue
            }
        }
        
        if (from.category == UnitCategory.CURRENCY) {
            val fromRate = CurrencyRatesManager.getRate(from.id)
            val toRate = CurrencyRatesManager.getRate(to.id)
            return value / fromRate * toRate
        }
        
        val valueInBase = value * from.factorToBase
        return valueInBase / to.factorToBase
    }
}
