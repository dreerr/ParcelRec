package com.android.parcelrec.networking

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.android.parcelrec.App
import com.android.parcelrec.R
import kotlinx.android.synthetic.main.upload_settings.view.*

class UploadSettings(var context : Context) {

    /**View items */
    val view = LayoutInflater.from(context).inflate(R.layout.upload_settings, null)
    val api_enable_switch = view.api_enable_switch
    val api_url_edittext = view.api_url_edittext
    val api_url_backup_edittext = view.api_url_backup_edittext
    val upload_interval_edittext = view.upload_interval_edittext

    fun OpenUploadSettings(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Upload Settings")
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
        App.settings.uploadEnabled = api_enable_switch.isChecked
        App.settings.url = api_url_edittext.text.toString()
        App.settings.urlBackup = api_url_backup_edittext.text.toString()
        App.settings.uploadInterval = upload_interval_edittext.text.toString().toInt()
    }

    private fun loadConfiguration(){
        api_enable_switch.isChecked = App.settings.uploadEnabled
        api_url_edittext.setText(App.settings.url)
        api_url_backup_edittext.setText(App.settings.urlBackup)
        upload_interval_edittext.setText(App.settings.uploadInterval.toString())
    }
}