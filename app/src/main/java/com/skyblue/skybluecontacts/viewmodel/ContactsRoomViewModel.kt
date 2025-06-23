package com.skyblue.skybluecontacts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.repository.ContactsRoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactsRoomViewModel(private val repository: ContactsRoomRepository) : ViewModel() {
    private val _contacts = MutableStateFlow<List<ContactsRoom>>(emptyList())
    val contacts: StateFlow<List<ContactsRoom>> = _contacts

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _filteredItems = MutableLiveData<List<ContactsRoom>>()
    val filteredItems: LiveData<List<ContactsRoom>> get() = _filteredItems

    fun insertContact(contact: List<ContactsRoom>) = viewModelScope.launch {
        repository.insertContact(contact)
    }

    fun getAllContacts() = viewModelScope.launch {
        _contacts.value = repository.getAllContacts()
    }

    fun deleteAllContacts() {
        viewModelScope.launch {
            repository.deleteAllContacts()
        }
    }

    fun checkIfContactsEmpty() {
        viewModelScope.launch {
            _isEmpty.postValue(repository.isContactsEmpty())
        }
    }

    fun filter(query: String) {
        val originalList = _contacts.value ?: return
        val lowerQuery = query.lowercase()

        _filteredItems.value = if (lowerQuery.isEmpty()) {
            originalList
        } else {
            originalList.filter { item ->
                item.firstName.contains(lowerQuery, ignoreCase = true)
            }
        }
    }

    fun deleteContact(contactId: Int) {
        viewModelScope.launch {
            repository.deleteContactByContactId(contactId)
        }
    }
}