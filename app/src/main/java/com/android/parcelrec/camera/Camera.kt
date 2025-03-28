package com.android.parcelrec.camera

import android.content.Context
import com.android.parcelrec.App
import com.android.parcelrec.utils.Config
import com.android.parcelrec.utils.Log
import com.android.parcelrec.utils.TAG
import com.android.parcelrec.utils.Util
import kotlinx.coroutines.*


class Camera(private val context: Context) {
    private var recording = false
    private var cameraRecorder: CameraRecorder? = null
    private var rotateJob: Job? = null
    private var totalRecordings = 0L
    val status: String get() {
        val icon = (if(recording) "🔴" else "") +
            (if(!Util.enoughFreeSpace()) "⚠️" else "") +
            (if(App.battery!!.batteryLevel < 5) "🔋️" else "")
        return "$totalRecordings $icon"
    }

    fun run() {
        if(App.settings.recMotionOnly) {
            App.accelerometer?.thresholdStartedListeners?.add {
                startRecording()
            }
            App.accelerometer?.thresholdEndedListeners?.add {
                stopRecording()
            }
        } else {
            startRecording()
        }
    }

    fun rotate() {
        Log.i(TAG, "Rotate Camera")
        stopRecording()
        startRecording()
    }

    private fun startRecording(retry: Boolean = true) {
        if (recording || !Util.enoughFreeSpace() || App.battery!!.batteryLevel < 2) return
        Log.i(TAG, "Starting new recording")
        try {
            cameraRecorder = CameraRecorder(context)
            recording = true
        } catch (e: Exception) {
            Log.d(TAG, "Error starting recording: ${e.message}")
            if(retry) startRecording(false)
            return
        }
        rotateJob = App.scope.launch(Dispatchers.IO) {
            delay(Config.rotateMillis)
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
         } catch (e: Throwable) {
             Log.d(TAG, "Error stopping recording: ${e.message}")
         }
         cameraRecorder = null
     }

    fun stop(){
        rotateJob?.cancel()
        if (recording){
            stopRecording()
        }
    }
}