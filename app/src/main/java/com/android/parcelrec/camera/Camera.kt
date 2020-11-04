package com.android.parcelrec.camera

import android.content.Context
import android.util.Log
import com.android.parcelrec.App
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.*

class Camera(context: Context) {
    private val context = context
    private var recording = false
    private var cameraRecorder: CameraRecorder? = null
    private var rotateJob: Job? = null
    private var intervalJob: Job? = null
    private var totalRecordings = 0L
    val status: String get() {
        return "$totalRecordings " +
                (if(recording) "🔴" else "") +
                (if(!Util.enoughFreeSpace()) "⚠️" else "")
    }

    fun run() {
        App.accelerometer?.thresholdStartedListeners?.add {
            intervalJob?.cancel()
            startRecording()
        }
        App.accelerometer?.thresholdEndedListeners?.add {
            stopRecording()
            intervalJob = App.scope.launch(Dispatchers.IO) {
                while (isActive) {
                    delay(60_000L * App.settings.recInterval)
                    Log.d("Camera", "Recording Interval reached!")
                    startRecording()
                    delay(1000L * App.settings.recDuration)
                    Log.d("Camera", "Recording Duration reached!")
                    stopRecording()
                }
            }
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
        intervalJob?.cancel()
        rotateJob?.cancel()
        if (recording){
            stopRecording()
        }
    }

    private fun startRecording() {
        if (recording || !Util.enoughFreeSpace()) return
        recording = true
        Log.i(TAG, "Starting new recording")
        try {
            cameraRecorder = CameraRecorder(context)
        } catch (e: Exception) {
            recording = false
            Log.d(TAG, "Error starting recording: ${e.message}")
        }
        rotateJob = App.scope.launch(Dispatchers.IO) {
            delay(60_000L * App.settings.uploadRate)
            Log.i("Camera", "Rotate recording")
            rotate()
        }
        totalRecordings++
    }

     private fun stopRecording() {
         if(!recording) return
         recording = false
         Log.i(TAG, "Stopping recording")
         rotateJob?.cancel()
         try {
             cameraRecorder?.stop()
         } catch (e: Exception) {
             Log.d(TAG, "Error stopping recording: ${e.message}")
         }
         cameraRecorder = null
     }
}