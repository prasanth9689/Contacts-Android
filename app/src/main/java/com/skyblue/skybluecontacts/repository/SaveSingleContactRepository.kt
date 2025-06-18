package com.skyblue.skybluecontacts.repository


import com.skyblue.skybluecontacts.model.SaveResponse
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import okhttp3.RequestBody

class SaveSingleContactRepository {
    private val contactsService = RetrofitInstance.contactsService

    suspend fun saveSingleContact(requestBody: RequestBody): SaveResponse {
        return contactsService.saveSingleContact(requestBody)
    }
}