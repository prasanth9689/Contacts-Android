package com.skyblue.skybluecontacts.activity.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.PreferenceHelper
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.activity.LoginActivity
import com.skyblue.skybluecontacts.databinding.ActivitySettingsBinding
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.showMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    lateinit var session: SessionHandler
    lateinit var user: User
    private val context = this
    private val TAG = "Settings_"
    private var langDialog: Dialog? = null
    private var selectedLanguage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        binding.back.setOnClickListener {
            finish()
        }

        binding.display.setOnClickListener {
            startActivity(Intent(context, DisplaySettingsActivity::class.java))
        }

        binding.language.setOnClickListener {
            initLanguage()
        }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            @Suppress("DEPRECATION")
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build()


            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            googleSignInClient.signOut().addOnCompleteListener {
                if (it.isSuccessful) {
                    showMessage("Signed out successfully")

                    val credentialManager = CredentialManager.create(context)
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            session.logoutUser()
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            Log.d(TAG, "Credential cleared!")
                        } catch (e: Exception) {
                            Log.d(TAG, "Credential clear failed: ${e.message}")
                        }
                    }

                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                } else {
                    Log.d(TAG, "Google sign-out failed!")
                }
            }
        }
    }

    private fun initLanguage() {
        langDialog = Dialog(context)
        langDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        langDialog!!.setContentView(R.layout.item_select_language)
        Objects.requireNonNull(langDialog!!.window)
            ?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        langDialog!!.setCancelable(true)

        langDialog!!.show()

        val english = langDialog!!.findViewById<RelativeLayout>(R.id.english)
        val tamil = langDialog!!.findViewById<RelativeLayout>(R.id.tamil)
        val okButton = langDialog!!.findViewById<TextView>(R.id.ok)
        val cancelButton = langDialog!!.findViewById<TextView>(R.id.cancel)

        val englishRadioBtn = langDialog!!.findViewById<RadioButton>(R.id.eng_radio_btn)
        val tamilRadioBtn = langDialog!!.findViewById<RadioButton>(R.id.tamil_radio_btn)

        englishRadioBtn.isEnabled = true
        tamilRadioBtn.isEnabled = true
        okButton.isEnabled = true
        cancelButton.isEnabled = true

        english.setOnClickListener {
            selectedLanguage = 1
            englishRadioBtn.isChecked = true

            tamilRadioBtn.isChecked = false
        }

        tamil.setOnClickListener {
            selectedLanguage = 2
            tamilRadioBtn.isChecked = true

            englishRadioBtn.isChecked = false
        }

        englishRadioBtn.setOnClickListener {
            selectedLanguage = 1
            englishRadioBtn.isChecked = true

            tamilRadioBtn.isChecked = false
        }

        tamilRadioBtn.setOnClickListener {
            selectedLanguage = 2
            tamilRadioBtn.isChecked = true

            englishRadioBtn.isChecked = false
        }

        okButton.setOnClickListener {
            when (selectedLanguage) {
                1 -> {
                    PreferenceHelper.saveLanguage(context, "en")
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Log.d(TAG, "Selected english")
                }

                2 -> {
                    PreferenceHelper.saveLanguage(context, "ta")
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                        Log.d(TAG, "Selected tamil")
                }
            }
        }

        cancelButton.setOnClickListener { langDialog!!.dismiss() }
    }
}