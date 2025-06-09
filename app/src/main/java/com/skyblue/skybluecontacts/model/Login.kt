package com.skyblue.skybluecontacts.model

import com.skyblue.skybluecontacts.LoginActivity

data class Login(
    val status: String,
    val message: String,

    val response: List<UserResponse>?
)

