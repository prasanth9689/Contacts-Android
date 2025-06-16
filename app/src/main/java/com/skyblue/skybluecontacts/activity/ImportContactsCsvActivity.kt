package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.databinding.ActivityImportContactsCsvBinding

class ImportContactsCsvActivity : BaseActivity() {
    private lateinit var binding: ActivityImportContactsCsvBinding
    private val context = this
    val TAG = "ImportContactsCsv_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportContactsCsvBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}