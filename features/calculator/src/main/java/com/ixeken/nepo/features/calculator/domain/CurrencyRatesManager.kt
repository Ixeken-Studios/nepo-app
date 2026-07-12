package com.ixeken.nepo.features.calculator.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object CurrencyRatesManager {
    // Fallback rates relative to 1 USD (Base: USD = 1.0)
    // Updated as of 2026-07-09
    private val fallbackRates = mapOf(
        "CURRENCY_USD" to 1.0,
        "CURRENCY_EUR" to 0.87,
        "CURRENCY_MXN" to 17.54,
        "CURRENCY_ARS" to 1490.90,
        "CURRENCY_CLP" to 935.76,
        "CURRENCY_COP" to 3341.32,
        "CURRENCY_PEN" to 3.40,
        "CURRENCY_BRL" to 5.14,
        "CURRENCY_UYU" to 40.22,
        "CURRENCY_CRC" to 454.89,
        "CURRENCY_BOB" to 9.91,
        "CURRENCY_DOP" to 58.80,
        "CURRENCY_GTQ" to 7.62,
        "CURRENCY_GBP" to 0.74,
        "CURRENCY_JPY" to 162.37,
        "CURRENCY_CAD" to 1.41,
        "CURRENCY_CNY" to 6.80,
        "CURRENCY_RUB" to 90.0,
        "CURRENCY_KRW" to 1350.0
    )

    private val ratesMap = HashMap<String, Double>().apply {
        putAll(fallbackRates)
    }

    // Expose ratesDate reactively so UI updates immediately
    var ratesDate by mutableStateOf("offline")
        private set

    fun getRate(currencyId: String): Double {
        return ratesMap[currencyId] ?: fallbackRates[currencyId] ?: 1.0
    }

    suspend fun fetchLatestRates(isEnabled: Boolean) {
        // If disabled, return immediately. Strict constraint: no internet connection attempts.
        if (!isEnabled) {
            ratesDate = "offline"
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val responseMap = Gson().fromJson(responseText, Map::class.java) as Map<*, *>
                    val apiRates = responseMap["rates"] as? Map<*, *>
                    
                    if (apiRates != null) {
                        // Update rates in memory
                        apiRates.forEach { (key, value) ->
                            val currencyId = "CURRENCY_$key"
                            if (fallbackRates.containsKey(currencyId)) {
                                val rateValue = (value as? Number)?.toDouble()
                                if (rateValue != null) {
                                    ratesMap[currencyId] = rateValue
                                }
                            }
                        }

                        // Parse date
                        val utcTime = responseMap["time_last_update_utc"]?.toString() ?: ""
                        val parsedDate = if (utcTime.contains(",")) {
                            utcTime.substringAfter(",").substringBefore("00:").trim()
                        } else {
                            utcTime
                        }

                        withContext(Dispatchers.Main) {
                            ratesDate = parsedDate.ifEmpty { "2026-07-09" }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        ratesDate = "offline"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ratesDate = "offline"
                }
            }
        }
    }
}
