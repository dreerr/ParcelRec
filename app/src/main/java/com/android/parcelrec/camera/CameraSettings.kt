package com.android.parcelrec.camera

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.parcelrec.App
import com.android.parcelrec.R
import kotlinx.android.synthetic.main.camera_settings.view.*


class CameraSettings(var context : Context) {
    /**View items */
    private val view = LayoutInflater.from(context).inflate(R.layout.camera_settings, null)
    private val cameraId = view.camera_id
    private val resolution = view.resolution
    private val recDuration = view.rec_duration
    private val recInterval = view.rec_interval

    private val settingsHelper = CameraSettingsHelper(context)
    private val cameraIdList = settingsHelper.getCameraIDs()

    fun openCameraSettings(){

        buildCameraId()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Camera Settings")
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

    private fun buildCameraId(){
        val cameraIdSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context, android.R.layout.simple_spinner_item,
            cameraIdList
        )
        cameraId.adapter = cameraIdSpinnerAdapter

        cameraId.setSelection(App.settings.camId!!.toInt())

        cameraId.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                buildResolutions(cameraIdList[position])
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                //
            }
        }
    }

    fun buildResolutions(id : String){
        val cameraResolutionSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context, android.R.layout.simple_spinner_item,
            settingsHelper.getPossibleVideoSizes(id)!!.map {
                "${it.width} x ${it.height}"
            }.toTypedArray()
        )
        resolution.adapter = cameraResolutionSpinnerAdapter
        if (this.cameraId.selectedItem == App.settings.camId){
            resolution.setSelection(getSavedSizePosition(cameraResolutionSpinnerAdapter))
        }
    }

    private fun saveConfiguration(){
        App.settings.camId = cameraId.selectedItem as String

        val selectedSize = settingsHelper.getPossibleVideoSizes(cameraId.selectedItem as String)
        val selectedSizeIdx = resolution.selectedItemPosition

        App.settings.height = selectedSize!![selectedSizeIdx].height
        App.settings.width = selectedSize!![selectedSizeIdx].width

        App.settings.recDuration = recDuration.text.toString().toInt()
        App.settings.recInterval = recInterval.text.toString().toInt()
    }

    private fun loadConfiguration(){
        cameraId.setSelection(App.settings.camId!!.toInt())
        recDuration.setText(App.settings.recDuration.toString())
        recInterval.setText(App.settings.recInterval.toString())

        //Resolution is loaded in buildCameraResolutionSpinner
    }

    private fun getSavedSizePosition(adapter : ArrayAdapter<String>) : Int {
        val width = App.settings.width
        val height = App.settings.height
        val item = "$width x $height"

        return adapter.getPosition(item)
    }
}
