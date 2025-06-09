package com.skyblue.skybluecontacts.model

data class ContactPayload(
    val userId: String,
    val acc: String = "save_contacts",
    val contacts: List<Contacts>
)
