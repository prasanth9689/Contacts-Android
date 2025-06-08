package com.skyblue.skybluecontacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.repository.ContactsRepository
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ContactsViewModel : ViewModel(){
    val TAG = "CloudContacts_"
    private val repository = ContactsRepository()

    private val _contacts = MutableLiveData<List<Contacts>>()
    val contacts: LiveData<List<Contacts>> = _contacts

    fun fetchContacts(requestBody: RequestBody){
        viewModelScope.launch {
            try {
                val contacts = repository.getContacts(requestBody)
                if (contacts.status == "true") {
                    _contacts.value = contacts.response
                } else {
                    _contacts.value = emptyList()
                    Log.e(TAG, contacts.message )
                }
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }
}