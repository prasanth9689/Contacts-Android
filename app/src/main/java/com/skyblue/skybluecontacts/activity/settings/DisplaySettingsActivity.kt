package com.skyblue.skybluecontacts.activity.settings

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.toDrawable
import com.skyblue.skybluecontacts.AppConstants.SHARED_PREF
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityDisplaySettingsBinding
import java.util.Objects

class DisplaySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDisplaySettingsBinding
    private val context: Context = this
    private var editor: SharedPreferences.Editor? = null
    private var themeDialog: Dialog? = null
    private var selectedTheme = 0
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

        binding.back.setOnClickListener { finish() }
    }

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
    }

    private fun initTheme() {
        themeDialog = Dialog(context)
        themeDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        themeDialog!!.setContentView(R.layout.model_theme_select)
        Objects.requireNonNull(themeDialog!!.window)
            ?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        themeDialog!!.setCancelable(true)

        themeDialog!!.show()

        //   themeDialog.show();
        val systemDefault = themeDialog!!.findViewById<RelativeLayout>(R.id.system_default)
        val light = themeDialog!!.findViewById<RelativeLayout>(R.id.light)
        val dark = themeDialog!!.findViewById<RelativeLayout>(R.id.dark)
        val okButton = themeDialog!!.findViewById<TextView>(R.id.ok)
        val cancelButton = themeDialog!!.findViewById<TextView>(R.id.cancel)


        val systemDefaultRadioBtn =
            themeDialog!!.findViewById<RadioButton>(R.id.system_def_radio_btn)
        val lightRadioBtn = themeDialog!!.findViewById<RadioButton>(R.id.light_radio_btn)
        val darkRadioBn = themeDialog!!.findViewById<RadioButton>(R.id.dark_radio_btn)

        //    systemDefault.setEnabled(true);
        darkRadioBn.isEnabled = true
        lightRadioBtn.isEnabled = true
        systemDefaultRadioBtn.isEnabled = true
        okButton.isEnabled = true
        cancelButton.isEnabled = true

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
                    showToast("Selected system default theme")
                }

                2 -> {
                    enableLightMode()
                    showToast("Selected light theme")
                }

                3 -> {
                    enableDarkMode()
                    showToast("Selected dark theme")
                }

                else -> showToast("Theme not selected!")
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

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}