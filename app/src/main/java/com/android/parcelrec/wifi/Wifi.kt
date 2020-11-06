package com.android.parcelrec.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import com.android.parcelrec.App
import com.android.parcelrec.utils.Config
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.*

class Wifi(context : Context) : Logger(context, "WIFI") {
    private var wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var scanJob: Job? = null
    private var availableNetworks = mutableListOf<ScanResult>()

    fun run(){
        if (!wifiManager.isWifiEnabled){
            Toast.makeText(context, "Wifi is turned off, SSIDs will not be logged.", Toast.LENGTH_LONG).show()
            return
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        App.accelerometer?.thresholdStartedListeners?.add {
            if(scanJob!=null && scanJob!!.isActive) return@add
            scanJob = App.scope.launch(Dispatchers.IO) {
                while (true) {
                    try {
                        val success = wifiManager.startScan()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "startScan failed, ${e.localizedMessage}")
                    }
                    delay(Config.Wifi.INTERVAL)
                }
            }
        }

        App.accelerometer?.thresholdEndedListeners?.add {
            scanJob?.cancel()
        }
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            }
        }
    }

    private fun scanSuccess() = App.scope.launch(Dispatchers.IO) {
        val results = wifiManager.scanResults
        Log.d("WIFI", "Wifi scan success.")
        results.forEach {
            if (!availableNetworks.contains(it)){
                //New network found
                availableNetworks.add(it)
                val line = "${Util.simpleTime};${it.SSID};${it.BSSID};;\n"
                writeLine(line)
            }
        }

        val elementToRemove = arrayListOf<ScanResult>()
        availableNetworks.forEach {
            if (!results.contains(it)){
                //Lost a network
                elementToRemove.add(it)
                val line = "${Util.simpleTime};;;${it.SSID};${it.BSSID}\n"
                writeLine(line)
            }
        }

        //Remove elements that were lost
        elementToRemove.forEach {
            availableNetworks.remove(it)
        }
    }



    fun stop(){
        scanJob?.cancel()
        context.unregisterReceiver(wifiScanReceiver)
        stopLog()
    }

}