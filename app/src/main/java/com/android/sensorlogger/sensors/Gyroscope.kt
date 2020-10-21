package com.android.parcelrec.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import com.android.parcelrec.utils.Config

class Gyroscope(context: Context, fileName: String) : SensorEventListener, SensorBase(context, fileName) {

    init {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        thresholdX = Config.Sensor.GYRO_X_THRESHOLD
        thresholdY = Config.Sensor.GYRO_Y_THRESHOLD
        thresholdZ = Config.Sensor.GYRO_Z_THRESHOLD
    }
}