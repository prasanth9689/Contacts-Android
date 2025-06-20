package com.skyblue.skybluecontacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.activity.AddContactManuallyActivity
import com.skyblue.skybluecontacts.activity.AddContactsDeviceActivity
import com.skyblue.skybluecontacts.activity.DialPadActivity
import com.skyblue.skybluecontacts.activity.ImportContactsVcfActivity
import com.skyblue.skybluecontacts.activity.settings.SettingsActivity
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.adapter.ContactsRoomAdapter
import com.skyblue.skybluecontacts.databinding.ActivityRoomContactsBinding
import com.skyblue.skybluecontacts.databinding.BottomSheetAddContactBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.Options
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.core.net.toUri

class RoomContactsActivity : BaseActivity() {
    private lateinit var binding: ActivityRoomContactsBinding
    private val context = this
    val TAG = "RoomContacts_"
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactAdapter
    lateinit var session: SessionHandler
    lateinit var user: User
    private lateinit var viewModelRoom: ContactsRoomViewModel
    private lateinit var adapterRoom: ContactsRoomAdapter
    private val REQUEST_CALL_PERMISSION = 1
    private var mPhoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

       // viewModelRoom.deleteAllContacts()

        viewModelRoom.isEmpty.observe(this) { isEmpty ->
            if (isEmpty) {
                synchContacts()
                Log.d(TAG, "No contacts found! in local room database \n Fetching contacts from server")
                showMessage(getString(R.string.contacts_sync_please_wait))
            } else {
                Log.d(TAG, "Contacts are available. in local room database")
            }
        }

        viewModelRoom.checkIfContactsEmpty()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModelRoom.contacts.collectLatest { contacts ->
                adapterRoom = ContactsRoomAdapter(
                    contacts,
                    onClick = {
                        if (it.action.equals("call")){
                            mPhoneNumber = it.phoneNumber
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

                        if (it.action.equals("message")){
                            openSmsApp(it.phoneNumber)
                        }

                        if (it.action.equals("whatsapp")){
                            openWhatsAppChat(it.phoneNumber)
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
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        viewModel.contacts.observe(this) { list ->
            Log.d(TAG, "Fetched items: $list")

            if (list.isNullOrEmpty()) {
                showMessage(getString(R.string.no_contacts_found))
                binding.shimmerLayout.visibility = View.GONE
                binding.noContactsLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }else{
                binding.recyclerView.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
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
                // Keyboard is open
                binding.bottomNav.visibility = View.GONE
            } else {
                // Keyboard is closed
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

        val adapter = ContactsRoomAdapter(emptyList()) { contact ->
            Toast.makeText(this, "Clicked: ${contact.action}", Toast.LENGTH_SHORT).show()
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
        val dialog = BottomSheetDialog(context)
        val binding = BottomSheetAddContactBinding.inflate(layoutInflater)
        val view = binding.root

        dialog.setOnDismissListener {
            Log.d("BottomSheet", "Closed or Collapsed")
           // showMessage("Closed or Collapsed")
        }

        binding.selectFrContact.setOnClickListener {
            val intent = Intent(context, AddContactsDeviceActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        binding.addManually.setOnClickListener {
            val intent = Intent(context, AddContactManuallyActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        binding.importVcf.setOnClickListener {
            val intent = Intent(context, ImportContactsVcfActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        viewModelRoom.getAllContacts()
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
            // Permission granted — retry the call or notify user
        } else {
            showMessage(getString(R.string.call_permission_denied))
        }
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