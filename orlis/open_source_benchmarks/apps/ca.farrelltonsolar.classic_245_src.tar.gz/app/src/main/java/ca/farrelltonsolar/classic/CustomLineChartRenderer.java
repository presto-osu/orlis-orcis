package ca.farrelltonsolar.classic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by Me on 5/14/2016.
 */
public class CustomLineChartRenderer extends LineChartRenderer {


    public CustomLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    private float[] mLineBuffer = new float[4];

    @Override
    protected void drawLinear(Canvas canvas, ILineDataSet dataSet) {
        if (dataSet.isDrawSteppedEnabled()) {
            int entryCount = dataSet.getEntryCount();
            mRenderPaint.setStyle(Paint.Style.STROKE);
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
            Entry entryFrom = dataSet.getEntryForXIndex((mMinX < 0) ? 0 : mMinX, DataSet.Rounding.DOWN);
            Entry entryTo = dataSet.getEntryForXIndex(mMaxX, DataSet.Rounding.UP);

            int diff = (entryFrom == entryTo) ? 1 : 0;
            int minx = Math.max(dataSet.getEntryIndex(entryFrom) - diff, 0);
            int maxx = Math.min(Math.max(minx + 2, dataSet.getEntryIndex(entryTo) + 1), entryCount);

            final int count = (int) (Math.ceil((float) (maxx - minx) + (float) (minx)));
            if (mLineBuffer.length != 2 * 2)
                mLineBuffer = new float[2 * 2];
            float bottom = mChart.getYChartMin();

            for (int j = minx;
                 j < count;
                 j++) {

                if (count > 1 && j == count - 1) {
                    // Last point, we have already drawn a line to this point
                    break;
                }

                Entry e = dataSet.getEntryForIndex(j);
                if (e == null) continue;

                mLineBuffer[0] = e.getXIndex();
                mLineBuffer[1] = bottom;

                if (j + 1 < count) {

                    e = dataSet.getEntryForIndex(j + 1);

                    if (e == null) break;
                    mLineBuffer[2] = e.getXIndex();
                    mLineBuffer[3] = bottom;

                } else {
                    mLineBuffer[2] = mLineBuffer[0];
                    mLineBuffer[3] = mLineBuffer[1];
                }

                trans.pointValuesToPixel(mLineBuffer);

                if (!mViewPortHandler.isInBoundsRight(mLineBuffer[0]))
                    break;

                // make sure the lines don't do shitty things outside
                // bounds
                if (!mViewPortHandler.isInBoundsLeft(mLineBuffer[2])
                        || (!mViewPortHandler.isInBoundsTop(mLineBuffer[1]) && !mViewPortHandler
                        .isInBoundsBottom(mLineBuffer[3]))
                        || (!mViewPortHandler.isInBoundsTop(mLineBuffer[1]) && !mViewPortHandler
                        .isInBoundsBottom(mLineBuffer[3])))
                    continue;

                // get the color that is set for this line-segment
                float undcoddedState = e.getVal() * 10;
                int colour = stateToColour((int)undcoddedState);
                mRenderPaint.setColor(colour);

                canvas.drawLines(mLineBuffer, 0, 2 * 2, mRenderPaint);
                mRenderPaint.setPathEffect(null);
            }
        }
        else {
            super.drawLinear(canvas, dataSet);
        }
    }

    public static final int[] STATE_COLORS = {
            Color.rgb(0,0,0), //Resting
            Color.rgb(250,164,96), //Absorb
            Color.rgb(50,205,50), //BulkMPPT
            Color.rgb(30,144,255), //Float
            Color.rgb(0,206,209), //FloatMPPT
            Color.rgb(255,20,147), //Equalize
            Color.rgb(255,0,0), //HyperVoc
            Color.rgb(238,130,238) //EqMppt
    };

    private int stateToColour(int state) {
        int rVal = Color.WHITE;
        switch (state) {
            case 0:
                rVal = STATE_COLORS[0];
                break;
            case 3:
                rVal = STATE_COLORS[1];
                break;
            case 4:
                rVal = STATE_COLORS[2];
                break;
            case 5:
                rVal = STATE_COLORS[3];
                break;
            case 6:
                rVal = STATE_COLORS[4];
                break;
            case 7:
                rVal = STATE_COLORS[5];
                break;
            case 10:
                rVal = STATE_COLORS[6];
                break;
            case 18:
                rVal = STATE_COLORS[7];
                break;
        }
        return rVal;
    }
}
