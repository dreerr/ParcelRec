package com.android.parcelrec

import android.app.Application
import android.util.Log
import com.android.parcelrec.camera.Camera
import com.android.parcelrec.gps.Gps
import com.android.parcelrec.utils.Settings
import com.android.parcelrec.networking.UploadManager
import com.android.parcelrec.sensors.Accelerometer
import com.android.parcelrec.sensors.Gyroscope
import com.android.parcelrec.sensors.Magnetometer
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.wifi.Wifi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.io.File

class App : Application() {
    companion object {
        lateinit var uploadManager : UploadManager
        lateinit var settings : Settings
        lateinit var storageDir: File

        var accelerometer : Accelerometer? = null
        var gyroscope : Gyroscope? = null
        var magnetometer: Magnetometer? = null
        var camera : Camera? = null
        var gps : Gps? = null
        var wifi : Wifi? = null
        val scope = MainScope()
    }

    override fun onCreate() {
        super.onCreate()
        storageDir = applicationContext.getExternalFilesDir(null)!!
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
        accelerometer = tryOrNull { Accelerometer(applicationContext, "ACC") }
        gyroscope = tryOrNull { Gyroscope(applicationContext, "GYRO") }
        magnetometer = tryOrNull { Magnetometer(applicationContext, "MAG") }
        gps = tryOrNull { Gps(applicationContext) }
        camera = tryOrNull { Camera(applicationContext) }
        wifi = tryOrNull { Wifi(applicationContext) }
    }

    override fun onTerminate() {
        super.onTerminate()
        scope.cancel()
    }
}