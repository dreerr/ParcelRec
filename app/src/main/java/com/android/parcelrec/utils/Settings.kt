package com.android.parcelrec.utils

import android.content.Context

/**Camera settings*/
const val KEY_WIDTH = "width"
const val KEY_HEIGHT = "height"
const val KEY_REC_DURATION = "recDuration"
const val KEY_REC_INTERVAL = "recInterval"
const val KEY_CAM_ID = "camId"

/**Network settings*/
const val KEY_API_URL = "url"
const val KEY_API_BACKUP_URL = "urlBackup"
const val KEY_RATE = "uploadRate"

class Settings(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    var uploadRate
        get() = this.sharedPreferences.getInt(KEY_RATE, 15)
        set(value) = editor.putInt(KEY_RATE, value).apply()

    var url: String?
        get() = this.sharedPreferences.getString(KEY_API_URL, "https://palacz.my.to/1")
        set(value) = editor.putString(KEY_API_URL, value).apply()

    var urlBackup: String?
        get() = this.sharedPreferences.getString(KEY_API_BACKUP_URL, "http://10.1.1.2/1")
        set(value) = editor.putString(KEY_API_BACKUP_URL, value).apply()

    var width
        get() = this.sharedPreferences.getInt(KEY_WIDTH, 1920)
        set(value) = editor.putInt(KEY_WIDTH, value).apply()

    var height
        get() = this.sharedPreferences.getInt(KEY_HEIGHT, 1080)
        set(value) = editor.putInt(KEY_HEIGHT, value).apply()

    var recDuration
        get() = this.sharedPreferences.getInt(KEY_REC_DURATION, 30)
        set(value) = editor.putInt(KEY_REC_DURATION, value).apply()

    var recInterval
        get() = this.sharedPreferences.getInt(KEY_REC_INTERVAL, 60)
        set(value) = editor.putInt(KEY_REC_INTERVAL, value).apply()


    var camId
        get() = this.sharedPreferences.getString(KEY_CAM_ID, "0")
        set(value) = editor.putString(KEY_CAM_ID, value).apply()
}