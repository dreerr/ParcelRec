package com.android.parcelrec.sensors

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class Battery(context : Context) : Logger(context, "Battery") {
    var batteryManager =
        context.getSystemService(BATTERY_SERVICE) as BatteryManager
    private var lastLevel = -1
    val batteryLevel
        get() = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    var scanJob: Job? = null

    fun run() {
        if(scanJob != null && scanJob!!.isActive) return
        scanJob = App.scope.launch {
            while(isActive) {
                if(lastLevel != batteryLevel) {
                    val line = "${Util.dateString};$batteryLevel\n"
                    write(line)
                    lastLevel = batteryLevel
                }
                delay(15 * 60_000)
            }
        }
    }
    fun stop() {
        scanJob?.cancel()
        stopLog()
    }
}