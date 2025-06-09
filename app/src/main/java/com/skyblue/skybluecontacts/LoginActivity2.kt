package com.skyblue.skybluecontacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity

class LoginActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val signInClient = Identity.getSignInClient(context)
        val request = GetSignInIntentRequest.builder()
            .setServerClientId(WEB_CLIENT_ID)
            .build()

        signInClient.getSignInIntent(request)
            .addOnSuccessListener { result ->
                val intent = result.intent
                startActivityForResult(intent, RC_SIGN_IN)
            }
            .addOnFailureListener {
                Log.e("SignIn", "Sign-in intent request failed", it)
            }

    }
}