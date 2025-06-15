package com.skyblue.skybluecontacts.model

import com.skyblue.skybluecontacts.activity.LoginActivity

data class Login(
    val status: String,
    val message: String,

    val response: List<LoginActivity.UserResponse>?
)

