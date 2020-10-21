package com.android.parcelrec.utils

import android.content.Context
import android.util.Log
import com.android.parcelrec.App
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.Charset
import java.util.*

open class Logger(open var context: Context, var fileNameTag : String) {
    private var logFile : File? = null
    private var bufferedWriter: BufferedWriter? = null
    private var lastCreated = 0L

    private fun newLog(){
        logFile = Util.getFile(fileNameTag, "txt")
        bufferedWriter = logFile?.bufferedWriter(Charset.defaultCharset(), 16 * 1024)
        lastCreated = Date().time
        Log.d(TAG, "Created new file ${logFile?.name}")
    }

    fun writeLine(line : String) {
        if(lastCreated == 0L) newLog()
        if((Date().time - lastCreated) > 1_000 * App.settings.uploadRate) {
            rotate()
        }
        bufferedWriter?.write(line)
    }

    fun rotate() {
        closeLog()
        newLog()
    }

    fun closeLog(){
        if(logFile==null) return
        bufferedWriter?.close()
        Log.d(TAG, "Closed file ${logFile?.name}")
        if (logFile!!.length() > 0) {
            App.uploadManager.add(logFile!!)
        } else {
            logFile?.delete()
        }
    }
}