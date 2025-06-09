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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.adapter.ContactsSelectionAdapter
import com.skyblue.skybluecontacts.databinding.ActivityAddContactsDeviceBinding
import com.skyblue.skybluecontacts.model.ContactPayload
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.ContactsSelection
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import com.skyblue.skybluecontacts.viewmodel.ContactsSelViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddContactsDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactsDeviceBinding
    private val viewModel: ContactsSelViewModel by viewModels()
    private lateinit var adapter: ContactsSelectionAdapter
    private val contactsList: MutableList<ContactsSelection> = mutableListOf()
    val READ_CONTACTS_PERMISSION = "1"
    private val context = this
    val TAG = "AddContactsDevice_"
    private var allSelected = false
    lateinit var session: SessionHandler
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactsDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else {
            listContacts()
        }


        adapter = ContactsSelectionAdapter(emptyList()) { position ->
            viewModel.toggleSelection(position)

            contactsList[position].isSelected = !contactsList[position].isSelected
            adapter.notifyItemChanged(position)
            val selectedCount = contactsList.count { it.isSelected }
            binding.checkedContacts.text = selectedCount.toString()
            //Toast.makeText(this, "Selected: $selectedCount", Toast.LENGTH_SHORT).show()
        }



        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel.contacts.observe(this) {
            adapter.updateData(it)
        }

        binding.btnShowSelected.setOnClickListener {
            val selectedContacts = contactsList.filter { it.isSelected }
            val payload = ContactPayload(
                contacts = selectedContacts.map {
                    Contacts(it.firstName, it.phoneNumber)
                },
                userId = user.userId.toString()
            )

            RetrofitInstance.apiInterface.sendContacts(payload)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddContactsDeviceActivity, "Contacts sent successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("SendContacts", "Error code: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("SendContacts", "Failure: ${t.message}")
                    }
                })

//            val selected = viewModel.getSelectedContacts()
//            Toast.makeText(context, "Selected: ${selected.size}", Toast.LENGTH_SHORT).show()
        }

        binding.back.setOnClickListener{
            finish()
        }

        binding.checkAll.setOnClickListener {
            allSelected = !allSelected
            contactsList.forEach { it.isSelected = allSelected }
            adapter.updateData(contactsList)

        }
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
            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.setContactsSelection(
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