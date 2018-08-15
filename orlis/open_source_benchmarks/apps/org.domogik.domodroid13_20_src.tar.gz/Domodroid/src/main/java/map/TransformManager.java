package map;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * perform matrix transformations
 */
class TransformManager {
    private static final int POS_X = 0;
    private static final int POS_Y = 1;

    private static final int NONE = 0;
    private static final int ZOOM = 1;
    private static final int DRAG = 2;
    private int mode = NONE;
    private boolean zoom;
    private boolean drag;

    private final Matrix savedMatrix = new Matrix();
    private final Matrix savedMatrix2 = new Matrix();
    private final PointF mid = new PointF();
    private final PointF mid2 = new PointF();
    private float oldDist = 1f;
    private float lastPosX = 0;
    private float lastPosY = 0;
    //float lastScale = 1;
    public final Matrix matrix = new Matrix();
    private int screenMode = 2;
    private final float[] value = new float[9];
    private final int[] pos = new int[2];
    private final float[] savedValue = new float[9];
    public float currentScale;
    public float mapWidth;
    public float mapHeight;
    public float screenWidth;
    public float screenHeight;

    public void actionDown(float evX, float evY) {
        matrix.getValues(value);
        savedMatrix.set(matrix);
        lastPosX = evX;
        lastPosY = evY;
    }

    public void actionPointerDown(MotionEvent event) {
        oldDist = spacing(event);
        if (oldDist > 10f) {
            savedMatrix.set(matrix);
            midPoint(mid, event);
        }
    }

    public void actionUp(float evX, float evY) {
        mode = NONE;
        matrix.getValues(value);
        pos[POS_X] = (int) ((evX - value[2]) / value[0]);
        pos[POS_Y] = (int) ((evY - value[5]) / value[0]);
    }

    public int[] currentPosition(float evX, float evY) {
        matrix.getValues(value);
        pos[POS_X] = (int) ((evX - value[2]) / value[0]);
        pos[POS_Y] = (int) ((evY - value[5]) / value[0]);
        return pos;
    }

    public void actionMove(int nbPointers, MotionEvent event) {
        if (nbPointers == 2) {
            matrix.set(savedMatrix);
            float newDist = spacing(event);
            float scale = newDist / oldDist;

            midPoint(mid2, event);
            if (mode == NONE && (scale > 1.20 || scale < 0.80) && zoom)
                mode = ZOOM;

            if (mode == NONE && (mid.x - mid2.x > 20 || mid.y - mid2.y > 20 || mid.x - mid2.x < -20 || mid.y - mid2.y < -20)
                    && (scale < 1.20 && scale > 0.80) && drag)
                mode = DRAG;

            if (mode != DRAG && zoom) {
                matrix.postScale(scale, scale, mid.x, mid.y);
                //lastScale = scale;
            }
            //else matrix.postScale(lastScale, lastScale, mid.x, mid.y);

            if (mode != ZOOM && drag) {
                matrix.postTranslate((event.getX(0) + event.getX(1)) / 2 - mid.x, (event.getY(0) + event.getY(1)) / 2 - mid.y);
                lastPosX = event.getX() - mid.x;
                lastPosY = event.getY() - mid.y;
            }
            matrixTuning();
        }

    }

    // rearrange the matrix
    private void matrixTuning() {
        matrix.getValues(value);
        savedMatrix2.getValues(savedValue);
        if (value[0] * currentScale > 1.5 || value[4] * currentScale > 1.5) {
            value[0] = 1.5f / currentScale;
            value[4] = 1.5f / currentScale;
            value[2] = savedValue[2];
            value[5] = savedValue[5];
        }

        if (value[0] * currentScale < 0.5 || value[4] * currentScale < 0.5) {
            value[0] = 0.5f / currentScale;
            value[4] = 0.5f / currentScale;
            value[2] = savedValue[2];
            value[5] = savedValue[5];
        }

        matrix.setValues(value);
        savedMatrix2.set(matrix);
    }

    // space between two fingers
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    // mid point two fingers
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void setScreenConfigScaling() {
        matrix.getValues(value);
        value[2] = 0;
        value[5] = 0;
        matrix.setValues(value);
    }

    public void setLandscapeMode() {
        screenMode = 2;
        matrix.getValues(value);
        value[0] = 1;
        value[4] = 1;
        value[2] = 0;
        value[5] = 0;
        matrix.setValues(value);
    }

    public void setZoom(boolean zoom) {
        this.zoom = zoom;
    }

    public void setDrag(boolean drag) {
        this.drag = drag;
    }

}
