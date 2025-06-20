package com.skyblue.skybluecontacts.encrypt.retrofit

import com.skyblue.skybluecontacts.encrypt.model.ApiResponse
import com.skyblue.skybluecontacts.encrypt.model.EncryptPostRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EncryptAPIInterface {
    @POST("encrypt.php")
    suspend fun uploadEncryptedContacts(@Body encryptedData: EncryptPostRequest): Response<ApiResponse>
}