package com.saladdressing.veterondo.pojos;

import com.saladdressing.veterondo.interfaces.ColorCallback;

/**
 * Created by sebas on 3/27/2016.
 */
public class Dot {

    private String color;
    private boolean isAnimating;
    private boolean isInGroupAnimation;


    public Dot() {

    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setColorDelayed(String color, long delay, ColorCallback ccb) {

        new ColorThread(this, color, delay, ccb).start();

    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setIsAnimating(boolean isAnimating) {
        this.isAnimating = isAnimating;
    }

    public boolean isInGroupAnimation() {
        return isInGroupAnimation;
    }

    public void setIsInGroupAnimation(boolean isInGroupAnimation) {
        this.isInGroupAnimation = isInGroupAnimation;
    }

    @Override
    public String toString() {
        return "Dot color: " + color + " and is it animating? : " + isAnimating;
    }

    private static final class ColorThread extends Thread {

        final long delay;
        final String color;
        final Dot dot;
        ColorCallback ccb;

        public ColorThread(Dot dot, String color, long delay, ColorCallback colorCallback) {
            this.delay = delay;
            this.color = color;
            this.dot = dot;
            this.ccb = colorCallback;
        }

        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            dot.setColor(color);

            ccb.notifyAdapterDelayed();


        }
    }
}
