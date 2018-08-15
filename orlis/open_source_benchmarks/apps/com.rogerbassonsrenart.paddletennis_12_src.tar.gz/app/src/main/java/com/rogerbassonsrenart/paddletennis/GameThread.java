package com.rogerbassonsrenart.paddletennis;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by roger on 07/03/15.
 */
public class GameThread extends Thread {
    private GameView gv_;
    private boolean run_;
    private SurfaceHolder sh_;
    private Game g_;
    private float rightPaddleTouch;


    public void setRunning(boolean running) {
        run_ = running;
    }

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView, Game g) {
        super();
        sh_ = surfaceHolder;
        gv_ = gameView;
        g_ = g;
    }

    @Override
    public void run() {
        while (run_) {
            g_.play(rightPaddleTouch);
            Canvas c = sh_.lockCanvas();
            g_.draw(c);
            sh_.unlockCanvasAndPost(c);
        }
    }

    public boolean sendEvent(MotionEvent e) {
        boolean res = false;
        if (e.getAction() == MotionEvent.ACTION_UP) {
            rightPaddleTouch = -1;
        } else {
            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
                rightPaddleTouch = e.getY();
                res = true;
            }
        }
        return res;
    }

}
