package com.skyblue.skybluecontacts.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.databinding.ActivityCloudContactsBinding
import com.skyblue.skybluecontacts.databinding.BottomSheetAddContactBinding
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CloudContactsActivity : BaseActivity() {
    private lateinit var binding: ActivityCloudContactsBinding
    private val context = this
    val TAG = "CloudContacts_"
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactAdapter
    lateinit var session: SessionHandler
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCloudContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onClick()

        session = SessionHandler
        user = session.getUserDetails()!!

        // Toast.makeText(context, "User: ${user.userId}", Toast.LENGTH_SHORT).show()

        adapter = ContactAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

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
                adapter.updateData(list)

                binding.recyclerView.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
                binding.noContactsLayout.visibility = View.GONE
            }
        }

        viewModel.fetchContacts(requestBody)
    }

    private fun onClick() {
        binding.addContacts.setOnClickListener {
            loadBottomSheetDialog()
        }

        binding.openDialPad.setOnClickListener {
          val intent = Intent(context, DialPadActivity::class.java)
            startActivity(intent)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filter(newText.toString())
                return false
            }
        })

        viewModel.filteredItems.observe(this) { contacts ->
            adapter.updateData(contacts)
        }

        val searchView = findViewById<SearchView>(R.id.searchView)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(getColor(R.color.primary))

        searchEditText.setHintTextColor(ContextCompat.getColor(context, R.color.textHintColor))

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

        dialog.setContentView(view)
        dialog.show()
    }
}