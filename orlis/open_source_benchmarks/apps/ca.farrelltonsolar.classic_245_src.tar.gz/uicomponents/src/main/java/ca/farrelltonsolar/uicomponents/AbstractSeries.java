/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.uicomponents;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSeries {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	protected Paint mPaint = new Paint();

	protected float mScaleX = 1;
	protected float mScaleY = 1;

	private List<AbstractPoint> mPoints;
	private boolean mPointsSorted = false;

	private double mMinX = Double.MAX_VALUE;
	private double mMaxX = Double.MIN_VALUE;
	private double mMinY = Double.MAX_VALUE;
	private double mMaxY = Double.MIN_VALUE;

	private double mRangeX = 0;
	private double mRangeY = 0;

	protected abstract void drawPoint(Canvas canvas, AbstractPoint point, float scaleX, float scalY, Rect gridBounds);

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public AbstractSeries() {
		mPaint.setAntiAlias(true);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public List<AbstractPoint> getPoints() {
		Collections.sort(mPoints);
		return mPoints;
	}

	public void setPoints(List<? extends AbstractPoint> points) {
		mPoints = new ArrayList<AbstractPoint>();
		mPoints.addAll(points);

		sortPoints();
		resetRange();

		for (AbstractPoint point : mPoints) {
			extendRange(point.getX(), point.getY());
		}
	}

	public void addPoint(AbstractPoint point) {
		if (mPoints == null) {
			mPoints = new ArrayList<AbstractPoint>();
		}

		extendRange(point.getX(), point.getY());
		mPoints.add(point);

		mPointsSorted = false;
	}

	// Line properties

	public void setLineColor(int color) {
		mPaint.setColor(color);
	}

	public void setLineWidth(float width) {
		mPaint.setStrokeWidth(width);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void sortPoints() {
		if (!mPointsSorted) {
			Collections.sort(mPoints);
			mPointsSorted = true;
		}
	}

	private void resetRange() {
		mMinX = Double.MAX_VALUE;
		mMaxX = Double.MIN_VALUE;
		mMinY = Double.MAX_VALUE;
		mMaxY = Double.MIN_VALUE;

		mRangeX = 0;
		mRangeY = 0;
	}

	private void extendRange(double x, double y) {
		if (x < mMinX) {
			mMinX = x;
		}

		if (x > mMaxX) {
			mMaxX = x;
		}

		if (y < mMinY) {
			mMinY = y;
		}

		if (y > mMaxY) {
			mMaxY = y;
		}

		mRangeX = mMaxX - mMinX;
		mRangeY = mMaxY - mMinY;
	}

	double getMinX() {
		return mMinX;
	}

	double getMaxX() {
		return mMaxX;
	}

	double getMinY() {
		return mMinY;
	}

	double getMaxY() {
		return mMaxY;
	}

	double getRangeX() {
		return mRangeX;
	}

	double getRangeY() {
		return mRangeY;
	}

	void draw(Canvas canvas, Rect gridBounds, RectD valueBounds) {
		sortPoints();

		final float scaleX = (float) gridBounds.width() / (float) valueBounds.width();
		final float scaleY = (float) gridBounds.height() / (float) valueBounds.height();

		for (AbstractPoint point : mPoints) {
			drawPoint(canvas, point, scaleX, scaleY, gridBounds);
		}

		onDrawingComplete();
	}

	protected void onDrawingComplete() {
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	public static abstract class AbstractPoint implements Comparable<AbstractPoint> {
		private double mX;
		private double mY;

		public AbstractPoint() {
		}

		public AbstractPoint(double x, double y) {
			mX = x;
			mY = y;
		}

		public double getX() {
			return mX;
		}

		public double getY() {
			return mY;
		}

		public void set(double x, double y) {
			mX = x;
			mY = y;
		}

		@Override
		public int compareTo(AbstractPoint another) {
			return Double.compare(mX, another.mX);
		}
	}
}