package com.skyblue.skybluecontacts.repository

import com.skyblue.skybluecontacts.model.ContactResponse
import com.skyblue.skybluecontacts.model.DeleteSingleCloud
import com.skyblue.skybluecontacts.model.DeleteSingleCloudResponse
import com.skyblue.skybluecontacts.model.RenameCloudContact
import com.skyblue.skybluecontacts.model.RenameResponse
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance
import okhttp3.RequestBody

class ContactsRepository {
    private val contactsService = RetrofitInstance.contactsService

    suspend fun getContacts(requestBody: RequestBody): ContactResponse {
        return contactsService.getContacts(requestBody)
    }

    suspend fun deleteCloudContact(deleteSingleCloud: DeleteSingleCloud): DeleteSingleCloudResponse {
        return contactsService.deleteCloudContact(deleteSingleCloud)
    }

    suspend fun renameCloudContact(renameCloudContact: RenameCloudContact): RenameResponse {
        return contactsService.renameContact(renameCloudContact)
    }
}