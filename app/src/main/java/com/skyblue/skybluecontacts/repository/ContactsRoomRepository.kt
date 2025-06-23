package com.skyblue.skybluecontacts.repository

import androidx.lifecycle.LiveData
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.room.ContactsRoomDao

class ContactsRoomRepository(private val contactDao: ContactsRoomDao) {

        suspend fun deleteContactByContactId(contactId: Int) {
            contactDao.deleteContactByContactId(contactId)
        }

    suspend fun insertContact(contact: List<ContactsRoom>) = contactDao.insertContact(contact)
    suspend fun getAllContacts() = contactDao.getAllContacts()

    suspend fun deleteAllContacts() = contactDao.deleteAllContacts()
    suspend fun isContactsEmpty(): Boolean {
        return contactDao.getContactsCount() == 0
    }
}