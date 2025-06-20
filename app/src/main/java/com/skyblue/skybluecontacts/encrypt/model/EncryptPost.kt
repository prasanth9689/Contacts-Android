package com.skyblue.skybluecontacts.encrypt.model

import com.skyblue.skybluecontacts.model.Contacts

data class EncryptPost(
    val acc: String,
    val data: Contacts
)