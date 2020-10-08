package com.android.sensorlogger.utils

import android.media.MediaRecorder

object Config {

    object Sensor {
        var ACC_X_THRESHOLD = 0.1
        var ACC_Y_THRESHOLD = 0.1
        var ACC_Z_THRESHOLD = 0.1

        var GYRO_X_THRESHOLD = 0.1
        var GYRO_Y_THRESHOLD = 0.1
        var GYRO_Z_THRESHOLD = 0.1

        var MAG_X_THRESHOLD = 0.1
        var MAG_Y_THRESHOLD = 0.1
        var MAG_Z_THRESHOLD = 0.1

        var ACC_DX_THRESHOLD = 0.1
        var ACC_DY_THRESHOLD = 0.1
        var ACC_DZ_THRESHOLD = 0.1
    }

    object Camera {
        var VIDEO_ENCODER = MediaRecorder.VideoEncoder.H264
        var AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC
        val RECORDER_VIDEO_BITRATE: Int = 10_000_000
    }

}