package com.android.parcelrec.camera

import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
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
    val view = LayoutInflater.from(context).inflate(R.layout.camera_settings, null)
    val cameraIdSpinner = view.camera_id_spinner
    val cameraResolutionSpinner = view.camera_resolution_spinner
    val cameraFpsEditText = view.camera_fps_edittext

    val settingsHelper = CameraSettingsHelper(context)
    val cameraIdList = settingsHelper.getCameraIDs()

    fun OpenCameraSettings(){
        cameraFpsEditText.setFilters(arrayOf<InputFilter>(InputFilterMinMax("1", "30")))

        buildCameraIdSpinner()
        //buildCameraResolutionSpinner("0")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Camera settings")
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

    fun buildCameraIdSpinner(){
        val cameraIdSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context, android.R.layout.simple_spinner_item,
            cameraIdList
        )
        cameraIdSpinner.adapter = cameraIdSpinnerAdapter

        cameraIdSpinner.setSelection(App.settings.camId!!.toInt())

        cameraIdSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                buildCameraResolutionSpinner(cameraIdList[position])
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                //
            }
        }
    }

    fun buildCameraResolutionSpinner(cameraid : String){
        val cameraResolutionSpinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context, android.R.layout.simple_spinner_item,
            settingsHelper.getPossibleVideoSizes(cameraid)!!.map {
                "${it.width} x ${it.height}"
            }.toTypedArray()
        )
        cameraResolutionSpinner.adapter = cameraResolutionSpinnerAdapter
        if (cameraIdSpinner.selectedItem == App.settings.camId){
            cameraResolutionSpinner.setSelection(getSavedSizePosition(cameraResolutionSpinnerAdapter))
        }
    }

    private fun saveConfiguration(){
        App.settings.camId = cameraIdSpinner.selectedItem as String

        val selectedSize = settingsHelper.getPossibleVideoSizes(cameraIdSpinner.selectedItem as String)
        val selectedSizeIdx = cameraResolutionSpinner.selectedItemPosition

        App.settings.height = selectedSize!![selectedSizeIdx].height
        App.settings.width = selectedSize!![selectedSizeIdx].width

        App.settings.fps = cameraFpsEditText.text.toString().toInt()
    }

    private fun loadConfiguration(){
        cameraIdSpinner.setSelection(App.settings.camId!!.toInt())
        cameraFpsEditText.setText(App.settings.fps.toString())

        //Resolution is loaded in buildCameraResolutionSpinner
    }

    private fun getSavedSizePosition(adapter : ArrayAdapter<String>) : Int {
        val width = App.settings.width
        val height = App.settings.height
        val item = "$width x $height"

        return adapter.getPosition(item)
    }
}
