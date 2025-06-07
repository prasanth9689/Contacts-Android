package com.skyblue.skybluecontacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.User
import com.skyblue.skybluecontacts.repository.ContactsRepository
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel(){
    val TAG = "CloudContacts_"
    private val repository = ContactsRepository()

    private val _contacts = MutableLiveData<List<Contacts>>()
    val contacts: LiveData<List<Contacts>> = _contacts

    fun fetchContacts(user: User){
        viewModelScope.launch {
            try {
                val contacts = repository.getContacts(user)
                _contacts.value = listOf(contacts)
                Log.i(TAG, "response: " + contacts.toString())
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }
}