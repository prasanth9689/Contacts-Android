package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.skybluecontacts.databinding.ActivityAddContactManuallyBinding

class AddContactManuallyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddContactManuallyBinding
    private val context = this
    val TAG = "AddContactManually_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactManuallyBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}