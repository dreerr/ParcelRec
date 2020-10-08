package com.android.sensorlogger

import android.app.Application
import com.android.sensorlogger.Utils.SessionManager
import com.android.sensorlogger.networking.UploadManager
import java.io.File

class App : Application() {
    companion object {
        lateinit var uploadManager : UploadManager
        lateinit var sessionManager : SessionManager
        var inMovement = false
        lateinit var storage: File
        var lastUpload = "-"
        var networkTraffic = 0.0
    }

    override fun onCreate() {
        super.onCreate()
        storage = applicationContext.getExternalFilesDir(null)!!
        sessionManager = SessionManager(applicationContext)
        uploadManager = UploadManager(applicationContext)
    }
}