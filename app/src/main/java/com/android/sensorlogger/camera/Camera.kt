package com.android.sensorlogger.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.CameraProfile
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
import com.android.sensorlogger.Utils.Config
import com.android.sensorlogger.Utils.Util.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Camera(context: Context) {

    var mContext = context

    /**Variables for camera operations*/
    private val cameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var camera: CameraDevice
    private lateinit var session: CameraCaptureSession
    private var initSuccessful  = false

    /**Log file variables*/
    private val appDirectory = File(context.getExternalFilesDir(null).toString() + "/SensorLogger")
    private val logDirectory = File("$appDirectory/logs")
    private var fileName = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US).format(Date()) + ".mp4"
    private var outputFile : File? = null

    private val uploadHandler = Handler()
    private var uploadTask = Runnable { uploadVideo(true) }
    private var uploadPeriod : Long = App.sessionManager.getUploadRate().toLong() * 1000

    /**New thread for camera operations*/
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    /**Variables for recording*/
    private val recorderSurface: Surface by lazy {
        val surface = MediaCodec.createPersistentInputSurface()

        outputFile = File(logDirectory, fileName)
        createRecorder(surface).apply {
            prepare()
            release()
        }
        outputFile!!.delete()
        surface
    }

    private val recordRequest: CaptureRequest by lazy {
        session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(recorderSurface)
            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(App.sessionManager.getFps(), App.sessionManager.getFps()))
        }.build()
    }

    private lateinit var recorder : MediaRecorder
    private var recording = false

    private val recordHandler = Handler()
    private val movementChecker = Runnable { movementListener() }

    private fun initializeCamera() = GlobalScope.launch(Dispatchers.Main){
            if (cameraManager.cameraIdList.isEmpty()) {
                Toast.makeText(mContext, "No cameras were found on the device.", Toast.LENGTH_SHORT).show()
            }
            else {
                try {
                    Log.d("CAMTAG", "Beginning camera initialization.")
                    val cameraId = App.sessionManager.getCamId()!!
                    camera = openCamera(cameraId)!!
                    session = createCaptureSession(camera, listOf(recorderSurface))
                    initSuccessful = true
                    Log.d("CAMTAG", "Session created.")

                } catch (e: Exception) {
                    Log.d("CAMTAG", "Error during camera initialization: $e")
                }
            }
        }

    private suspend fun openCamera(cameraId: String) : CameraDevice? = suspendCoroutine {cont ->
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Permission has not been granted.", Toast.LENGTH_SHORT).show()
            cont.resume(null)
        }
        cameraManager.openCamera(cameraId, object: CameraDevice.StateCallback() {
            override fun onDisconnected(cameraDevice: CameraDevice){
                cameraThread.quitSafely()
                recorder.release()
                //recorderSurface.release()
                cameraDevice.close()
                Log.d("CAM", "Camera disconnected.")
            }
            override fun onError(p0: CameraDevice, p1: Int){
                Log.d("CAM", "Failed to open camera")
                cont.resume(null)
            }
            override fun onOpened(cameraDevice: CameraDevice){
                Log.d("CAM", "Camera opened.")
                cont.resume(cameraDevice)
            }
        }, cameraHandler)
    }

    private suspend fun createCaptureSession(device: CameraDevice, targets: List<Surface>): CameraCaptureSession = suspendCoroutine { cont ->

        device.createCaptureSession(targets, object: CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                cont.resumeWithException(exc)
            }
        }, cameraHandler)
    }

    private fun createRecorder(surface: Surface) = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC) //TODO - remove this optionally, and add permission handling
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(outputFile)
        setVideoEncodingBitRate(Config.Camera.RECORDER_VIDEO_BITRATE)
        setVideoFrameRate(App.sessionManager.getFps())
        setVideoSize(App.sessionManager.getWidth(), App.sessionManager.getHeight())
        setVideoEncoder(Config.Camera.VIDEO_ENCODER)
        setAudioEncoder(Config.Camera.AUDIO_ENCODER)
        setInputSurface(surface)
    }

    private fun movementListener(){
        if (App.inMovement && !recording){
            startRecording()
            uploadHandler.postDelayed(uploadTask, uploadPeriod)
            Log.d("CAM", "User is in movement, started recording.")
        }
        if (!App.inMovement && recording){
            Log.d("CAM", "User has not moved for 30 seconds, stopped recording.")
            uploadHandler.removeCallbacks(uploadTask)
            uploadVideo(false)
        }

        recordHandler.postDelayed(movementChecker,1000)
    }

    private fun startRecording() = GlobalScope.launch(Dispatchers.Default){
        if (initSuccessful) {
            recording = true

            //New file:
            fileName = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US).format(Date()) + ".mp4"
            outputFile = File(logDirectory, fileName)
            recorder = createRecorder(recorderSurface)

            session.setRepeatingRequest(recordRequest, null, cameraHandler)
            recorder.apply {
                // Sets output orientation based on current sensor value at start time
                //relativeOrientation.value?.let { setOrientationHint(it) }
                prepare()
                start()
            }
        }
        else {
            Toast.makeText(mContext, "Error: Video is not being recorded.", Toast.LENGTH_LONG).show()
        }
    }

    fun stopRecording(){
        recording = false
        recordHandler.removeCallbacks(movementChecker)
        recorder.stop()
    }

    fun start() = GlobalScope.launch(Dispatchers.IO){
        initializeCamera().join()
        recordHandler.postDelayed(movementChecker, 1000)
    }

    fun stop(){
        recordHandler.removeCallbacks(movementChecker)
        uploadHandler.removeCallbacks(uploadTask)
        if (recording){
            uploadVideo(false)
        }
    }

    private fun uploadVideo(startNewSession: Boolean){
        if (isOnline()){
            Log.d("CAM", "Started uploading video.")
            GlobalScope.launch(Dispatchers.IO){
                val fileToUpload = outputFile
                stopRecording()

                if (startNewSession){
                    startRecording()
                }

                if (fileToUpload != null) {
                    App.ApiService.uploadFile(fileToUpload, mContext)

                    //Delete old file
                    fileToUpload.delete()
                }
            }
        }
        if (startNewSession) uploadHandler.postDelayed(uploadTask, uploadPeriod)

    }

    fun triggerManualUpload(){
        if (outputFile!!.exists()){
            if (recording){
                uploadHandler.removeCallbacks(uploadTask)
                uploadVideo(true)
            }
            else{
                //Note: We shouldn't get here. This branch means that video is present on device,
                //but camera is not recording anymore.
                uploadVideo(false)
            }
        }
    }
}