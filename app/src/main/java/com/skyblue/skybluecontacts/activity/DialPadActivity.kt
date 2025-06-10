package com.skyblue.skybluecontacts.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ActivityDialPadBinding

class DialPadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDialPadBinding
    val context = this
    private var currentNumber = ""
    private val REQUEST_CALL_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialPadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onClick();

        binding.number.setText("")
    }
    private fun onClick() {
        binding.one.setOnClickListener{
            updateNumber("1")
        }

        binding.two.setOnClickListener{
            updateNumber("2")
        }

        binding.three.setOnClickListener{
            updateNumber("3")
        }

        binding.four.setOnClickListener{
            updateNumber("4")
        }
        binding.five.setOnClickListener{
            updateNumber("5")
        }

        binding.six.setOnClickListener{
            updateNumber("6")
        }

        binding.seven.setOnClickListener{
            updateNumber("7")
        }

        binding.eight.setOnClickListener{
            updateNumber("8")
        }

        binding.nine.setOnClickListener{
            updateNumber("9")
        }

        binding.zero.setOnClickListener{
            updateNumber("0")
        }

        binding.star.setOnClickListener{
            updateNumber("*")
        }
        binding.hash.setOnClickListener{
            updateNumber("#")
        }

        binding.call.setOnClickListener{
            val mNumber = binding.number.text;

            if (mNumber.isNotEmpty()){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    //  startActivity(callIntent)

                    nowCallStart()
                } else {
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
                }

            } else {
                Toast.makeText(context, "Phone number not empty", Toast.LENGTH_SHORT).show()
            }


        }

        binding.number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val textLength: Int = binding.number.length()
                binding.number.setSelection(textLength, textLength)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val textLength: Int = binding.number.length()
                Log.i("dial_", "Dial pad number lenth: " + textLength)

                if (textLength == 0){
                    binding.delete.visibility = View.GONE
                    return
                }
                binding.delete.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                val textLength: Int = binding.number.length()
                binding.number.setSelection(textLength, textLength)
            }
        })

        binding.delete.setOnClickListener {

            val mNUmber = binding.number.text
            val mNumberLenth = mNUmber.length

            Log.i("Del__", "Delete before available text: " + mNUmber.toString())
            Log.i("Del__", "Current available lenth: " + mNumberLenth.toString())

            if (mNumberLenth > 0) {
                mNUmber.delete(mNumberLenth - 1, mNumberLenth)
                currentNumber = mNUmber.toString()
                Log.i("Del__", "Delete after available text: " + mNUmber.toString())
                Log.i("Del__", "Global currentNumber: " + currentNumber)
            }
        }
    }

    private fun nowCallStart() {
        val mNumber = binding.number.text;
        val phoneNumber = "tel:" + mNumber

        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse(phoneNumber)
        }

        startActivity(callIntent)
    }

    private fun updateNumber(digit: String) {
        currentNumber += digit
        binding.number.setText(currentNumber)
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            nowCallStart()
            // Permission granted — retry the call or notify user
        } else {
            Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}