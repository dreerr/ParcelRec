package com.android.sensorlogger.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import com.android.sensorlogger.Utils.Config

class Magnetometer(context: Context, fileName: String) : SensorEventListener, SensorBase(context, fileName) {

    init {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        thresholdX = Config.Sensor.MAG_X_THRESHOLD
        thresholdY = Config.Sensor.MAG_Y_THRESHOLD
        thresholdZ = Config.Sensor.MAG_Z_THRESHOLD
    }
}