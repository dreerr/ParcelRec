package com.agrolytics.agrolytics_android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.sensorlogger.App
import com.android.sensorlogger.Utils.Logger
import com.android.sensorlogger.Utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class Gps(context: Context) : LocationListener, Logger(context, "GPS")
{
    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun run(){
        if (PermissionHelper.hasGpsPermission(context)) {
            locationManager.requestLocationUpdates("fused", 5000, 0f, this)
            initLogFile()
        }
    }

    fun stop(){
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(loc: Location) {
        //Low rate, so can be done in every iteration
        if (App.inMovement){
            GlobalScope.launch(Dispatchers.IO) {
                val line = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US).format(Date()) + ":" +
                        loc.latitude.toString() + ";"
                        loc.longitude.toString() + "\n"
                Log.d("GPS", "Logging coordinates: ${loc.latitude} ${loc.longitude}")
                writeToFile(line)
                closeFile()
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
}