package com.skyblue.skybluecontacts.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.databinding.ActivityImportContactsVcfBinding

class ImportContactsVcfActivity : BaseActivity() {
    private lateinit var binding: ActivityImportContactsVcfBinding
    private val context = this
    val TAG = "ImportContactsVcf_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}