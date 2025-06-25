package com.skyblue.skybluecontacts.repository

import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.model.TrashResponse
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance

class TrashRepository {
    private val contactsService = RetrofitInstance.contactsService

    suspend fun getTrashContacts(trashRequest: TrashRequest): TrashResponse {
        return contactsService.getTrashContacts(trashRequest)
    }
}