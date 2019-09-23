package com.serveroverload.recorder.customview

import java.util.ArrayList
import java.util.Collections

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class RecorderVisualizerView// constructor
(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var amplitudes: MutableList<Float>? = null // amplitudes for line lengths
    var widthValue: Int = 0// widthValue of this View
    var heightValue: Int = 0// heightValue of this View
    private val linePaint: Paint // specifies line drawing characteristics

    //amplitudes.get(amplitudes.size()-1)
    val averageAmplitude: Float
        get() {
            val count = amplitudes!!.size

            var sum = 0.0f

            for (i in amplitudes!!.indices) {
                sum += amplitudes!![i]
            }

            if (amplitudes!!.isEmpty()) return 0.0f
            val averageAmp = sum / count
            Log.e("Recorder", "" + averageAmp)

            return (20 * Math.log10(averageAmp.toDouble())).toFloat()
        }

    val maximumAmplitude: Float
        get() {

            val maxAmplitude = Collections.max(amplitudes)
            Log.e("Recorder", "" + maxAmplitude)

            return (20 * Math.log10(maxAmplitude.toDouble())).toFloat()
        }

    init {
        linePaint = Paint() // create Paint for lines
        linePaint.color = Color.CYAN
        //linePaint.setShader(new LinearGradient(0, 0, 0, LINE_WIDTH*2, Color.GREEN, Color.RED, Shader.TileMode.MIRROR));
        // set color to green
        linePaint.strokeWidth = LINE_WIDTH.toFloat() // set stroke widthValue
    }// call superclass constructor

    // called when the dimensions of the View change
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        widthValue = w // new widthValue of this View
        heightValue = h // new heightValue of this View
        amplitudes = ArrayList(widthValue / LINE_WIDTH)
    }

    // clear all amplitudes to prepare for a new visualization
    fun clear() {
        amplitudes!!.clear()
    }

    // add the given amplitude to the amplitudes ArrayList
    fun addAmplitude(amplitude: Float) {
        amplitudes!!.add(amplitude) // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes!!.size * LINE_WIDTH >= widthValue) {
            amplitudes!!.removeAt(0) // remove oldest power value
        }
    }

    // draw the visualizer with scaled lines representing the amplitudes
    public override fun onDraw(canvas: Canvas) {
        val middle = heightValue / 2 // get the middle of the View
        var curX = 0f // start curX at zero

        // for each item in the amplitudes ArrayList
        for (power in amplitudes!!) {
            val scaledHeight = power / LINE_SCALE // scale the power
            curX += LINE_WIDTH.toFloat() // increase X by LINE_WIDTH
            //linePaint.setShader(new LinearGradient(0, 0, 0, scaledHeight/2, Color.GREEN, Color.RED, Shader.TileMode.MIRROR));
            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle - scaledHeight / 2, linePaint)
        }
    }

    companion object {
        private val LINE_WIDTH = 2 // widthValue of visualizer lines
        private val LINE_SCALE = 100 // scales visualizer lines
    }

}