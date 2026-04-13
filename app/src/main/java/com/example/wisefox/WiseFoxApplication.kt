package com.example.wisefox

import android.app.Application
import com.example.wisefox.utils.SessionManager

class WiseFoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}