package com.example.wisefox

import android.app.Application
import android.content.Context
import com.example.wisefox.utils.LocaleHelper
import com.example.wisefox.utils.SessionManager

class WiseFoxApplication : Application() {

    // FIX #2: SessionManager must be initialised BEFORE attachBaseContext touches
    // SharedPreferences; we initialise here using a temporary context, then again
    // safely in onCreate() so prefs are usable for sure.
    override fun attachBaseContext(base: Context) {
        SessionManager.init(base)
        super.attachBaseContext(LocaleHelper.applyStoredLocale(base))
    }

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
