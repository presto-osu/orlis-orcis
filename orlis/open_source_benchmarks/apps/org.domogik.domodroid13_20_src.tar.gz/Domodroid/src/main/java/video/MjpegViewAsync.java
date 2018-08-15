package video;


import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegViewAsync extends SurfaceView implements SurfaceHolder.Callback {
    public final static int POSITION_UPPER_LEFT = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD = 1;
    public final static int SIZE_BEST_FIT = 4;
    public final static int SIZE_FULLSCREEN = 8;

    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;
    private Handler handler;


    public class MjpegViewThread extends AsyncTask<Void, Void, Void> {
        private final SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN)
                return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();

            while (mRun) {
                if (surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                                bm = mIn.readMjpegFrame();
                                destRect = destRect(bm.getWidth(), bm.getHeight());
                                c.drawColor(Color.TRANSPARENT);
                                c.drawBitmap(bm, null, destRect, p);

                                if (showFps) {
                                    p.setXfermode(mode);
                                    p.setXfermode(null);
                                    frameCounter++;
                                    if ((System.currentTimeMillis() - start) >= 1000) {
                                        handler.sendEmptyMessage(frameCounter);
                                        frameCounter = 0;
                                        start = System.currentTimeMillis();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } finally {
                        if (c != null) mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
            return null;
        }
    }

    private void init(Context context) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    private void startPlayback() {
        if (mIn != null) {
            mRun = true;
            thread.execute();
        }
    }

    public void stopPlayback() {
        mRun = false;
        boolean retry = true;
        while (retry) {
            retry = false;
        }
    }

    public MjpegViewAsync(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public MjpegViewAsync(Context context) {
        super(context);
        init(context);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    public void showFps(boolean b) {
        showFps = b;
    }

    public void setSource(MjpegInputStream source) {
        mIn = source;
        startPlayback();
    }

    public void setDisplayMode(int s) {
        displayMode = s;
    }
}
