package com.skyblue.skybluecontacts.activity.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.adapter.TrashAdapter
import com.skyblue.skybluecontacts.databinding.ActivityTrashBinding
import com.skyblue.skybluecontacts.model.TrashContact
import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.AppConstants.GET_CLOUD_CONTACTS
import com.skyblue.skybluecontacts.util.AppConstants.GET_TRASH_CLOUD_CONTACTS
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.TrashViewModel

class TrashActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrashBinding
    private val context = this
    private val TAG = "Trash_"
    private val viewModelTrash: TrashViewModel by viewModels()
    private lateinit var adapter: TrashAdapter
    lateinit var session: SessionHandler
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        onClick()

        adapter = TrashAdapter(mutableListOf()){ options ->
            if (options.action.equals("restore")){
                restoreContact(options.trashContact)
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        viewModelTrash.trashContacts.observe(this){ trashContacts ->
            if (trashContacts.isNullOrEmpty()) {
                showMessage("No contacts found!")
                binding.shimmerLayout.visibility = View.GONE
                binding.emptyLayout.visibility = View.VISIBLE
            }else{
                adapter.updateData(trashContacts.toMutableList())
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        val trashRequest = TrashRequest(GET_TRASH_CLOUD_CONTACTS, user.userId.toString())
        Log.e(TAG, GET_CLOUD_CONTACTS + "\n" + user.userId.toString())
        viewModelTrash.fetchTrashContacts(trashRequest)
    }

    private fun restoreContact(trashContact: TrashContact) {
        adapter.removeItem(trashContact)
        //showMessage(trashContact.firstName)

    }

    private fun onClick() {
        binding.back.setOnClickListener {
            finish()
        }
    }
}