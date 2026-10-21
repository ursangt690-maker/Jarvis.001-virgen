package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.NoteEntity
import com.example.data.local.PreferencesManager
import com.example.data.model.AssistantStatus
import com.example.data.model.ChatMessage
import com.example.data.model.SupportedLanguage
import com.example.data.remote.GeminiRepository
import com.example.service.BatteryMonitor
import com.example.service.FlashlightManager
import com.example.service.SpeechRecognitionManager
import com.example.service.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class JarvisScreen {
    HOME,
    CHAT,
    NOTES,
    SETTINGS
}

data class ConnectionTestState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val geminiRepo = GeminiRepository()
    private val noteDao = AppDatabase.getInstance(application).noteDao()

    val batteryMonitor = BatteryMonitor(application)
    val flashlightManager = FlashlightManager(application)

    // Current Screen Navigation
    private val _currentScreen = MutableStateFlow(JarvisScreen.HOME)
    val currentScreen: StateFlow<JarvisScreen> = _currentScreen.asStateFlow()

    // Assistant Status
    private val _assistantStatus = MutableStateFlow(AssistantStatus.IDLE)
    val assistantStatus: StateFlow<AssistantStatus> = _assistantStatus.asStateFlow()

    // Active Language
    private val _currentLanguage = MutableStateFlow(prefs.getLanguage())
    val currentLanguage: StateFlow<SupportedLanguage> = _currentLanguage.asStateFlow()

    // Gemini Settings
    private val _apiKey = MutableStateFlow(prefs.getApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(prefs.getSelectedModel())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    // Connection Diagnostic State
    private val _connectionTestState = MutableStateFlow(ConnectionTestState())
    val connectionTestState: StateFlow<ConnectionTestState> = _connectionTestState.asStateFlow()

    // TTS Settings
    private val _speechRate = MutableStateFlow(prefs.getSpeechRate())
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefs.getPitch())
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow(prefs.getTtsVoiceName())
    val selectedVoiceName: StateFlow<String?> = _selectedVoiceName.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(prefs.isAutoSpeakEnabled())
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    // Chat Conversation
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "Greetings, sir. JARVIS online and all neural subroutines are fully operational. How may I be of assistance today?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputQuery = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Notes State
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notesList: StateFlow<List<NoteEntity>> = _notesSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteDao.getAllNotes()
            } else {
                noteDao.searchNotes(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Managers for Voice & TTS
    val ttsManager: TextToSpeechManager = TextToSpeechManager(application) { isSpeaking ->
        if (isSpeaking) {
            _assistantStatus.value = AssistantStatus.SPEAKING
        } else if (_assistantStatus.value == AssistantStatus.SPEAKING) {
            _assistantStatus.value = AssistantStatus.IDLE
        }
    }

    val speechManager: SpeechRecognitionManager = SpeechRecognitionManager(application) { recognizedText ->
        _inputQuery.value = recognizedText
        _assistantStatus.value = AssistantStatus.IDLE
    }

    init {
        ttsManager.applySettings(
            _speechRate.value,
            _speechPitch.value,
            _selectedVoiceName.value
        )
    }

    fun navigateTo(screen: JarvisScreen) {
        _currentScreen.value = screen
    }

    fun updateInputQuery(query: String) {
        _inputQuery.value = query
    }

    fun setLanguage(language: SupportedLanguage) {
        _currentLanguage.value = language
        prefs.setLanguage(language)
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
        prefs.setApiKey(key)
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
        prefs.setSelectedModel(model)
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        prefs.setSpeechRate(rate)
        ttsManager.applySettings(rate, _speechPitch.value, _selectedVoiceName.value)
    }

    fun setSpeechPitch(pitch: Float) {
        _speechPitch.value = pitch
        prefs.setPitch(pitch)
        ttsManager.applySettings(_speechRate.value, pitch, _selectedVoiceName.value)
    }

    fun setSelectedVoice(voiceName: String?) {
        _selectedVoiceName.value = voiceName
        prefs.setTtsVoiceName(voiceName)
        ttsManager.applySettings(_speechRate.value, _speechPitch.value, voiceName)
    }

    fun setAutoSpeak(enabled: Boolean) {
        _autoSpeakEnabled.value = enabled
        prefs.setAutoSpeakEnabled(enabled)
    }

    // Voice recognition toggles
    fun toggleListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            _assistantStatus.value = AssistantStatus.IDLE
        } else {
            ttsManager.stop()
            speechManager.clearError()
            _assistantStatus.value = AssistantStatus.LISTENING
            speechManager.startListening(_currentLanguage.value)
        }
    }

    fun stopListening() {
        speechManager.stopListening()
        if (_assistantStatus.value == AssistantStatus.LISTENING) {
            _assistantStatus.value = AssistantStatus.IDLE
        }
    }

    // TTS actions
    fun speakMessage(text: String) {
        ttsManager.speak(text, _currentLanguage.value)
    }

    fun stopSpeaking() {
        ttsManager.stop()
        if (_assistantStatus.value == AssistantStatus.SPEAKING) {
            _assistantStatus.value = AssistantStatus.IDLE
        }
    }

    fun repeatLastSpeech() {
        ttsManager.repeat(_currentLanguage.value)
    }

    fun testVoiceOutput() {
        val testPhrase = when (_currentLanguage.value) {
            SupportedLanguage.ENGLISH -> "Audio synthesizer verified. JARVIS vocal interface operational, sir."
            SupportedLanguage.NEPALI -> "आवाज प्रणाली सुचारु छ। म JARVIS, तपाईंको सेवामा तयार छु।"
            SupportedLanguage.HINDI -> "ध्वनि प्रणाली सक्रिय है। मैं जार्विस, आपकी सहायता के लिए प्रस्तुत हूँ।"
        }
        ttsManager.speak(testPhrase, _currentLanguage.value)
    }

    // Chat actions
    fun sendMessage(overrideText: String? = null) {
        val textToSend = (overrideText ?: _inputQuery.value).trim()
        if (textToSend.isBlank() || _isGenerating.value) return

        if (overrideText == null) {
            _inputQuery.value = ""
        }

        val userMessage = ChatMessage(content = textToSend, isUser = true)
        val updatedList = _messages.value + userMessage
        _messages.value = updatedList

        _isGenerating.value = true
        _assistantStatus.value = AssistantStatus.THINKING
        ttsManager.stop()

        viewModelScope.launch {
            val result = geminiRepo.sendMessage(
                history = updatedList,
                userMessage = textToSend,
                language = _currentLanguage.value,
                model = _selectedModel.value,
                apiKey = _apiKey.value
            )

            _isGenerating.value = false

            result.onSuccess { responseText ->
                _assistantStatus.value = AssistantStatus.IDLE
                val aiMessage = ChatMessage(content = responseText, isUser = false)
                _messages.value = _messages.value + aiMessage

                if (_autoSpeakEnabled.value) {
                    speakMessage(responseText)
                }
            }.onFailure { error ->
                _assistantStatus.value = AssistantStatus.ERROR
                val errorText = error.localizedMessage ?: "Unknown transmission failure occurred."
                val errorMessage = ChatMessage(
                    content = "Error: $errorText",
                    isUser = false,
                    isError = true
                )
                _messages.value = _messages.value + errorMessage
            }
        }
    }

    fun clearChatHistory() {
        ttsManager.stop()
        _messages.value = listOf(
            ChatMessage(
                content = "Neural conversation log purged. Systems re-initialized, sir.",
                isUser = false
            )
        )
        _assistantStatus.value = AssistantStatus.IDLE
    }

    // Diagnostic Connection Test
    fun testGeminiConnection() {
        _connectionTestState.value = ConnectionTestState(isLoading = true)
        viewModelScope.launch {
            val result = geminiRepo.testConnection(
                apiKey = _apiKey.value,
                model = _selectedModel.value
            )
            result.onSuccess { reply ->
                _connectionTestState.value = ConnectionTestState(
                    isLoading = false,
                    successMessage = "SUCCESS // Neural link established with ${_selectedModel.value}. Ping response: '$reply'"
                )
            }.onFailure { error ->
                _connectionTestState.value = ConnectionTestState(
                    isLoading = false,
                    errorMessage = "FAILED // ${error.localizedMessage}"
                )
            }
        }
    }

    // Notes Actions
    fun updateNotesSearch(query: String) {
        _notesSearchQuery.value = query
    }

    fun saveNote(id: Long, title: String, content: String, tag: String = "GENERAL") {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = title.trim(),
                content = content.trim(),
                timestamp = System.currentTimeMillis(),
                tag = tag
            )
            if (id == 0L) {
                noteDao.insertNote(note)
            } else {
                noteDao.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    fun discussNoteInChat(note: NoteEntity) {
        navigateTo(JarvisScreen.CHAT)
        val prompt = "Sir, regarding my saved note '${note.title}':\n${note.content}\nPlease analyze this and provide your insights."
        sendMessage(prompt)
    }

    override fun onCleared() {
        super.onCleared()
        batteryMonitor.cleanup()
        flashlightManager.cleanup()
        speechManager.cleanup()
        ttsManager.cleanup()
    }
}
