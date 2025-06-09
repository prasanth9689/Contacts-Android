package com.skyblue.skybluecontacts.model

data class ContactsSelection (
    val firstName: String,
    val phoneNumber: String,
    val isSelected: Boolean = false
)