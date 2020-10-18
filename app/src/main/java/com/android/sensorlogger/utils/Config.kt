package com.android.sensorlogger.utils

import android.media.MediaRecorder

object Config {
    object Sensor {
        var ACC_X_THRESHOLD = 0.1
        var ACC_Y_THRESHOLD = 0.1
        var ACC_Z_THRESHOLD = 0.1

        var MOVEMENT_DELAY = 5000L

        var GYRO_X_THRESHOLD = 0.1
        var GYRO_Y_THRESHOLD = 0.1
        var GYRO_Z_THRESHOLD = 0.1

        var MAG_X_THRESHOLD = 0.1
        var MAG_Y_THRESHOLD = 0.1
        var MAG_Z_THRESHOLD = 0.1

    }
}