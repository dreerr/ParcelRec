package com.android.sensorlogger.networking

import android.content.Context
import android.util.Log
import com.android.sensorlogger.App
import com.android.sensorlogger.utils.TAG
import com.android.sensorlogger.utils.Util
import kotlinx.coroutines.*
import java.io.File

class UploadManager(val context: Context) {
    var filesToUpload = mutableListOf<File>()
    private var apiService = ApiService()
    private var uploadJob: Job? = null

    init {
        App.storage.walk().forEach {
            if(!it.isFile) return@forEach
            filesToUpload.add(it)
        }
        uploadFiles()
    }

    fun add(file:File) {
        if(!filesToUpload.contains(file)) {
            filesToUpload.add(file)
            Log.d(TAG, "${file.name} was added for upload, ${filesToUpload.size} in queue")
            uploadFiles()
        }
    }

    private fun uploadFiles() {
        if(uploadJob!=null && uploadJob!!.isActive) return
        uploadJob = GlobalScope.launch(Dispatchers.IO) {
            while (filesToUpload.isNotEmpty()) {
                while (!Util.isOnline()) {
                    Log.d(TAG, "Waiting 30s for network")
                    delay(30_000)
                }

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
        }
    }
}