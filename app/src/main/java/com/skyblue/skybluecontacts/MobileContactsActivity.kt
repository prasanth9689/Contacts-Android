package com.skyblue.skybluecontacts

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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.skyblue.skybluecontacts.databinding.ActivityContactsMobileBinding
import com.skyblue.skybluecontacts.model.Contacts

class MobileContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsMobileBinding
    private val context = this
    val READ_CONTACTS_PERMISSION = "1"
    val REQUEST_CALL_PERMISSION = 2
    private val contactsList: MutableList<Contacts> = mutableListOf()
    val TAG = "Contacts_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsMobileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else {
            listContacts()
        }

        onClick()
    }

    private fun onClick() {
        binding.search.setOnClickListener{

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
                        val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contactsList.add(Contacts(name, phoneNumber))
                        phoneCursor.close()
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        } else{
            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show()
            return
        }

        val listView: ListView = findViewById(R.id.contacts_list_view)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsList.map { it.firstName })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val contact = contactsList[position]
            makeCall(contact.phoneNumber)
        }
    }

    private fun makeCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()){

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                nowCallStart(phoneNumber)
            } else {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
            }
        } else {
            // need ui expectation
            Toast.makeText(context, "Phone number not empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun nowCallStart(phoneNumber: String) {
        val phoneNumberUri = "tel:" + phoneNumber

        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = phoneNumberUri.toUri()
        }
        startActivity(callIntent)
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

        if (requestCode == 2) {
            if (requestCode == REQUEST_CALL_PERMISSION &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission granted
            } else {
                // Call permission denied. check  user has selected "Don't ask again"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS_PERMISSION)) {
                    showDialogOpenAppSettings(getString(R.string.call_permission_denied_open_app_settings))
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