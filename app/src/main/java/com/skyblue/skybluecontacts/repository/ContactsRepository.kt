package com.skyblue.skybluecontacts.repository

import com.skyblue.skybluecontacts.model.ContactResponse
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import okhttp3.RequestBody

class ContactsRepository {
    private val contactsService = RetrofitInstance.contactsService

    suspend fun getContacts(requestBody: RequestBody): ContactResponse {
        return contactsService.getContacts(requestBody)
    }
}