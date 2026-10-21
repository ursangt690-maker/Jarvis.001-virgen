package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.GeminiModelConstants
import com.example.data.model.SupportedLanguage

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_ai_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL = "gemini_selected_model"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_SPEECH_RATE = "tts_speech_rate"
        private const val KEY_PITCH = "tts_pitch"
        private const val KEY_VOICE_NAME = "tts_voice_name"
        private const val KEY_AUTO_SPEAK = "auto_speak_responses"
    }

    fun getApiKey(): String {
        val storedKey = prefs.getString(KEY_API_KEY, null)?.trim()
        if (!storedKey.isNullOrEmpty()) {
            return storedKey
        }
        // Fall back to BuildConfig.GEMINI_API_KEY if configured with real key
        return try {
            val key = BuildConfig.GEMINI_API_KEY.trim()
            if (key == "MY_GEMINI_API_KEY" || key.isEmpty()) "" else key
        } catch (_: Throwable) {
            ""
        }
    }

    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_MODEL, GeminiModelConstants.MODEL_FLASH)
            ?: GeminiModelConstants.MODEL_FLASH
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
    }

    fun getLanguage(): SupportedLanguage {
        val code = prefs.getString(KEY_LANGUAGE, SupportedLanguage.ENGLISH.code) ?: "en"
        return SupportedLanguage.fromCode(code)
    }

    fun setLanguage(language: SupportedLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun getSpeechRate(): Float {
        return prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
    }

    fun getPitch(): Float {
        return prefs.getFloat(KEY_PITCH, 1.0f)
    }

    fun setPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_PITCH, pitch).apply()
    }

    fun getTtsVoiceName(): String? {
        return prefs.getString(KEY_VOICE_NAME, null)
    }

    fun setTtsVoiceName(voiceName: String?) {
        prefs.edit().putString(KEY_VOICE_NAME, voiceName).apply()
    }

    fun isAutoSpeakEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SPEAK, true)
    }

    fun setAutoSpeakEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
    }
}
