package com.serveroverload.recorder.customview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RecorderVisualizerView extends View {
    private static final int LINE_WIDTH = 2; // width of visualizer lines
    private static final int LINE_SCALE = 100; // scales visualizer lines
    private List<Float> amplitudes; // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private Paint linePaint; // specifies line drawing characteristics

    // constructor
    public RecorderVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        linePaint.setColor(Color.CYAN);
        //linePaint.setShader(new LinearGradient(0, 0, 0, LINE_WIDTH*2, Color.GREEN, Color.RED, Shader.TileMode.MIRROR));
       // set color to green
        linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
    } 

    // called when the dimensions of the View change
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w; // new width of this View
        height = h; // new height of this View
        amplitudes = new ArrayList(width / LINE_WIDTH);
    } 

    // clear all amplitudes to prepare for a new visualization
    public void clear() {
        amplitudes.clear();
    } 

    // add the given amplitude to the amplitudes ArrayList
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        } 
    } 

    // draw the visualizer with scaled lines representing the amplitudes
    @Override
    public void onDraw(Canvas canvas) {
        int middle = height / 2; // get the middle of the View
        float curX = 0; // start curX at zero

        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            float scaledHeight = (power / LINE_SCALE); // scale the power
            curX += LINE_WIDTH; // increase X by LINE_WIDTH
            //linePaint.setShader(new LinearGradient(0, 0, 0, scaledHeight/2, Color.GREEN, Color.RED, Shader.TileMode.MIRROR));
            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                    - scaledHeight / 2, linePaint);
        } 
    }

    public float getAverageAmplitude(){
        int count=amplitudes.size();

       float sum=0.0f;

       for (int i=0;i<amplitudes.size();i++){
           sum+=amplitudes.get(i);
       }

       if(amplitudes.isEmpty()) return 0.0f;

//amplitudes.get(amplitudes.size()-1)
       float averageAmp=sum/count;
        Log.e("Recorder",""+averageAmp);
       float db=(float)(20 * Math.log10(averageAmp));

       return db;
    }

    public float getMaximumAmplitude(){

        float maxAmplitude= Collections.max(amplitudes);
        Log.e("Recorder",""+maxAmplitude);
        float db=(float)(20 * Math.log10(maxAmplitude));

        return db;
    }

}