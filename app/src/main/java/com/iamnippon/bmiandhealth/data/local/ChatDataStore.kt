package com.iamnippon.bmiandhealth.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iamnippon.bmiandhealth.ui.chat.ChatMessage
import kotlinx.coroutines.flow.first

private val Context.chatDataStore by preferencesDataStore("chat_prefs")

class ChatDataStore(private val context: Context) {

    private val gson = Gson()
    private val CHAT_KEY = stringPreferencesKey("chat_history")

    suspend fun save(messages: List<ChatMessage>) {
        context.chatDataStore.edit { prefs ->
            prefs[CHAT_KEY] = gson.toJson(messages)
        }
    }

    suspend fun load(): List<ChatMessage> {
        val prefs = context.chatDataStore.data.first()
        val json = prefs[CHAT_KEY] ?: return emptyList()

        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun clear() {
        context.chatDataStore.edit { prefs ->
            prefs.remove(CHAT_KEY)
        }
    }
}
