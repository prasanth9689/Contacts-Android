package com.skyblue.skybluecontacts.util

import android.content.Context
import com.skyblue.skybluecontacts.util.AppConstants.SHARED_PREF

object PreferenceHelper {
    private const val PREF_NAME = SHARED_PREF
    private const val KEY_FONT_SCALE = "font_scale"
    private const val LANGUAGE_KEY = "app_language"

    fun saveFontScale(context: Context, scale: Float) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    fun getFontScale(context: Context): Float {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_FONT_SCALE, 1.0f) // 1.0f = normal font size
    }

    fun saveLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, lang).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "en") ?: "en" // Default is English
    }
}
