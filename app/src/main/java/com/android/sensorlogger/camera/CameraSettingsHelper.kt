package com.android.parcelrec.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.util.Size


class CameraSettingsHelper(var mContext: Context) {

    private val cameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var camera: CameraDevice

    fun  getCameraIDs() : Array<String> {
        return cameraManager.cameraIdList
    }

    fun getPossibleVideoSizes(cameraId : String): Array<out Size>? {
            val char = cameraManager.getCameraCharacteristics(cameraId)
            val map = char.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            return map!!.getOutputSizes(SurfaceTexture::class.java)
    }
}