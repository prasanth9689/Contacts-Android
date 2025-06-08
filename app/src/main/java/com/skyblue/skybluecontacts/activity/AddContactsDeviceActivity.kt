package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.skybluecontacts.databinding.ActivityAddContactsDeviceBinding

class AddContactsDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactsDeviceBinding
    private val context = this
    val TAG = "AddContactsDevice_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactsDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}