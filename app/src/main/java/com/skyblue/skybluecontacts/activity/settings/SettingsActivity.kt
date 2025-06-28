@file:Suppress("DEPRECATION")

package com.skyblue.skybluecontacts.activity.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.graphics.drawable.toDrawable
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.ContactsRoomViewModelFactory
import com.skyblue.skybluecontacts.util.PreferenceHelper
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.activity.AccountActivity
import com.skyblue.skybluecontacts.activity.CloudContactsActivity
import com.skyblue.skybluecontacts.activity.LoginActivity
import com.skyblue.skybluecontacts.databinding.ActivitySettingsBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Objects

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    lateinit var session: SessionHandler
    lateinit var user: User
    private val context = this
    private val tag = "Settings_"
    private var langDialog: Dialog? = null
    private var selectedLanguage = 0
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var viewModelRoom: ContactsRoomViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]


        onClick()
    }

    private fun onClick() {
        binding.myAccount.setOnClickListener {
            startActivity(Intent(context, AccountActivity::class.java))
        }

        binding.about.setOnClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
        }

        binding.trash.setOnClickListener {
            startActivity(Intent(context, TrashActivity::class.java))
        }

        binding.trash.setOnClickListener {
            startActivity(Intent(context, TrashActivity::class.java))
        }

        binding.syncContacts.setOnClickListener {
            binding.syncProgress.visibility = View.VISIBLE
            syncContacts()
        }

        binding.back.setOnClickListener {
            finish()
        }

        binding.display.setOnClickListener {
            startActivity(Intent(context, DisplaySettingsActivity::class.java))
        }

        binding.language.setOnClickListener {
            initLanguage()
        }

        binding.cloudContacts.setOnClickListener {
            startActivity(Intent(context, CloudContactsActivity::class.java))
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
                    showMessage(getString(R.string.signed_out_successfully))

                    val credentialManager = CredentialManager.create(context)
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            session.logoutUser()
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            Log.d(tag, "Credential cleared!")
                        } catch (e: Exception) {
                            Log.d(tag, "Credential clear failed: ${e.message}")
                        }
                    }

                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                } else {
                    Log.d(tag, "Google sign-out failed!")
                }
            }
        }
    }

     private fun syncContacts() {
        showMessage(getString(R.string.sync_started))

        val jsonObject = JSONObject().apply {
            put("acc", "get_contacts")
            put("userId", user.userId)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        viewModel.contacts.observe(this) { list ->
            Log.d(tag, "Fetched items: $list")

            if (list.isNullOrEmpty()) {
                showMessage(getString(R.string.no_contacts_found))
                binding.syncProgress.visibility = View.GONE
            }else{
                val contactList = list.map {
                    ContactsRoom(contactId = it.contactId, firstName = it.firstName, phoneNumber = it.phoneNumber)
                }
                viewModelRoom.deleteAllContacts()
                viewModelRoom.insertContact(contactList)
                viewModelRoom.getAllContacts()
                showMessage(getString(R.string.contacts_sync_success))
                binding.syncProgress.visibility = View.GONE
            }
        }
        viewModel.fetchContacts(requestBody)
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
                    Log.d(tag, "Selected english")
                }

                2 -> {
                    PreferenceHelper.saveLanguage(context, "ta")
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                        Log.d(tag, "Selected tamil")
                }
            }
        }

        if (PreferenceHelper.getLanguage(context) == "en"){
            selectedLanguage = 1
            englishRadioBtn.isChecked = true

            tamilRadioBtn.isChecked = false
        } else {
            selectedLanguage = 2
            tamilRadioBtn.isChecked = true

            englishRadioBtn.isChecked = false
        }

        cancelButton.setOnClickListener { langDialog!!.dismiss() }
    }
}