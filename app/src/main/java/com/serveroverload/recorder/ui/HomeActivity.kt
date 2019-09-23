package com.serveroverload.recorder.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction

import com.serveroverload.recorder.R

import java.util.ArrayList
import java.util.Collections

import pub.devrel.easypermissions.EasyPermissions

class HomeActivity : FragmentActivity() {

    internal var mMediaPlayer: MediaPlayer? = null

    //Collections.reverse(recordings);
    var recordings = ArrayList<String>()

    var RecordingNumber: Int = 0

    /**
     * @return the mMediaPlayer
     */
    fun getmMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mMediaPlayer = MediaPlayer()

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager
                .beginTransaction()
        fragmentTransaction.replace(R.id.container, RecordAudioFragment2())
        fragmentTransaction.addToBackStack("RecordAudioFragment")
        fragmentTransaction.commit()

        /*AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()

        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer = null
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
