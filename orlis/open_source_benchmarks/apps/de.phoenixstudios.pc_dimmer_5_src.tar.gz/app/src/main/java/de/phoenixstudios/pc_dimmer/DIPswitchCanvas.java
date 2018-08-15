package de.phoenixstudios.pc_dimmer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DIPswitchCanvas extends View {

    Context context;
    private Paint mPaint;
    private Bitmap dipbackground;
    private Bitmap dipknob;
    private Rect src_bg;
    private Rect dst_bg;
    private Rect src_knob;
    private Rect dst_knob;

    public DIPswitchCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        dipbackground = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_dipswitchbg);
        dipknob = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_dipswitchknob);

        src_bg = new Rect(0,0,dipbackground.getWidth()-1, dipbackground.getHeight()-1);
        dst_bg = new Rect(0,0,710-1, 300-1);

        src_knob = new Rect(0, 0, dipknob.getWidth()-1, dipknob.getHeight()-1);
        dst_knob = new Rect(0, 0, 46-1, 58-1);

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);

//        mPaint.setColor(Color.WHITE);
//        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        mPaint.setStrokeJoin(Paint.Join.ROUND);
//        mPaint.setStrokeWidth(4f);
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(dipbackground, src_bg, dst_bg, mPaint);

        // on: top=75
        // off: top=170
        int top=0;
        int left=0;
        for (int i=0;i<9;i++){
            switch(i){
                case 0:
                    left = 60;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L)) != 0) top = 75; else top = 170;
                    break;
                case 1:
                    left = 127;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 1)) != 0) top = 75; else top = 170;
                    break;
                case 2:
                    left = 191;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 2)) != 0) top = 75; else top = 170;
                    break;
                case 3:
                    left = 259;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 3)) != 0) top = 75; else top = 170;
                    break;
                case 4:
                    left = 332;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 4)) != 0) top = 75; else top = 170;
                    break;
                case 5:
                    left = 398;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 5)) != 0) top = 75; else top = 170;
                    break;
                case 6:
                    left = 468;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 6)) != 0) top = 75; else top = 170;
                    break;
                case 7:
                    left = 537;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 7)) != 0) top = 75; else top = 170;
                    break;
                case 8:
                    left = 608;
                    if ((Main.CurrentSetupDevice.Startaddress & (1L << 8)) != 0) top = 75; else top = 170;
                    break;
            }

            dst_knob.top=top;
            dst_knob.left=left;
            dst_knob.bottom=58-1+top;
            dst_knob.right=46-1+left;
            canvas.drawBitmap(dipknob, src_knob, dst_knob, mPaint);
        }
    }

    public void setPoint(float x, float y) {
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        if ((x>60) && (x<(45+60))){
            Main.CurrentSetupDevice.Startaddress ^= (1);
        }
        if ((x>127) && (x<(45+127))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 1);
        }
        if ((x>191) && (x<(45+191))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 2);
        }
        if ((x>259) && (x<(45+259))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 3);
        }
        if ((x>332) && (x<(45+332))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 4);
        }
        if ((x>398) && (x<(45+398))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 5);
        }
        if ((x>468) && (x<(45+468))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 6);
        }
        if ((x>537) && (x<(45+537))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 7);
        }
        if ((x>608) && (x<(45+608))){
            Main.CurrentSetupDevice.Startaddress ^= (1 << 8);
        }

        Main.SetAddressEdit();
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
    }

    public void clearCanvas() {
        invalidate();
    }

    // when ACTION_UP stop touch
    private void upTouch() {
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
