package com.skyblue.skybluecontacts

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.skybluecontacts.util.PreferenceHelper
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceHelper.getLanguage(newBase)
        val fontScale = PreferenceHelper.getFontScale(newBase)

        // Clone current configuration
        val config = Configuration(newBase.resources.configuration)

        // Apply font scale
        config.fontScale = fontScale

        // Apply locale
        val locale = Locale(lang)
        Locale.setDefault(locale)
        config.setLocale(locale)

        val updatedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(updatedContext)
    }
}
