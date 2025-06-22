package com.skyblue.skybluecontacts.model

import android.view.View

data class Options(
    val action: String,
    val firstName: String,
    val phoneNumber: String,
    val view: View,
    val contact: ContactsRoom)