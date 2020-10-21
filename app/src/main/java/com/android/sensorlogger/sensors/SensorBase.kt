package com.android.sensorlogger.sensors
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.android.sensorlogger.App
import com.android.sensorlogger.utils.*
import kotlinx.coroutines.*
import java.util.*

open class SensorBase(context: Context, filename_tag:String) : SensorEventListener, Logger(context, filename_tag)  {
    // Sensor Variables
    var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    lateinit var sensor : Sensor

    // Previous values
    var prevX : Float? = null
    var prevY : Float? = null
    var prevZ : Float? = null

    // Threshold Levels
    var thresholdX : Double = 0.0
    var thresholdY : Double = 0.0
    var thresholdZ : Double = 0.0

    // Threshold Events
    var inThreshold = false
    var thresholdDidEndJob: Job? = null
    var thresholdStartedListeners = ArrayList<()->Unit>()
    var thresholdEndedListeners = ArrayList<()->Unit>()

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val thresholdExceeded = prevX == null ||
                    kotlin.math.abs(prevX!! - x) > thresholdX ||
                    kotlin.math.abs(prevY!! - y) > thresholdY ||
                    kotlin.math.abs(prevZ!! - z) > thresholdZ

        prevX = x
        prevY = y
        prevZ = z

        if(thresholdExceeded) onThresholdExceeded(event)
    }

    open fun onThresholdExceeded(event: SensorEvent?) {
        if(event==null) return

        // Do Logging
        val line = "${Util.simpleTime};${event.values.joinToString(";")}\n"
        writeLine(line)

        // Handle Threshold Events
        if(inThreshold) {
            thresholdDidEndJob?.cancel()
        } else {
            Log.i(sensor.name,"onThresholdStarted")
            inThreshold = true
            App.scope.launch(Dispatchers.IO) {
                for(l in thresholdStartedListeners) l.invoke()
            }
        }
        thresholdDidEndJob = App.scope.launch(Dispatchers.IO) {
            delay(Config.Sensor.MOVEMENT_DELAY)
            inThreshold = false
            Log.i(sensor.name, "onThresholdEnded")
            App.scope.launch(Dispatchers.IO) {
                for (l in thresholdEndedListeners) l.invoke()
            }
        }
    }

    fun run(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop(){
        sensorManager.unregisterListener(this)
        if(inThreshold) for(l in thresholdEndedListeners) l.invoke()
        closeLog()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}