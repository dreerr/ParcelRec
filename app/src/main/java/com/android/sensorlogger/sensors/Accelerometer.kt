package com.android.sensorlogger.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.util.Log
import com.android.sensorlogger.App
import com.android.sensorlogger.Utils.Config
import com.android.sensorlogger.Utils.TAG
import java.lang.RuntimeException

class Accelerometer(context: Context, fileName: String) : SensorEventListener, SensorBase(context, fileName) {
    private val movementDelay : Long = 30000
    private val movementHandler = Handler()
    private val movementResetRunnable = Runnable{
        App.inMovement = false
        Log.d(TAG, "Not moved for ${movementDelay/1000} seconds. Resetting movement state.")
    }

    init {
        this.sensor = try {
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        } catch (e: RuntimeException) {
            Log.e(TAG, "No TYPE_LINEAR_ACCELERATION available, falling back to TYPE_ACCELEROMETER")
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        thresholdX = Config.Sensor.ACC_X_THRESHOLD
        thresholdY = Config.Sensor.ACC_Y_THRESHOLD
        thresholdZ = Config.Sensor.ACC_Z_THRESHOLD
    }

    override fun onThresholdExceeded(event: SensorEvent?) {
        super.onThresholdExceeded(event)

        if (App.inMovement){
            movementHandler.removeCallbacks(movementResetRunnable)
            movementHandler.postDelayed(movementResetRunnable, movementDelay)
            Log.d(TAG, "Threshold exceeded, reset runnable.")
        } else {
            App.inMovement = true
            movementHandler.postDelayed(movementResetRunnable, movementDelay)
            Log.d(TAG, "Threshold exceeded, started runnable")
        }
    }
}