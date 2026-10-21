package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.data.model.SupportedLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechRecognitionManager(
    private val context: Context,
    private val onResultRecognized: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private fun ensureRecognizer() {
        if (speechRecognizer == null && isRecognitionAvailable) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _speechError.value = null
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            _rmsLevel.value = rmsdB.coerceAtLeast(0f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
            _rmsLevel.value = 0f
        }

        override fun onError(error: Int) {
            _isListening.value = false
            _rmsLevel.value = 0f
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout."
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try speaking again."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition service is busy."
                SpeechRecognizer.ERROR_SERVER -> "Recognition server error."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Timed out."
                else -> "Speech recognition error code: $error"
            }
            _speechError.value = message
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _rmsLevel.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrEmpty()) {
                onResultRecognized(recognizedText)
            } else {
                _speechError.value = "No speech detected."
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull()?.trim()
            if (!partialText.isNullOrEmpty()) {
                onResultRecognized(partialText)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening(language: SupportedLanguage) {
        if (!isRecognitionAvailable) {
            _speechError.value = "Speech recognition service is not available on this device."
            return
        }

        mainHandler.post {
            try {
                ensureRecognizer()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.localeTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language.localeTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizer?.startListening(intent)
                _isListening.value = true
                _speechError.value = null
            } catch (e: Exception) {
                _isListening.value = false
                _speechError.value = "Failed to activate speech recognizer: ${e.localizedMessage}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _isListening.value = false
            _rmsLevel.value = 0f
        }
    }

    fun clearError() {
        _speechError.value = null
    }

    fun cleanup() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
            _isListening.value = false
        }
    }
}
