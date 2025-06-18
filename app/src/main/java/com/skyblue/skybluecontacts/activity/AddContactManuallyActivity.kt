package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.ContactsRoomViewModelFactory
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityAddContactManuallyBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import com.skyblue.skybluecontacts.viewmodel.SaveSingleContactViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AddContactManuallyActivity : BaseActivity() {
    private lateinit var binding: ActivityAddContactManuallyBinding
    private val context = this
    val TAG = "AddContactManually_"
    private val viewModelManualSave: SaveSingleContactViewModel by viewModels()
    lateinit var session: SessionHandler
    lateinit var user: User
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var viewModelRoom: ContactsRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactManuallyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

        val number = intent.getStringExtra("number")

        if (number != null){
            if (number.isNotEmpty()){
                binding.phone.setText(number)
            }
        }

        binding.save.setOnClickListener {
            val name = binding.name.text.toString()
            val phone = binding.phone.text.toString()

            if (phone.isEmpty()) {
                showError("Please enter phone number")
                return@setOnClickListener
            }
            if (name.isEmpty()){
                showError("Please enter name")
                return@setOnClickListener
            }
            savePhone(phone, name)
        }

        binding.phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    binding.errorLayout.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    binding.errorLayout.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        viewModelManualSave.contacts.observe(this) { list ->
            if (list.status == "true") {
                synchContacts()
            }else{
                disableSaveProgress()
                showError("Something went wrong. Please try again.")
            }
        }
    }

    private fun synchContacts() {
        val jsonObject = JSONObject().apply {
            put("acc", "get_contacts")
            put("userId", user.userId)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        viewModel.contacts.observe(this) { list ->
            Log.d(TAG, "Fetched items: $list")

            if (list.isNullOrEmpty()) {
                // No contacts found
            }else{
                val contactList = list.map {
                    ContactsRoom(contactId = it.contactId, firstName = it.firstName, phoneNumber = it.phoneNumber)
                }
                viewModelRoom.deleteAllContacts()
                viewModelRoom.insertContact(contactList)

                binding.name.setText("")
                binding.phone.setText("")

                disableSaveProgress()
                showSuccess(getString(R.string.contacts_saved_success))
            }
        }
        viewModel.fetchContacts(requestBody)
    }

    private fun disableSaveProgress() {
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = getString(R.string.save_cloud_now)
        binding.save.isEnabled = true
        binding.save.background = ContextCompat.getDrawable(context, R.drawable.btn_custom)
    }

    private fun showSuccess(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.successLayout.visibility = View.VISIBLE
            binding.successText.text = message
            delay(5000)
            binding.successLayout.visibility = View.GONE
        }
    }

    private fun savePhone(phone: String, name: String) {
        val jsonObject = JSONObject().apply {
            put("acc", "save_single_contact")
            put("userId", user.userId)
            put("phoneNumber", phone)
            put("firstName", name)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        viewModelManualSave.saveSingleContact(requestBody)
        enableSaveProgress()
    }

    private fun enableSaveProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = getString(R.string.saving_please_wait)
        binding.save.isEnabled = false
        binding.save.background = ContextCompat.getDrawable(context, R.drawable.btn_disabled)
    }

    private fun showError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            startAction(message)
            delay(5000)
            stopAction(message)
        }
    }

    private fun stopAction(message: String) {
        binding.errorLayout.visibility = View.GONE
        binding.errorText.text = message
    }

    private fun startAction(message: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}