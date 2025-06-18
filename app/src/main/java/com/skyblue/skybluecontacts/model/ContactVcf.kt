package com.skyblue.skybluecontacts.model

data class ContactVcf(
    val name: String,
    val phone: String,
    var isSelected: Boolean = false
)
