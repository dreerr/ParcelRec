package com.android.parcelrec.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import com.android.parcelrec.utils.Config

class Gyroscope(context: Context) : SensorEventListener, SensorBase(context, "Gyroscope") {

    init {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        threshold = Config.Sensor.GYRO_THRESHOLD
    }
}