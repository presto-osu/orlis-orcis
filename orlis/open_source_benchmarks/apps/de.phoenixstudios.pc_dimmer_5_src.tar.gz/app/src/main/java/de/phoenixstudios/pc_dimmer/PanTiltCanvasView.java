package de.phoenixstudios.pc_dimmer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PanTiltCanvasView extends View {

    private Canvas mCanvas;
    private Path mPath;
    Context context;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;

    public PanTiltCanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        Bitmap mBitmap;
        if ((w>0) && (h>0)) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }else{
            mBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the mPath with the mPaint on the canvas when onDraw
        //canvas.drawPath(mPath, mPaint); // Linien zeichnen

        canvas.drawLine(0, 0, 0, canvas.getHeight(), mPaint); // links
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), mPaint); // rechts
        canvas.drawLine(0, 0, canvas.getWidth(), 0, mPaint); // oben
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), mPaint); // unten


        canvas.drawLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, mPaint); // Mittlerer waagrechter Strich
        canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight(), mPaint); // Mittlerer senkrechter Strich
        canvas.drawCircle(mX, mY, 35.0f, mPaint); // Punkt an aktuelle Position zeichnen

        Main.set_pantilt(Main.CurrentDeviceOrGroupID, -1, Math.round((mX / canvas.getWidth()) * 255), -1, Math.round((mY / canvas.getHeight()) * 255), Main.GlobalFadetime, -1);
        //Main.set_channel(Main.CurrentDeviceOrGroupID, "Pan", -1, Math.round((mX / canvas.getWidth()) * 255), 150, -1);
        //Main.set_channel(Main.CurrentDeviceOrGroupID, "Tilt", -1, Math.round((mY/canvas.getHeight())*255), 150, -1);
    }

    public void setPoint(float x, float y) {
        if (mCanvas!=null) {
            mX = (x / 10000) * mCanvas.getWidth();
            mY = (y / 10000) * mCanvas.getHeight();
        }
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        mPath.lineTo(mX, mY);
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }
}
