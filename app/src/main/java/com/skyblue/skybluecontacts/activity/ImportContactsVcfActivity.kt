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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.adapter.ContactVcfAdapter
import com.skyblue.skybluecontacts.databinding.ActivityImportContactsVcfBinding
import com.skyblue.skybluecontacts.viewmodel.ContactVcfViewModel
import com.skyblue.skybluecontacts.repository.ContactVcfRepository

class ImportContactsVcfActivity : BaseActivity() {
    private lateinit var binding: ActivityImportContactsVcfBinding
    private val context = this
    val TAG = "ImportContactsVcf_"
    private lateinit var contactViewModel: ContactVcfViewModel
    private lateinit var contactAdapter: ContactVcfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportContactsVcfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.import2.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+
                openFilePicker()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12
                checkPermissionAndRequest()
            } else {
                // Android 11 or below
                checkPermissionAndRequest()
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
            val nowAllSelected = contactViewModel.toggleSelectAll()
//            .text = if (nowAllSelected) "Deselect All" else "Select All"
        }

        binding.save.setOnClickListener {
            val selectedContacts = contactViewModel.getSelectedContacts()

            for (contact in selectedContacts) {
                Log.d(TAG, "SelectedContact ${contact.name} - ${contact.phone}")
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs access to your storage to select VCF files.")
            .setPositiveButton("Allow") { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel", null)
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