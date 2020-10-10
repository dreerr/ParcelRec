package com.android.sensorlogger.gps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.android.sensorlogger.utils.Logger
import com.android.sensorlogger.utils.PermissionHelper
import com.android.sensorlogger.utils.TAG
import com.android.sensorlogger.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class Gps(context: Context) : LocationListener, Logger(context, "GPS")
{
    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    private var lastLoc : Location? = null

    @SuppressLint("MissingPermission")
    fun run(){
        if (PermissionHelper.hasGpsPermission(context)) {
            locationManager.requestLocationUpdates("fused", 5000, 0f, this)
        }
    }

    fun stop(){
        locationManager.removeUpdates(this)
        closeFile()
    }

    override fun onLocationChanged(loc: Location) {
        GlobalScope.launch(Dispatchers.IO) {

            if(lastLoc?.latitude == loc.latitude &&
                lastLoc?.longitude == loc.longitude) return@launch

            val line = "${Util.simpleTime};${loc.latitude};${loc.longitude}\n"
            Log.d(TAG, "Logging coordinates: ${loc.latitude}, ${loc.longitude}")

            writeLine(line)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
}