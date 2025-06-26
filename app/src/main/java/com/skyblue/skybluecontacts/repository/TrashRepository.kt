package com.skyblue.skybluecontacts.repository

import com.skyblue.skybluecontacts.model.DeleteTrashCloud
import com.skyblue.skybluecontacts.model.DeleteTrashCloudResponse
import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.model.TrashResponse
import com.skyblue.skybluecontacts.model.TrashRestoreCloud
import com.skyblue.skybluecontacts.model.TrashRestoreCloudResponse
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance

class TrashRepository {
    private val contactsService = RetrofitInstance.contactsService

    suspend fun getTrashContacts(trashRequest: TrashRequest): TrashResponse {
        return contactsService.getTrashContacts(trashRequest)
    }

    suspend fun deleteTrashCloudContact(deleteTrashCloud: DeleteTrashCloud): DeleteTrashCloudResponse {
        return contactsService.deleteTrashCloudContact(deleteTrashCloud)
    }

    suspend fun restoreTrashCloudContact(trashRestoreCloud: TrashRestoreCloud): TrashRestoreCloudResponse {
        return contactsService.restoreTrashCloudContact(trashRestoreCloud)
    }
}