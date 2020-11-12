package com.android.parcelrec.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.*
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.PermissionHelper
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util

class Gps(context: Context) : LocationListener, Logger(context, "GPS")
{
    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    private var lastLoc : Location? = null
    private var provider = ""

    init {
        if (!PermissionHelper.hasGpsPermission(context)) {
            throw Exception("No Permission for GPS!")
        }
    }
    @SuppressLint("MissingPermission")
    fun run() {
        App.accelerometer?.thresholdStartedListeners?.add {
            try {
                locationManager.requestLocationUpdates("fused", 1000, 0f, this,  Looper.getMainLooper())
            } catch (e: Throwable) {
                Log.e(TAG, "Could not requestLocationUpdates ${e.localizedMessage}")
            }
        }
        App.accelerometer?.thresholdEndedListeners?.add {
            locationManager.removeUpdates(this)
        }
    }

    override fun onLocationChanged(loc: Location) {
        if(lastLoc?.latitude == loc.latitude &&
            lastLoc?.longitude == loc.longitude) return

        val line = "${Util.dateString};${loc.latitude};${loc.longitude};${loc.altitude};${loc.accuracy};${loc.speed};${loc.bearing}\n"

        writeLine(line)
        lastLoc = loc
    }

    fun stop(){
        stopLog()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
}