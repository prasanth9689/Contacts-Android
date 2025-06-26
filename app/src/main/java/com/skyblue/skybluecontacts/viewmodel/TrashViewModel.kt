package com.skyblue.skybluecontacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.DeleteTrashCloud
import com.skyblue.skybluecontacts.model.TrashContact
import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.model.TrashRestoreCloud
import com.skyblue.skybluecontacts.repository.TrashRepository
import kotlinx.coroutines.launch

class TrashViewModel : ViewModel() {
    val TAG = "Trash_"
    private val trashRepository = TrashRepository()
    private val _trashContacts = MutableLiveData<List<TrashContact>>()
    val trashContacts: LiveData<List<TrashContact>> = _trashContacts

    private val _deleteContact = MutableLiveData<Boolean>()
    val isdeleteContact: LiveData<Boolean> = _deleteContact

    val _isRestoreContact = MutableLiveData<Boolean>()
    val isRestoreContact: LiveData<Boolean> = _isRestoreContact

    fun fetchTrashContacts(trashRequest: TrashRequest){
        viewModelScope.launch {
            try {
                val trashResponse = trashRepository.getTrashContacts(trashRequest)
                Log.d(TAG, "Fetched items: ${trashResponse.message}")
                if (trashResponse.status == "true") {
                    _trashContacts.value = trashResponse.response
                    Log.d(TAG, "Fetched items: ${trashResponse.response}")
                } else {
                    _trashContacts.value = emptyList()
                    Log.e(TAG, trashResponse.message)
                }
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }

    fun restoreContact(trashRestoreCloud: TrashRestoreCloud){
        viewModelScope.launch {
            try {
                val response = trashRepository.restoreTrashCloudContact(trashRestoreCloud)
                if (response.status == "true") {
                    _isRestoreContact.value = true
                    Log.d(TAG, "Contact restored successfully")
                } else {
                    _isRestoreContact.value = false
                    Log.d(TAG, "Contact restore error!")
                }
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }

    fun deleteTrashCloudContact(deleteTrashCloud: DeleteTrashCloud){
        viewModelScope.launch {
            try {
                val response = trashRepository.deleteTrashCloudContact(deleteTrashCloud)
                if (response.status == "true") {
                    _deleteContact.value = true
                    Log.d(TAG, "Contact deleted successfully")
                } else {
                    _deleteContact.value = false
                    Log.d(TAG, "Contact deleted successfully")
                }
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }
}