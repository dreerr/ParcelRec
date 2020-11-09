package com.android.parcelrec

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import android.text.format.Formatter.formatShortFileSize
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.parcelrec.camera.CameraSettings
import com.android.parcelrec.networking.UploadSettings
import com.android.parcelrec.utils.PermissionHelper
import com.android.parcelrec.utils.SensorServiceActions
import com.android.parcelrec.utils.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    var statusJob : Job? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isMeasurementRunning()) {
            uploadButton.visibility = View.VISIBLE
            cameraSettingButton.visibility = View.GONE
            uploadSettingButton.visibility = View.GONE
            startStopButton.text = "STOP"
        } else {
            uploadButton.visibility = View.GONE
            cameraSettingButton.visibility = View.VISIBLE
            uploadSettingButton.visibility = View.VISIBLE
        }

        requestPermissionsIfNeeded()

        startStopButton.setOnClickListener {if (!isMeasurementRunning()) startMeasurement() else stopMeasurement()}
        cameraSettingButton.setOnClickListener {
            val cameraSettings = CameraSettings(this)
            cameraSettings.openCameraSettings()
        }

        uploadSettingButton.setOnClickListener {
            val uploadSettings = UploadSettings(this)
            uploadSettings.OpenUploadSettings()
        }

        uploadButton.setOnClickListener {
            actionOnService(SensorServiceActions.ROTATE)
        }

        registerBroadcastReceiver()

        statusJob = App.scope.launch(Dispatchers.IO) {
            while(isActive) {
                runOnUiThread {
                    status.text = """
                        Camera: ${App.camera?.status}
                        Accelerometer: ${App.accelerometer?.status}
                        Gyroscope: ${(if (App.gyroscope != null) App.gyroscope!!.status else "n/a")}
                        Magnetometer: ${(if (App.magnetometer != null) App.magnetometer!!.status else "n/a")}
                        GPS: ${App.gps?.status}
                        WiFi: ${App.wifi?.status}
                        
                        Uploads: ${App.uploadManager.status}
                        Traffic: ${formatFileSize(applicationContext, App.uploadManager.totalTraffic)}
                        Free Space: ${formatFileSize(applicationContext, Util.bytesAvailable)}
                    """.trimIndent()
                }
                delay(250)
            }
        }
    }

    private fun startMeasurement(){
        startStopButton.text = "STOP"
        uploadButton.visibility = View.VISIBLE
        cameraSettingButton.visibility = View.GONE
        uploadSettingButton.visibility = View.GONE

        actionOnService(SensorServiceActions.START)
    }

    private fun stopMeasurement(){
        startStopButton.text = "START"
        uploadButton.visibility = View.GONE
        cameraSettingButton.visibility = View.VISIBLE
        uploadSettingButton.visibility = View.VISIBLE

        actionOnService(SensorServiceActions.STOP)
    }

    private fun isMeasurementRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (SensorService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun actionOnService(action: SensorServiceActions) {
        Intent(this, SensorService::class.java).also {
            it.action = action.name
            startForegroundService(it)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!PermissionHelper.hasAllPermissions(this)) {
            Toast.makeText(
                this,
                "All permissions are needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                PermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
        recreate()
    }

    private fun requestPermissionsIfNeeded(){
        val permissions = arrayListOf<String>()
        if (!PermissionHelper.hasCameraPermission(this)) {
            permissions.add(CAMERA)
        }
        if(!PermissionHelper.hasMicPermission(this)){
            permissions.add(RECORD_AUDIO)
        }
        if(!PermissionHelper.hasGpsPermission(this)){
            permissions.add(ACCESS_FINE_LOCATION)
            permissions.add(ACCESS_COARSE_LOCATION)
        }
        if(!PermissionHelper.hasStoragePermission(this)){
            permissions.add(WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()){
            PermissionHelper.requestPermission(this, permissions)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
        statusJob?.cancel()
    }

    private var mPowerKeyReceiver: BroadcastReceiver? = null

    private fun registerBroadcastReceiver() {
        val theFilter = IntentFilter()
        /** System Defined Broadcast  */
        theFilter.addAction(Intent.ACTION_SCREEN_ON)
        theFilter.addAction(Intent.ACTION_SCREEN_OFF)

        mPowerKeyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val strAction = intent!!.action
                if (strAction == Intent.ACTION_SCREEN_ON) {
                    if(isMeasurementRunning()) {
                        // actionOnService(SensorServiceActions.ROTATE)
                    }
                }
            }
        }

        applicationContext.registerReceiver(mPowerKeyReceiver, theFilter)
    }

    private fun unregisterReceiver() {
        val apiLevel = Build.VERSION.SDK_INT

        if (apiLevel >= 7) {
            try {
                applicationContext.unregisterReceiver(mPowerKeyReceiver)
            } catch (e: IllegalArgumentException) {
                mPowerKeyReceiver = null
            }

        } else {
            applicationContext.unregisterReceiver(mPowerKeyReceiver)
            mPowerKeyReceiver = null
        }
    }
}