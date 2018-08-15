package com.rogerbassonsrenart.paddletennis;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.Random;

/**
 * Created by roger on 07/03/15.
 */
public class SquareBall {
    private Rect r_;
    private Paint p_;
    private int dx_;
    private int dy_;


    SquareBall(int size) {
        r_ = new Rect(0, 0, size, size);
        p_ = new Paint();
        p_.setStyle(Paint.Style.FILL);
        p_.setColor(Color.WHITE);
        dx_ = size/4;
        dy_ = 0;
    }

    public void moreSpeed() {
        dx_++;
    }

    public float exactCenterY() {
        return r_.exactCenterY();
    }

    public void center(int w, int h) {
        int newLeft = (w / 2) - (r_.width() / 2);
        int newTop = (h / 2) - (r_.height() / 2);
        r_.offsetTo(newLeft, newTop);
    }

    public void move(int w, int h) {
        if (r_.top - dy_ < 0) {
            r_.offsetTo(r_.left, 0);
            dy_ *= -1;
        } else if (r_.bottom + dy_ > h) {
            r_.offsetTo(r_.left, h - r_.height());
            dy_ *= -1;
        }

        r_.offset(dx_, dy_);
    }

    public boolean isOutsideRightSide(int w) {
        return r_.left > w;
    }

    public boolean isOutsideLeftSide() {
        return r_.right < 0;
    }

    public void randomVerticalSpeed() {
        Random r = new Random();
        int dxDoubled = Math.abs(dx_) * 2;
        dy_ = r.nextInt(dxDoubled*2+1) - dxDoubled;
    }
    public void randomHoritzontalDirection() {
        Random r = new Random();
        int direction = r.nextInt(2);
        if (direction == 0) {
            dx_ *= -1;
        }
    }

    public void invertHoritzontalDirection() {
        dx_ *= -1;
    }

    public void invertVerticalDirection() {
        dy_ *= -1;
    }

    public boolean hasCollided(Paddle p) {
        Rect r = p.getRect();
        return Rect.intersects(r, r_);
    }

    public Rect getRect() {
        return r_;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(r_, p_);
    }
}
