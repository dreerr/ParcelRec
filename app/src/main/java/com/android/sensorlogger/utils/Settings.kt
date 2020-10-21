package com.android.parcelrec.utils

import android.content.Context

/**Camera settings*/
const val KEY_WIDTH = "width"
const val KEY_HEIGHT = "height"
const val KEY_FPS = "fps"
const val KEY_CAM_ID = "camId"

/**Network settings*/
const val KEY_API_URL = "url"
const val KEY_RATE = "uploadRate"

class Settings(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    var uploadRate
        get() = this.sharedPreferences.getInt(KEY_RATE, 30)
        set(value) = editor.putInt(KEY_RATE, value).apply()

    var url: String?
        get() = this.sharedPreferences.getString(KEY_API_URL, "https://palacz.my.to/1")
        set(value) = editor.putString(KEY_API_URL, value).apply()

    var width
        get() = this.sharedPreferences.getInt(KEY_WIDTH, 640)
        set(value) = editor.putInt(KEY_WIDTH, value).apply()

    var height
        get() = this.sharedPreferences.getInt(KEY_HEIGHT, 480)
        set(value) = editor.putInt(KEY_HEIGHT, value).apply()

    var fps
        get() = this.sharedPreferences.getInt(KEY_FPS, 24)
        set(value) = editor.putInt(KEY_FPS, value).apply()

    var camId
        get() = this.sharedPreferences.getString(KEY_CAM_ID, "0")
        set(value) = editor.putString(KEY_CAM_ID, value).apply()
}