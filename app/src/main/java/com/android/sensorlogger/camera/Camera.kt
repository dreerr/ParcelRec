package com.android.sensorlogger.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.Surface
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.sensorlogger.App
import com.android.sensorlogger.utils.Config
import com.android.sensorlogger.utils.TAG
import com.android.sensorlogger.utils.Util
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Camera(context: Context) {
    private val context = context
    private var recording = false
    private var cameraRecorder: CameraRecorder? = null
    private var uploadJob: Job? = null

    fun run() {
        App.accelerometer?.thresholdStartedListeners?.add {
            startRecording()
        }
        App.accelerometer?.thresholdEndedListeners?.add {
            uploadJob?.cancel()
            stopRecording()
        }
    }

    fun stop(){
        if (recording){
            stopRecording()
        }
    }

    private fun startRecording() {
        if (recording) return
        Log.i(TAG, "Starting new recording")
        val args = CameraRecorderArgs(
            App.sessionManager.getCamId()!!, //TODO: Better init!!
            App.sessionManager.getFps(),
            App.sessionManager.getWidth(),
            App.sessionManager.getHeight()
        )
        cameraRecorder = CameraRecorder(context, args)
        recording = true
        uploadJob = CoroutineScope(Dispatchers.IO).launch {
            delay(App.sessionManager.getUploadRate() * 1000L)
            Log.i("Camera", "Circle Camera")
            stopRecording()
            startRecording()
        }
    }
     private fun stopRecording() {
         if(!recording) return
         Log.i(TAG, "Stopping recording")
         cameraRecorder?.stop()
         cameraRecorder = null
         recording = false
     }
}