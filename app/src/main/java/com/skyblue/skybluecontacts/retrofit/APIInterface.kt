package com.skyblue.skybluecontacts.retrofit

import com.skyblue.skybluecontacts.model.ContactPayload
import com.skyblue.skybluecontacts.model.ContactResponse
import com.skyblue.skybluecontacts.model.Login
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface APIInterface {
    @POST("contacts.php")
    fun sendContacts(@Body payload: ContactPayload): Call<ResponseBody>

    @POST("contacts.php")
    suspend fun getContacts(@Body requestBody: RequestBody): ContactResponse

    @POST("contacts.php")
    fun login(@Body requestBody: RequestBody): Call<Login>
}