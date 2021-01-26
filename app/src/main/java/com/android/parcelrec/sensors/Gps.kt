package com.android.parcelrec.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Looper
import com.android.parcelrec.utils.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class Gps(context: Context) : Logger(context, "GPS") {
    private var lastLoc: Location? = null

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    lateinit var settingsClient: SettingsClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    init {
        createLocationRequest()
        createLocationCallBack()
    }

    @SuppressLint("MissingPermission")
    fun createLocationRequest() {

        locationRequest = LocationRequest().apply {
            interval = 20 * 1000
            fastestInterval = 5 * 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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

    @SuppressLint("MissingPermission")
    private fun createLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onLocationChanged(locationResult.lastLocation)
            }
        }
        App.accelerometer?.thresholdStartedListeners?.add {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
        }
    }

    fun onLocationChanged(loc: Location) {
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
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopLog()
    }
}