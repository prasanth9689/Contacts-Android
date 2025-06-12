package com.skyblue.skybluecontacts.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.skyblue.skybluecontacts.model.ContactsRoom

@Database(entities = [ContactsRoom::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactsRoomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "contacts_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}