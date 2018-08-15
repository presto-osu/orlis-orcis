package com.rogerbassonsrenart.paddletennis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by roger on 07/03/15.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread_;
    private Game g_;

    GameView(Context context, Game g) {
        super(context);
        getHolder().addCallback(this);
        g_ = g;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!g_.isInitialized()) {
            g_.initialize(getWidth(), getHeight());
        }
        gameThread_ = new GameThread(getHolder(), this, g_);
        gameThread_.setRunning(true);
        gameThread_.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean done = false;
        while (!done) {
            try {
                gameThread_.join();
                done = true;
            } catch (InterruptedException e) {
                Log.d(null,e.getMessage());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return gameThread_.sendEvent(event);
    }

    public void stopView() {
        if (gameThread_ != null) {
            gameThread_.setRunning(false);
        }
    }
}
