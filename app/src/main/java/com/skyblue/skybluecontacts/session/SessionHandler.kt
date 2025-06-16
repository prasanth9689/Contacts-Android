package com.skyblue.skybluecontacts.session

import android.content.Context
import android.content.SharedPreferences
import com.skyblue.skybluecontacts.model.User
import java.util.Date
import androidx.core.content.edit

object SessionHandler {
    private var PREFS_KEY = "prefs"
    private const val MODE = Context.MODE_PRIVATE
    private var USER_ID_KEY = "user_id"
    private var USER_NAME_KEY = "user_name"
    private const val KEY_EXPIRES = "expires"
    private const val KEY_EMPTY = "expires"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_KEY, MODE)
    }

    // TODO step 1: call `AppPreferences.setup(applicationContext)` in your MainActivity's `onCreate` method
    // TODO step 2: set your app name here
    fun loginUser(userId: String, userName: String){
        sharedPreferences.edit {
            putString(USER_ID_KEY, userId)
            putString(USER_NAME_KEY, userName)
            val date = Date()

            //Set user session for next 7 days
            val millis = date.time + 7 * 24 * 60 * 60 * 1000
            putLong(KEY_EXPIRES, millis)
        }
    }

    /**
     * Fetches and returns user details
     *
     * @return user details
     */
    fun getUserDetails(): User? {
        //Check if user is logged in first
        if (!isLoggedIn()) {
            return null
        }
        val user = User()
        user.userId = sharedPreferences.getString(USER_ID_KEY, KEY_EMPTY)
        user.userName = sharedPreferences.getString(USER_NAME_KEY, KEY_EMPTY)
        user.sessionExpiryDate = Date(sharedPreferences.getLong(KEY_EXPIRES, 0))

        return user
    }


    /**
     * Logs out user by clearing the session
     */
    fun logoutUser() {
        sharedPreferences.edit(commit = true) {
            clear()
        }
    }

    /**
     * Checks whether user is logged in
     *
     * @return
     */
    fun isLoggedIn(): Boolean {
        val currentDate = Date()
        val millis: Long = sharedPreferences.getLong(KEY_EXPIRES, 0)

        /* If shared preferences does not have a value
         then user is not logged in
         */if (millis == 0L) {
            return false
        }
        val expiryDate = Date(millis)

        /* Check if session is expired by comparing
        current date and Session expiry date
        */return currentDate.before(expiryDate)
    }
}