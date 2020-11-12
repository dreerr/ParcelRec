package com.android.parcelrec.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.*
import java.lang.Exception

class Wifi(context : Context) : Logger(context, "WiFi") {
    private var wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var scanJob: Job? = null
    private var lastScanResults = mutableListOf<ScanResult>()


    init {
        if (!wifiManager.isWifiEnabled){
            throw Exception("Could not start WiFi!")
        }
    }

    fun run(){

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        App.accelerometer?.thresholdStartedListeners?.add {
            if(scanJob!=null && scanJob!!.isActive) return@add
            scanJob = App.scope.launch(Dispatchers.IO) {
                while (isActive) {
                    try {
                        val success = wifiManager.startScan()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "startScan failed, ${e.localizedMessage}")
                    }
                    delay(60*60*1000L)
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
        val scanResults = wifiManager.scanResults
        Log.d("WIFI", "Wifi scan success.")
        scanResults.forEach {
            if (!lastScanResults.any{ lastNetwork -> lastNetwork.BSSID == it.BSSID }){
                //New network found
                lastScanResults.add(it)
                val line = "${Util.dateString};${it.SSID};${it.BSSID};;\n"
                writeLine(line)
            }
        }

        lastScanResults.toList().forEach {
            if (!scanResults.any{ network -> network.BSSID == it.BSSID }){
                //Lost a network
                lastScanResults.remove(it)
                val line = "${Util.dateString};;;${it.SSID};${it.BSSID}\n"
                writeLine(line)
            }
        }
    }



    fun stop(){
        scanJob?.cancel()
        try {
            context.unregisterReceiver(wifiScanReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "context.unregisterReceiver(wifiScanReceiver) failed!")
        }
        stopLog()
    }

}