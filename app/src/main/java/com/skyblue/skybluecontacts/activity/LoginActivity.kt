package com.skyblue.skybluecontacts.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.skyblue.skybluecontacts.BaseActivity
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.RoomContactsActivity
import com.skyblue.skybluecontacts.databinding.ActivityLoginBinding
import com.skyblue.skybluecontacts.model.Login
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import com.skyblue.skybluecontacts.session.SessionHandler
import com.skyblue.skybluecontacts.util.AppConstants.SHARED_PREF
import com.skyblue.skybluecontacts.util.showMessage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var context: Context = this@LoginActivity
    private val TAG = "GoogleSignIn_"
    lateinit var session: SessionHandler
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTheme()

        auth = Firebase.auth
        credentialManager = CredentialManager.create(baseContext)

        session = SessionHandler
        session.init(this)

        if(session.isLoggedIn()){
            val intent = Intent(context, RoomContactsActivity::class.java)
            startActivity(intent)
            finish()
        }

        onClick()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun onClick() {

        binding.appPermissionButton.setOnClickListener {
            binding.appPermissionsLayout.visibility = View.GONE
            binding.googleSignInLayout.visibility = View.VISIBLE
        }

        binding.agreePrivacyPolicyButton.setOnClickListener {
            binding.privacyPolicyLayout.visibility = View.GONE
            binding.appPermissionsLayout.visibility = View.VISIBLE
        }

        binding.continueWelcomeButton.setOnClickListener {
            binding.privacyPolicyLayout.visibility = View.VISIBLE
            binding.welComeScreenLayout.visibility = View.GONE

            binding.webView.settings.javaScriptEnabled = true

            binding.webView.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }


            binding.webView.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                        binding.privacyPolicyProgressBar.visibility = View.GONE
                        binding.webView.visibility = View.VISIBLE
                        binding.agreePrivacyPolicyButton.visibility = View.VISIBLE
                }
            }

            binding.webView.loadUrl("https://contacts.skyblue.co.in/pages/privacy_policy.html")
        }


        binding.google.setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(this)

            if (account != null) {
                openGoogleSignIn()
                Log.d("GoogleSignIn", "Already signed in: ${account.email}")
            } else {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()

                googleSignInClient = GoogleSignIn.getClient(this, gso)

                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun openGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = baseContext,
                    request = request
                )

                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.e("GoogleSignIn_", "User: ${user.toString()}")
        Log.e("GoogleSignIn_", "DisplayName: ${user?.displayName}")
        Log.e("GoogleSignIn_", "Email: ${user?.email}")
        Log.e("GoogleSignIn_", "UID: ${user?.uid}")
        Log.e("GoogleSignIn_", "PhotoURL: ${user?.photoUrl}")
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            Log.e(TAG, "Google ID Token: ${googleIdTokenCredential.idToken}")
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.e(TAG, "Started Firebase Auth With Google: $idToken")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)

                    binding.googleSignInLayout.visibility = View.GONE
                    binding.loginInitLayout.visibility = View.VISIBLE
                    loginNow(user?.uid.orEmpty(), user?.displayName.orEmpty(), user?.email.orEmpty())
                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                    showMessage(
                        getString(
                            R.string.google_sign_in_failed,
                            task.exception?.localizedMessage
                        ))
                }
            }
    }

    data class UserResponse(
        val userId: String
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
                        val userId = login?.response?.getOrNull(0)?.userId
                        if (login != null) {

                            Log.e("Login_", userId.toString())

                                session.loginUser(userId.toString(), displayName)
                                val intent = Intent(context, RoomContactsActivity::class.java)
                                intent.putExtra("userId", userId.toString())
                                intent.putExtra("displayName", displayName)
                                startActivity(intent)
                                finish()
                        }
                    }else {
                        showMessage(getString(R.string.login_failed))
                    }
                } else {
                    showMessage(getString(R.string.login_failed))
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                showMessage(getString(R.string.login_failed))
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

      if (currentUser != null){
          startActivity(
              Intent(
                  this, RoomContactsActivity
                  ::class.java
              )
          )
          finish()
      } else{
          Log.e("GoogleSignIn_", "User is null")
      }
    }

    private fun initTheme() {
        val sharedPreferences = getSharedPreferences(
            SHARED_PREF,
            MODE_PRIVATE
        )

        val isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false)

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                openGoogleSignIn()
                Log.d("GoogleSignIn", "Signed in successfully: ${account.email}")
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed: ${e.statusCode}")
            }
        }
    }

}