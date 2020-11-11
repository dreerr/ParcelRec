package com.android.parcelrec

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import com.android.parcelrec.camera.Camera
import com.android.parcelrec.sensors.Gps
import com.android.parcelrec.utils.Settings
import com.android.parcelrec.networking.UploadManager
import com.android.parcelrec.sensors.Accelerometer
import com.android.parcelrec.sensors.Battery
import com.android.parcelrec.sensors.Gyroscope
import com.android.parcelrec.sensors.Magnetometer
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.sensors.Wifi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.io.File

class App : Application() {
    companion object {
        lateinit var uploadManager : UploadManager
        lateinit var settings : Settings
        lateinit var storageDir: File

        var accelerometer : Accelerometer? = null
        var battery : Battery? = null
        var camera : Camera? = null
        var gyroscope : Gyroscope? = null
        var magnetometer: Magnetometer? = null
        var gps : Gps? = null
        var wifi : Wifi? = null
        val scope = MainScope()
    }

    override fun onCreate() {
        super.onCreate()
        val storageDirs = applicationContext.getExternalFilesDirs(null)
        storageDir = storageDirs.filterNotNull().last()
        settings = Settings(applicationContext)
        uploadManager = UploadManager(applicationContext)

        // Initialize all Loggers
        fun <T> tryOrNull(f: () -> T) =
            try {
                f()
            } catch (e: Exception) {
                Log.e(TAG, "Could not initialize: ${e.localizedMessage}")
                null
            }
        accelerometer = tryOrNull { Accelerometer(applicationContext) }
        battery =  tryOrNull { Battery(applicationContext) }
        camera = tryOrNull { Camera(applicationContext) }
        gps = tryOrNull { Gps(applicationContext) }
        gyroscope = tryOrNull { Gyroscope(applicationContext) }
        magnetometer = tryOrNull { Magnetometer(applicationContext) }
        wifi = tryOrNull { Wifi(applicationContext) }
    }

    override fun onTerminate() {
        super.onTerminate()
        scope.cancel()
    }
}