package com.android.parcelrec.utils

import android.content.Context
import android.util.Log
import com.android.parcelrec.App
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

open class Logger(open var context: Context, var fileNameTag : String) {
    private var logFile : File? = null
    private var bufferedWriter: BufferedWriter? = null
    private var lastCreated = 0L
    private var totalLines = 0L
    val status get() = "$totalLines"

    private fun newLog(){
        logFile = Util.getFile(fileNameTag, "txt")
        bufferedWriter = logFile?.bufferedWriter(Charset.defaultCharset(), 16 * 1024)
        lastCreated = Date().time
        Log.d(TAG, "Created new file ${logFile?.name}")
    }

    fun writeLine(line : String) {
        if(lastCreated == 0L) newLog()
        if((Date().time - lastCreated) > 60_000L * App.settings.uploadRate) {
            rotate()
        }
        bufferedWriter.runCatching { this!!.write(line) }
            .onSuccess { totalLines++ }
            .onFailure { Log.e(TAG, "Could not write line: ${it.localizedMessage}") }
    }

    fun rotate() {
        closeLog()
        newLog()
    }

    fun closeLog() {
        if(logFile==null) return
        bufferedWriter?.close()
        Log.d(TAG, "Closed file ${logFile?.name}")
        if (logFile!!.length() > 0) {
            App.uploadManager.add(logFile!!)
        } else {
            logFile?.delete()
        }
    }

    fun stopLog() {
        closeLog()
        lastCreated = 0L
    }
}