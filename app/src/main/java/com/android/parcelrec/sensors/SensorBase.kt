package com.android.parcelrec.sensors
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.android.parcelrec.utils.Log
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
    var numPrevValues = 5
    private var lastUpdate = Date().time

    // Threshold Levels
    var threshold : Double = 0.0
    var sampleRate : Long = 250L

    // Threshold Events
    private var inThreshold = false
    private var thresholdDidEndJob: Job? = null
    var thresholdStartedListeners = ArrayList<()->Unit>()
    var thresholdEndedListeners = ArrayList<()->Unit>()

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return

        // Check if sampleRate has passed
        val curTime = Date().time
        if (curTime - lastUpdate < sampleRate) return
        lastUpdate = curTime

        // Check Threshold for each axis
        var thresholdExceeded = (prevValues.count() == 0)
        var differentValues = (prevValues.count() == 0)
        event.values.withIndex().forEach { axis ->
            prevValues.forEach { prevValue ->
                val prevAxis = prevValue[axis.index]
                val diff = abs(prevAxis - axis.value)
                if (diff > threshold) thresholdExceeded = true
                if (diff > 0) differentValues = true
            }
        }

        // Keep track of previous values
        prevValues.add(event.values.copyOf())
        if(prevValues.count() > numPrevValues) prevValues.removeAt(0)

        if(thresholdExceeded) processListeners()
        if(differentValues) logEvent(event)
    }

    open fun logEvent(event: SensorEvent?) {
        if(event==null) return

        // Do Logging
        val line = "${Util.dateString};${event.values.joinToString(";")}\n"
        write(line)
    }

    private fun processListeners() {
        if(thresholdStartedListeners.count() == 0 &&
            thresholdEndedListeners.count() == 0) return

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
