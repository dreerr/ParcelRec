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
import com.android.sensorlogger.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Camera(context: Context) {
    private val context = context
    private val uploadHandler = Handler()
    private var uploadTask = Runnable {
        Log.d("CAM", "Uploading video with periodic task.")
        uploadVideo()
    }
    private var uploadPeriod : Long = App.sessionManager.getUploadRate().toLong() * 1000
    private var recording = false
    private val recordHandler = Handler()
    private val movementChecker = Runnable { movementListener() }
    private var cameraRecorder: CameraRecorder? = null

    private fun movementListener(){
        Log.d("CAM", "Movementlistener called.")
        var movementCheckerStarted = false
        if (App.inMovement && !recording){
            startRecording()
            uploadHandler.postDelayed(uploadTask, uploadPeriod)
            Log.d("CAM", "User is in movement, recording started...")
        }
        if (!App.inMovement && recording){
            Log.d("CAM", "User has not moved for 30 seconds, stopped recording and started to upload video.")
            uploadHandler.removeCallbacks(uploadTask)
            stopRecording()
            movementCheckerStarted = true
        }

        //movementChecker is started when uploadVideo is called
        if (!movementCheckerStarted) recordHandler.postDelayed(movementChecker,1000)
    }

    private fun startRecording() {
        if (recording) return
        val args = CameraRecorderArgs(
            App.sessionManager.getCamId()!!, //TODO: Better init!!
            App.sessionManager.getFps(),
            App.sessionManager.getWidth(),
            App.sessionManager.getHeight()
        )
        cameraRecorder = CameraRecorder(context, args)
        recording = true
    }
     private fun stopRecording() {
         if(!recording) return
         val file = cameraRecorder?.stop()
         if (file != null) {
             App.uploadManager.add(file)
         }
         cameraRecorder = null
         recording = false
     }

    private fun uploadVideo() {
        stopRecording()
        startRecording()
    }

    fun run() {
        recordHandler.postDelayed(movementChecker, 1000)
    }

    fun stop(){
        recordHandler.removeCallbacks(movementChecker)
        uploadHandler.removeCallbacks(uploadTask)
        if (recording){
            Log.d("CAM", "Measurement has been stopped, uploading video.")
            stopRecording()
        }
     }


}