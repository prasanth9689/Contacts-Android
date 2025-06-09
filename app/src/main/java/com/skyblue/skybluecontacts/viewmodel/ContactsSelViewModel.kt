package com.skyblue.skybluecontacts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.skyblue.skybluecontacts.model.ContactsSelection

class ContactsSelViewModel : ViewModel(){
    private val _contacts = MutableLiveData<List<ContactsSelection>>()
    val contacts: LiveData<List<ContactsSelection>> = _contacts

    fun setContactsSelection(list: List<ContactsSelection>){
        _contacts.value = list
    }

    fun toggleSelection(position: Int){
        _contacts.value = _contacts.value?.toMutableList()?.also {
            val contact = it[position]
            it[position] = contact.copy(isSelected = !contact.isSelected)
        }
    }

    fun getSelectedContacts(): List<ContactsSelection>{
        return _contacts.value?.filter { it.isSelected } ?: emptyList()
    }
}