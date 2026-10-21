package com.example.data.remote

import com.example.data.model.ChatMessage
import com.example.data.model.SupportedLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val apiService: GeminiApiService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApiService::class.java)

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        language: SupportedLanguage,
        model: String,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Gemini API key is not configured. Please enter your API key in Settings or configure .env.")
            )
        }

        // Build conversation contents (up to last 10 turns for context efficiency)
        val contentsList = mutableListOf<GeminiContent>()

        val recentHistory = history
            .filter { !it.isError && it.content.isNotBlank() }
            .takeLast(10)

        for (msg in recentHistory) {
            contentsList.add(
                GeminiContent(
                    role = if (msg.isUser) "user" else "model",
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // Append current message
        contentsList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userMessage))
            )
        )

        // System instructions based on selected language
        val systemPrompt = when (language) {
            SupportedLanguage.ENGLISH ->
                "You are JARVIS, an advanced, highly capable, cinematic AI assistant. " +
                "Respond intelligently, concisely, and respectfully. Address the user with poise. " +
                "Do not use markdown bold/bullets excessively so it reads naturally when spoken aloud."
            SupportedLanguage.NEPALI ->
                "तपाईं JARVIS हुनुहुन्छ, एक उन्नत र सहयोगी कृत्रिम बुद्धिमत्ता (AI) सहायक। " +
                "प्रयोगकर्तालाई आदरपूर्वक, स्पष्ट र स्वाभाविक नेपाली भाषामा उत्तर दिनुहोस्।"
            SupportedLanguage.HINDI ->
                "आप JARVIS हैं, एक अत्यधिक सक्षम, विनम्र और बुद्धिमान AI सहायक। " +
                "उपयोगकर्ता को स्पष्ट, स्वाभाविक और सहायक हिंदी में उत्तर दें।"
        }

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                topK = 40
            )
        )

        try {
            val response = apiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                val candidate = body?.candidates?.firstOrNull()
                val responseText = candidate?.content?.parts?.mapNotNull { it.text }?.joinToString("\n")

                if (!responseText.isNullOrBlank()) {
                    Result.success(responseText.trim())
                } else {
                    Result.failure(IllegalStateException("Received empty response from Gemini."))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(response.code(), errorBody)
                Result.failure(IOException(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Network error: Please verify your internet connection. (${e.localizedMessage})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank."))
        }

        val testRequest = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello JARVIS, system diagnostics check. Respond with 'Online'."))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2f
            )
        )

        try {
            val response = apiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = testRequest
            )

            if (response.isSuccessful) {
                val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                Result.success(text?.trim() ?: "Connection successful (Status 200 OK)")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(IOException(parseErrorMessage(response.code(), errorBody)))
            }
        } catch (e: Exception) {
            Result.failure(IOException("Connection failed: ${e.localizedMessage}"))
        }
    }

    private fun parseErrorMessage(statusCode: Int, rawBody: String?): String {
        if (!rawBody.isNullOrBlank()) {
            try {
                val adapter = moshi.adapter(GeminiResponse::class.java)
                val parsed = adapter.fromJson(rawBody)
                if (parsed?.error?.message != null) {
                    return when (statusCode) {
                        400 -> "Invalid Request: ${parsed.error.message}"
                        401, 403 -> "Authentication Error (${statusCode}): API key invalid or unauthorized. ${parsed.error.message}"
                        429 -> "Rate Limit Exceeded: Quota exhausted. Please try again in a few moments."
                        500, 503 -> "Gemini Server Error (${statusCode}): Server temporarily unavailable."
                        else -> "API Error (${statusCode}): ${parsed.error.message}"
                    }
                }
            } catch (_: Exception) {
                // Ignore json parsing fallback
            }
        }

        return when (statusCode) {
            400 -> "Invalid request (HTTP 400). Please check model parameters."
            401, 403 -> "Invalid API Key or unauthorized access (HTTP $statusCode)."
            429 -> "Quota or rate limit reached (HTTP 429). Please wait before retrying."
            500, 503 -> "Gemini service temporarily unavailable (HTTP $statusCode)."
            else -> "Server returned HTTP status $statusCode."
        }
    }
}
