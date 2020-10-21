package com.android.sensorlogger.networking

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.android.sensorlogger.App
import com.android.sensorlogger.R
import kotlinx.android.synthetic.main.upload_settings.view.*

class UploadSettings(var context : Context) {

    /**View items */
    val view = LayoutInflater.from(context).inflate(R.layout.upload_settings, null)
    val api_url_edittext = view.api_url_edittext
    val upload_rate_edittext = view.upload_rate_edittext

    fun OpenUploadSettings(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("com.android.sensorlogger.networking.Upload settings")
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            saveConfiguration()
            Toast.makeText(context, "Configuration saved.", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
            .setCancelable(true)

        builder.show()

        loadConfiguration()
    }

    private fun saveConfiguration(){
        App.settings.url = api_url_edittext.text.toString()
        App.settings.uploadRate = upload_rate_edittext.text.toString().toInt()
    }

    private fun loadConfiguration(){
        api_url_edittext.setText(App.settings.url)
        upload_rate_edittext.setText(App.settings.uploadRate.toString())
    }
}