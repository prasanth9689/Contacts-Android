package com.skyblue.skybluecontacts.retrofit

import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ContactsService {
    @POST("contacts.php")
    suspend fun getContacts(@Body user: User): Contacts
}