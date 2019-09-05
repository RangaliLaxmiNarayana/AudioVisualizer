package com.serveroverload.recorder.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.serveroverload.recorder.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class HomeActivity extends FragmentActivity {

	MediaPlayer mMediaPlayer;

	private ArrayList<String> recordings = new ArrayList<String>();

	public int RecordingNumber;

	/**
	 * @return the mMediaPlayer
	 */
	public MediaPlayer getmMediaPlayer() {
		return mMediaPlayer;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mMediaPlayer = new MediaPlayer();

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.container, new RecordAudioFragment2());
		fragmentTransaction.addToBackStack("RecordAudioFragment");
		fragmentTransaction.commit();
		
		/*AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer = null;
		}

	}

	public ArrayList<String> getRecordings() {
		return recordings;
	}

	public void setRecordings(ArrayList<String> recordings) {
        //Collections.reverse(recordings);
		this.recordings = recordings;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
