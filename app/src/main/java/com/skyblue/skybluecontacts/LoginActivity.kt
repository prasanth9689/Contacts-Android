package com.skyblue.skybluecontacts

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.skyblue.mya.SessionHandler
import com.skyblue.skybluecontacts.databinding.ActivityLoginBinding
import com.skyblue.skybluecontacts.model.Login
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
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

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var context: Context = this@LoginActivity
    private val TAG = "GoogleSignIn_"
    val requestCode = 123
    lateinit var session: SessionHandler
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun onClick() {
        binding.google.setOnClickListener(){
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.client_id))
                .setFilterByAuthorizedAccounts(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    // Launch Credential Manager UI
                    val result = credentialManager.getCredential(
                        context = baseContext,
                        request = request
                    )

                    // Extract credential from the result returned by Credential Manager
                    handleSignIn(result.credential)
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.e("GoogleSignIn_", "User: ${user.toString()}")
        Log.e("GoogleSignIn_", "DisplayName: ${user?.displayName}")
        Log.e("GoogleSignIn_", "Email: ${user?.email}")
        Log.e("GoogleSignIn_", "UID: ${user?.uid}")
        Log.e("GoogleSignIn_", "PhotoURL: ${user?.photoUrl}")

        // Toast.makeText(context, user?.displayName , Toast.LENGTH_SHORT).show()
    }

    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
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
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)

                    showMessage("Google sign-in success")
                    binding.googleSignInLayout.visibility = View.GONE
                    binding.loginInitLayout.visibility = View.VISIBLE
                    // Ensure these values are not null before passing to loginNow
                    loginNow(user?.uid.orEmpty(), user?.displayName.orEmpty(), user?.email.orEmpty())
                } else {
                    // If sign in fails, display a message to the user
                    // Log the actual exception for detailed error information
                    Log.e(TAG, "signInWithCredential:failure", task.exception) // Use Log.e for errors
                    updateUI(null)
                    showMessage("Google sign-in failed: ${task.exception?.localizedMessage}") // Show user-friendly error
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

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
       // updateUI(currentUser)

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
}