package com.android.sensorlogger

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import com.android.sensorlogger.utils.SensorServiceActions
import com.android.sensorlogger.utils.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SensorService : Service(){
    private var isServiceStarted = false
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Some component wants to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Log.d(TAG, "using an intent with action $action")
            when (action) {
                SensorServiceActions.START.name -> startService()
                SensorServiceActions.STOP.name -> stopService()
                SensorServiceActions.ROTATE.name -> rotate()
                else -> Log.d(TAG, "This should never happen. No action in the received intent")
            }
        } else {
            Log.d(TAG,"with a null intent. It has been probably restarted by the system." )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "The service has been created")
        val notification = createNotification()
        startForeground(1, notification)
    }


    private fun startService() {
        if (isServiceStarted) return
        Log.d(TAG,"Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService::lock").apply {
                    acquire()
                }
            }

        App.wifi?.run()
        App.accelerometer?.run()
        App.gyroscope?.run()
        App.magnetometer?.run()
        App.camera?.run()
        App.gps?.run()
    }

    private fun stopService() {
        if (!isServiceStarted) return
        Log.d(TAG, "Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(TAG, "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        App.wifi?.stop()
        App.accelerometer?.stop()
        App.gyroscope?.stop()
        App.magnetometer?.stop()
        App.gps?.stop()
        App.camera?.stop()
    }

    private fun rotate() {
        App.wifi?.rotate()
        App.accelerometer?.rotate()
        App.gyroscope?.rotate()
        App.magnetometer?.rotate()
        App.gps?.rotate()
        App.camera?.rotate()
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "SENSOR LOGGER CHANNEL"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId,
            "Sensor Logger notifications channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "Sensor Logger channel"
            it.enableLights(true)
            it.lightColor = Color.RED
            it
        }
        notificationManager.createNotificationChannel(channel)


        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder =  Notification.Builder(this, notificationChannelId )

        return builder
            .setContentTitle("Sensor Logger")
            .setContentText("Measurement is running in the background.")
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(pendingIntent)
            .setTicker("Sensor Logger")
            .build()
    }
}