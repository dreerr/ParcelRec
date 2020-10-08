package com.android.sensorlogger.sensors
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.android.sensorlogger.utils.Logger
import java.text.SimpleDateFormat
import java.util.*


open class SensorBase(context: Context, filename_tag:String) : SensorEventListener, Logger(context, filename_tag)  {
    //Sensor variables
    var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    lateinit var sensor : Sensor

    // Previous values
    var prevX : Float? = null
    var prevY : Float? = null
    var prevZ : Float? = null

    //Threshold levels
    var thresholdX : Double = 0.0
    var thresholdY : Double  = 0.0
    var thresholdZ : Double  = 0.0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Nothing to do yet.
    }

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
        val line = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US).format(Date()) +
                ":${event.values.joinToString(";")}\n"
        writeLine(line)
    }

    fun run(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop(){
        sensorManager.unregisterListener(this)
        closeFile()
    }
}