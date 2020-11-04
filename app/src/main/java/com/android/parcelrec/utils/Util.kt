package com.android.parcelrec.utils
import android.os.StatFs
import android.util.Log
import com.android.parcelrec.App
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*


object Util {

    fun isOnline(): Boolean {
        try {
            var addresses = InetAddress.getAllByName("www.google.com")
            return addresses[0].hostAddress != ""
        } catch (e: UnknownHostException) {
            // Log error
        }
        return false
    }
    fun getFile(fileNameTag: String, extension: String): File {
        return File(App.storageDir, "$fileNameTag$simpleTime.$extension")

    }
    val bytesAvailable: Long
    get() {
        val stat = StatFs(App.storageDir.path)
        return stat.blockSizeLong * stat.availableBlocksLong
    }

    fun enoughFreeSpace() : Boolean {
        val megAvailable = bytesAvailable / (1024 * 1024)
        return megAvailable > 1024 // over 1GB
    }
    @JvmStatic
    val simpleTime: String
        get() = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US).format(Date())
}

val Any.TAG: String
    get() {
        return if (!javaClass.isAnonymousClass) {
            val name = javaClass.simpleName
            if (name.length <= 23) name else name.substring(0, 23)// first 23 chars
        } else {
            val name = javaClass.name
            if (name.length <= 23) name else name.substring(name.length - 23, name.length)// last 23 chars
        }
    }
