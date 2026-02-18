package com.iamnippon.bmiandhealth.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iamnippon.bmiandhealth.domain.model.BmiResult
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "bmi_store")

class BmiDataStore(private val context: Context) {

    private val HISTORY_KEY = stringPreferencesKey("bmi_history")

    suspend fun saveHistory(list: List<BmiResult>) {
        context.dataStore.edit { prefs ->
            prefs[HISTORY_KEY] = Gson().toJson(list)
        }
    }

    suspend fun loadHistory(): List<BmiResult> {
        val json = context.dataStore.data.first()[HISTORY_KEY]
            ?: return emptyList()

        val type = object : TypeToken<List<BmiResult>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(HISTORY_KEY)
        }
    }
}
