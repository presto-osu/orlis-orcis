/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import org.pixmob.freemobile.netstat.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom component showing a multi-level Pie Chart.
 * 
 * @author gilbsgilbs
 */
public class PieChartView extends View {

    public PieChartView(Context context) {
        super(context);
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public class PieChartComponent implements Comparable<PieChartComponent> {
		private final int color1;
		private final int color2;
		private final int percent; // Relative to parent
		private final PieChartComponent parentComponent;
		private final List<PieChartComponent> childComponents = new ArrayList<>();

		/**
		 * Shaded top-level component
		 */
		public PieChartComponent(int color1, int color2, int percent) {
			this.color1 = color1;
			this.color2 = color2;
			this.percent = normalizePercent(percent);
			this.parentComponent = null;
		}

		/**
		 * Shaded child component
		 */
		public PieChartComponent(int color1, int color2, int percent, final PieChartComponent parentComponent) {
			this.color1 = color1;
			this.color2 = color2;
			this.percent = normalizePercent(percent);
			this.parentComponent = parentComponent;
			parentComponent.childComponents.add(this);
			Collections.sort(parentComponent.childComponents);
		}

		public Paint getShadedPaint() {
			Paint paint = new Paint();
	    	paint.setAntiAlias(true);
	    	paint.setStyle(Paint.Style.FILL);

	        paint.setShader(new LinearGradient(0, 0, 0, getHeight(), getResources().getColor(color1), getResources().getColor(color2), Shader.TileMode.CLAMP));

	        return paint;
		}

		public PieChartComponent getParentComponent() {
			return parentComponent;
		}

		public boolean isLeaf() {
			return childComponents.isEmpty();
		}

		public boolean isRoot() {
			return parentComponent == null;
		}

		public List<PieChartComponent> getChildren() {
			return childComponents;
		}

		public int getPercent() {
			return percent;
		}

		public double getAbsolutePercent() {
			if (isRoot())
				return percent;
			return percent * parentComponent.getAbsolutePercent() / 100.d;
		}

		@Override
		public int compareTo(PieChartComponent other) {
			return this.percent < other.percent ? -1
				   : this.percent == other.percent ? 0
				   : 1;
		}
	}

	private final List<PieChartComponent> components = new ArrayList<>();
    private final RectF circleBounds = new RectF();
    private Paint arcBorderPaint;
    private Paint unknownPaint;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (unknownPaint == null) {
            unknownPaint = new Paint();
            unknownPaint.setAntiAlias(true);
            unknownPaint.setStyle(Paint.Style.FILL);
            unknownPaint.setColor(getResources().getColor(R.color.unknown_mobile_network_color));
        }
        if (arcBorderPaint == null) {
            arcBorderPaint = new Paint();
            arcBorderPaint.setAntiAlias(true);
            arcBorderPaint.setStyle(Paint.Style.STROKE);
            arcBorderPaint.setColor(getResources().getColor(R.color.pie_border_color));
            arcBorderPaint.setStrokeWidth(2);
        }

        final int w = getWidth();
        final int h = getHeight();
        final int diameter = Math.min(w, h);
        final int startX = (w - diameter) / 2;
        final int startY = (h - diameter) / 2;
        circleBounds.set(startX, startY, startX + diameter, startY + diameter);

        drawChart(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
    
    public void addPieChartComponent(final PieChartComponent component) {
    	if (!component.isRoot())
    		throw new IllegalArgumentException("Added chart component must be root.");
    	components.add(component);
    }
    
    public void clear() {
    	components.clear();
    }

    private static float percentToAngle(double p) {
        return (float)(p * 360 / 100d);
    }

    private static int normalizePercent(int p) {
        return Math.min(100, Math.max(0, p));
    }
    
    private void drawChart(Canvas canvas) {
    	final float startAngle = getStartAngle();
    	float currentAngle = startAngle;
    	
    	for (final PieChartComponent component : components) {
    		if (component.isRoot() && component.getPercent() > 0) {
    			currentAngle += drawPieChartComponentsRecursively(canvas, component, currentAngle, 1);
    		}
    	}
        final float unknownAngle = 360 - (currentAngle - startAngle);
        if (unknownAngle > 0) {
            canvas.drawArc(circleBounds, currentAngle, unknownAngle, true, arcBorderPaint);
            canvas.drawArc(circleBounds, currentAngle, unknownAngle, true, unknownPaint);
        }
    }
    
    private float drawPieChartComponentsRecursively(Canvas canvas, PieChartComponent componentToDraw,
    		final float startAngle, final int level) { 

		float sweepAngle = 0;
		
    	if (componentToDraw.getPercent() > 0) { // Draw the component itself
    		sweepAngle = percentToAngle(componentToDraw.getAbsolutePercent());
    		final RectF newBounds = new RectF(circleBounds);
    		// Sigmoid function ; f(1) = 1 ; f(infinity) = 
    		//final float ratio = -1 / (float)(1 + Math.exp(3 - level)) + 1 + 1 / (float)(1 + Math.exp(2));
    		float ratio = -1 / (float)(1 + Math.exp(3 - level)) + 1.119202923f;
    		scaleRectF(newBounds, ratio);
    		canvas.drawArc(newBounds, startAngle, sweepAngle, true, arcBorderPaint);
    		canvas.drawArc(newBounds, startAngle, sweepAngle, true, componentToDraw.getShadedPaint());
    		
    		if (!componentToDraw.isLeaf()) { // Draw its childs recursively
    			float currentAngle = startAngle;
    			for (PieChartComponent component : componentToDraw.getChildren())
    				currentAngle += drawPieChartComponentsRecursively(canvas, component, currentAngle, level + 1);
    		}
    	}
		
		return sweepAngle;
    }

    private static void scaleRectF(RectF rect, float factor){
        float diffHorizontal = (rect.right-rect.left) * (factor-1f);
        float diffVertical = (rect.bottom-rect.top) * (factor-1f);

        rect.top -= diffVertical/2f;
        rect.bottom += diffVertical/2f;

        rect.left -= diffHorizontal/2f;
        rect.right += diffHorizontal/2f;
    }
    
	private float getStartAngle() {
		for (PieChartComponent component : components)
			if (component.getParentComponent() == null)
				return -percentToAngle(component.getAbsolutePercent()) / 2.f;
		return 0;
	}
}
