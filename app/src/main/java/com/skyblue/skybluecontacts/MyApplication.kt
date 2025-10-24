package com.skyblue.skybluecontacts

import android.app.Application
import com.skyblue.skybluecontacts.session.SessionHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionHandler.init(this)
    }
}