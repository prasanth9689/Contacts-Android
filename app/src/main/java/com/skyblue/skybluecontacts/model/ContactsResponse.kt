package com.skyblue.skybluecontacts.model

data class ContactResponse(
    val status: String,
    val message: String,
    val response: List<Contacts>
)
