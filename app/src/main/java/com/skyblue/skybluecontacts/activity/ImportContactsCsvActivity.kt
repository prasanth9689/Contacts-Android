package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.skybluecontacts.databinding.ActivityImportContactsCsvBinding

class ImportContactsCsvActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImportContactsCsvBinding
    private val context = this
    val TAG = "ImportContactsCsv_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportContactsCsvBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}