package com.android.parcelrec.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import com.android.parcelrec.utils.Config

class Magnetometer(context: Context, fileName: String) : SensorEventListener, SensorBase(context, fileName) {

    init {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        threshold = Config.Sensor.MAG_THRESHOLD
        numPrevValues = 5
    }
}