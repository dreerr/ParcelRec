package com.android.parcelrec.utils

import android.content.Context

/**Camera settings*/
const val KEY_WIDTH = "width"
const val KEY_HEIGHT = "height"
const val KEY_REC_DURATION = "recDuration"
const val KEY_REC_MOTION_ONLY = "recMotionOnly"
const val KEY_CAM_ID = "camId"

/**Network settings*/
const val KEY_API_ENABLED = "uploadEnabled"
const val KEY_API_URL = "url"
const val KEY_API_BACKUP_URL = "urlBackup"
const val KEY_INTERVAL = "uploadInterval"

const val KEY_SERVICE_STARTED = "serviceStarted"

class Settings(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    var uploadEnabled
        get() = this.sharedPreferences.getBoolean(KEY_API_ENABLED, true)
        set(value) = editor.putBoolean(KEY_API_ENABLED, value).apply()

    var uploadInterval
        get() = this.sharedPreferences.getInt(KEY_INTERVAL, 60)
        set(value) = editor.putInt(KEY_INTERVAL, value).apply()

    var url: String?
        get() = this.sharedPreferences.getString(KEY_API_URL, "https://palacz.my.to:49900/")
        set(value) = editor.putString(KEY_API_URL, value).apply()

    var urlBackup: String?
        get() = this.sharedPreferences.getString(KEY_API_BACKUP_URL, "http://10.1.1.2:3000/")
        set(value) = editor.putString(KEY_API_BACKUP_URL, value).apply()

    var width
        get() = this.sharedPreferences.getInt(KEY_WIDTH, 1920)
        set(value) = editor.putInt(KEY_WIDTH, value).apply()

    var height
        get() = this.sharedPreferences.getInt(KEY_HEIGHT, 1080)
        set(value) = editor.putInt(KEY_HEIGHT, value).apply()

    var recDuration
        get() = this.sharedPreferences.getInt(KEY_REC_DURATION, 120)
        set(value) = editor.putInt(KEY_REC_DURATION, value).apply()

    var camId
        get() = this.sharedPreferences.getString(KEY_CAM_ID, "0")
        set(value) = editor.putString(KEY_CAM_ID, value).apply()

    var recMotionOnly
        get() = this.sharedPreferences.getBoolean(KEY_REC_MOTION_ONLY, true)
        set(value) = editor.putBoolean(KEY_REC_MOTION_ONLY, value).apply()

    var serviceStarted
        get() = this.sharedPreferences.getBoolean(KEY_SERVICE_STARTED, false)
        set(value) = editor.putBoolean(KEY_SERVICE_STARTED, value).apply()

}

object Config {
    const val rotateMillis = 15 * 60_000L
    object Sensor {
        const val ACC_THRESHOLD = 0.13
        const val GYRO_THRESHOLD = 0.1
        const val MAG_THRESHOLD = 0.3
    }

    object Network {
        const val API_KEY = "dc5daf82-f7a0-11ea-adc1-0242ac120002"
    }
}