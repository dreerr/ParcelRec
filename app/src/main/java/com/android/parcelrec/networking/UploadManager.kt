package com.android.parcelrec.networking

import android.content.Context
import android.text.format.Formatter
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.Config
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.lang.Long.max
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

enum class UploadManagerStatus {
    SLEEPING, ERROR, UPLOADING, INITIALIZE
}

class UploadManager(val context: Context) {
    private var filesToUpload = mutableListOf<File>()
    private var currentStatus = UploadManagerStatus.INITIALIZE
    private val client = OkHttpClient()
    private var url = App.settings.url!!
    private var totalTraffic = 0L
    private var totalUploads = 0
    private var lastUpdate = 0L
    private val nextUpdate
        get() = lastUpdate + App.settings.uploadInterval * 60_000L


    val status: String
        get() {
            val icon = when (currentStatus) {
                UploadManagerStatus.INITIALIZE -> "🎀"
                UploadManagerStatus.SLEEPING -> "💤"
                UploadManagerStatus.UPLOADING -> "🌍"
                UploadManagerStatus.ERROR -> "❌"
            }
            val millisRemaining = max(0, nextUpdate - Date().time)
            val date = Date(millisRemaining)
            val formatter = SimpleDateFormat("mm:ss")
            val remaining = if(millisRemaining > 0 && filesToUpload.size > 0)
                "(${formatter.format(date)})" else ""
            return """
                Files in Queue: ${filesToUpload.size}  $icon   $remaining 
                Total Traffic: ${Formatter.formatFileSize(
                    context,
                    totalTraffic
                )} / $totalUploads Files
                Free Space: ${Formatter.formatFileSize(
                    context,
                    Util.bytesAvailable
                )}""".trimIndent()
        }



    init {
        App.storageDir.walk().forEach {
            if(it.isFile) filesToUpload.add(it)
        }
        App.scope.launch(Dispatchers.IO) {
            while (isActive) {
                while (nextUpdate > Date().time) {
                    currentStatus = UploadManagerStatus.SLEEPING
                    delay(1_000)
                }

                while (!Util.isOnline()) {
                    currentStatus = UploadManagerStatus.ERROR
                    Log.d(TAG, "Waiting 10s for network")
                    delay(10_000)
                }

                currentStatus = UploadManagerStatus.UPLOADING
                filesToUpload.toList().forEach { file ->
                    if (file.length() <= 0) {
                        Log.d(TAG, "${file.name} has 0 Byte, deleting")
                        try {
                            file.delete()
                            filesToUpload.remove(file)
                        } catch (e: Exception) {
                            Log.e(TAG, "Deleting failed: ${file.name} ${e.localizedMessage}")
                        }
                        return@forEach
                    }

                    try {
                        uploadFile(file)
                        file.delete()
                        filesToUpload.remove(file)
                    } catch (e: Exception) {
                        Log.e(TAG, "Uploading failed: ${file.name} ${e.localizedMessage}")
                    }
                }

                if(filesToUpload.isEmpty()) {
                    currentStatus = UploadManagerStatus.SLEEPING
                    lastUpdate = Date().time
                } else {
                    currentStatus = UploadManagerStatus.ERROR
                    delay(1_000)
                    // retry
                }
            }
        }
    }

    fun uploadNow() {
        lastUpdate = 0L
    }

    fun add(file:File) {
        if(!filesToUpload.contains(file)) {
            filesToUpload.add(file)
            Log.d(TAG, "${file.name} was added for upload, ${filesToUpload.size} in queue")
        }
    }

    private fun uploadFile(file: File) {
        val contentType = Files.probeContentType(file.toPath()).toMediaType()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody(contentType))
            .build()

        fun requestOnURL(url: String): Response = run {
            val request: Request = Request.Builder()
                .header("X-API-Key", Config.Network.API_KEY)
                .url(url)
                .post(requestBody)
                .build()

            val response: Response = client.newCall(request).execute()
            response.close()
            return response
        }

        var response: Response? = null

        runCatching { response = requestOnURL(url) }
            .onFailure {
                Log.e(TAG, "API not available, trying backup: ${it.localizedMessage}")
                response = requestOnURL(App.settings.urlBackup!!)
                // If we fail here, we get back to uploadFiles()

                Log.i(TAG, "Backup API ${App.settings.urlBackup} successful!")
                url = App.settings.urlBackup!!

                // After 15 minutes set back to normal URL
                App.scope.launch(Dispatchers.IO) {
                    delay(15 * 60_000)
                    url = App.settings.url!!
                }
            }
        if (!response!!.isSuccessful) {
            throw IOException("Unexpected HTTP code: ${response!!.code}")
        }
        // If nothing did throw we are happy!
        totalTraffic += file.length()
        totalUploads++
    }
}