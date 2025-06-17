package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityAddContactManuallyBinding
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.showMessage
import com.skyblue.skybluecontacts.viewmodel.ContactsViewModel
import com.skyblue.skybluecontacts.viewmodel.SaveSingleContactViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AddContactManuallyActivity : BaseActivity() {
    private lateinit var binding: ActivityAddContactManuallyBinding
    private val context = this
    val TAG = "AddContactManually_"
    private val viewModel: SaveSingleContactViewModel by viewModels()
    lateinit var session: SessionHandler
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactManuallyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        user = session.getUserDetails()!!

        binding.save.setOnClickListener {
            val name = binding.name.text.toString()
            val phone = binding.phone.text.toString()

            var job: Job? = null

            if (phone.isEmpty()) {
                showError("Please enter phone number")
                return@setOnClickListener
            }

            if (name.isEmpty()){
                showError("Please enter name")
                return@setOnClickListener
            }

            savePhone(phone, name)
        }

        binding.phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    binding.errorLayout.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    binding.errorLayout.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        viewModel.contacts.observe(this) { list ->
            Log.d(TAG, "Fetched items: $list")

            if (list.status == "true") {
                // showMessage("Contact saved successfully")
                showSuccess("Contact saved success")
                binding.name.setText("")
                binding.phone.setText("")
                disableSaveProgress()
            }else{
                //showMessage(list.message)
                disableSaveProgress()
                showError("Something went wrong. Please try again.")
            }
        }

    }

    private fun disableSaveProgress() {
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = "Save cloud now"
        binding.save.isEnabled = true
        binding.save.background = ContextCompat.getDrawable(context, R.drawable.btn_custom)
    }

    private fun showSuccess(message: String) {
        var job: Job? = null

        job = CoroutineScope(Dispatchers.Main).launch {
            binding.successLayout.visibility = View.VISIBLE
            binding.successText.text = message
            delay(5000)
            binding.successLayout.visibility = View.GONE
        }
    }

    private fun savePhone(phone: String, name: String) {

        // showMessage(phone + name)

        val jsonObject = JSONObject().apply {
            put("acc", "save_single_contact")
            put("userId", user.userId)
            put("phoneNumber", phone)
            put("firstName", name)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        viewModel.saveSingleContact(requestBody)

       enableSaveProgress()
    }

    private fun enableSaveProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        binding.progressText.text = "Saving please wait."
        binding.save.isEnabled = false
        binding.save.background = ContextCompat.getDrawable(context, R.drawable.btn_disabled)
    }

    private fun showError(message: String) {
        var job: Job? = null

        job = CoroutineScope(Dispatchers.Main).launch {
            startAction(message)
            delay(5000)
            stopAction(message)
        }
    }

    private fun stopAction(message: String) {
        binding.errorLayout.visibility = View.GONE
        binding.errorText.text = message
    }

    private fun startAction(message: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.errorText.text = message
    }
}