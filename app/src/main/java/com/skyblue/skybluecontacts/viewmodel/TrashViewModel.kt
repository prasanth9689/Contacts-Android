package com.skyblue.skybluecontacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.TrashContact
import com.skyblue.skybluecontacts.model.TrashRequest
import com.skyblue.skybluecontacts.repository.TrashRepository
import kotlinx.coroutines.launch

class TrashViewModel : ViewModel() {
    val TAG = "Trash_"
    private val trashRepository = TrashRepository()
    private val _trashContacts = MutableLiveData<List<TrashContact>>()
    val trashContacts: LiveData<List<TrashContact>> = _trashContacts
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

    fun restoreContact(){

    }
}