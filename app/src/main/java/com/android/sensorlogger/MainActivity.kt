package com.android.sensorlogger

import android.Manifest.permission.*
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.sensorlogger.utils.Actions
import kotlinx.android.synthetic.main.activity_main.*
import com.android.sensorlogger.utils.PermissionHelper
import com.android.sensorlogger.camera.CameraSettings
import com.android.sensorlogger.networking.UploadSettings

class MainActivity : AppCompatActivity() {

    var statisticsHandler = Handler()
    var statisticsUpdater = Runnable { updateStatistics() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isMeasurementRunning()) {
            animationView.visibility = View.VISIBLE
            uploadButton.visibility = View.VISIBLE
            cameraSettingButton.visibility = View.GONE
            uploadSettingButton.visibility = View.GONE
            startStopButton.text = "STOP"
        } else {
            animationView.visibility = View.GONE
            uploadButton.visibility = View.GONE
            cameraSettingButton.visibility = View.VISIBLE
            uploadSettingButton.visibility = View.VISIBLE
        }

        requestPermissionsIfNeeded()

        startStopButton.setOnClickListener {if (!isMeasurementRunning()) startMeasurement() else stopMeasurement()}
        cameraSettingButton.setOnClickListener {
            val cameraSettings = CameraSettings(this)
            cameraSettings.OpenCameraSettings()
        }

        uploadSettingButton.setOnClickListener {
            val uploadSettings = UploadSettings(this)
            uploadSettings.OpenUploadSettings()
        }

        uploadButton.setOnClickListener {
            var serviceIntent = Intent(this, SensorService::class.java)
            serviceIntent.putExtra("UPLOAD_REQUEST", 1)
            startForegroundService(serviceIntent)
        }

        statisticsHandler.postDelayed(statisticsUpdater, 1000)
    }

    private fun startMeasurement(){
        startStopButton.text = "STOP"
        animationView.visibility = View.VISIBLE
        uploadButton.visibility = View.VISIBLE
        cameraSettingButton.visibility = View.GONE
        uploadSettingButton.visibility = View.GONE

        actionOnService(Actions.START)
    }

    private fun stopMeasurement(){
        startStopButton.text = "START"
        animationView.visibility = View.GONE
        uploadButton.visibility = View.GONE
        cameraSettingButton.visibility = View.VISIBLE
        uploadSettingButton.visibility = View.VISIBLE

        actionOnService(Actions.STOP)
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

    private fun actionOnService(action: Actions) {
        Intent(this, SensorService::class.java).also {
            it.action = action.name
            startForegroundService(it)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!PermissionHelper.hasAllPermissions(this)) {
            Toast.makeText(this, "All permissions are needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                PermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
        recreate()
    }

    private fun updateStatistics(){
        last_upload.text = App.lastUpload
        network_traffic.text = "${App.networkTraffic} MByte"
        statisticsHandler.postDelayed(statisticsUpdater, 1000)
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
        statisticsHandler.removeCallbacks(statisticsUpdater)
    }
}