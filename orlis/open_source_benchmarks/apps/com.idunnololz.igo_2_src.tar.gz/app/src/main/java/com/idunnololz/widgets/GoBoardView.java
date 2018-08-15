package com.idunnololz.widgets;

import java.util.ArrayList;
import java.util.List;

import com.idunnololz.igo.R;
import com.idunnololz.utils.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class GoBoardView extends ViewGroup {
	private static final String TAG = GoBoardView.class.getSimpleName();

	private static final int BOARD_COLOR = 0xFFca9d30;

	private int width, height;

	private Paint bgPaint;
	private Paint fgPaint;

	private int gridSize = 9;

	private int boardPadding;

	private RectF square;

	private List<PointInfo> btns = new ArrayList<PointInfo>();

	private LayoutInflater inflater;

	private GoBoardAdapter adapter;

	private OnPointClickListener onPointClickListener;
	private OnPointLongClickListener onPointLongClickListener;

	private float scale = 1f;
	private float offX = 0f, offY = 0f;

	private int[] starPoints = new int[0];
	private float starPointRadius;

	private float touchSlop;
	
	private Rect clampBounds = new Rect();

	public GoBoardView(Context context) {
		super(context);
	}

	public GoBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GoBoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		Log.d(TAG, "onFinishInflate");

		scaleDetector = new ScaleGestureDetector(getContext(), scaleListener);

		inflater = LayoutInflater.from(getContext());

		bgPaint = new Paint();
		bgPaint.setColor(BOARD_COLOR);
		bgPaint.setStyle(Paint.Style.FILL);

		fgPaint = new Paint();
		fgPaint.setColor(Color.BLACK);
		fgPaint.setStrokeWidth(Utils.convertToPixels(getContext(), 1));
		fgPaint.setTextSize(Utils.convertToPixels(getContext(), 12));

		starPointRadius = Utils.convertToPixels(getContext(), 3);

		boardPadding = Utils.convertToPixels(getContext(), 20);
		square = new RectF();

		ViewConfiguration vc = ViewConfiguration.get(getContext());
		touchSlop = vc.getScaledTouchSlop();

		setFocusable(true);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout(" + l + "," + t + "," + r + "," + b + ")");

		for (PointInfo info : btns) {
			float startX = square.left + boardPadding;
			float startY = square.top + boardPadding;
			float sqWidth = square.width();
			float interval = (sqWidth - (boardPadding << 1)) / (gridSize - 1);

			int half = (int) (interval / 2);

			int x = info.across;
			int y = info.down;

			int ll = (int)(startX + x * interval);
			int tt = (int)(startY + y * interval);
			int rr = ll + (int)(interval);
			int bb = tt + (int)(interval);

			info.view.layout(ll - half, tt - half, rr - half, bb - half);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		width = getWidth();
		height = getHeight();
		
		clampBounds.left = width / 2;
		clampBounds.right = -width / 2;
		clampBounds.top = height / 2;
		clampBounds.bottom = -height / 2;

		if (width > height) {
			// height is limiting factor...
			int marginLR = (width - height) >> 1;
			square.left = marginLR;
			square.right = marginLR + height;
			square.top = 0;
			square.bottom = height;
		} else {
			// height is limiting factor...
			int marginTB = (height - width) >> 1;
			square.top = marginTB;
			square.bottom = marginTB + width;
			square.left = 0;
			square.right = width;
		}

		float sqWidth = square.width();
		float interval = (sqWidth - (boardPadding << 1)) / (gridSize - 1);
		int wSpec = MeasureSpec.makeMeasureSpec((int) interval, MeasureSpec.EXACTLY);
		int hSpec = MeasureSpec.makeMeasureSpec((int) interval, MeasureSpec.EXACTLY);
		for (PointInfo b : btns) {
			b.view.measure(wSpec, hSpec);
			int pad = (int) (interval * 0.06f);
			b.view.setPadding(pad, pad, pad, pad);
		}

		Log.d(TAG, "size: " + square.toString());
		Log.d(TAG, "size: " + width + "," + height);

		requestLayout();
	}

	public void setBoardSize(int size) {
		gridSize = size;

		setupButtons();
		requestLayout();
	}

	private void setupButtons() {
		removeAllViews();

		// Create an array of buttons...
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				ImageButton b = (ImageButton) inflater.inflate(R.layout.go_button, this, false);

				b.setOnClickListener(pointClickListener);
				b.setOnLongClickListener(pointLongClickListener);
				b.setId(btns.size());

				PointInfo info = new PointInfo();
				info.across = j;
				info.down = i;
				info.view = b;

				addView(b);
				btns.add(info);
			}
		}
	}

	public void setBoard(int size, int[] starPoints) {
		setBoardSize(size);
		this.starPoints = starPoints;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();
		canvas.scale(scale, scale);
		canvas.translate(offX, offY);
		
		// draw the bg
		canvas.drawRect(0, 0, width, height, bgPaint);

		float startX = square.left + boardPadding;
		float startY = square.top + boardPadding;
		float sqWidth = square.width();
		float interval = (sqWidth - (boardPadding << 1)) / (gridSize - 1);

		final Rect bounds = new Rect();
		fgPaint.getTextBounds("0", 0, 1, bounds);
		final float textHeight = bounds.height();
		final float textMargin = Utils.convertToPixels(getContext(), 5);
		final float textTopOff = bounds.height() / 2f;
		final char[] c = new char[1];
		
		for (int i = 0; i < gridSize; i++) {
			final float x = i * interval;
			canvas.drawLine(startX + x, startY, startX + x, startY + sqWidth - (boardPadding << 1), fgPaint);
			canvas.drawLine(startX, startY + x, startX + sqWidth - (boardPadding << 1), startY + x, fgPaint);
			
			fgPaint.setTextAlign(Paint.Align.RIGHT);
			canvas.drawText(String.valueOf(19 - i), startX - textMargin, startY + x + textTopOff, fgPaint);
			fgPaint.setTextAlign(Paint.Align.LEFT);
			canvas.drawText(String.valueOf(19 - i), startX + sqWidth - (boardPadding << 1) + textMargin, startY + x + textTopOff, fgPaint);
			fgPaint.setTextAlign(Paint.Align.CENTER);
			c[0] = (char) ('A' + i);
			canvas.drawText(c, 0, 1, startX + x, startY - textMargin, fgPaint);
			canvas.drawText(c, 0, 1, startX + x, startY + sqWidth - (boardPadding << 1) + textMargin + textHeight, fgPaint);
		}

		// draw star points...
		for (int i = 0; i < starPoints.length; i+=2) {
			final float x = starPoints[i] * interval;
			final float y = starPoints[i+1] * interval;
			canvas.drawCircle(startX + x, startY + y, starPointRadius, fgPaint);
		}

		super.dispatchDraw(canvas);

		canvas.restore();
	}

	public void setOnPointClickListener(OnPointClickListener listener) {
		onPointClickListener = listener;
	}
	
	public void setOnPointLongClickListener(OnPointLongClickListener listener) {
		onPointLongClickListener = listener;
	}

	private OnClickListener pointClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PointInfo info = btns.get(v.getId());
			if (onPointClickListener != null) {
				onPointClickListener.onPointClick(v, info.across, info.down);
				invalidate();
			}
		}

	};
	
	private OnLongClickListener pointLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			PointInfo info = btns.get(v.getId());
			if (onPointLongClickListener != null) {
				invalidate();
				return onPointLongClickListener.onPointLongClick(v, info.across, info.down);
			}
			return false;
		}
		
	};

	public void setAdapter(GoBoardAdapter adapter) {
		this.adapter = adapter;

		if (adapter != null) {
			adapter.setDataSetChangedObserver(dataSetObserver);

			adapter.notifyDataSetChanged();
		}
	}

	private DataSetChangedObserver dataSetObserver = new DataSetChangedObserver() {

		@Override
		public void onDataSetChanged() {
			// refresh all views...

			for (PointInfo info : btns) {
				adapter.preparePoint(info.view, info.across, info.down);
			}

			invalidate();
		}

		@Override
		public void onSingleDataChanged(int across, int down) {
			PointInfo info = btns.get(across + down * gridSize);
			adapter.preparePoint(info.view, across, down);

			invalidate();
		}

	};

	private static interface DataSetChangedObserver {
		void onDataSetChanged();
		void onSingleDataChanged(int across, int down);
	}

	public static interface OnPointClickListener {
		void onPointClick(View v, int across, int down);
	}
	
	public static interface OnPointLongClickListener {
		boolean onPointLongClick(View v, int across, int down);
	}

	private static class PointInfo {
		ImageButton view;
		int across, down;
	}

	public static abstract class GoBoardAdapter {
		private DataSetChangedObserver observer;

		private void setDataSetChangedObserver(DataSetChangedObserver observer) {
			this.observer = observer;
		}

		public void notifyDataSetChanged() {
			if (observer != null) {
				observer.onDataSetChanged();
			}
		}

		public void notifySingleDataChanged(int across, int down) {
			if (observer != null) {
				observer.onSingleDataChanged(across, down);
			}
		}

		public abstract void preparePoint(ImageButton view, int across, int down);
	}

	public int getBoardSize() {
		return gridSize;
	}

	private boolean scrollCanceled = false;
	private boolean isScrolling = false;
	private float startX, startY;
	private float lastX, lastY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		float x = ev.getX();
		float y = ev.getY();
		
		float scaledX = x / scale - offX;
		float scaledY = y / scale - offY;

		ev.setLocation(scaledX, scaledY);

		final int action = MotionEventCompat.getActionMasked(ev);

		// Always handle the case of the touch gesture being complete.
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			// Release the scroll.
			isScrolling = false;
			return false; // Do not intercept touch event, let the child handle it
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			startX = x;
			startY = y;

			lastX = x;
			lastY = y;
			scrollCanceled = false;
			isScrolling = false;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			if (isScrolling) {
				return true;
			}

			if (Math.abs(x - startX) > touchSlop || Math.abs(y - startY) > touchSlop) {
				isScrolling = true;
				return true;
			}
			break;
		}
		}

		return false;
	}

	private void clampScrollOffset() {
		if (offX > clampBounds.left ) {
			offX = clampBounds.left;
		} else if (offX - (width/scale) < -width + clampBounds.right) {
			offX = -width + (width/scale) + clampBounds.right;
		}

		if (offY > clampBounds.top) {
			offY = clampBounds.top;
		} else if (offY - (height/scale) < -height + clampBounds.bottom) {
			offY = -height + (height/scale) + clampBounds.bottom;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		scaleDetector.onTouchEvent(ev);

		float x = ev.getX();
		float y = ev.getY();

		final int action = MotionEventCompat.getActionMasked(ev);

		if (ev.getPointerCount() > 1) {
			scrollCanceled = true;
			return true;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			if (scrollCanceled) return true;
			
			if (isScrolling) {
				// We're currently scrolling, so yes, intercept the 
				// touch event!

				offX += (x - lastX) / scale;
				offY += (y - lastY) / scale;
				clampScrollOffset();
				invalidate();

				lastX = x;
				lastY = y;

				return true;
			}

			if (Math.abs(x - startX) > touchSlop || Math.abs(y - startY) > touchSlop) {
				isScrolling = true;
			}
			break;
		}

		return true;
	}

	private ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = 
			new ScaleGestureDetector.SimpleOnScaleGestureListener() {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float sf = detector.getScaleFactor();
			float oldScale = scale;
			scale *= sf;
	
			// Don't let the object get too small or too large.
			scale = Math.max(1f, Math.min(scale, 5.0f));
			
			if (scale == oldScale) return true;
	
			float x = (detector.getFocusX() / scale) * (1 - sf);
			float y = (detector.getFocusY() / scale) * (1 - sf);
			offX += x;
			offY += y;

			clampScrollOffset();

			invalidate();
			return true;
		}
	};

	private ScaleGestureDetector scaleDetector;
}
