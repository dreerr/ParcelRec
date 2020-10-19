package com.android.sensorlogger.camera

import android.content.Context
import android.util.Log
import com.android.sensorlogger.App
import com.android.sensorlogger.utils.TAG
import kotlinx.coroutines.*

class Camera(context: Context) {
    private val context = context
    private var recording = false
    private var cameraRecorder: CameraRecorder? = null
    private var rotateJob: Job? = null

    fun run() {
        App.accelerometer?.thresholdStartedListeners?.add {
            startRecording()
        }
        App.accelerometer?.thresholdEndedListeners?.add {
            stopRecording()
        }
    }

    fun rotate() {
        if(recording) {
            Log.i(TAG, "Rotate Camera")
            stopRecording()
            startRecording()
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
        rotateJob = CoroutineScope(Dispatchers.IO).launch {
            delay(App.sessionManager.getUploadRate() * 1000L)
            Log.i("Camera", "Rotate Camera")
            rotate()
        }
        recording = true
    }

     private fun stopRecording() {
         if(!recording) return
         Log.i(TAG, "Stopping recording")
         rotateJob?.cancel()
         cameraRecorder?.stop()
         cameraRecorder = null
         recording = false
     }
}