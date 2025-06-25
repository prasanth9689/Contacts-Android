package com.skyblue.skybluecontacts.model

data class TrashResponse (
    val status: String,
    val message: String,
    val response: List<TrashContact>
)