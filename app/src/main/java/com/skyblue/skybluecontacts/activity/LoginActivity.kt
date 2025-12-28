package com.skyblue.skybluecontacts.activity

// LoginActivity.kt
// Modern implementation using Google One-Tap (Identity.getSignInClient) + CredentialManager fallback
// Firebase authentication with Google ID token

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.getOrNull

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionHandler
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    // IMPORTANT: replace this with your WEB_CLIENT_ID (not Android client id)
    // Example: "1234567890-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com"
    // Put it in strings.xml as <string name="web_client_id">...</string>
    private val WEB_CLIENT_ID by lazy { getString(R.string.client_id) }

    private val TAG = "LoginActivity"

    // One-tap request objects
    private val oneTapRequest by lazy {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false) // show account chooser; set true to filter
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    // ActivityResultLauncher to handle the One-Tap IntentSender result
    private val oneTapLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val data = result.data
                if (data != null) {
                    // Identity.getSignInClient(this).getSignInCredentialFromIntent(data) can extract the token
                    val credential: SignInCredential = Identity.getSignInClient(this)
                        .getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    if (!idToken.isNullOrEmpty()) {
                        // Got ID token — authenticate with Firebase
                        firebaseAuthWithGoogle(idToken)
                    } else {
                        Log.e(TAG, "No ID token in One-Tap credential")
                        showMessage(getString(R.string.google_sign_in_failed, "No id token"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "One-tap result processing failed", e)
            }
        } else {
            Log.e(TAG, "One-tap canceled or failed, resultCode=${result.resultCode}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Clipboard: only use when Activity is foreground
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Log app SHA1 (optional)
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            for (signature in info.signingInfo?.apkContentsSigners ?: emptyArray()) {
                val md = MessageDigest.getInstance("SHA1")
                md.update(signature.toByteArray())
                val sha1 = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("AppSHA1", sha1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate SHA1", e)
        }

        SessionHandler.init(applicationContext)
        initTheme()

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        session = SessionHandler
        session.init(this)

        if (session.isLoggedIn()) {
            startActivity(Intent(this, RoomContactsActivity::class.java))
            finish()
        }

        onClick()
    }

    @Suppress("DEPRECATION")
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

        // -- GOOGLE ONE-TAP CLICK --
        binding.google.setOnClickListener {
            startOneTapSignIn()

            // Demo
//            session.loginUser("30", "Prasanth")
//            val intent = Intent(this@LoginActivity, RoomContactsActivity::class.java)
//            intent.putExtra("userId", "30")
//            intent.putExtra("displayName", "Prasanth")
//            startActivity(intent)
//            finish()
        }
    }

    private fun startOneTapSignIn() {
        // Try One-Tap first
        Identity.getSignInClient(this)
            .beginSignIn(oneTapRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSender = result.pendingIntent.intentSender
                    val request = IntentSenderRequest.Builder(intentSender).build()
                    oneTapLauncher.launch(request)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch One-Tap intent", e)
                    // Fallback to Credential Manager / GetCredential
                    fallbackToCredentialManager()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "One-Tap beginSignIn failed: ${e.localizedMessage}")
                // fallback
                fallbackToCredentialManager()
            }
    }

    private fun fallbackToCredentialManager() {
        // Use CredentialManager to request authorized accounts (filterByAuthorizedAccounts=true)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // IMPORTANT: pass an Activity context (this@LoginActivity) so selector UI can show
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )

                handleCredential(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "CredentialManager getCredential failed: ${e.localizedMessage}")
                // last resort - show message
                showMessage(getString(R.string.google_sign_in_failed, e.localizedMessage ?: ""))
            }
        }
    }

    private fun handleCredential(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            if (!idToken.isNullOrBlank()) {
                firebaseAuthWithGoogle(idToken)
            } else {
                Log.e(TAG, "Credential contained empty idToken")
                showMessage(getString(R.string.google_sign_in_failed, "Empty id token"))
            }
        } else {
            Log.w(TAG, "Credential is not a Google ID token credential")
            showMessage(getString(R.string.google_sign_in_failed, "Invalid credential type"))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "Firebase auth with Google ID token starting")
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
                    showMessage(getString(R.string.google_sign_in_failed, task.exception?.localizedMessage))
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.d(TAG, "User: ${user?.uid}")
        Log.d(TAG, "DisplayName: ${user?.displayName}")
        Log.d(TAG, "Email: ${user?.email}")
        Log.d(TAG, "PhotoURL: ${user?.photoUrl}")
    }

    data class UserResponse( val userId: String )

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

                    if (status) {
                        val userId = login?.response?.getOrNull(0)?.userId
                        if (login != null) {
                            session.loginUser(userId.toString(), displayName)
                            val intent = Intent(this@LoginActivity, RoomContactsActivity::class.java)
                            intent.putExtra("userId", userId.toString())
                            intent.putExtra("displayName", displayName)
                            startActivity(intent)
                            finish()
                        }
                    } else {
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
        if (currentUser != null) {
            startActivity(Intent(this, RoomContactsActivity::class.java))
            finish()
        } else {
            Log.d(TAG, "No current user")
        }
    }

    private fun initTheme() {
        val sharedPreferences = getSharedPreferences(
            SHARED_PREF,
            MODE_PRIVATE
        )

        val isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false)

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
