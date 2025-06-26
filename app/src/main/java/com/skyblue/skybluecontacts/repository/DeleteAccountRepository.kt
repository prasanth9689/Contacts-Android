package com.skyblue.skybluecontacts.repository

import com.skyblue.skybluecontacts.model.DeleteAccount
import com.skyblue.skybluecontacts.model.DeleteAccountResponse
import com.skyblue.skybluecontacts.retrofit.RetrofitInstance

class DeleteAccountRepository {
    private val contactService = RetrofitInstance.contactsService

    suspend fun deleteAccount(deleteAccount: DeleteAccount): DeleteAccountResponse {
        return contactService.deleteAccount(deleteAccount)
    }
}