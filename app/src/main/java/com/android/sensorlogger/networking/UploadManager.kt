package com.android.sensorlogger.networking

import android.content.Context
import android.util.Log
import com.android.sensorlogger.Utils.TAG
import com.android.sensorlogger.Utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class UploadManager(val context: Context) {
    private val logDirectory = File(context.getExternalFilesDir(null).toString() + "/SensorLogger/logs")
    private var filesToUpload = mutableListOf<File>()
    private var apiService = ApiService()
    private var isActive = false

    init {
        logDirectory.walk().forEach {
            if(!it.isFile) return@forEach
            filesToUpload.add(it)
        }
        uploadFiles()
    }

    fun add(file:File) {
        if(!filesToUpload.contains(file)) {
            filesToUpload.add(file)
            Log.d(TAG, "${file.name} was added for upload")
            uploadFiles()
        }
    }

    private fun uploadFiles() = GlobalScope.launch(Dispatchers.IO) {
        if(isActive) return@launch
        isActive = true
        while (filesToUpload.isNotEmpty()) {
            while (!Util.isOnline()) {
                Log.d(TAG, "Waiting 30s for network")
                delay(30_000)
            }
            Log.d(TAG, "${filesToUpload.size} Files to upload!")

            filesToUpload.toMutableList().forEach {
                if (it.length() <= 0) {
                    Log.d(TAG, "${it.name} has 0 Byte, deleting")
                    try {
                        it.delete()
                        filesToUpload.remove(it)
                    } catch (e: Exception) {
                        Log.e(TAG, "Deleting failed: ${it.name} ${e.localizedMessage}")
                    }
                    return@forEach
                }
                try {
                    apiService.uploadFile(it, context)
                    it.delete()
                    filesToUpload.remove(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Uploading failed: ${it.name} ${e.localizedMessage}")
                }
            }
        }
        isActive = false
    }
}