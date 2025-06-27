package com.skyblue.skybluecontacts.activity.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.paypal.setOnClickListener {
            val url = "https://www.paypal.com/ncp/payment/FZ2L6GK4PW4AY"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}