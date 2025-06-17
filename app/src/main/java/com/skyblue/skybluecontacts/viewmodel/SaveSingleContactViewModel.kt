package com.skyblue.skybluecontacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.SaveResponse
import com.skyblue.skybluecontacts.repository.SaveSingleContactRepository
import kotlinx.coroutines.launch
import okhttp3.RequestBody


class SaveSingleContactViewModel : ViewModel(){
    val TAG = "SaveContactToCloud_"
    private val repository = SaveSingleContactRepository()
    private val _contacts = MutableLiveData<SaveResponse>()
    val contacts: LiveData<SaveResponse> = _contacts

    fun saveSingleContact(requestBody: RequestBody){
        viewModelScope.launch {
            try {
                val contacts = repository.saveSingleContact(requestBody)
                Log.d(TAG, "Response: $contacts")
                if (contacts.status == "true") {
                    _contacts.value = contacts
                } else {
                    _contacts.value = contacts
                    Log.e(TAG, contacts.message )
                }
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }
}