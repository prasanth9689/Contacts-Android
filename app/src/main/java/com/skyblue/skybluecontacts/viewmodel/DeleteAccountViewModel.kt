package com.skyblue.skybluecontacts.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.DeleteAccount
import com.skyblue.skybluecontacts.repository.DeleteAccountRepository
import kotlinx.coroutines.launch

class DeleteAccountViewModel : ViewModel() {
    private val deleteAccountRepository = DeleteAccountRepository()
    private val _deleteAccount = MutableLiveData<Boolean>()
    val isDeleteAccount: LiveData<Boolean> = _deleteAccount

    fun deleteAccount(deleteAccount: DeleteAccount) {
        viewModelScope.launch {
            try {
                val response = deleteAccountRepository.deleteAccount(deleteAccount)
                if (response.status == "true") {
                    _deleteAccount.value = true
                    Log.d(TAG, "Account deleted successfully")
                } else {
                    _deleteAccount.value = false
                    Log.d(TAG, "Account deletion error!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }
}