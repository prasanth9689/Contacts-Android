package com.skyblue.skybluecontacts.repository

import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance

class ContactsRepository {
    private val contactsService = RetrofitInstance.contactsService

    suspend fun getContacts(user: User): Contacts {
        return contactsService.getContacts(user)
    }
}