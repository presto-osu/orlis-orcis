package ca.farrelltonsolar.classic;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.renderer.LineChartRenderer;

/**
 * Created by Me on 5/14/2016.
 */
public class CustomLineChart extends BarLineChartBase<LineData> implements LineDataProvider {

    public CustomLineChart(Context context) {
        super(context);
    }

    public CustomLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new CustomLineChartRenderer(this, mAnimator, mViewPortHandler);
    }

    @Override
    protected void calcMinMax() {
        super.calcMinMax();

        if (mXAxis.mAxisRange == 0 && mData.getYValCount() > 0)
            mXAxis.mAxisRange = 1;
    }

    @Override
    public LineData getLineData() {
        return mData;
    }

    @Override
    protected void onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if(mRenderer != null && mRenderer instanceof CustomLineChartRenderer) {
            ((LineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }
}
