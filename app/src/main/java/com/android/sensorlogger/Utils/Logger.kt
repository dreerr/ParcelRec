package com.android.sensorlogger.Utils

import android.content.Context
import android.util.Log
import com.android.sensorlogger.App
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

open class Logger(open var context: Context, var fileNameTag : String) {
    private lateinit var logFile : File
    private var bufferedWriter: BufferedWriter? = null
    private var lastCreated = 0L


    private fun initNewFile(){
        logFile = Util.getFile(fileNameTag, "txt")
        logFile = File(App.storage, fileNameTag + SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(Date()) + ".txt")
        bufferedWriter = logFile.bufferedWriter(Charset.defaultCharset(), 16 * 1024)
        lastCreated = Date().time
        Log.d(TAG, "Created new file ${logFile.name}")
    }

    fun writeLine(line : String) {
        if(lastCreated == 0L) initNewFile()
        val now = Date().time
        if((now - lastCreated) > 1_000 * App.sessionManager.getUploadRate()) {
            closeFile()
            initNewFile()
            lastCreated = now
        }
        bufferedWriter?.write(line)
    }

    fun closeFile(){
        bufferedWriter?.close()
        Log.d(TAG, "Closed file ${logFile.name}")
        if (logFile.length() > 0) {
            App.uploadManager.add(logFile)
        } else {
            logFile.delete()
        }
    }
}