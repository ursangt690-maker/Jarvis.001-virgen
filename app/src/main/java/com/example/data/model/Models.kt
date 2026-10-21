package com.example.data.model

import java.util.UUID

enum class AssistantStatus(val label: String) {
    IDLE("AWAITING COMMAND"),
    LISTENING("LISTENING…"),
    THINKING("PROCESSING…"),
    SPEAKING("SPEAKING…"),
    ERROR("SYSTEM ANOMALY")
}

enum class SupportedLanguage(
    val code: String,
    val localeTag: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "en-US", "English", "English"),
    NEPALI("ne", "ne-NP", "Nepali", "नेपाली"),
    HINDI("hi", "hi-IN", "Hindi", "हिन्दी");

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val isSpeaking: Boolean = false
)

data class BatteryInfo(
    val level: Int = -1,
    val isCharging: Boolean = false,
    val statusText: String = "Unknown",
    val powerSource: String = "Battery",
    val isAvailable: Boolean = false
)

object GeminiModelConstants {
    const val MODEL_FLASH = "gemini-3.5-flash"
    const val MODEL_PRO = "gemini-3.1-pro-preview"
    const val MODEL_LITE = "gemini-3.1-flash-lite-preview"

    val AVAILABLE_MODELS = listOf(
        MODEL_FLASH,
        MODEL_PRO,
        MODEL_LITE
    )
}
