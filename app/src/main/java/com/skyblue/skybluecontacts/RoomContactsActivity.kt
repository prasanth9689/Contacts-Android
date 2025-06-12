package com.skyblue.skybluecontacts

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.activity.AddContactsDeviceActivity
import com.skyblue.skybluecontacts.activity.DialPadActivity
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.adapter.ContactsRoomAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.skyblue.skybluecontacts.databinding.ActivityRoomContactsBinding
import com.skyblue.skybluecontacts.databinding.BottomSheetAddContactBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoomContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoomContactsBinding
    private val context = this
    val TAG = "RoomContacts_"
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactAdapter
    lateinit var session: SessionHandler
    lateinit var user: User
    private lateinit var viewModelRoom: ContactsRoomViewModel
    private lateinit var adapterRoom: ContactsRoomAdapter

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
                Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Contacts are available", Toast.LENGTH_SHORT).show()
            }
        }

        // trigger check (e.g. in `onCreate` or after insert/delete)
        viewModelRoom.checkIfContactsEmpty()


        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModelRoom.contacts.collectLatest { contacts ->
                adapterRoom = ContactsRoomAdapter(contacts)
                binding.recyclerView.adapter = adapterRoom
            }
        }
        viewModelRoom.getAllContacts()

        onClick()
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
                showMessage("No contacts found!")
                binding.shimmerLayout.visibility = View.GONE
                binding.noContactsLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }else{
             //   adapter.updateData(list)
                binding.recyclerView.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
                binding.noContactsLayout.visibility = View.GONE

                val contactList = list.map {
                    ContactsRoom(firstName = it.firstName, phoneNumber = it.phoneNumber)
                }
                viewModelRoom.insertContact(contactList)
                viewModelRoom.getAllContacts()
            }
        }

        viewModel.fetchContacts(requestBody)
    }

    private fun onClick() {
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
    }

    private fun openSearch(){
        binding.searchView.visibility = View.VISIBLE
        binding.searchView.isIconified = false

        binding.searchView.requestFocus()
        binding.searchView.requestFocusFromTouch()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun openHome() {
        val options = ActivityOptionsCompat.makeCustomAnimation(
            context,
            0,
            0
        )

        val intent = Intent(context, CloudContactsActivity::class.java)
        startActivity(intent, options.toBundle())
        finish()
    }

    private fun loadBottomSheetDialog() {
        val dialog = BottomSheetDialog(context)
        val binding = BottomSheetAddContactBinding.inflate(layoutInflater)
        val view = binding.root

        binding.selectFrContact.setOnClickListener {
            val intent = Intent(context, AddContactsDeviceActivity::class.java)
            startActivity(intent)
        }

        binding.addManually.setOnClickListener {
            val intent = Intent(context, DialPadActivity::class.java)
            startActivity(intent)
        }

        binding.importVcf.setOnClickListener {
            Toast.makeText(context, "Import .VCF file", Toast.LENGTH_SHORT).show()
        }

        binding.importCsv.setOnClickListener {
            Toast.makeText(context, "Import .CSV file", Toast.LENGTH_SHORT).show()
        }

        dialog.setContentView(view)
        dialog.show()
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