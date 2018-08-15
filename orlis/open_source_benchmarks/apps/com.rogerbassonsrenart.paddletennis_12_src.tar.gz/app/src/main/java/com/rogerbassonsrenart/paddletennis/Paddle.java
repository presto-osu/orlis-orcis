package com.rogerbassonsrenart.paddletennis;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Created by roger on 07/03/15.
 */
public class Paddle {
    private int movement_; // 0 is stopped, 1 is up, 2 is down
    private int speed_;

    private Rect r_;
    private Paint p_;

    Paddle(int width, int height) {
        r_ = new Rect(0,0,width,height);
        p_ = new Paint();
        p_.setStyle(Paint.Style.FILL);
        p_.setColor(Color.WHITE);
        movement_ = 0;
        speed_ = height/8;
    }

    public float getYCenter() {
        return r_.exactCenterY();
    }

    public Rect getRect() {
        return r_;
    }

    public void setLeft(int w, int h) {
        int newLeft = 15;
        int newTop = (h / 2) - (r_.height() / 2);
        r_.offsetTo(newLeft,newTop);
    }

    public void setRight(int w, int h) {
        int newLeft = w - r_.width() - 15;
        int newTop = (h / 2) - (r_.height() / 2);
        r_.offsetTo(newLeft,newTop);
    }

    public void accelerateUp() {
        movement_ = 1;
    }

    public void accelerateDown() {
        movement_ = 2;
    }

    public void stop() {
        movement_ = 0;
    }

    public void move(int max) {
        int dx = 0;
        int dy = 0;
        switch (movement_) {
            case 1:
                if (r_.top-speed_ >= 0) {
                    dy = -speed_;
                }
                break;
            case 2:
                if (r_.bottom+speed_ <= max) {
                    dy = speed_;
                }
                break;
        }
        r_.offset(dx,dy);
    }

    public void follow(SquareBall b) {
        float ballY = b.exactCenterY();
        if (Math.abs(r_.exactCenterY() - ballY) > r_.height()/2) {
            if (r_.exactCenterY() < ballY) {
                movement_ = 2;
            } else if (r_.exactCenterY() > ballY) {
                movement_ = 1;
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(r_,p_);
    }
}
