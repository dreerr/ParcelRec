package com.android.sensorlogger

import android.app.Application
import com.android.sensorlogger.utils.SessionManager
import com.android.sensorlogger.networking.UploadManager

class App : Application() {
    companion object {
        lateinit var uploadManager : UploadManager
        lateinit var sessionManager : SessionManager
        var inMovement = false
        var lastUpload = "-"
        var networkTraffic = 0.0
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
        uploadManager = UploadManager(applicationContext)
    }
}