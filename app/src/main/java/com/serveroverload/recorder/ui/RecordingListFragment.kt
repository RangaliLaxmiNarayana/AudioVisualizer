/**
 *
 */
package com.serveroverload.recorder.ui

import java.io.IOException

import android.app.Activity
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView

import com.serveroverload.recorder.R
import com.serveroverload.recorder.customview.PlayerVisualizerView
import com.serveroverload.recorder.util.Helper
import com.serveroverload.recorder.util.RecordingsLoaderTask

/**
 * @author Hitesh
 */
class RecordingListFragment : Fragment(), OnRefreshListener {
    private var swipeLayout: SwipeRefreshLayout? = null
    private var recordingsListView: ListView? = null
    private var mLinearLayout: LinearLayout? = null
    private var mVisualizerView: com.serveroverload.recorder.customview.PlayerVisualizerView? = null
    private var mVisualizer: Visualizer? = null
    private var rootView: View? = null
    private var back: ImageView? = null
    private var SONGPAUSED: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.recording_list_fragment,
                container, false)

        (activity as HomeActivity).RecordingNumber = 0
        //
        // ((HomeActivity) getActivity()).getmActionBarTitle().setText(
        // getResources().getString(R.string.title_album));

        recordingsListView = rootView!!
                .findViewById<View>(R.id.listView_Recording) as ListView

        back = rootView!!.findViewById<View>(R.id.back) as ImageView
        back!!.setOnClickListener { activity!!.onBackPressed() }

        RecordingsLoaderTask(swipeLayout, recordingsListView, activity)
                .execute(Helper.LOAD_RECORDINGS)

        recordingsListView!!.isFastScrollEnabled = true

        // }

        swipeLayout = rootView!!
                .findViewById<View>(R.id.swipe_container) as SwipeRefreshLayout
        swipeLayout!!.setOnRefreshListener(this@RecordingListFragment)
        swipeLayout!!.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        // listen for when the music stream ends playing
        (activity as HomeActivity).getmMediaPlayer()!!
                .setOnCompletionListener {
                    // disable the visualizer as it's no longer
                    // needed

                    if (null != mVisualizer)
                        mVisualizer!!.enabled = false

                    rootView!!.findViewById<View>(R.id.btnPauseSlider).visibility = View.GONE

                    rootView!!.findViewById<View>(R.id.btnPlaySlider).visibility = View.VISIBLE
                }

        mLinearLayout = rootView!!
                .findViewById<View>(R.id.linearLayoutVisual) as LinearLayout
        // Create a VisualizerView to display the audio waveform for the current
        // settings
        mVisualizerView = PlayerVisualizerView(activity as HomeActivity)
        mVisualizerView!!.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (VISUALIZER_HEIGHT_DIP * resources
                        .displayMetrics.density).toInt())
        mLinearLayout!!.addView(mVisualizerView)

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = Visualizer((activity as HomeActivity)
                .getmMediaPlayer()!!.audioSessionId)

        mVisualizer!!.captureSize = Visualizer.getCaptureSizeRange()[1]

        mVisualizer!!.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer,
                                                       bytes: ByteArray, samplingRate: Int) {
                        mVisualizerView!!.updateVisualizer(bytes)
                    }

                    override fun onFftDataCapture(visualizer: Visualizer,
                                                  bytes: ByteArray, samplingRate: Int) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false)

        mVisualizer!!.enabled = true

        rootView!!.findViewById<View>(R.id.btnPauseSlider).setOnClickListener {
            Helper.getHelperInstance().makeHepticFeedback(
                    activity)

            if (null != mVisualizer)
                mVisualizer!!.enabled = false

            (activity as HomeActivity).getmMediaPlayer()!!
                    .pause()

            SONGPAUSED = true

            rootView!!.findViewById<View>(R.id.btnPauseSlider).visibility = View.GONE

            rootView!!.findViewById<View>(R.id.btnPlaySlider).visibility = View.VISIBLE
        }

        rootView!!.findViewById<View>(R.id.btnPlaySlider).setOnClickListener {
            Helper.getHelperInstance().makeHepticFeedback(
                    activity)

            if (SONGPAUSED) {

                resumeSong()

            } else {

                playSong((activity as HomeActivity).RecordingNumber)

            }
        }

        rootView!!.findViewById<View>(R.id.btnNextSlider).setOnClickListener {
            Helper.getHelperInstance().makeHepticFeedback(
                    activity)

            if ((activity as HomeActivity).recordings
                            .size > 0) {
                if ((activity as HomeActivity).RecordingNumber < (activity as HomeActivity)
                                .recordings.size - 1) {
                    (activity as HomeActivity).RecordingNumber = (activity as HomeActivity).RecordingNumber + 1
                } else {
                    (activity as HomeActivity).RecordingNumber = 0
                }

                playSong((activity as HomeActivity).RecordingNumber)
            }
        }

        rootView!!.findViewById<View>(R.id.btnBackSlider).setOnClickListener {
            Helper.getHelperInstance().makeHepticFeedback(
                    activity)

            if ((activity as HomeActivity).recordings
                            .size > 0) {
                if ((activity as HomeActivity).RecordingNumber > 0) {
                    (activity as HomeActivity).RecordingNumber = (activity as HomeActivity).RecordingNumber - 1
                } else {
                    (activity as HomeActivity).RecordingNumber = (activity as HomeActivity)
                            .recordings.size - 1
                }

                playSong((activity as HomeActivity).RecordingNumber)
            }
        }

        rootView!!.findViewById<View>(R.id.btnNextSlider).setOnClickListener {
            if ((activity as HomeActivity).recordings
                            .size > 0) {
                if ((activity as HomeActivity).RecordingNumber < (activity as HomeActivity)
                                .recordings.size - 1) {
                    (activity as HomeActivity).RecordingNumber = (activity as HomeActivity).RecordingNumber + 1
                } else {
                    (activity as HomeActivity).RecordingNumber = 0
                }
            }
            playSong((activity as HomeActivity).RecordingNumber)
        }

        recordingsListView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            (activity as HomeActivity).RecordingNumber = position

            playSong(position)
        }

        return rootView
    }

    override fun onRefresh() {

        RecordingsLoaderTask(swipeLayout, recordingsListView, activity)
                .execute(Helper.LOAD_RECORDINGS)

    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()

        if (null != mVisualizer)
            mVisualizer!!.release()
        mVisualizer = null
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        super.onPause()

        if (null != mVisualizer)
            mVisualizer!!.release()
        mVisualizer = null

        (activity as HomeActivity).getmMediaPlayer()!!.reset()
    }

    override fun onAttach(activity: Activity?) {
        // TODO Auto-generated method stub
        super.onAttach(activity)

        // activityReference = activity;
    }

    private fun playSong(RecordingNumber: Int) {
        val mMediaPlayer = (activity as HomeActivity)
                .getmMediaPlayer()

        if (null != mVisualizer)
            mVisualizer!!.enabled = true

        if (null != mMediaPlayer && !(activity as HomeActivity).recordings.isEmpty()) {
            mMediaPlayer.reset()
            try {
                mMediaPlayer.setDataSource((activity as HomeActivity)
                        .recordings[RecordingNumber])

                mMediaPlayer.prepare()
                mMediaPlayer.start()

                rootView!!.findViewById<View>(R.id.btnPauseSlider).visibility = View.VISIBLE

                rootView!!.findViewById<View>(R.id.btnPlaySlider).visibility = View.GONE
            } catch (e: IllegalArgumentException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: SecurityException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }
    }

    private fun resumeSong() {

        if (null != mVisualizer)
            mVisualizer!!.enabled = true

        (activity as HomeActivity).getmMediaPlayer()!!.start()

        rootView!!.findViewById<View>(R.id.btnPauseSlider).visibility = View.VISIBLE

        rootView!!.findViewById<View>(R.id.btnPlaySlider).visibility = View.GONE

        SONGPAUSED = false
    }

    companion object {

        private val VISUALIZER_HEIGHT_DIP = 100f
    }

    // private Activity activityReference;

}
