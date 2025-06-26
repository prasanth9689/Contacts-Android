package com.skyblue.skybluecontacts.activity.settings

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.adapter.ContactAdapter
import com.skyblue.skybluecontacts.adapter.TrashAdapter
import com.skyblue.skybluecontacts.databinding.ActivityTrashBinding
import com.skyblue.skybluecontacts.model.DeleteTrashCloud
import com.skyblue.skybluecontacts.model.OptionsTrash
import com.skyblue.skybluecontacts.model.TrashContact
import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.model.TrashRestoreCloud
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.AppConstants.DELETE_TRASH_CLOUD
import com.skyblue.skybluecontacts.util.AppConstants.GET_CLOUD_CONTACTS
import com.skyblue.skybluecontacts.util.AppConstants.GET_TRASH_CLOUD_CONTACTS
import com.skyblue.skybluecontacts.util.AppConstants.RESTORE_TRASH_CLOUD
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.TrashViewModel

class TrashActivity : BaseActivity() {
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

            if (options.action.equals("delete")){
                deleteTrashContact(options)
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

    private fun deleteTrashContact(options: OptionsTrash) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.item_select_trash, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Focusable
        )

        popupWindow.setBackgroundDrawable(Color.WHITE.toDrawable())
        popupWindow.elevation = 10f

        popupView.findViewById<RelativeLayout>(R.id.delete).setOnClickListener {
            deleteSingleContact(options.trashContact)
            // Toast.makeText(context, "Delete clicked ${contactsRoom.contactId}", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        popupView.findViewById<RelativeLayout>(R.id.select).setOnClickListener {
            //Toast.makeText(context, "Edit clicked", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(context, MultipleSelectionActivity::class.java))
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(options.view, 100, 0) // You can offset with x/y if needed

    }

    private fun deleteSingleContact(trashContact: TrashContact) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("This action can't undone")
            .setMessage("Delete contact permanently")
            .setPositiveButton("Yes") { dialog, _ ->
                adapter.removeItem(trashContact)
                showMessage(getString(R.string.deleted_success))
                dialog.dismiss()

                val deleteTrashCloud = DeleteTrashCloud(DELETE_TRASH_CLOUD,
                    trashContact.trashId,
                    user.userId.toString(),
                    trashContact.contactId)

                Log.e(TAG, "deleteSingleContact: " + deleteTrashCloud.toString())

                viewModelTrash.isdeleteContact.observe(this) {
                    if (it) {
                        showMessage("Contact deleted successfully")
                    } else {
                        showMessage(getString(R.string.error_deleting_contact))
                    }
                }

                viewModelTrash.deleteTrashCloudContact(deleteTrashCloud)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun restoreContact(trashContact: TrashContact) {
        adapter.removeItem(trashContact)
        val trashRestoreCloud = TrashRestoreCloud(RESTORE_TRASH_CLOUD,
            trashContact.trashId,
            user.userId.toString(),
            trashContact.contactId)

        viewModelTrash.isRestoreContact.observe(this) {
            if (it) {
                showMessage("Contact restored successfully")
            } else {
                showMessage("Error restore")
            }
        }

        viewModelTrash.restoreContact(trashRestoreCloud)
    }

    private fun onClick() {
        binding.back.setOnClickListener {
            finish()
        }
    }
}