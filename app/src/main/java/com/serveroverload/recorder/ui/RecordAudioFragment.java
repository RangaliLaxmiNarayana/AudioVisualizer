package com.serveroverload.recorder.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.serveroverload.recorder.R;
import com.serveroverload.recorder.customview.RecorderVisualizerView;
import com.serveroverload.recorder.util.Helper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class RecordAudioFragment extends Fragment {

    private String currentOutFile;
    private MediaRecorder myAudioRecorder;

    private boolean isRecording;
    private RecorderVisualizerView visualizerView;
    private View rootView;

    private TextView maxDB;
    private TextView minDB;
    private TextView averageDB;

    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    public static final int REPEAT_INTERVAL = 40;

    private Handler handler = new Handler(); // Handler for updating the

    private final int EVENT1 = 1;
    private Button test;
    private Spinner staticSpinner;
    private EditText max_dbValue;
    private EditText min_dbValue;
    private LinearLayout txt_alert_information_layout;
    private LinearLayout bitRate_layout;
    private String selectedMinDecibles;
    private String selectedMaxDecibles;
    // visualize

    long testInterval = 1000 * 5;
    View buttonBar;
    String selectedBitRate = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.record_audio_fragment, container,
                false);

        rootView.findViewById(R.id.stop_recording).setEnabled(false);
        rootView.findViewById(R.id.delete_recording).setEnabled(false);

        visualizerView = (RecorderVisualizerView) rootView
                .findViewById(R.id.visualizer);


        staticSpinner = (Spinner) rootView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.bitrate_array,
                        android.R.layout.simple_spinner_item);
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        staticSpinner.setAdapter(staticAdapter);

        staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedBitRate = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        minDB = rootView.findViewById(R.id.min_db);
        maxDB = rootView.findViewById(R.id.max_db);
        averageDB = rootView.findViewById(R.id.averagedb);
        txt_alert_information_layout = rootView.findViewById(R.id.txt_alert_information1);
        bitRate_layout = rootView.findViewById(R.id.bitRate_layout);

        buttonBar = rootView.findViewById(R.id.recording_actions);

        max_dbValue = (EditText) rootView.findViewById(R.id.max_dbValue);
        min_dbValue = (EditText) rootView.findViewById(R.id.min_dbValue);

        min_dbValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                minDB.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                        if (s.length() != 0)
                            minDB.setText(s.toString());
            }
        });

        max_dbValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                            maxDB.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(!minDB.getText().toString().isEmpty()) {
                        if (s.length() != 0)
                            maxDB.setText(s.toString());
                }
                else{
                    max_dbValue.getText().clear();
                    Toast.makeText(getActivity(), "Please Enter Min decibles first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        test = rootView.findViewById(R.id.txt_alert_information);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((min_dbValue.getText().toString() != null && !min_dbValue.getText().toString().isEmpty()) &&
                        (max_dbValue.getText().toString() != null && !max_dbValue.getText().toString().isEmpty())) {

                        startRecording();
                        test.setEnabled(false);
                        //handler1.sendMessageAtTime(handler1.obtainMessage(EVENT1), System.currentTimeMillis()+testInterval);

                        new CountDownTimer(testInterval, 100) {

                            public void onTick(long millisUntilFinished) {
                                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                            }

                            public void onFinish() {

                                //Toast.makeText(getContext(), "Event 1", Toast.LENGTH_SHORT).show();
                                stopTesting();
                                TextView textView = new TextView(getContext());

                                textView.setTextSize(20);
                                if(!isGoodEnv()) {
                                    textView.setText("Not Good Env. for Recording");
                                    textView.setBackgroundColor(getResources().getColor(R.color.redColor));
                                    textView.setTextColor(Color.WHITE);
                                }
                                else{
                                    textView.setText("Good Env. for Recording");
                                    textView.setBackgroundColor(getResources().getColor(R.color.white));
                                    textView.setTextColor(getResources().getColor(R.color.black));
                                }
                                new AlertDialog.Builder(getContext())
                                        .setCancelable(false)
                                        .setCustomTitle(textView)
                                        .setMessage("do you Want to continue recording?")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Continue with delete operation

                                                test.setVisibility(View.INVISIBLE);
                                                txt_alert_information_layout.setVisibility(View.INVISIBLE);
                                                bitRate_layout.setVisibility(View.INVISIBLE);
                                                buttonBar.setVisibility(View.VISIBLE);

                                            }
                                        })

                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }.start();
                }
                else{
                    Toast.makeText(getActivity(), "Please enter the min and max decibles and try testing!", Toast.LENGTH_LONG).show();
                }
            }
        });


        rootView.findViewById(R.id.start_recording).setOnTouchListener(
                new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        startRecording();
                        return false;
                    }
                });

        rootView.findViewById(R.id.stop_recording).setOnTouchListener(
                new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        Helper.getHelperInstance().makeHepticFeedback(
                                getActivity());
                        try {

                            if (null != myAudioRecorder) {
                                myAudioRecorder.stop();
                                myAudioRecorder.release();
                                myAudioRecorder = null;


                                Toast.makeText(
                                        getActivity(),
                                        getActivity().getResources().getString(
                                                R.string.rec_saved)
                                                + currentOutFile,
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(
                                    getActivity(),
                                    getActivity().getResources().getString(
                                            R.string.rec_fail),
                                    Toast.LENGTH_LONG).show();
                        }

                        rootView.findViewById(R.id.start_recording).setEnabled(
                                true);
                        rootView.findViewById(R.id.stop_recording).setEnabled(
                                false);
                        rootView.findViewById(R.id.delete_recording)
                                .setEnabled(true);

                        isRecording = false;

                        handler.removeCallbacks(updateVisualizer);

                        return false;
                    }
                });

        rootView.findViewById(R.id.delete_recording).setOnTouchListener(
                new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        File recording = new File(currentOutFile);

                        if (recording.exists() && recording.delete()) {
                            Toast.makeText(
                                    getActivity(),
                                    getResources().getString(
                                            R.string.rec_deleted)
                                            + currentOutFile,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(
                                    getActivity(),
                                    getActivity().getResources().getString(
                                            R.string.rec_delete_fail)
                                            + currentOutFile,
                                    Toast.LENGTH_SHORT).show();
                        }

                        rootView.findViewById(R.id.stop_recording).setEnabled(
                                false);
                        rootView.findViewById(R.id.delete_recording)
                                .setEnabled(false);
                        return false;
                    }
                });

        rootView.findViewById(R.id.txt_cancel).setOnTouchListener(
                new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        Helper.getHelperInstance().makeHepticFeedback(
                                getActivity());

                        try {

                            if (null != myAudioRecorder) {
                                myAudioRecorder.stop();
                                myAudioRecorder.release();
                                myAudioRecorder = null;

                                Toast.makeText(
                                        getActivity(),
                                        getActivity().getResources().getString(
                                                R.string.rec_saved)
                                                + currentOutFile,
                                        Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(
                                    getActivity(),
                                    getActivity().getResources().getString(
                                            R.string.rec_fail),
                                    Toast.LENGTH_LONG).show();
                        }

                        isRecording = false;

                        handler.removeCallbacks(updateVisualizer);

                        System.exit(0);

                        return false;
                    }
                });

        rootView.findViewById(R.id.browse_recording).setOnTouchListener(
                new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        FragmentManager fragmentManager = getActivity()
                                .getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.container,
                                new RecordingListFragment());
                        fragmentTransaction
                                .addToBackStack("RecordingListFragment");
                        fragmentTransaction.commit();

                        return false;
                    }
                });

        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK) {

                    if (doubleBackToExitPressedOnce) {
                        // super.onBackPressed();

                        if (mHandler != null) {
                            mHandler.removeCallbacks(mRunnable);
                        }

                        getActivity().finish();

                        return true;
                    }

                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(getActivity(),
                            "Please click BACK again to exit",
                            Toast.LENGTH_SHORT).show();

                    mHandler.postDelayed(mRunnable, 2000);

                }
                return true;
            }
        });

        return rootView;

    }

    public boolean isGoodEnv() {
        selectedMinDecibles = min_dbValue.getText().toString();
        selectedMaxDecibles = max_dbValue.getText().toString();
        return maxDecibles > Double.parseDouble(selectedMinDecibles) && maxDecibles < Double.parseDouble(selectedMaxDecibles);
    }

    public void stopTesting() {

        Helper.getHelperInstance().makeHepticFeedback(
                getActivity());
        try {

            if (null != myAudioRecorder) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
                test.setEnabled(true);
				/*Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.rec_saved)
								+ currentOutFile,
						Toast.LENGTH_SHORT).show();*/
            }

        } catch (Exception e) {
            e.printStackTrace();
			/*Toast.makeText(
					getActivity(),
					getActivity().getResources().getString(
							R.string.rec_fail),
					Toast.LENGTH_LONG).show();*/
        }

        rootView.findViewById(R.id.start_recording).setEnabled(
                true);
        rootView.findViewById(R.id.stop_recording).setEnabled(
                false);
        rootView.findViewById(R.id.delete_recording)
                .setEnabled(true);

        isRecording = false;

        handler.removeCallbacks(updateVisualizer);
    }

    public static final int RC_CAMERA_AND_LOCATION = 3;

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    void startRecording() {
        methodRequiresTwoPermission();
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            startrecord();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (EasyPermissions.hasPermissions(getActivity(), permissions)) {
            startrecord();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA_AND_LOCATION, permissions);
        }
    }

    private void startrecord(){

            Helper.getHelperInstance().makeHepticFeedback(
                    getActivity());

            if (Helper.getHelperInstance().createRecordingFolder()) {

                /*File dir = new File(Helper.RECORDING_PATH
                        + "/recording_");
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyyMMdd_HH_mm_ss");
                String currentTimeStamp = dateFormat
                        .format(new Date());

                currentOutFile = Helper.RECORDING_PATH
                        + "/recording_" + currentTimeStamp + ".3gp";

                myAudioRecorder = new MediaRecorder();
                myAudioRecorder
                        .setAudioSource(MediaRecorder.AudioSource.MIC);
                myAudioRecorder
                        .setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                myAudioRecorder
                        .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                myAudioRecorder.setAudioChannels(1);
                myAudioRecorder.setAudioSamplingRate(48100);
                myAudioRecorder.setAudioEncodingBitRate(Integer.parseInt(selectedBitRate));
                myAudioRecorder.setOutputFile(currentOutFile);

                Log.e("RecordAduio", "startToch");
                try {
                    visualizerView.clear();
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();

                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.rec_start),
                            Toast.LENGTH_LONG).show();

                    rootView.findViewById(R.id.start_recording)
                            .setEnabled(false);
                    rootView.findViewById(R.id.stop_recording)
                            .setEnabled(true);
                    rootView.findViewById(R.id.delete_recording)
                            .setEnabled(false);

                    isRecording = true;

                    handler.post(updateVisualizer);
                } catch (IllegalStateException e) {
                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.rec_fail),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                    rootView.findViewById(R.id.start_recording).setEnabled(true);
                    rootView.findViewById(R.id.stop_recording)
                            .setEnabled(false);
                    rootView.findViewById(R.id.delete_recording)
                            .setEnabled(true);

                    isRecording = false;
                } catch (IOException e) {
                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.rec_fail),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                    rootView.findViewById(R.id.start_recording)
                            .setEnabled(true);
                    rootView.findViewById(R.id.stop_recording)
                            .setEnabled(false);
                    rootView.findViewById(R.id.delete_recording)
                            .setEnabled(true);

                    isRecording = false;
                }
            } else {

                Toast.makeText(
                        getActivity(),
                        getActivity().getResources().getString(
                                R.string.rec_fail_mkdir),
                        Toast.LENGTH_LONG).show();

                isRecording = false;
            }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        if (isRecording) {

            try {

                if (null != myAudioRecorder) {

                    myAudioRecorder.stop();
                    myAudioRecorder.release();
                    myAudioRecorder = null;

                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.rec_saved)
                                    + currentOutFile, Toast.LENGTH_SHORT)
                            .show();

                    rootView.findViewById(R.id.start_recording)
                            .setEnabled(true);
                    rootView.findViewById(R.id.stop_recording)
                            .setEnabled(false);
                    rootView.findViewById(R.id.delete_recording).setEnabled(
                            true);

                    handler.removeCallbacks(updateVisualizer);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(
                        getActivity(),
                        getActivity().getResources().getString(
                                R.string.rec_fail), Toast.LENGTH_LONG).show();

                rootView.findViewById(R.id.start_recording).setEnabled(true);
                rootView.findViewById(R.id.stop_recording).setEnabled(false);
                rootView.findViewById(R.id.delete_recording).setEnabled(true);

                handler.removeCallbacks(updateVisualizer);

            }
        }
    }

    // updates the visualizer every 50 milliseconds
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) // if we are already recording
            {
                // get the current amplitude
                int x = myAudioRecorder.getMaxAmplitude();
                visualizerView.addAmplitude(x); // update the VisualizeView
                visualizerView.invalidate(); // refresh the VisualizerView


                averageAmplitute = visualizerView.getAverageAmplitude();
                maximumAmplitute = visualizerView.getMaximumAmplitude();
                maxDecibles = 20 *Math.log10(x / 32767.0);;
                //maxDecibles = 20 * Math.log10(maximumAmplitute / ((2 ^ (Integer.parseInt(selectedBitRate)-1))-1));
                averageDB.setText("Recorded Max dB : " + String.format("%.2f", maxDecibles));

                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL);
            }
        }
    };

    float averageAmplitute;
    float maximumAmplitute;
    double maxDecibles;
    double avgDecibles;

}
