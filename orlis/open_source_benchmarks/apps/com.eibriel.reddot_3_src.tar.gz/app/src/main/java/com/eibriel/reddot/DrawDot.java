package com.eibriel.reddot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.Date;
import java.util.Random;

public class DrawDot extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    //Paint paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
    float centerX;
    float centerY;
    //float radius;
    float base;

    public DrawDot(Context context) {
        super(context);
        paint.setColor(Color.RED);
        //paint2.setColor(Color.rgb(150, 0, 0));
        //paint3.setColor(Color.rgb(80, 0, 0));
    }


    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        centerX = w/2;
        centerY = h/2;

        if (w < 500) {
            base = 50;
        } else if (w < 1000) {
            base = 100;
        } else {
            base = 200;
        }
    }


    private float timeToSin(long milliTime,
                            long offset,
                            float amplitude,
                            double truncate,
                            double threshold) {
        long milliseconds = (milliTime-offset) % (long)truncate;
        double milli_float = (milliseconds / truncate) * (2 * Math.PI) ;

        float sin = (float)Math.sin( milli_float );
        sin = (sin + 1) / 2;

        //Log.d("TimerExample", "Sin... " + sin);

        if (sin < threshold) {
            sin = 0;
        }

        return sin * amplitude;
    }


    private float timeToRadius(long milliTime) {

        //1000 = 1 second
        //10000 = 10 seconds
        //100000 = 100 seconds, 1.66 minutes
        //1000000 = 1000 seconds, 16.6 minutes
        //10000000 = 2.7 hours
        //100000000 = 27 hours
        //1000000000 = 11.25 days

        // Amplitude sines
        float amp_sine1 = timeToSin(milliTime, 864,   1, 10000.0, 0);
        float amp_sine2 = timeToSin(milliTime, 44579, 1, 100000.0, 0);
        float amp_sine3 = timeToSin(milliTime, 3245,  1, 1000000.0, 0);
        float amp_sine4 = timeToSin(milliTime, 1158,  1, 1000000.0, 0);
        //
        float amp_sine5 = timeToSin(milliTime, 864879546,  1, 100000000.0, 0);
        float amp_sine6 = timeToSin(milliTime, 1457954771, 1, 100000000.0, 0);
        float amp_sine7 = timeToSin(milliTime, 324545654,  1, 1000000000.0, 0);
        float amp_sine8 = timeToSin(milliTime, 1158123221,  1, 1000000000.0, 0);

        // Radius sines
        float sine1 = timeToSin(milliTime, 1894,    10* amp_sine1*amp_sine5, 100.0, 0);
        float sine2 = timeToSin(milliTime, 55689,   30* amp_sine2*amp_sine6, 1000.0, 0);
        float sine3 = timeToSin(milliTime, 8897,    30*amp_sine3*amp_sine7, 10000.0, 0);
        float sine4 = timeToSin(milliTime, 1124568, 30*amp_sine4*amp_sine8, 100000.0, 0);

        float radius_ = base + ((sine1 + sine2 + sine3 + sine4)*(base/50));

        //Log.d("TimerExample", "Ampsine1 " + amp_sine1);
        return radius_;
    }


    private int timeToColor(long milliTime) {
        // Color sine
        float r_color_sine = 200 + timeToSin(milliTime, 87956421, 55, 1000000.0, 0);
        float g_color_sine = timeToSin(milliTime, 87956421, 55, 1000000.0, 0);
        float b_color_sine = timeToSin(milliTime, 56421, 55, 1000000.0, 0);

        return Color.rgb((int)r_color_sine, (int)g_color_sine, (int)b_color_sine);
    }


    @Override
    public void onDraw(Canvas canvas) {
        //canvas.drawLine(0, 0, 20, 20, paint);
        //canvas.drawLine(20, 0, 0, 20, paint);
        //Random diceRoller = new Random();
        //radius = diceRoller.nextInt(100) + 100;

        Date time = new Date();
        //long rest = 14310300 * 100000;
        float radius = timeToRadius(time.getTime());
        paint.setColor(timeToColor(time.getTime()));
        //float radius2 = timeToRadius( time.getTime()-500);
        //float radius3 = timeToRadius( time.getTime()-1000);

        //Log.d("TimerExample", "Going for... " + milli2_float);
        //canvas.drawCircle(centerX, centerY, radius3, paint3);
        //canvas.drawCircle(centerX, centerY, radius2, paint2);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

}