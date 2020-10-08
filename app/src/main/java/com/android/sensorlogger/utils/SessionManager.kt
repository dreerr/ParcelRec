package com.android.sensorlogger.utils

import android.content.Context

class SessionManager(context: Context) {

    /**Camera settings*/
    val KEY_WIDTH = "width"
    val KEY_HEIGHT = "height"
    val KEY_FPS = "fps"
    val KEY_CAM_ID = "camId"

    /**Network settings*/
    val KEY_API_URL = "url"
    val KEY_UPLOADRATE = "uploadRate"
    val KEY_ENDPOINT = "endpoint"

    val sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    fun setUploadRate(rate : Int){
        editor.putInt(KEY_UPLOADRATE, rate).apply()
    }

    fun getUploadRate() : Int{
        return sharedPreferences.getInt(KEY_UPLOADRATE, 30)
    }

    fun setUrl(url: String){
        editor.putString(KEY_API_URL, url).apply()
    }

    fun getUrl() : String?{
        return sharedPreferences.getString(KEY_API_URL, "https://palacz.my.to/")
    }

    fun setEndpoint(endpoint: String){
        editor.putString(KEY_ENDPOINT, endpoint).apply()
    }

    fun getEndpoint() : String?{
        return sharedPreferences.getString(KEY_ENDPOINT, "parcel/1")
    }

    fun setWidth(width : Int){
        editor.putInt(KEY_WIDTH, width).apply()
    }
    fun getWidth() : Int{
        return sharedPreferences.getInt(KEY_WIDTH, 640)
    }

    fun setHeight(height : Int){
        editor.putInt(KEY_HEIGHT, height).apply()
    }
    fun getHeight() : Int{
        return sharedPreferences.getInt(KEY_HEIGHT, 480)
    }

    fun setFps(fps : Int) {
        editor.putInt(KEY_FPS, fps).apply()
    }
    fun getFps() : Int{
        return sharedPreferences.getInt(KEY_FPS, 24)
    }

    fun setCamId(id : String){
        editor.putString(KEY_CAM_ID, id).apply()
    }
    fun getCamId() : String? {
        return sharedPreferences.getString(KEY_CAM_ID, "0")
    }
}