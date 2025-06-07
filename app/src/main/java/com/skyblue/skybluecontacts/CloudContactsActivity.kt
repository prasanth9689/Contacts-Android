package com.skyblue.skybluecontacts

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.databinding.ActivityCloudContactsBinding
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel

class CloudContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCloudContactsBinding
    private val context = this
    val TAG = "CloudContacts_"
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCloudContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ContactAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val user = User("get_contacts", "1")


        viewModel.contacts.observe(this) { list ->
            Log.d(TAG, "Fetched items: $list")
            //Log.d("LiveData", "Received list: $list") // add this
            adapter.updateData(list)
        }

        viewModel.fetchContacts(user)
    }
}