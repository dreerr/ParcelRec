package com.android.parcelrec.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.PermissionHelper
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    override fun onLocationChanged(loc: Location) {
        App.scope.launch(Dispatchers.IO) {
            if(lastLoc?.latitude == loc.latitude &&
                lastLoc?.longitude == loc.longitude) return@launch

            val line = "${Util.dateString};${loc.latitude};${loc.longitude}\n"
            Log.d(TAG, "Logging coordinates: ${loc.latitude}, ${loc.longitude}")

            writeLine(line)
            lastLoc = loc
        }
    }

    fun stop(){
        locationManager.removeUpdates(this)
        stopLog()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
}