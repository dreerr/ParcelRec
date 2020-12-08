package com.android.parcelrec.sensors
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Logger
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

open class SensorBase(context: Context, fileNameTag:String) : SensorEventListener, Logger(context, fileNameTag)  {
    // Sensor Variables
    var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    lateinit var sensor : Sensor

    // Previous values
    private var prevValues = ArrayList<FloatArray>()
    var numPrevValues = 10
    private var lastUpdate = Date().time

    // Threshold Levels
    var threshold : Double = 0.0

    // Threshold Events
    private var inThreshold = false
    private var thresholdDidEndJob: Job? = null
    var thresholdStartedListeners = ArrayList<()->Unit>()
    var thresholdEndedListeners = ArrayList<()->Unit>()

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return

        // Check if 100ms have passed
        val curTime = Date().time
        if (curTime - lastUpdate < 100) return
        lastUpdate = curTime

        // Check Threshold for each axis
        var thresholdExceeded = (prevValues.count() == 0)
        event.values.withIndex().forEach { axis ->
            prevValues.forEach { prevValue ->
                val prevAxis = prevValue[axis.index]
                val diff = abs(prevAxis - axis.value)
                if (diff > threshold) {
                    thresholdExceeded = true
                }
            }
        }

        // Keep track of previous values
        prevValues.add(event.values.copyOf())
        if(prevValues.count() > numPrevValues) prevValues.removeAt(0)

        // Finally work with the exceeded threshold
        if(thresholdExceeded) onThresholdExceeded(event)
    }

    open fun onThresholdExceeded(event: SensorEvent?) {
        if(event==null) return

        // Do Logging
        val line = "${Util.dateString(event.timestamp)};${event.values.joinToString(";")}\n"
        writeLine(line)

        // Handle Threshold Events
        if(inThreshold) {
            thresholdDidEndJob?.cancel()
        } else {
            Log.i(sensor.name,"onThresholdStarted")
            inThreshold = true
            App.scope.launch(Dispatchers.IO) {
                thresholdStartedListeners.forEach { it.invoke() }
            }
        }
        thresholdDidEndJob = App.scope.launch(Dispatchers.IO) {
            delay(App.settings.recDuration * 1000L)
            inThreshold = false
            Log.i(sensor.name, "Threshold Ended")
            App.scope.launch(Dispatchers.IO) {
                thresholdEndedListeners.forEach { it.invoke() }
            }
        }
    }

    fun run(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop(){
        sensorManager.unregisterListener(this)
        if(inThreshold) {
            thresholdEndedListeners.forEach { it.invoke() }
            inThreshold = false
        }
        thresholdDidEndJob?.cancel()
        thresholdStartedListeners.clear()
        thresholdEndedListeners.clear()
        stopLog()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
