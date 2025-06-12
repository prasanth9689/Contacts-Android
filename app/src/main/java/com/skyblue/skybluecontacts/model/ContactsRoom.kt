package com.skyblue.skybluecontacts.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactsRoom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val phoneNumber: String
)
