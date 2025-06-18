package com.skyblue.skybluecontacts.activity.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.toDrawable
import com.skyblue.skybluecontacts.util.AppConstants.SHARED_PREF
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.util.PreferenceHelper
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityDisplaySettingsBinding
import java.util.Objects

class DisplaySettingsActivity : BaseActivity() {
    private lateinit var binding: ActivityDisplaySettingsBinding
    private val context: Context = this
    private var editor: SharedPreferences.Editor? = null
    private var themeDialog: Dialog? = null
    private var fontSizeDialog: Dialog? = null
    private var selectedTheme = 0
    private var selectedFontSize = 0
    private val TAG = "Display_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplaySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()

        val sharedPreferences = getSharedPreferences(
            SHARED_PREF,
            MODE_PRIVATE
        )
        editor = sharedPreferences.edit()

        binding.theme.setOnClickListener {
            initTheme()
        }

        binding.fontSize.setOnClickListener {
            initFontSize()
        }

        binding.back.setOnClickListener { finish() }
    }

    private fun initFontSize() {
        fontSizeDialog = Dialog(context)
        fontSizeDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        fontSizeDialog!!.setContentView(R.layout.model_font_size_select)
        Objects.requireNonNull(fontSizeDialog!!.window)
            ?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        fontSizeDialog!!.setCancelable(true)

        fontSizeDialog!!.show()

        val small = fontSizeDialog!!.findViewById<RelativeLayout>(R.id.small)
        val medium = fontSizeDialog!!.findViewById<RelativeLayout>(R.id.medium)
        val large = fontSizeDialog!!.findViewById<RelativeLayout>(R.id.large)
        val okButton = fontSizeDialog!!.findViewById<TextView>(R.id.ok)
        val cancelButton = fontSizeDialog!!.findViewById<TextView>(R.id.cancel)

        val smallRadioBtn =
            fontSizeDialog!!.findViewById<RadioButton>(R.id.small_def_radio_btn)
        val mediumRadioBtn = fontSizeDialog!!.findViewById<RadioButton>(R.id.medium_radio_btn)
        val largeRadioBtn = fontSizeDialog!!.findViewById<RadioButton>(R.id.large_radio_btn)

        smallRadioBtn.isEnabled = true
        mediumRadioBtn.isEnabled = true
        largeRadioBtn.isEnabled = true
        okButton.isEnabled = true
        cancelButton.isEnabled = true

        small.setOnClickListener {
            selectedFontSize = 1
            smallRadioBtn.isChecked = true

            mediumRadioBtn.isChecked = false
            largeRadioBtn.isChecked = false
        }

        medium.setOnClickListener {
            selectedFontSize = 2
            mediumRadioBtn.isChecked = true

            smallRadioBtn.isChecked = false
            largeRadioBtn.isChecked = false
        }

        large.setOnClickListener {
            selectedFontSize = 3
            largeRadioBtn.isChecked = true

            smallRadioBtn.isChecked = false
            mediumRadioBtn.isChecked = false
        }

        smallRadioBtn.setOnClickListener {
            selectedFontSize = 1
            smallRadioBtn.isChecked = true

            mediumRadioBtn.isChecked = false
            largeRadioBtn.isChecked = false
        }

        mediumRadioBtn.setOnClickListener {
            selectedFontSize = 2
            mediumRadioBtn.isChecked = true

            smallRadioBtn.isChecked = false
            largeRadioBtn.isChecked = false
        }

        largeRadioBtn.setOnClickListener {
            selectedFontSize = 3
            largeRadioBtn.isChecked = true

            smallRadioBtn.isChecked = false
            mediumRadioBtn.isChecked = false
        }

        val fontScale = PreferenceHelper.getFontScale(context)
         when {
            fontScale < 0.9f -> smallRadioBtn.isChecked = true
            fontScale < 1.2f -> mediumRadioBtn.isChecked = true // Default
            else  -> largeRadioBtn.isChecked = true
        }

        okButton.setOnClickListener {
            when (selectedFontSize) {
                1 -> {
                    PreferenceHelper.saveFontScale(context, 0.8f)
                    recreate()
                    Log.d(TAG, "Selected font size small")
                }

                2 -> {
                    PreferenceHelper.saveFontScale(context, 1.0f) // Default
                    recreate()
                    Log.d(TAG, "Selected font size medium")
                }

                3 -> {
                    PreferenceHelper.saveFontScale(context, 1.3f) // 30% larger
                    recreate()
                    Log.d(TAG, "Selected font size large")
                }

                else -> Log.d(TAG, "Font size not selected!")
            }
        }

        cancelButton.setOnClickListener { themeDialog!!.dismiss() }
    }

    @SuppressLint("SetTextI18n")
    private fun initUi() {
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        Log.d(TAG, currentNightMode.toString())

        /*
            0 -> Default (-1)
            1 -> Light
            2 -> Dark
         */

         if (currentNightMode == -1){
             binding.currentTheme.text = getString(R.string.system_default_theme)
         }

        if (currentNightMode == 1){
            binding.currentTheme.text = getString(R.string.light)
        }

        if (currentNightMode == 2){
            binding.currentTheme.text = getString(R.string.dark)
        }

        // For font size
        val fontScale = PreferenceHelper.getFontScale(context)
        when {
            fontScale < 0.9f -> binding.fontSizeText.text = getString(R.string.small)
            fontScale < 1.2f -> binding.fontSizeText.text = getString(R.string.medium) // Default
            else  -> binding.fontSizeText.text = getString(R.string.large)
        }
    }

    private fun initTheme() {
        themeDialog = Dialog(context)
        themeDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        themeDialog!!.setContentView(R.layout.model_theme_select)
        Objects.requireNonNull(themeDialog!!.window)
            ?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        themeDialog!!.setCancelable(true)

        themeDialog!!.show()

        val systemDefault = themeDialog!!.findViewById<RelativeLayout>(R.id.system_default)
        val light = themeDialog!!.findViewById<RelativeLayout>(R.id.light)
        val dark = themeDialog!!.findViewById<RelativeLayout>(R.id.dark)
        val okButton = themeDialog!!.findViewById<TextView>(R.id.ok)
        val cancelButton = themeDialog!!.findViewById<TextView>(R.id.cancel)


        val systemDefaultRadioBtn =
            themeDialog!!.findViewById<RadioButton>(R.id.system_def_radio_btn)
        val lightRadioBtn = themeDialog!!.findViewById<RadioButton>(R.id.light_radio_btn)
        val darkRadioBn = themeDialog!!.findViewById<RadioButton>(R.id.dark_radio_btn)

        darkRadioBn.isEnabled = true
        lightRadioBtn.isEnabled = true
        systemDefaultRadioBtn.isEnabled = true
        okButton.isEnabled = true
        cancelButton.isEnabled = true


        val currentNightMode = AppCompatDelegate.getDefaultNightMode()

        if (currentNightMode == -1){
            systemDefaultRadioBtn.isChecked = true
        }

        if (currentNightMode == 1){
            lightRadioBtn.isChecked = true
        }

        if (currentNightMode == 2){
            darkRadioBn.isChecked = true
        }

        systemDefault.setOnClickListener {
            selectedTheme = 1
            systemDefaultRadioBtn.isChecked = true

            lightRadioBtn.isChecked = false
            darkRadioBn.isChecked = false
        }

        light.setOnClickListener {
            selectedTheme = 2
            lightRadioBtn.isChecked = true

            systemDefaultRadioBtn.isChecked = false
            darkRadioBn.isChecked = false
        }

        dark.setOnClickListener {
            selectedTheme = 3
            darkRadioBn.isChecked = true

            lightRadioBtn.isChecked = false
            systemDefaultRadioBtn.isChecked = false
        }

        systemDefaultRadioBtn.setOnClickListener {
            selectedTheme = 1
            systemDefaultRadioBtn.isChecked = true

            lightRadioBtn.isChecked = false
            darkRadioBn.isChecked = false
        }

        lightRadioBtn.setOnClickListener {
            selectedTheme = 2
            lightRadioBtn.isChecked = true

            systemDefaultRadioBtn.isChecked = false
            darkRadioBn.isChecked = false
        }

        darkRadioBn.setOnClickListener {
            selectedTheme = 3
            darkRadioBn.isChecked = true

            lightRadioBtn.isChecked = false
            systemDefaultRadioBtn.isChecked = false
        }

        okButton.setOnClickListener {
            when (selectedTheme) {
                1 -> {
                    enableSystemDefaultMode()
                    Log.d(TAG, "Selected system default theme")
                }

                2 -> {
                    enableLightMode()
                    Log.d(TAG, "Selected light theme")
                }

                3 -> {
                    enableDarkMode()
                    Log.d(TAG, "Selected dark theme")
                }

                else -> Log.d(TAG, "Theme not selected!")
            }
        }

        cancelButton.setOnClickListener { themeDialog!!.dismiss() }
    }

    private fun enableSystemDefaultMode() {
        AppCompatDelegate
            .setDefaultNightMode(
                AppCompatDelegate
                    .MODE_NIGHT_FOLLOW_SYSTEM
            )

        // it will set isDarkModeOn
        // boolean to true
        editor!!.putBoolean(
            "isDarkModeOn",
            true
        )
        editor!!.apply()
        themeDialog!!.dismiss()
    }

    private fun enableLightMode() {
        AppCompatDelegate
            .setDefaultNightMode(
                AppCompatDelegate
                    .MODE_NIGHT_NO
            )
        editor!!.putBoolean(
            "isDarkModeOn",
            false
        )
        editor!!.apply()
        themeDialog!!.dismiss()
    }

    private fun enableDarkMode() {
        AppCompatDelegate
            .setDefaultNightMode(
                AppCompatDelegate
                    .MODE_NIGHT_YES
            )

        editor!!.putBoolean(
            "isDarkModeOn",
            true
        )
        editor!!.apply()

        themeDialog!!.dismiss()
    }
}