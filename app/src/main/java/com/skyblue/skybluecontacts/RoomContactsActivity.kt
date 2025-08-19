package com.skyblue.skybluecontacts

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyblue.skybluecontacts.activity.AddContactManuallyActivity
import com.skyblue.skybluecontacts.activity.AddContactsDeviceActivity
import com.skyblue.skybluecontacts.activity.DialPadActivity
import com.skyblue.skybluecontacts.activity.ImportContactsVcfActivity
import com.skyblue.skybluecontacts.activity.settings.SettingsActivity
import com.skyblue.skybluecontacts.adapter.ContactsRoomAdapter
import com.skyblue.skybluecontacts.databinding.ActivityRoomContactsBinding
import com.skyblue.skybluecontacts.databinding.BottomSheetAddContactBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.DeleteSingleCloud
import com.skyblue.skybluecontacts.model.RenameCloudContact
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class RoomContactsActivity : BaseActivity() {
    private lateinit var requestBody: RequestBody
    private lateinit var binding: ActivityRoomContactsBinding
    private val context = this
    val TAG = "CloudContacts_"
    private val viewModel: ContactsViewModel by viewModels()
    lateinit var session: SessionHandler
    lateinit var user: User
    private lateinit var viewModelRoom: ContactsRoomViewModel
    private lateinit var adapterRoom: ContactsRoomAdapter
    private val REQUEST_CALL_PERMISSION = 1
    private var mPhoneNumber = ""
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var contactsExportDialog: Dialog? = null
    private var renameContactDialog: Dialog? = null
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionHandler.init(applicationContext)
        initContactsExportDialog()

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

        viewModelRoom.isEmpty.observe(this) { isEmpty ->
            if (isEmpty) {
                synchContacts()
                Log.d(TAG, "No contacts found! in local room database \n Fetching contacts from server")
            } else {
                binding.syncContactsProgressBarLayout.visibility = View.GONE
                binding.noContactsLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                Log.d(TAG, "Contacts are available. in local room database")
            }
        }

        viewModelRoom.checkIfContactsEmpty()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModelRoom.contacts.collectLatest { contacts ->
                adapterRoom = ContactsRoomAdapter(
                    contacts.toMutableList(),
                    onClick = {
                        if (it.action.equals("call")){
                            mPhoneNumber = it.contact.phoneNumber
                            if (mPhoneNumber.isNotEmpty()){

                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    nowCallStart(mPhoneNumber)
                                } else {
                                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
                                }

                            } else {
                                showMessage(getString(R.string.phone_number_not_empty))
                            }
                        }

                        if (it.action == "message"){
                            openSmsApp(it.contact.phoneNumber)
                        }

                        if (it.action == "whatsapp"){
                            openWhatsAppChat(it.contact.phoneNumber)
                        }

                        if (it.action == "delete"){
                            initMoreDialog(it.view, it.contact)
                        }

                        if (it.action == "expand"){
                            binding.recyclerView.smoothScrollToPosition(adapterRoom.itemCount - 1)
                        }
                    }
                )
                binding.recyclerView.adapter = adapterRoom
            }
        }
        viewModelRoom.getAllContacts()

        onClick()

        val searchView = findViewById<SearchView>(R.id.searchView)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(getColor(R.color.primary))

        searchEditText.setHintTextColor(ContextCompat.getColor(context, R.color.textHintColor))
    }

    private fun initContactsExportDialog() {
        contactsExportDialog = Dialog(context)
        contactsExportDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        contactsExportDialog!!.setContentView(R.layout.item_export_progress)
        Objects.requireNonNull(contactsExportDialog!!.window)
            ?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        contactsExportDialog!!.setCancelable(false)
    }

    private fun initMoreDialog(anchorView: View, contactsRoom: ContactsRoom) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.item_select_more_options, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(Color.WHITE.toDrawable())
        popupWindow.elevation = 10f

        popupView.findViewById<RelativeLayout>(R.id.delete).setOnClickListener {
            adapterRoom.removeItem(contactsRoom)
            showMessage(getString(R.string.deleted_success))
            deleteSingleContact(contactsRoom)
            popupWindow.dismiss()
        }

        popupView.findViewById<RelativeLayout>(R.id.rename).setOnClickListener {
            renameContact(contactsRoom)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchorView, 100, 0)
    }

    private fun renameContact(contactsRoom: ContactsRoom) {

        renameContactDialog = Dialog(context)
        renameContactDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        renameContactDialog!!.setContentView(R.layout.item_contact_rename)
        Objects.requireNonNull(renameContactDialog!!.window)
            ?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        renameContactDialog!!.setCancelable(true)

        renameContactDialog!!.show()

        val editText = renameContactDialog!!.findViewById<EditText>(R.id.editText)
        val saveBtn = renameContactDialog!!.findViewById<Button>(R.id.save)
        val closeBtn = renameContactDialog!!.findViewById<RelativeLayout>(R.id.close)

        editText.isEnabled = true
        saveBtn.isEnabled = true
        closeBtn.isEnabled = true

        editText.setText(contactsRoom.firstName)

        closeBtn.setOnClickListener {
            renameContactDialog!!.dismiss()
        }

        saveBtn.setOnClickListener {
            val name = editText.text.toString().trim()

            if (!name.equals("")){

                renameContactDialog!!.dismiss()
                viewModelRoom.renameContact(contactsRoom.contactId.toInt(),
                    name)

                val contactsRoom2 = ContactsRoom(
                    contactsRoom.id,
                    contactsRoom.contactId,
                    name,
                    contactsRoom.phoneNumber
                )

                adapterRoom.updateItem(contactsRoom2)

                val renameCloudContact = RenameCloudContact("rename_contact",
                    user.userId.toString(),
                    contactsRoom.contactId,
                    name)

                viewModel.isRenameContact.observe(this){
                    if (it){
                        Log.i(TAG, "Rename success!")
                        viewModelRoom.renameContact(contactsRoom.contactId.toInt(),
                            name)
                    }else{
                        Log.i(TAG, "Rename failure!")
                    }
                }

                viewModel.renameContact(renameCloudContact)
                return@setOnClickListener
            }

            Toast.makeText(context, "Please enter name.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSingleContact(contactsRoom: ContactsRoom) {
        val deleteSingleCloud = DeleteSingleCloud("delete_contact",
            user.userId.toString(),
            contactsRoom.contactId)

        viewModel.isdeleteContact.observe(this){
            if (it){
                viewModelRoom.deleteContact(contactsRoom.contactId.toInt())
                viewModelRoom.checkIfContactsEmpty()
            }else{
                showMessage(getString(R.string.error_deleting_contact))
            }
        }

        viewModel.deleteCloudContact(deleteSingleCloud)
    }

    private fun openWhatsAppChat(phoneNumber: String) {
        val formattedNumber = phoneNumber.replace("+", "").replace(" ", "")
        val url = "https://wa.me/$formattedNumber"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = url.toUri()
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showMessage("WhatsApp is not installed.")
        }
    }

    private fun openSmsApp(phoneNumber: String) {
        val uri = "smsto:$phoneNumber".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No SMS app found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun nowCallStart(phone: String) {
        val phoneNumber = "tel:$phone"
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = phoneNumber.toUri()
        }
        startActivity(callIntent)
    }

    private fun synchContacts() {
        val jsonObject = JSONObject().apply {
            put("acc", "get_contacts")
            put("userId", user.userId)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        requestBody = jsonObject.toString().toRequestBody(mediaType)

        viewModel.contacts.observe(this) { list ->
            Log.d(TAG, "Fetched items: $list")
            if (list.isNullOrEmpty()) {
                binding.syncContactsProgressBarLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.noContactsLayout.visibility = View.VISIBLE
            }else{
                binding.syncContactsProgressBarLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.noContactsLayout.visibility = View.GONE

                val contactList = list.map {
                    ContactsRoom(contactId = it.contactId, firstName = it.firstName, phoneNumber = it.phoneNumber)
                }
                viewModelRoom.insertContact(contactList)
                viewModelRoom.getAllContacts()
            }
        }
        viewModel.fetchContacts(requestBody)
    }

    private fun onClick() {

        binding.settings.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.addContacts.setOnClickListener {
            loadBottomSheetDialog()
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home_menu -> openHome()
                R.id.profile_menu -> loadBottomSheetDialog()
                R.id.setting_menu -> openSearch()
            }
            true
        }

        binding.openDialPad.setOnClickListener {
            val intent = Intent(context, DialPadActivity::class.java)
            startActivity(intent)
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModelRoom.filter(newText.toString())
                return false
            }
        })

        viewModelRoom.filteredItems.observe(this) { contacts ->
            adapterRoom.updateData(contacts)
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra("onResume")
                Log.e("Debug_", "Received: $data")

                if (data == "refresh"){
                    viewModelRoom.checkIfContactsEmpty()
                    viewModelRoom.isEmpty.observe(this) { isEmpty ->
                        if (!isEmpty) {
                            Log.e("Debug_", "Room db: contacts available.")
                            binding.syncContactsProgressBarLayout.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.noContactsLayout.visibility = View.GONE
                        }else{
                            Log.e("Debug_", "Room db: empty")
                        }
                    }
                    viewModelRoom.getAllContacts()
                    adapterRoom.notifyDataSetChanged()
                }
            }
        }
    }

    private fun openSearch(){
        binding.searchView.visibility = View.VISIBLE
        binding.searchView.isIconified = false

        binding.searchView.requestFocus()
        binding.searchView.requestFocusFromTouch()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun openHome() {
        val options = ActivityOptionsCompat.makeCustomAnimation(
            context,
            0,
            0
        )

        val intent = Intent(context, RoomContactsActivity::class.java)
        startActivity(intent, options.toBundle())
        finish()
    }

    private fun loadBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(context)
        val binding = BottomSheetAddContactBinding.inflate(layoutInflater)
        val view = binding.root

        bottomSheetDialog?.setOnDismissListener {
            Log.d("BottomSheet", "Closed or Collapsed")
        }

        binding.selectFrContact.setOnClickListener {
            val intent = Intent(context, AddContactsDeviceActivity::class.java)
            resultLauncher.launch(intent)
            bottomSheetDialog?.dismiss()
        }

        binding.addManually.setOnClickListener {
            val intent = Intent(context, AddContactManuallyActivity::class.java)
            resultLauncher.launch(intent)
            bottomSheetDialog?.dismiss()
        }

        binding.importVcf.setOnClickListener {
            val intent = Intent(context, ImportContactsVcfActivity::class.java)
            resultLauncher.launch(intent)
            bottomSheetDialog?.dismiss()
        }

            binding.exportVcf.setOnClickListener {
                bottomSheetDialog?.dismiss()
                contactsExportDialog!!.show()
                lifecycleScope.launch {
                    val contactsRoom = withContext(Dispatchers.IO) {
                        viewModelRoom.getAllContacts2()
                    }
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "contacts_export_$timestamp.vcf"
                    val file = File(downloadsDir, fileName)
                    val success = exportContactsToVcf(contactsRoom, file)

                    if (success) {
                        contactsExportDialog!!.dismiss()
                        Log.e(TAG, "VCF saved to: ${file.absolutePath}")
                        Toast.makeText(context, "VCF saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    } else {
                        contactsExportDialog!!.dismiss()
                        Log.e(TAG, "Export failed")
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        bottomSheetDialog?.setContentView(view)
        bottomSheetDialog?.show()
    }

    fun exportContactsToVcf(contacts: List<ContactsRoom>, file: File): Boolean {
        return try {
            val outputStream = FileOutputStream(file)
            for (contact in contacts) {
                val vcard = "BEGIN:VCARD\n" +
                        "VERSION:3.0\n" +
                        "FN:${contact.firstName}\n" +
                        "TEL:${contact.phoneNumber}\n" +
                        "END:VCARD\n"
                outputStream.write(vcard.toByteArray())
            }
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            showMessage(getString(R.string.permission_granted))
            nowCallStart(mPhoneNumber)
        } else {
            showMessage(getString(R.string.call_permission_denied))
        }
    }

    override fun onResume() {
        super.onResume()
    }
}


class ContactsRoomViewModelFactory(private val repository: ContactsRoomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsRoomViewModel::class.java)) {
            return ContactsRoomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}