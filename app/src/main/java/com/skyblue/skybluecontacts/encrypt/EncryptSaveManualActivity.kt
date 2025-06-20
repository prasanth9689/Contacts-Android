package com.skyblue.skybluecontacts.encrypt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.encrypt.model.EncryptPost
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.session.SessionHandler

import com.skyblue.skybluecontacts.databinding.ActivityEncryptSaveManualBinding

class EncryptSaveManualActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEncryptSaveManualBinding
    private val context = this
    private val TAG = "EncryptSaveSingle_"
    lateinit var session: SessionHandler
    lateinit var user: User
    private val viewModelEncryptSaveSingle: ContactEncryptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncryptSaveManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        onClick()
    }

    private fun onClick() {

        binding.save.setOnClickListener {
            val data = Contacts("1",
                "pppppppppp",
                "894057061")

            val uploadCon = EncryptPost("saveEncryptedContact",
                data,
            )

            viewModelEncryptSaveSingle.uploadContacts(uploadCon)
        }
    }
}