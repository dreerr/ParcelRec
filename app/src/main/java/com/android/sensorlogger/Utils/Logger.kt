package com.android.sensorlogger.Utils

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.android.sensorlogger.App
import com.android.sensorlogger.Utils.Util.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

open class Logger(open var context: Context, var fileNameTag : String) {
    private val appDirectory = File(context.getExternalFilesDir(null).toString() + "/SensorLogger")
    private val logDirectory = File("$appDirectory/logs")
    private lateinit var logFile : File
    private var outputStreamWriter: OutputStreamWriter? = null

    private val uploadRate : Long = App.sessionManager.getUploadRate().toLong() * 1000
    private val uploadHandler = Handler()
    private var uploadTask = Runnable { uploadFile() }

    fun startPeriodicUpload(){
        uploadHandler.postDelayed(uploadTask, uploadRate)
    }

    fun stopPeriodicUpload(){
        //Remove previous file
        logFile.delete()
        //Remove previous callback
        uploadHandler.removeCallbacks(uploadTask)
    }

    private fun uploadFile(){
        if (logFile.length() > 0){
            GlobalScope.launch(Dispatchers.IO) {
                if (isOnline()) {
                    val fileToUpload = logFile

                    //Create new logfile
                    initLogFile()

                    App.apiService.uploadFile(fileToUpload, context)

                    //Delete old file
                    fileToUpload.delete()
                }
                else {
                    Log.d("LOGGER", "No internet, postponed upload")
                    Toast.makeText(context, "No network connection, postponed uploading.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        uploadHandler.postDelayed(uploadTask, uploadRate)

    }

    fun initLogFile(){
        if (!appDirectory.exists()) {
            appDirectory.mkdir()
        }
        if (!logDirectory.exists()) {
            logDirectory.mkdir()
        }
        logFile = File(logDirectory, fileNameTag + SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US).format(Date()) + ".txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        outputStreamWriter = OutputStreamWriter(FileOutputStream(logFile, true))
    }

    fun deleteFile(){
        closeFile()
        logFile.delete()
        outputStreamWriter = null
    }

    fun writeToFile(line : String){
        if (outputStreamWriter == null){
            outputStreamWriter = OutputStreamWriter(FileOutputStream(logFile, true))
        }
        outputStreamWriter?.write(line)
    }

    fun closeFile(){
        outputStreamWriter?.close()
        outputStreamWriter = null
    }

    fun triggerManualUpload(){
        if (logFile.exists()){
            uploadHandler.removeCallbacks(uploadTask)
            uploadFile()
        }
    }
}