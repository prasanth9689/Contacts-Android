package com.skyblue.skybluecontacts.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.ContactsRoomViewModelFactory
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.adapter.ContactVcfAdapter
import com.skyblue.skybluecontacts.databinding.ActivityImportContactsVcfBinding
import com.skyblue.skybluecontacts.model.ContactPayload
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.viewmodel.ContactVcfViewModel
import com.skyblue.skybluecontacts.repository.ContactVcfRepository
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
class ImportContactsVcfActivity : BaseActivity() {
    private lateinit var binding: ActivityImportContactsVcfBinding
    private val context = this
    val TAG = "ImportContactsVcf_"
    private lateinit var contactViewModel: ContactVcfViewModel
    private lateinit var contactAdapter: ContactVcfAdapter
    lateinit var session: SessionHandler
    lateinit var user: User
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var viewModelRoom: ContactsRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportContactsVcfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

        binding.import2.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openFilePicker() // Android 13+
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermissionAndRequest()  // Android 12
            } else {
                checkPermissionAndRequest() // Android 11 or below
            }
        }

        contactViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ContactVcfViewModel(ContactVcfRepository()) as T
                }
            }
        )[ContactVcfViewModel::class.java]

        contactAdapter = ContactVcfAdapter(emptyList()) { position ->
            contactViewModel.toggleContactSelection(position)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = contactAdapter

// Observe
        contactViewModel.contacts.observe(context) { list ->
            contactAdapter.updateList(list)

            binding.markAll.visibility = View.VISIBLE
            binding.selectedLayout.visibility = View.VISIBLE
           // binding.save.visibility = View.VISIBLE

            val count = list.count { it.isSelected }
            binding.selectedText.text = count.toString()

            val selectedCount = contactViewModel.getSelectedCount()

            binding.save.isVisible = selectedCount > 0

            Log.d(TAG, "Contacts observer: $list")
        }

        binding.back.setOnClickListener {
            finish()
        }

        binding.markAll.setOnClickListener {
            contactViewModel.toggleSelectAll()
        }

        binding.save.setOnClickListener {
            enableSaveProgress()
            val selectedContacts = contactViewModel.getSelectedContacts()
            val payload = ContactPayload(
                contacts = selectedContacts.map {
                    Contacts("", it.name, it.phone)
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

            for (contact in selectedContacts) {
                Log.d(TAG, "SelectedContact ${contact.name} - ${contact.phone}")
            }
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
        binding.progressText.text = getString(R.string.saving_please_wait)
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
                // No contacts found
            }else{
                val contactList = list.map {
                    ContactsRoom(contactId = it.contactId, firstName = it.firstName, phoneNumber = it.phoneNumber)
                }
                viewModelRoom.deleteAllContacts()
                viewModelRoom.insertContact(contactList)

                disableSaveProgress()
                showSuccess(getString(R.string.contacts_saved_success))
                contactViewModel.deselectAll()
                showMessage(getString(R.string.contacts_saved_success))
            }
        }
        viewModel.fetchContacts(requestBody)
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.this_app_needs_access_to_your_storage_to_select_vcf_files))
            .setPositiveButton(getString(R.string.allow)) { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/x-vcard" // or "*/*" for broader search
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Select VCF file"))
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                // Handle selected .vcf file
                binding.importLayout.visibility = View.GONE
                handleVcfFile(it)
            }
        }
    }

    private fun handleVcfFile(uri: Uri) {

        if (uri != null) {
            contactViewModel.loadContactsFromVcf(uri, context)
        }

//        // Example: read file name
//        val fileName = uri.lastPathSegment
//        Log.d("VCF", "Selected file: $fileName")
//
//        // Read file content if needed
//        val inputStream = contentResolver.openInputStream(uri)
//        val content = inputStream?.bufferedReader().use { it?.readText() }
//        Log.d("VCF", "File content: $content")
    }

    private fun checkPermissionAndRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted
            openFilePicker()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                // Explain why permission is needed
                showPermissionExplanationDialog()
            } else {
                // Check if "Don't ask again" was selected
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openFilePicker()
            } else {
                // Check if "Don't ask again" is selected
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )

                if (!showRationale) {
                    // User selected "Don't ask again"
                    openAppSettings()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }


    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }
}