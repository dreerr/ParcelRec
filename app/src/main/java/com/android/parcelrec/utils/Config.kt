package com.android.parcelrec.utils

object Config {
    object Sensor {
        var ACC_THRESHOLD = 0.15


        var GYRO_THRESHOLD = 0.1

        var MAG_THRESHOLD = 1.5

        var MOVEMENT_DELAY = 30_000L

    }
    object Wifi {
        var INTERVAL = 60_000L
    }
    object Network {
        var API_KEY = "dc5daf82-f7a0-11ea-adc1-0242ac120002"
    }
}