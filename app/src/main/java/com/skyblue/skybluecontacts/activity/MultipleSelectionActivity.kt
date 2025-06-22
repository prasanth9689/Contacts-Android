package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.ContactsRoomViewModelFactory
import com.skyblue.skybluecontacts.adapter.MultipleSelectionAdapter
import com.skyblue.skybluecontacts.databinding.ActivityMultipleSelectionBinding
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MultipleSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMultipleSelectionBinding
    private val context = this
    private val TAG = "MultipleSelectionActivity_"
    lateinit var session: SessionHandler
    lateinit var user: User
    private lateinit var viewModelRoom: ContactsRoomViewModel
    private lateinit var adapterRoom: MultipleSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultipleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.shimmerLayout.visibility = View.GONE
        lifecycleScope.launch {
            viewModelRoom.contacts.collectLatest { contacts ->
                adapterRoom = MultipleSelectionAdapter(
                    contacts.toMutableList(),
                    onClick = {

                    }
                )
                binding.recyclerView.adapter = adapterRoom
            }
        }
        viewModelRoom.getAllContacts()
    }
}