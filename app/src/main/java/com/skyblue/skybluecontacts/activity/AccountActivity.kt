package com.skyblue.skybluecontacts.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private val TAG = "AccountActivity_"
    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}