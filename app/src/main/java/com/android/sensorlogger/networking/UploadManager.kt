package com.android.parcelrec.networking

import android.content.Context
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Config
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.nio.file.Files

class UploadManager(val context: Context) {
    private var filesToUpload = mutableListOf<File>()
    private var uploadJob: Job? = null
    private val client = OkHttpClient()
    private var totalUploads = 0
    var totalTraffic = 0L
        private set
    val status: String
        get() = "${filesToUpload.size.toString()} / ${totalUploads.toString()}"


    init {
        App.storageDir.walk().forEach {
            if(it.isFile) filesToUpload.add(it)
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
        uploadJob = App.scope.launch(Dispatchers.IO) {
            val TAG = TAG
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
                        upload(it)
                        it.delete()
                        filesToUpload.remove(it)
                    } catch (e: Exception) {
                        Log.e(TAG, "Uploading failed: ${it.name} ${e.localizedMessage}")
                    }
                }
            }
        }
    }


    private fun upload(file: File) {
        val url = App.settings.url!!
        val contentType = Files.probeContentType(file.toPath()).toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody(contentType))
            .build()

        val request: Request = Request.Builder()
            .header("X-API-Key", Config.Network.API_KEY)
            .url(url)
            .post(requestBody)
            .build()

        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            totalTraffic += file.length()
            totalUploads++
        } else {
            throw IOException("Unexpected code ${response.code.toString()}")
        }
    }

}