package com.agrolytics.agrolytics_android.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.android.sensorlogger.Utils.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class Gps(private val context: Context) : LocationListener
{
    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

    private val appDirectory = File(context.getExternalFilesDir(null).toString() + "/SensorLogger")
    private val logDirectory = File("$appDirectory/logs")
    lateinit var logFile : File

    private fun initFile(){
        if (!appDirectory.exists()) {
            appDirectory.mkdir()
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdir()
        }
        logFile = File(logDirectory, "GPS_" + SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(Date()) + ".txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
    }

    @SuppressLint("MissingPermission")
    fun run(){
        initFile()
        if (PermissionHelper.hasGpsPermission(context)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this)
        }
    }

    fun stop(){
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(loc: Location) {
        Log.d("GPS", "${loc.latitude} ${loc.longitude}")

        //Low rate, so can be done in every iteration
        GlobalScope.launch(Dispatchers.IO) {
            writeToFile(loc)
        }
    }

    private fun writeToFile(loc: Location){
        val outputStreamWriter = OutputStreamWriter(FileOutputStream(logFile, true))

        val line = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US).format(Date()) + ":"
                   loc.latitude.toString() + ";"
                   loc.longitude.toString() + "\n"
        outputStreamWriter.write(line)
        outputStreamWriter.close()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
}