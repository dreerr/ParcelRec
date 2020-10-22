package com.android.parcelrec.camera

import android.content.Context
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.TAG
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
            App.settings.camId!!,
            App.settings.fps,
            App.settings.width,
            App.settings.height
        )
        cameraRecorder = CameraRecorder(context, args)
        rotateJob = App.scope.launch(Dispatchers.IO) {
            delay(App.settings.uploadRate * 1000L)
            Log.i("Camera", "Rotate recording")
            rotate()
        }
        recording = true
    }

     private fun stopRecording() {
         if(!recording) return
         Log.i(TAG, "Stopping recording")
         rotateJob?.cancel()
         try {
             cameraRecorder?.stop()
         } catch (e: Exception) {
             Log.d(TAG, "Error stopping recording: ${e.message}")
         }

         cameraRecorder = null
         recording = false
     }
}