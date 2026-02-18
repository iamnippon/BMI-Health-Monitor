package com.iamnippon.bmiandhealth.data.repository

import android.content.Context
import com.iamnippon.bmiandhealth.data.local.BmiDataStore
import com.iamnippon.bmiandhealth.domain.model.BmiResult

class BmiRepository private constructor(
    private val dataStore: BmiDataStore
) {

    private val history = mutableListOf<BmiResult>()

    companion object {
        @Volatile
        private var INSTANCE: BmiRepository? = null

        fun getInstance(context: Context): BmiRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BmiRepository(
                    BmiDataStore(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }

    suspend fun loadHistory() {
        history.clear()
        history.addAll(dataStore.loadHistory())
    }

    suspend fun saveResult(result: BmiResult) {

        // 🔥 Always reload from storage first
        loadHistory()

        history.add(0, result)

        if (history.size > 20) {
            history.removeAt(history.lastIndex)
        }

        dataStore.saveHistory(history)
    }

    suspend fun deleteResult(result: BmiResult) {
        history.remove(result)
        dataStore.saveHistory(history)
    }

    suspend fun clearHistory() {
        history.clear()              // 🔥 clear memory
        dataStore.clearHistory()     // 🔥 clear DataStore
    }

    fun getHistory(): List<BmiResult> = history.toList()
}
