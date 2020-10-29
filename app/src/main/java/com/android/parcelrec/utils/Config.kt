package com.android.parcelrec.utils

object Config {
    object Sensor {
        var ACC_X_THRESHOLD = 0.2
        var ACC_Y_THRESHOLD = 0.2
        var ACC_Z_THRESHOLD = 0.2

        var MOVEMENT_DELAY = 30_000L

        var GYRO_X_THRESHOLD = 0.1
        var GYRO_Y_THRESHOLD = 0.1
        var GYRO_Z_THRESHOLD = 0.1

        var MAG_X_THRESHOLD = 0.3
        var MAG_Y_THRESHOLD = 0.3
        var MAG_Z_THRESHOLD = 0.3
    }
    object Wifi {
        var INTERVAL = 60_000L
    }
    object Network {
        var API_KEY = "dc5daf82-f7a0-11ea-adc1-0242ac120002"
    }
}