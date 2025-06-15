package com.skyblue.skybluecontacts

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val fontScale = PreferenceHelper.getFontScale(newBase)

        val config = Configuration(newBase.resources.configuration)
        config.fontScale = fontScale

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}
