package com.skyblue.skybluecontacts.model

data class MultiSelect(
    val id: Int,
    val firstName: String,
    val phoneNumber: String,
    var isSelected: Boolean = false // used for selection
)
