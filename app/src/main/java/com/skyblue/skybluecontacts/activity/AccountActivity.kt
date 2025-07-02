package com.skyblue.skybluecontacts.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.ContactsRoomViewModelFactory
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityAccountBinding
import com.skyblue.skybluecontacts.model.DeleteAccount
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import com.skyblue.skybluecontacts.room.AppDatabase
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.AppConstants.DELETE_ACCOUNT
import com.skyblue.skybluecontacts.util.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsRoomViewModel
import com.skyblue.skybluecontacts.viewmodel.DeleteAccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountActivity : BaseActivity() {
    private lateinit var binding: ActivityAccountBinding
    private val TAG = "AccountActivity_"
    private val context = this
    lateinit var session: SessionHandler
    lateinit var user: User
    private val viewModelDeleteAccount: DeleteAccountViewModel by viewModels()
    private lateinit var viewModelRoom: ContactsRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        val contactDao = AppDatabase.getDatabase(this).contactDao()
        val repository = ContactsRoomRepository(contactDao)
        viewModelRoom = ViewModelProvider(this, ContactsRoomViewModelFactory(repository))[ContactsRoomViewModel::class.java]

        onClick()

        binding.back.setOnClickListener {
            finish()
        }
    }

    fun onClick(){
        binding.deleteAccount.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("This action can't undone")
                .setMessage("Delete account permanently")
                .setPositiveButton("Yes") { dialog, _ ->
                    showMessage(getString(R.string.deleted_success))
                    dialog.dismiss()

                    val deleteAccount = DeleteAccount(DELETE_ACCOUNT,
                        user.userId.toString())

                    viewModelDeleteAccount.isDeleteAccount.observe(this) {
                        if (it) {
                            showMessage("Account deleted successfully")
                            logout()
                        } else {
                            showMessage("Error deleting account")
                        }
                    }

                    viewModelDeleteAccount.deleteAccount(deleteAccount)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        @Suppress("DEPRECATION")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                showMessage("Signed out successfully")

                val credentialManager = CredentialManager.create(context)
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        session.logoutUser()
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        Log.d(TAG, "Credential cleared!")
                    } catch (e: Exception) {
                        Log.d(TAG, "Credential clear failed: ${e.message}")
                    }
                }
            } else {
                Log.d(TAG, "Google sign-out failed!")
            }
        }

        session.logoutUser()
        viewModelRoom.clearRoomDatabase()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}