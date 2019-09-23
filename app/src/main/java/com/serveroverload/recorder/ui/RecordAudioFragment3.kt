package com.serveroverload.recorder.ui

import android.Manifest
import android.content.DialogInterface
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
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

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class RecordAudioFragment3 : Fragment() {
    private var RECORDER_SAMPLERATE = 44100

    //private AudioRecord recorder = null;
    private var bufferSize = 0
    private var recordingThread: Thread? = null
    internal var isSave = false

    private var currentOutFile: String? = null
    private var myAudioRecorder: AudioRecord? = null

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
    private var sampleRateSpinner1: Spinner? = null
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
    internal var selectedSampleRate = ""

    private/*String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }*///currentOutFile = file.getAbsolutePath() + "/" + currentTimeStamp + AUDIO_RECORDER_FILE_EXT_WAV;
    val filename: String
        get() {

            val dateFormat = SimpleDateFormat(
                    "yyyyMMdd_HH_mm_ss")
            val currentTimeStamp = dateFormat
                    .format(Date())
            currentOutFile = (Helper.RECORDING_PATH
                    + "/recording_" + currentTimeStamp + AUDIO_RECORDER_FILE_EXT_WAV)
            return currentOutFile!!
        }

    private val tempFilename: String
        get() {
            val filepath = Environment.getExternalStorageDirectory().path
            val file = File(filepath, AUDIO_RECORDER_FOLDER)

            if (!file.exists()) {
                file.mkdirs()
            }

            val tempFile = File(filepath, AUDIO_RECORDER_TEMP_FILE)

            if (tempFile.exists())
                tempFile.delete()

            return file.absolutePath + "/" + AUDIO_RECORDER_TEMP_FILE
        }

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
                val sData = ShortArray(bufferSize)
                //myAudioRecorder.read(sData,0, sData.length);
                var sDataMax = 0
                for (i in 0 until bufferSize) {
                    if (Math.abs(sData[i].toInt()) >= sDataMax) {
                        sDataMax = Math.abs(sData[i].toInt())
                    }
                }

                // update the VisualizeView
                visualizerView!!.addAmplitude(sDataMax.toFloat())

                visualizerView!!.invalidate() // refresh the VisualizerView


                averageAmplitute = visualizerView!!.averageAmplitude
                maximumAmplitute = visualizerView!!.maximumAmplitude
                maxDecibles = 20 * Math.log10(sDataMax / 32767.0)
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

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT)


        staticSpinner = rootView!!.findViewById<View>(R.id.spinner) as Spinner
        val staticAdapter = ArrayAdapter
                .createFromResource(activity!!, R.array.bitrate_array,
                        android.R.layout.simple_spinner_item)
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        staticSpinner!!.adapter = staticAdapter
        val bitRateDefaultPosition = staticAdapter.getPosition("24")
        staticSpinner!!.setSelection(bitRateDefaultPosition)

        sampleRateSpinner1 = rootView!!.findViewById<View>(R.id.spinner1) as Spinner
        val sampleRateAdapter = ArrayAdapter
                .createFromResource(activity!!, R.array.samplerate_array,
                        android.R.layout.simple_spinner_item)
        sampleRateAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sampleRateSpinner1!!.adapter = sampleRateAdapter
        val sampleRateDefaultPosition = sampleRateAdapter.getPosition("41000")
        sampleRateSpinner1!!.setSelection(sampleRateDefaultPosition)

        staticSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                selectedBitRate = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // TODO Auto-generated method stub
            }
        }

        sampleRateSpinner1!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                selectedSampleRate = parent.getItemAtPosition(position) as String
                RECORDER_SAMPLERATE = Integer.parseInt(selectedSampleRate)
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
                isSave = false
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
                                    rootView!!.findViewById<View>(R.id.browse_recording).isEnabled = true
                                }

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton("No") { dialog, which -> test!!.isEnabled = true }
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                    }
                }.start()
            } else {
                Toast.makeText(activity, "Please enter the min and max decibles and try testing!", Toast.LENGTH_LONG).show()
            }
        }


        rootView!!.findViewById<View>(R.id.start_recording).setOnTouchListener { v, event ->
            isSave = true
            startRecording()
            false
        }

        rootView!!.findViewById<View>(R.id.stop_recording).setOnTouchListener { v, event ->
            try {
                if (null != myAudioRecorder) {
                    isRecording = false

                    val i = myAudioRecorder!!.state
                    if (i == 1)
                        myAudioRecorder!!.stop()
                    myAudioRecorder!!.release()
                    Toast.makeText(
                            activity,
                            activity!!.resources.getString(
                                    R.string.rec_saved),
                            Toast.LENGTH_SHORT).show()
                    myAudioRecorder = null
                    recordingThread = null
                }
                filename
                copyWaveFile(tempFilename, currentOutFile)
                deleteTempFile()

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
            rootView!!.findViewById<View>(R.id.browse_recording).isEnabled = true

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

    private fun deleteTempFile() {
        val file = File(tempFilename)

        file.delete()
    }

    private fun copyWaveFile(inFilename: String, outFilename: String?) {
        var `in`: FileInputStream? = null
        var out: FileOutputStream? = null
        var totalAudioLen: Long = 0
        var totalDataLen = totalAudioLen + 36
        val longSampleRate = RECORDER_SAMPLERATE.toLong()
        val channels = 1
        val byteRate = (RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8).toLong()

        val data = ByteArray(bufferSize)

        try {
            `in` = FileInputStream(inFilename)
            out = FileOutputStream(outFilename)
            totalAudioLen = `in`.channel.size()
            totalDataLen = totalAudioLen + 36



            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate)
            /*byte[] bytes2 = new byte[buffer.length * 2];
            ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().put(buffer);*/
            while (`in`.read(data) != -1) {
                out.write(data)
            }

            `in`.close()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun WriteWaveFileHeader(
            out: FileOutputStream, totalAudioLen: Long,
            totalDataLen: Long, longSampleRate: Long, channels: Int,
            byteRate: Long) {

        val header = ByteArray(44)

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = RECORDER_BPP.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        out.write(header, 0, 44)
    }

    fun stopTesting() {

        Helper.getHelperInstance().makeHepticFeedback(
                activity)
        try {

            if (null != myAudioRecorder) {
                isRecording = false

                val i = myAudioRecorder!!.state
                if (i == 1)
                    myAudioRecorder!!.stop()
                myAudioRecorder!!.release()

                myAudioRecorder = null
                recordingThread = null
            }
            filename
            copyWaveFile(tempFilename, currentOutFile)
            deleteTempFile()

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

    private fun writeAudioDataToFile() {
        val data = ByteArray(bufferSize)
        val filename = tempFilename
        var os: FileOutputStream? = null

        try {
            os = FileOutputStream(filename)
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        var read = 0

        if (null != os) {
            while (isRecording) {
                read = myAudioRecorder!!.read(data, 0, bufferSize)

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

            try {
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun startrecord() {

        Helper.getHelperInstance().makeHepticFeedback(
                activity)

        if (Helper.getHelperInstance().createRecordingFolder()) {

            myAudioRecorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize)

            val i = myAudioRecorder!!.state
            if (i == 1) {
                visualizerView!!.clear()
                myAudioRecorder!!.startRecording()
            }

            isRecording = true


            try {
                if (isSave) {
                    recordingThread = Thread(Runnable { writeAudioDataToFile() }, "AudioRecorder Thread")
                    recordingThread!!.start()
                }


                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_start),
                        Toast.LENGTH_LONG).show()

                rootView!!.findViewById<View>(R.id.start_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.browse_recording).isEnabled = false

                isRecording = true

                handler.post(updateVisualizer)
            } catch (e: Exception) {
                Toast.makeText(
                        activity,
                        activity!!.resources.getString(
                                R.string.rec_fail),
                        Toast.LENGTH_LONG).show()
                e.printStackTrace()

                rootView!!.findViewById<View>(R.id.start_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.stop_recording).isEnabled = false
                rootView!!.findViewById<View>(R.id.delete_recording).isEnabled = true
                rootView!!.findViewById<View>(R.id.browse_recording).isEnabled = true

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

        private val RECORDER_BPP = 16
        private val AUDIO_RECORDER_FILE_EXT_WAV = ".wav"
        private val AUDIO_RECORDER_FOLDER = "Recordings"
        private val AUDIO_RECORDER_TEMP_FILE = "record_temp.raw"
        private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
        private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

        const val REPEAT_INTERVAL = 2

       const val RC_CAMERA_AND_LOCATION = 3
    }

}
