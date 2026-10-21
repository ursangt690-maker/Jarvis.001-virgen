package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.model.SupportedLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(
    private val context: Context,
    private val onSpeechStatusChanged: (isSpeaking: Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastSpokenText = MutableStateFlow("")
    val lastSpokenText: StateFlow<String> = _lastSpokenText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices: StateFlow<List<Voice>> = _availableVoices.asStateFlow()

    private var currentRate: Float = 1.0f
    private var currentPitch: Float = 1.0f
    private var preferredVoiceName: String? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to initialize TTS engine: ${e.localizedMessage}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts
            if (engine != null) {
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        onSpeechStatusChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeechStatusChanged(false)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeechStatusChanged(false)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        onSpeechStatusChanged(false)
                        _errorMessage.value = "TTS error code: $errorCode"
                    }
                })

                try {
                    val voices = engine.voices?.toList() ?: emptyList()
                    _availableVoices.value = voices
                } catch (_: Exception) {}

                engine.setSpeechRate(currentRate)
                engine.setPitch(currentPitch)

                _isInitialized.value = true
                _errorMessage.value = null
            }
        } else {
            _isInitialized.value = false
            _errorMessage.value = "Text-to-Speech initialization failed (Status $status)."
        }
    }

    fun applySettings(rate: Float, pitch: Float, voiceName: String?) {
        currentRate = rate
        currentPitch = pitch
        preferredVoiceName = voiceName

        tts?.let { engine ->
            engine.setSpeechRate(rate)
            engine.setPitch(pitch)
            if (!voiceName.isNullOrEmpty()) {
                val voice = engine.voices?.find { it.name == voiceName }
                if (voice != null) {
                    try {
                        engine.voice = voice
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun getLocaleForLanguage(language: SupportedLanguage): Locale {
        return when (language) {
            SupportedLanguage.ENGLISH -> Locale.US
            SupportedLanguage.NEPALI -> Locale.forLanguageTag("ne-NP")
            SupportedLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
        }
    }

    fun isLanguageSupported(language: SupportedLanguage): Boolean {
        val engine = tts ?: return false
        val locale = getLocaleForLanguage(language)
        val result = engine.isLanguageAvailable(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String, language: SupportedLanguage): Boolean {
        val cleanText = text.replace(Regex("[*#`_~]"), "").trim()
        if (cleanText.isEmpty()) return false

        val engine = tts
        if (engine == null || !_isInitialized.value) {
            _errorMessage.value = "Text-to-Speech engine is not initialized yet."
            return false
        }

        // Stop current speech first to prevent overlapping sessions
        engine.stop()

        val locale = getLocaleForLanguage(language)
        val langResult = engine.isLanguageAvailable(locale)

        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Attempt fallback to English or notify
            if (language != SupportedLanguage.ENGLISH) {
                engine.language = Locale.US
                _errorMessage.value = "${language.displayName} TTS voice pack not installed on device. Falling back to English."
            } else {
                _errorMessage.value = "English TTS voice pack missing on device."
                return false
            }
        } else {
            engine.language = locale
            _errorMessage.value = null
        }

        if (!preferredVoiceName.isNullOrEmpty()) {
            val voice = engine.voices?.find { it.name == preferredVoiceName }
            if (voice != null) {
                try {
                    engine.voice = voice
                } catch (_: Exception) {}
            }
        }

        _lastSpokenText.value = cleanText
        val utteranceId = UUID.randomUUID().toString()
        val result = engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        onSpeechStatusChanged(false)
    }

    fun repeat(language: SupportedLanguage): Boolean {
        val last = _lastSpokenText.value
        return if (last.isNotEmpty()) {
            speak(last, language)
        } else {
            speak("No prior vocal transmission recorded, sir.", language)
        }
    }

    fun cleanup() {
        stop()
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
    }
}
