package com.skyblue.skybluecontacts.encrypt

import android.util.Log
import com.google.gson.Gson
import com.skyblue.skybluecontacts.encrypt.model.EncryptPost
import com.skyblue.skybluecontacts.encrypt.model.EncryptPostRequest
import com.skyblue.skybluecontacts.encrypt.retrofit.EncryptAPIInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactEncryptRepository @Inject constructor(private val apiInterface: EncryptAPIInterface) {

    suspend fun uploadContacts(encryptPost: EncryptPost): Boolean{
        val jsonAcc = encryptPost.acc
        val jsonData = Gson().toJson(encryptPost.data)
        val encryptedData = EncryptionUtil.encrypt(jsonData)


        val request = EncryptPostRequest(acc = jsonAcc, data = encryptedData)
        val response = apiInterface.uploadEncryptedContacts(request)
        Log.d("EncryptRepository__", "Request: $request")
        Log.d("EncryptRepository__", "Response: ${response.body()}")
        return response.body()?.status == true
    }
}