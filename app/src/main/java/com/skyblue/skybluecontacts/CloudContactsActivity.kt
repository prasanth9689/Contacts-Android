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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.activity.AddContactsDeviceActivity
import com.skyblue.skybluecontacts.activity.DialPadActivity
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
                viewModel.filter(newText.toString())
                return false
            }
        })

        viewModel.filteredItems.observe(this) { contacts ->
            adapter.updateData(contacts)
        }
    }

    private fun openSearch(){
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