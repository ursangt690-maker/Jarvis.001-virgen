package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesManager
import com.example.data.model.SupportedLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS AI", appName)
    }

    @Test
    fun `preferences manager handles language and settings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        prefs.setLanguage(SupportedLanguage.HINDI)
        assertEquals(SupportedLanguage.HINDI, prefs.getLanguage())

        prefs.setLanguage(SupportedLanguage.NEPALI)
        assertEquals(SupportedLanguage.NEPALI, prefs.getLanguage())

        prefs.setSpeechRate(1.25f)
        assertEquals(1.25f, prefs.getSpeechRate(), 0.01f)

        prefs.setPitch(0.9f)
        assertEquals(0.9f, prefs.getPitch(), 0.01f)
    }
}
