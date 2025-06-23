package com.skyblue.skybluecontacts.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.Contacts
import com.skyblue.skybluecontacts.model.DeleteSingleCloud
import com.skyblue.skybluecontacts.repository.ContactsRepository
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ContactsViewModel : ViewModel(){
    val TAG = "CloudContacts_"
    private val repository = ContactsRepository()
    private val _contacts = MutableLiveData<List<Contacts>>()
    private val _filteredItems = MutableLiveData<List<Contacts>>()
    val filteredItems: LiveData<List<Contacts>> get() = _filteredItems
    val contacts: LiveData<List<Contacts>> = _contacts

    private val _deleteContact = MutableLiveData<Boolean>()
    val isdeleteContact: LiveData<Boolean> = _deleteContact

    fun fetchContacts(requestBody: RequestBody){
        viewModelScope.launch {
            try {
                val contacts = repository.getContacts(requestBody)
                if (contacts.status == "true") {
                    _contacts.value = contacts.response
                    _filteredItems.value = contacts.response
                } else {
                    _contacts.value = emptyList()
                    Log.e(TAG, contacts.message )
                }
            } catch (e: Exception){
                Log.e(TAG, "error: " + e.message.toString())
            }
        }
    }

    fun filter(query: String) {
        val originalList = _contacts.value ?: return
        val lowerQuery = query.lowercase()

        _filteredItems.value = if (lowerQuery.isEmpty()) {
            originalList
        } else {
            originalList.filter { item ->
                item.firstName.contains(lowerQuery, ignoreCase = true) ||
                        item.firstName.contains(lowerQuery, ignoreCase = true)
            }
        }
    }

    fun deleteCloudContact(deleteSingleCloud: DeleteSingleCloud){
        viewModelScope.launch {
            try {
                val response = repository.deleteCloudContact(deleteSingleCloud)
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