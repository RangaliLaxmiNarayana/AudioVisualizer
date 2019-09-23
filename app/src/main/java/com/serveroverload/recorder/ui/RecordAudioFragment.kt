package com.serveroverload.recorder.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.serveroverload.recorder.R
import com.serveroverload.recorder.customview.RecorderVisualizerView
import com.serveroverload.recorder.util.Helper

import org.apache.commons.io.FileUtils

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class RecordAudioFragment : Fragment() {

    private var currentOutFile: String? = null
    private var myAudioRecorder: MediaRecorder? = null

    private var isRecording: Boolean = false
    private var visualizerView: RecorderVisualizerView? = null
    private var rootView: View? = null

    private var maxDB: TextView? = null
    private var minDB: TextView? = null
    private var averageDB: TextView? = null

    private var doubleBackToExitPressedOnce: Boolean = false
    private val mHandler = Handler()

    private val mRunnable = Runnable { doubleBackToExitPressedOnce = false }

    private val handler = Handler() // Handler for updating the

    private val EVENT1 = 1
    private var test: Button? = null
    private var staticSpinner: Spinner? = null
    private var max_dbValue: EditText? = null
    private var min_dbValue: EditText? = null
    private var txt_alert_information_layout: LinearLayout? = null
    private var bitRate_layout: LinearLayout? = null
    private var selectedMinDecibles: String? = null
    private var selectedMaxDecibles: String? = null
    // visualize

    internal var testInterval = (1000 * 5).toLong()
    internal lateinit var buttonBar: View
    internal var selectedBitRate = ""

    val isGoodEnv: Boolean
        get() {
            selectedMinDecibles = min_dbValue!!.text.toString()
            selectedMaxDecibles = max_dbValue!!.text.toString()
            return maxDecibles > java.lang.Double.parseDouble(selectedMinDecibles!!) && maxDecibles < java.lang.Double.parseDouble(selectedMaxDecibles!!)
        }

    // updates the visualizer every 50 milliseconds
    internal var updateVisualizer: Runnable = object : Runnable {
        override fun run() {
            if (isRecording)
            // if we are already recording
            {
                // get the current amplitude
                val x = myAudioRecorder!!.maxAmplitude
                visualizerView!!.addAmplitude(x.toFloat()) // update the VisualizeView
                visualizerView!!.invalidate() // refresh the VisualizerView


                averageAmplitute = visualizerView!!.averageAmplitude
                maximumAmplitute = visualizerView!!.maximumAmplitude
                maxDecibles = 20 * Math.log10(x / 32767.0)
                //maxDecibles = 20 * Math.log10(maximumAmplitute / ((2 ^ (Integer.parseInt(selectedBitRate)-1))-1));
                averageDB!!.text = "Recorded Max dB : " + String.format("%.2f", maxDecibles)

                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL.toLong())
            }
        }
    }

    internal var averageAmplitute: Float = 0.toFloat()
    internal var maximumAmplitute: Float = 0.toFloat()
    internal var maxDecibles: Double = 0.toDouble()
    internal var avgDecibles: Double = 0.toDouble()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(R.layout.record_audio_fragment, container,
                false)

        rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
        rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = false

        visualizerView = rootView!!
                .findViewById<View>(R.id.visualizer) as RecorderVisualizerView


        staticSpinner = rootView!!.findViewById<View>(R.id.spinner) as Spinner
        val staticAdapter = ArrayAdapter
                .createFromResource(activity!!, R.array.bitrate_array,
                        android.R.layout.simple_spinner_item)
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        staticSpinner!!.adapter = staticAdapter

        staticSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                selectedBitRate = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // TODO Auto-generated method stub
            }
        }

        minDB = rootView!!.findViewById(R.id.min_db)
        maxDB = rootView!!.findViewById(R.id.max_db)
        averageDB = rootView!!.findViewById(R.id.averagedb)
        txt_alert_information_layout = rootView!!.findViewById(R.id.txt_alert_information1)
        bitRate_layout = rootView!!.findViewById(R.id.bitRate_layout)

        buttonBar = rootView!!.findViewById(R.id.recording_actions)

        max_dbValue = rootView!!.findViewById<View>(R.id.max_dbValue) as EditText
        min_dbValue = rootView!!.findViewById<View>(R.id.min_dbValue) as EditText

        min_dbValue!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                minDB!!.text = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.length != 0)
                    minDB!!.text = s.toString()
            }
        })

        max_dbValue!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                maxDB!!.text = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (!minDB!!.text.toString().isEmpty()) {
                    if (s.length != 0)
                        maxDB!!.text = s.toString()
                } else {
                    max_dbValue!!.text.clear()
                    Toast.makeText(activity, "Please Enter Min decibles first!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        test = rootView!!.findViewById(R.id.txt_alert_information)
        test!!.setOnClickListener {
            if (min_dbValue!!.text.toString() != null && !min_dbValue!!.text.toString().isEmpty() && max_dbValue!!.text.toString() != null && !max_dbValue!!.text.toString().isEmpty()) {

                startRecording()
                test!!.isEnabled = false
                //handler1.sendMessageAtTime(handler1.obtainMessage(EVENT1), System.currentTimeMillis()+testInterval);

                object : CountDownTimer(testInterval, 100) {

                    override fun onTick(millisUntilFinished: Long) {
                        //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    override fun onFinish() {

                        //Toast.makeText(getContext(), "Event 1", Toast.LENGTH_SHORT).show();
                        stopTesting()
                        val textView = TextView(context)

                        textView.textSize = 20f
                        if (!isGoodEnv) {
                            textView.text = "Not Good Env. for Recording"
                            textView.setBackgroundColor(resources.getColor(R.color.redColor))
                            textView.setTextColor(Color.WHITE)
                        } else {
                            textView.text = "Good Env. for Recording"
                            textView.setBackgroundColor(resources.getColor(R.color.white))
                            textView.setTextColor(resources.getColor(R.color.black))
                        }
                        AlertDialog.Builder(context!!)
                                .setCancelable(false)
                                .setCustomTitle(textView)
                                .setMessage("do you Want to continue recording?")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton("Yes") { dialog, which ->
                                    // Continue with delete operation

                                    test!!.visibility = View.INVISIBLE
                                    txt_alert_information_layout!!.visibility = View.INVISIBLE
                                    bitRate_layout!!.visibility = View.INVISIBLE
                                    buttonBar.visibility = View.VISIBLE
                                }

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton("No") { dialog, which -> }
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                    }
                }.start()
            } else {
                Toast.makeText(activity, "Please enter the min and max decibles and try testing!", Toast.LENGTH_LONG).show()
            }
        }


        rootView!!.findViewById<View>(R.id.start_recording).setOnTouchListener { v, event ->
            startRecording()
            false
        }

        rootView!!.findViewById<View>(R.id.stop_recording).setOnTouchListener { v, event ->
            Helper.getHelperInstance().makeHepticFeedback(
                    activity)
            try {

                if (null != myAudioRecorder) {
                    myAudioRecorder!!.stop()
                    myAudioRecorder!!.release()
                    myAudioRecorder = null


                    Toast.makeText(
                            activity,
                            activity!!.resources.getString(
                                    R.string.rec_saved) + currentOutFile!!,
                            Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_fail),
                        Toast.LENGTH_LONG).show()
            }

            rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
            rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
            rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true

            isRecording = false

            handler.removeCallbacks(updateVisualizer)

            false
        }

        rootView!!.findViewById<View>(R.id.delete_recording).setOnTouchListener { v, event ->
            val recording = File(currentOutFile)

            if (recording.exists() && recording.delete()) {
                Toast.makeText(
                        activity,
                        resources.getString(
                                R.string.rec_deleted) + currentOutFile!!,
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_delete_fail) + currentOutFile!!,
                        Toast.LENGTH_SHORT).show()
            }

            rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
            rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = false
            false
        }

        rootView!!.findViewById<View>(R.id.txt_cancel).setOnTouchListener { v, event ->
            Helper.getHelperInstance().makeHepticFeedback(
                    activity)

            try {

                if (null != myAudioRecorder) {
                    myAudioRecorder!!.stop()
                    myAudioRecorder!!.release()
                    myAudioRecorder = null

                    Toast.makeText(
                            activity,
                            activity!!.resources.getString(
                                    R.string.rec_saved) + currentOutFile!!,
                            Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_fail),
                        Toast.LENGTH_LONG).show()
            }

            isRecording = false

            handler.removeCallbacks(updateVisualizer)

            System.exit(0)

            false
        }

        rootView!!.findViewById<View>(R.id.browse_recording).setOnTouchListener { v, event ->
            val fragmentManager = activity!!
                    .supportFragmentManager
            val fragmentTransaction = fragmentManager
                    .beginTransaction()
            fragmentTransaction.replace(R.id.container,
                    RecordingListFragment())
            fragmentTransaction
                    .addToBackStack("RecordingListFragment")
            fragmentTransaction.commit()

            false
        }

        rootView!!.isFocusableInTouchMode = true
        rootView!!.requestFocus()
        rootView!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                if (doubleBackToExitPressedOnce) {
                    // super.onBackPressed();

                    mHandler?.removeCallbacks(mRunnable)

                    activity!!.finish()

                    return@OnKeyListener true
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(activity,
                        "Please click BACK again to exit",
                        Toast.LENGTH_SHORT).show()

                mHandler.postDelayed(mRunnable, 2000)

            }
            true
        })

        return rootView

    }

    fun stopTesting() {

        Helper.getHelperInstance().makeHepticFeedback(
                activity)
        try {

            if (null != myAudioRecorder) {
                myAudioRecorder!!.stop()
                myAudioRecorder!!.release()
                myAudioRecorder = null
                test!!.isEnabled = true
                /*Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.rec_saved)
								+ currentOutFile,
						Toast.LENGTH_SHORT).show();*/
            }

        } catch (e: Exception) {
            e.printStackTrace()
            /*Toast.makeText(
					getActivity(),
					getActivity().getResources().getString(
							R.string.rec_fail),
					Toast.LENGTH_LONG).show();*/
        }

        rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
        rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
        rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true

        isRecording = false

        handler.removeCallbacks(updateVisualizer)
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    internal fun startRecording() {
        methodRequiresTwoPermission()
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private fun methodRequiresTwoPermission() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(activity!!, *perms)) {
            startrecord()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA_AND_LOCATION, *perms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (EasyPermissions.hasPermissions(activity!!, *permissions)) {
            startrecord()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA_AND_LOCATION, *permissions)
        }
    }

    private fun startrecord() {

        Helper.getHelperInstance().makeHepticFeedback(
                activity)

        if (Helper.getHelperInstance().createRecordingFolder()) {

            /*File dir = new File(Helper.RECORDING_PATH
                        + "/recording_");
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            val dateFormat = SimpleDateFormat(
                    "yyyyMMdd_HH_mm_ss")
            val currentTimeStamp = dateFormat
                    .format(Date())

            currentOutFile = (Helper.RECORDING_PATH
                    + "/recording_" + currentTimeStamp + ".3gp")

            myAudioRecorder = MediaRecorder()
            myAudioRecorder!!
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
            myAudioRecorder!!
                    .setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            myAudioRecorder!!
                    .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            myAudioRecorder!!.setAudioChannels(1)
            myAudioRecorder!!.setAudioSamplingRate(48100)
            myAudioRecorder!!.setAudioEncodingBitRate(Integer.parseInt(selectedBitRate))
            myAudioRecorder!!.setOutputFile(currentOutFile)

            Log.e("RecordAduio", "startToch")
            try {
                visualizerView!!.clear()
                myAudioRecorder!!.prepare()
                myAudioRecorder!!.start()

                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_start),
                        Toast.LENGTH_LONG).show()

                rootView!!.findViewById<View>(R.id.start_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = false

                isRecording = true

                handler.post(updateVisualizer)
            } catch (e: IllegalStateException) {
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_fail),
                        Toast.LENGTH_LONG).show()
                e.printStackTrace()

                rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true

                isRecording = false
            } catch (e: IOException) {
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_fail),
                        Toast.LENGTH_LONG).show()
                e.printStackTrace()

                rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true

                isRecording = false
            }

        } else {

            Toast.makeText(
                    activity,
                    activity!!.resources.getString(
                            R.string.rec_fail_mkdir),
                    Toast.LENGTH_LONG).show()

            isRecording = false
        }
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        super.onPause()

        if (isRecording) {

            try {

                if (null != myAudioRecorder) {

                    myAudioRecorder!!.stop()
                    myAudioRecorder!!.release()
                    myAudioRecorder = null

                    Toast.makeText(
                            activity,
                            activity!!.resources.getString(
                                    R.string.rec_saved) + currentOutFile!!, Toast.LENGTH_SHORT)
                            .show()

                    rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
                    rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
                    rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true

                    handler.removeCallbacks(updateVisualizer)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_fail), Toast.LENGTH_LONG).show()

                rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true

                handler.removeCallbacks(updateVisualizer)

            }

        }
    }

    companion object {

        const val REPEAT_INTERVAL = 40

        const val RC_CAMERA_AND_LOCATION = 3
    }

}
