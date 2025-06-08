package com.skyblue.skybluecontacts

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.databinding.ActivityLoginBinding
import com.skyblue.skybluecontacts.model.Login
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var context: Context = this@LoginActivity
    private val TAG = "GoogleSignIn_"
    private  lateinit var mGoogleSignInClient: GoogleSignInClient
    val requestCode = 123
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var session: SessionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionHandler
        session.init(this)

        if(session.isLoggedIn()){
            val intent = Intent(context, CloudContactsActivity::class.java)
            startActivity(intent)
            finish()
        }

        FirebaseApp.initializeApp(context)

        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOption)
        firebaseAuth = FirebaseAuth.getInstance()

        onClick()
    }

    private fun onClick() {
        binding.google.setOnClickListener(){
            signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showMessage("Google sign-in success")
                binding.googleSignInLayout.visibility = View.GONE
                binding.loginInitLayout.visibility = View.VISIBLE
                loginNow(account.id.toString(), account.displayName.toString(), account.email.toString())
            }
        }
    }

    data class UserResponse(
        val user_id: String
    )

    private fun loginNow(googleId: String, displayName: String, email: String) {
        val currentDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val currentTime: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val currentDateTime = "$currentDate $currentTime"

        val jsonObject = JSONObject().apply {
            put("acc", "login")
            put("googleId", googleId)
            put("email", email)
            put("displayName", displayName)
            put("dateTime", currentDateTime)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)
        RetrofitInstance.apiInterface.login(requestBody).enqueue(object : Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                if (response.isSuccessful) {
                    val login = response.body()
                    val status: Boolean = login?.status == "true"

                    if (status){
                        val userId = login?.response?.getOrNull(0)?.user_id
                        if (login != null) {
                                session.loginUser(userId.toString(), displayName)
                                val intent = Intent(context, CloudContactsActivity::class.java)
                                intent.putExtra("userId", userId.toString())
                                intent.putExtra("displayName", displayName)
                                startActivity(intent)
                                finish()
                            showMessage(login.message)
                        }
                    }else {
                        showMessage("Login Failed!")
                    }
                } else {
                    showMessage("Login Failed!")
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                showMessage("Login Failed!")
            }
        })
    }

    fun showMessage(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(getColor(R.color.primary))
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}