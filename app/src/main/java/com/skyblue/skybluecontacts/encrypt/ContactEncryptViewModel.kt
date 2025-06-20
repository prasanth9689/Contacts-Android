package com.skyblue.skybluecontacts.encrypt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyblue.skybluecontacts.encrypt.model.EncryptPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactEncryptViewModel @Inject constructor(private val repository: ContactEncryptRepository) : ViewModel() {
    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private val _encrypt = MutableLiveData<EncryptPost>()
    val encrypt: LiveData<EncryptPost> = _encrypt

    fun uploadContacts(encryptPost: EncryptPost) = viewModelScope.launch {
        _uploadStatus.value = repository.uploadContacts(encryptPost)
    }
}