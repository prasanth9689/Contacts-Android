package com.skyblue.skybluecontacts.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.ContactsRoomViewModelFactory
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.adapter.ContactsSelectionAdapter
import com.skyblue.skybluecontacts.databinding.ActivityAddContactsDeviceBinding
import com.skyblue.skybluecontacts.model.ContactPayload
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.ContactsSelection
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsSelViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddContactsDeviceActivity : BaseActivity() {
    private lateinit var binding: ActivityAddContactsDeviceBinding
    private val viewModelSel: ContactsSelViewModel by viewModels()
    private lateinit var adapter: ContactsSelectionAdapter
    private val contactsList: MutableList<ContactsSelection> = mutableListOf()
    val READ_CONTACTS_PERMISSION = "1"
    private val context = this
    val TAG = "AddContactsDevice_"
    lateinit var session: SessionHandler
    lateinit var user: User
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var viewModelRoom: ContactsRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactsDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else {
            listContacts()
        }

        adapter = ContactsSelectionAdapter(emptyList()) { position ->
            viewModelSel.toggleSelection(position)
            contactsList[position].isSelected = !contactsList[position].isSelected
            adapter.notifyItemChanged(position)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModelSel.contacts.observe(this) {
            adapter.updateData(it)

            val selectedCount = viewModelSel.getSelectedCount()
            if (selectedCount > 0) {
                binding.save.visibility = View.VISIBLE
            } else{
                binding.save.visibility = View.GONE
            }
            binding.checkedContacts.text = selectedCount.toString()
        }

        binding.save.setOnClickListener {
            enableSaveProgress()
            val selectedContacts = viewModelSel.getSelectedContacts()
            val payload = ContactPayload(
                contacts = selectedContacts.map {
                    Contacts("", it.firstName, it.phoneNumber)
                },
                userId = user.userId.toString()
            )

            RetrofitInstance.apiInterface.sendContacts(payload)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            synchContacts()
                        } else {
                            disableSaveProgress()
                            showMessage(getString(R.string.error_try_again))
                            Log.e(TAG, "Error code: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        showMessage(getString(R.string.error_try_again))
                        Log.e(TAG, "Failure: ${t.message}")
                    }
                })
        }

        binding.back.setOnClickListener{
            finish()
        }

        binding.checkAll.setOnClickListener {
            viewModelSel.toggleSelectAll()
        }
    }

    private fun disableSaveProgress() {
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = getString(R.string.save_cloud_now)
        binding.save.isEnabled = true
        binding.save.background = ContextCompat.getDrawable(context, R.drawable.btn_custom)
    }

    private fun enableSaveProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = getString(R.string.save_cloud_now)
        binding.save.isEnabled = false
        binding.save.background = ContextCompat.getDrawable(context, R.drawable.btn_disabled)
    }

    private fun showSuccess(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.successLayout.visibility = View.VISIBLE
            binding.successText.text = message
            delay(5000)
            binding.successLayout.visibility = View.GONE
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
            if (list.isNullOrEmpty()) {
               // No contacts found!
            }else{
                val contactList = list.map {
                    ContactsRoom(contactId = it.contactId, firstName = it.firstName, phoneNumber = it.phoneNumber)
                }
                viewModelRoom.deleteAllContacts()
                viewModelRoom.insertContact(contactList)

                disableSaveProgress()
                showSuccess(getString(R.string.contacts_saved_success))
                viewModelSel.deselectAll()
                showMessage(getString(R.string.contacts_saved_success))
            }
        }
        viewModel.fetchContacts(requestBody)
    }

    @SuppressLint("Range")
    private fun listContacts(){
        val contentResolver: ContentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, // All columns
            null,
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val phoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val name = cursor.getString(nameIndex)
                val hasPhoneNumber = cursor.getString(phoneIndex).toInt()

                // Only load contacts with phone numbers
                if (hasPhoneNumber > 0) {
                    val phoneCursor: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))),
                        null
                    )

                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contactsList.add(ContactsSelection(name, phoneNumber))
                        phoneCursor.close()
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        } else{
            showMessage(getString(R.string.no_contacts_found))
            return
        }

        viewModelSel.setContactsSelection(
            contactsList.map { ContactsSelection(it.firstName, it.phoneNumber) }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listContacts()
            } else {
                Log.e(TAG, "Permission Denied")
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS_PERMISSION)) {
                    Log.e(TAG, "User has selected Don't ask again, the permission will not be requested again")
                    showDialogOpenAppSettings(getString(R.string.contact_permission_denied_open_app_settings))
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showDialogOpenAppSettings(message: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_open_app_setting, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialog_message)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)

        messageTextView.text = message

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogButton.setOnClickListener {
            dialog.dismiss()
            openAppSettings()
        }
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 1002)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002) {
            Log.i(TAG, requestCode.toString())
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_CONTACTS), 1)
            } else {
                listContacts()
            }
        }
    }
}