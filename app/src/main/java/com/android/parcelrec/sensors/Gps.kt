package com.android.parcelrec.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Looper
import com.android.parcelrec.App
import com.android.parcelrec.utils.Log
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.concurrent.TimeUnit


class Gps(context: Context) : Logger(context, "GPS") {
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    lateinit var settingsClient: SettingsClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    private var lastLoc: Location? = null

    init {
        createFused()
    }

    private fun createFused() {
        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(6)
            fastestInterval = TimeUnit.SECONDS.toMillis(3)
            maxWaitTime = TimeUnit.MINUTES.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    processLocation(location)
                }
            }
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        settingsClient = LocationServices.getSettingsClient(context)

        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            Log.e(TAG, "No GPS: {${exception.localizedMessage}}")
            throw Exception(exception.localizedMessage)
        }
    }

    fun processLocation(loc: Location) {
        if (lastLoc?.latitude == loc.latitude &&
            lastLoc?.longitude == loc.longitude
        ) return
        val line = "${Util.dateString(loc.time)};${loc.latitude};${loc.longitude};" +
                "${loc.altitude};${loc.accuracy};${loc.speed};${loc.bearing}\n"

        write(line)
        lastLoc = loc
    }

    @SuppressLint("MissingPermission")
    fun run() {
        App.accelerometer?.thresholdStartedListeners?.add {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }

        App.accelerometer?.thresholdEndedListeners?.add {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    fun stop() {
        stopLog()
    }
}