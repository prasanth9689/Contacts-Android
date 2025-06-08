package com.skyblue.skybluecontacts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.databinding.ActivityCloudContactsBinding
import com.skyblue.skybluecontacts.databinding.BottomSheetAddContactBinding
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CloudContactsActivity : AppCompatActivity() {
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

        loadBottomSheetDialog()

        session = SessionHandler
        user = session.getUserDetails()!!

        adapter = ContactAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val jsonObject = JSONObject().apply {
            put("acc", "get_contacts")
            put("userId", "22")
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

    private fun loadBottomSheetDialog() {
        val dialog = BottomSheetDialog(context)
        val binding = BottomSheetAddContactBinding.inflate(layoutInflater)
        val view = binding.root

        binding.selectFrContact.setOnClickListener {
            Toast.makeText(context, "Select from contacts", Toast.LENGTH_SHORT).show()
        }

        binding.addManually.setOnClickListener {
            Toast.makeText(context, "Add manually", Toast.LENGTH_SHORT).show()
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