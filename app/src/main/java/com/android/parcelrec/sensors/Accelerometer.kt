package com.android.parcelrec.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Config
import com.android.parcelrec.utils.TAG
import java.lang.RuntimeException

class Accelerometer(context: Context) : SensorEventListener, SensorBase(context, "Accelerometer") {
    init {
        // Initialize Sensor
        this.sensor = try {
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        } catch (e: RuntimeException) {
            Log.e(TAG, "No TYPE_LINEAR_ACCELERATION available, falling back to TYPE_ACCELEROMETER")
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        // Set Threshold from Settings
        threshold = Config.Sensor.ACC_THRESHOLD
        numPrevValues = 2
    }
}