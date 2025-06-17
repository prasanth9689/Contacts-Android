package com.skyblue.skybluecontacts.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.model.ContactVcf
import com.skyblue.skybluecontacts.repository.ContactVcfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactVcfViewModel(private val repository: ContactVcfRepository) : ViewModel() {

    private val _contacts = MutableLiveData<List<ContactVcf>>()
    val contacts: LiveData<List<ContactVcf>> = _contacts

    fun loadContactsFromVcf(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.parseVcfFile(uri, context)
            withContext(Dispatchers.Main) {
                _contacts.value = list
            }
        }
    }
}
