package com.android.sensorlogger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.se.omapi.Session
import com.android.sensorlogger.Utils.SessionManager
import com.android.sensorlogger.networking.ApiService
import java.io.File

class App : Application() {
    companion object {
        var CHANNEL_ID = "ServiceChannel"
        var ApiService = ApiService()
        var inMovement = false
        lateinit var sessionManager : SessionManager

        var lastUpload = "-"
        var networkTraffic = 0.0
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sessionManager = SessionManager(applicationContext)
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, "Channel name", importance)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}