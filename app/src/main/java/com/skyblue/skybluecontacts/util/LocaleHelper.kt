package com.skyblue.skybluecontacts.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocale(activity: Activity, language: String) {
        val context = setLocale(activity, language)
        activity.applyOverrideConfiguration(context.resources.configuration)
    }
}
