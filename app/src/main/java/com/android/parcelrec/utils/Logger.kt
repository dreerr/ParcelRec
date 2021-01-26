package com.android.parcelrec.utils

import android.content.Context
import com.android.parcelrec.App
import java.io.BufferedWriter
import java.io.File
import java.nio.charset.Charset
import java.util.*

open class Logger(open var context: Context, var fileNameTag : String) {
    var rotateMillis = Config.rotateMillis
    private var logFile : File? = null
    private var bufferedWriter: BufferedWriter? = null
    private var lastCreated = 0L
    private var totalLines = 0L
    val status get() = "$totalLines"

    private fun newLog(){
        logFile = Util.newFileWithDate(fileNameTag, "txt")
        bufferedWriter = logFile?.bufferedWriter(Charset.defaultCharset(), 16 * 1024)
        lastCreated = Date().time
        Log.d(TAG, "Created new file ${logFile?.name}")
    }

    fun write(string : String) {
        if(lastCreated == 0L) newLog()
        if(rotateMillis > 0 && (Date().time - lastCreated) > rotateMillis) {
            rotate()
        }
        bufferedWriter.runCatching { this!!.write(string) }
            .onSuccess { totalLines++ }
            .onFailure { Log.e(TAG, "Could not write line: ${it.localizedMessage}") }
    }

    fun rotate() {
        closeLog()
        newLog()
    }

    private fun closeLog() {
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