package com.ixeken.nepo.features.calculator.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Data class representing a single math calculation record in the history list.
 *
 * @property id Unique identifier of the entry.
 * @property expression The math formula evaluated.
 * @property result The computed output value.
 * @property timestamp Time of evaluation in milliseconds.
 */
data class HistoryEntry(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("expression")
    val expression: String,
    @SerializedName("result")
    val result: String,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Repository handling saving, deletion, and retrieval of [HistoryEntry] items.
 *
 * Persists the entries as a JSON string inside [SharedPreferences].
 *
 * @param context Android application context.
 */
class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nepo_history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HISTORY = "history_entries"
    }

    /**
     * Retrieves all saved history entries, sorted from newest to oldest.
     */
    fun getHistory(): List<HistoryEntry> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = gson.fromJson(json, Array<HistoryEntry>::class.java) ?: return emptyList()
            array.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Appends a new calculation record to the persistent history.
     *
     * @param expression The math formula.
     * @param result The computed output value.
     */
    fun addEntry(expression: String, result: String) {
        val currentList = getHistory().toMutableList()
        currentList.add(0, HistoryEntry(expression = expression, result = result))
        saveHistory(currentList)
    }

    /**
     * Removes specified entries from the history database.
     *
     * @param ids Set of entry IDs to delete.
     */
    fun deleteEntries(ids: Set<String>) {
        val currentList = getHistory().filterNot { it.id in ids }
        saveHistory(currentList)
    }

    /**
     * Erases the entire history database.
     */
    fun clearAll() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory(list: List<HistoryEntry>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
}
