package com.android.parcelrec.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.PermissionHelper
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit

class Gps(context: Context) : Logger(context, "GPS") {
    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    private var lastLoc: Location? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    init {
        initLocation()
    }

    private fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(3)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(30)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult?.lastLocation != null) {
                    onLocationChanged(locationResult.lastLocation)
                } else {
                    Log.d(TAG, "Location information isn't available.")
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw Exception("No Permission for GPS!")
        }
    }

    fun onLocationChanged(loc: Location) {
        if (lastLoc?.latitude == loc.latitude &&
            lastLoc?.longitude == loc.longitude
        ) return
        val line = "${Util.dateString(loc.time)};${loc.latitude};${loc.longitude};" +
                "${loc.altitude};${loc.accuracy};${loc.speed};${loc.bearing}\n"

        writeLine(line)
        lastLoc = loc
    }

    @SuppressLint("MissingPermission")
    fun run() {
        App.accelerometer?.thresholdStartedListeners?.add {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        App.accelerometer?.thresholdEndedListeners?.add {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    fun stop() {
        stopLog()
    }
}