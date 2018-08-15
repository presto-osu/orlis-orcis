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
import android.graphics.PointF;
import android.graphics.Rect;

public class LinearSeries extends AbstractSeries {
	private PointF mLastPoint;

	@Override
	public void drawPoint(Canvas canvas, AbstractPoint point, float scaleX, float scaleY, Rect gridBounds) {
		final float x = (float) (gridBounds.left + (scaleX * (point.getX() - getMinX())));
		final float y = (float) (gridBounds.bottom - (scaleY * (point.getY() - getMinY())));

		if (mLastPoint != null) {
			canvas.drawLine(mLastPoint.x, mLastPoint.y, x, y, mPaint);
		}
		else {
			mLastPoint = new PointF();
		}

		mLastPoint.set(x, y);
	}

	@Override
	protected void onDrawingComplete() {
		mLastPoint = null;
	}

	public static class LinearPoint extends AbstractPoint {
		public LinearPoint() {
			super();
		}

		public LinearPoint(double x, double y) {
			super(x, y);
		}
	}
}