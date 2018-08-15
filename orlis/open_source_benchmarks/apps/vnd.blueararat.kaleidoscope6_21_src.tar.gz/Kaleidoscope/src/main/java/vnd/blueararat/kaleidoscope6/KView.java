package vnd.blueararat.kaleidoscope6;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import vnd.blueararat.kaleidoscope6.filters.SimplyRGB;

public class KView extends View implements Camera.PreviewCallback {
	// Camera.PictureCallback
	// private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG
	// | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
	// | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
	// | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

	// SharedPreferences preferences;
	// static final String KEY_NUMBER_OF_MIRRORS = "number_of_mirrors";
	// static final String KEY_IMAGE_URI = "image_uri";
	// private static final String TAG = "KView";
	// private final float SCALE = getResources().getDisplayMetrics().density;
	static final int MIN_NOM = 2;
	// static final int MIN_ALPHA_VALUE = 1;
	static final String KEY_NUMBER_OF_MIRRORS = "number_of_mirrors";
	static final String KEY_BLUR = "blur";
	static final String KEY_BLUR_VALUE = "blur_value";
	// private static final int CHANGE_NUMBER_OF_MIRRORS = 1;
	// private static final int OPEN_PICTURE = 2;
	// private static final int MIN_NOM = 2;

	private int mNumberOfMirrors;
	private int mOffset;
	private float mTopOffset = -0.4f;
	private float mAngle, mAngle2;
	// private float mLocalAngle = 0;
	private int mBitmapNewHeight, mBitmapViewHeight;
	private int mRadius, mBitmapViewWidth, mBitmapViewWidthInitial, mCenterX,
			mCenterY, mScreenRadius;
	private float mScale;
	private Bitmap mViewBitmap, mBitmap;
	// private Bitmap mScaledBitmap;
	// private static Uri imageUri;
	// private KaleidoscopeView k;
	// private String mStringUri = "";
	private int sWidth, sHeight, texX, texY, texWidth;
	private int mScaledHeight, mScaledWidth, mBitmapWidth, mBitmapHeight;
	private int mX, mY;
	// private float startX, startY;
	private float sX1, sY1, sD, sMx, sMy;
	boolean mAlpha;
	boolean mBlur;
	boolean mAlphaMark = false;
	int mBlurVal;
	private boolean msgNextTime = true;

	// private int mFormat = 0;
	// private Menu mMenu;

	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	// private final Paint mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Context mContext;
	// private Bitmap , mViewBitmap;
	// public int mNumberOfMirrors = super.mNumberOfMirrors;
	private int mCurX, mCurY;
	private static final float mStartAngle = 0;
	// private float mdX = 1;
	// private float mdY = 1;
	// private float mF;

	// private final Paint mPaint;
	// private Size previewImageSize;
	// private boolean processing;
	// private byte[] data;
	// private int[] rgb;
	// private int n = 0;
	// private String debugString = "";
	// private long lastFrame = System.currentTimeMillis();
	private int mDataLength, mPreviewWidth, mPreviewHeight;
	// private int mPictureWidth, mPictureHeight;
	private SharedPreferences preferences;

	private boolean use3D = false;
	private K3DRenderer mK3DRenderer;

	public void setK3DMode(boolean use3D, K3DRenderer mK3DRenderer) {
		this.use3D = use3D;
		this.mK3DRenderer = mK3DRenderer;
	}

	public KView(Context context) {
		this(context, BitmapFactory.decodeResource(context.getResources(),
				R.drawable.transparent));
	}

	public KView(Context context, Bitmap bitmap) {
		super(context);
		setFocusable(true);
		mContext = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		mNumberOfMirrors = MIN_NOM
				+ preferences.getInt(KEY_NUMBER_OF_MIRRORS, 6 - MIN_NOM);
		mAngle = (float) 180 / (float) mNumberOfMirrors;
		mAngle2 = mAngle * 2;
		mOffset = calculateOffset(mNumberOfMirrors);

		mBlur = preferences.getBoolean(KEY_BLUR, true);
		if (mBlur) {
			mBlurVal = (int) (2.55 * (99.0 - preferences.getInt(KEY_BLUR_VALUE,
					49)));
		} else {
			mBlurVal = -1;
		}
		mBitmap = bitmap;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		sHeight = getHeight();
		sWidth = getWidth();
		if (sHeight == 0 || sWidth == 0) {
			android.util.DisplayMetrics mMetrics = new android.util.DisplayMetrics();
			android.view.WindowManager wm = (android.view.WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(mMetrics);
			sHeight = mMetrics.heightPixels;
			sWidth = mMetrics.widthPixels;
		}

		if (sHeight < sWidth) {
			texY = 0;
			texX = (sWidth - sHeight) / 2;
			;
			texWidth = sHeight;
		} else {
			texY = (sHeight - sWidth) / 2;
			texX = 0;
			texWidth = sWidth;
		}

		mBitmapViewWidth = (int) (sWidth / 2);
		mCenterX = mBitmapViewWidth;
		mCenterY = sHeight / 2;
		mScreenRadius = Math.min(mBitmapViewWidth, sHeight / 2);
		// mBitmap.getHeight()));
		setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		setBitmap();
		drawIntoBitmap();
	}

	// private Bitmap rotatedBitmap(float angle, Bitmap bm) {
	// Matrix matrix = new Matrix();
	// float px = bm.getWidth();
	// float py = bm.getHeight();
	// // matrix.postRotate(mLocalAngle, mBitmapViewWidth, mBitmapViewWidth);
	// matrix.postRotate(angle, px / 2, py / 2);
	// Bitmap bmr = Bitmap.createBitmap(bm, 0, 0, (int) px, (int) py,
	// matrix, true);
	// return bmr;
	// }

	private void drawIntoBitmap() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		if (mAlpha) {
			if (!mBlur) {
				mViewBitmap.eraseColor(Color.TRANSPARENT);
			}
		}
		if (mBlur) {
			if (mAlphaMark) {
				p.setAlpha(mBlurVal);
			} else {
				mAlphaMark = true;
			}
		}
		Canvas c = new Canvas(mViewBitmap);
		c.save();
		Path path = new Path();
		path.moveTo(0, 0);
		path.arcTo(new RectF(-mBitmapViewWidth, -mBitmapViewWidth,
				mBitmapViewWidth, mBitmapViewWidth), 0, mAngle);
		// path.lineTo(0, 1);
		path.close();
		c.clipPath(path);
		// c.drawRGB(0, 0, 0);
		// RectF r = new RectF(0, 0, mBitmapViewWidth, mBitmapViewHeight);
		c.drawBitmap(mBitmap, new Rect(mCurX, mCurY, mRadius + mCurX,
				mBitmapNewHeight + mCurY), new RectF(0, 0, mBitmapViewWidth,
				mBitmapViewHeight), p);
	}

	private void drawIntoBitmap(int i) {
		Canvas c = new Canvas(mViewBitmap);
		// Paint p = new Paint();
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		if (mBlur) {
			if (mAlphaMark) {
				p.setAlpha(mBlurVal);
			} else {
				mAlphaMark = true;
			}
		}
		// p.setAntiAlias(true);
		c.save();
		Path path = new Path();
		path.moveTo(0, 0);
		path.arcTo(new RectF(-mBitmapViewWidth, -mBitmapViewWidth,
				mBitmapViewWidth, mBitmapViewWidth), 0, mAngle);
		// path.lineTo(0, 1);
		path.close();
		c.clipPath(path);
		// RectF r = new RectF(0, 0, mBitmapViewWidth, mBitmapViewHeight);
		c.drawBitmap(mBitmap, new Rect(mCurX, mCurY, mRadius + mCurX,
				mBitmapNewHeight + mCurY), new RectF(0, 0, mBitmapViewWidth,
				mBitmapViewHeight), p);
		// mPaint.setTextSize(28);
		// mPaint.setColor(Color.RED);
		// c.drawText(debugString, 150, 50, mPaint);
	}

	private void drawIntoBitmap(Bitmap bm, Bitmap initialBitmap, int posX,
			int posY) {
		int x = bm.getWidth();
		int y = bm.getHeight();
		Canvas c = new Canvas(bm);

		c.save();
		Path path = new Path();
		path.moveTo(0, 0);
		path.arcTo(new RectF(-x, -x, x, x), 0, mAngle);
		// path.lineTo(0, 1);
		path.close();
		c.clipPath(path);
		// RectF r = new RectF(0, 0, x, y);
		c.drawBitmap(initialBitmap, new Rect(posX, posY, mRadius + posX,
				mBitmapNewHeight + posY), new RectF(0, 0, x, y), mPaint);
	}

	String exportImage(Bitmap bmp) {

		Bitmap.Config g;
		Bitmap.CompressFormat cf;
		String ext = preferences.getString("format",
				mContext.getString(R.string.default_save_format));
		int format = (ext.equals("JPEG")) ? 1 : 0;
		ext = (format == 1) ? ".jpg" : ".png";
		int q;
		if (format == 0) {
			g = Bitmap.Config.ARGB_8888;
			cf = Bitmap.CompressFormat.PNG;
			q = 100;
		} else {
			g = Bitmap.Config.RGB_565;
			cf = Bitmap.CompressFormat.JPEG;
			q = 50 + preferences.getInt("jpeg_quality", 40);
		}
		Bitmap exportBitmap = null;
		if (bmp == null) {
			int x, rad, factor;
			Bitmap newBitmap = mViewBitmap;
			if (!mBlur) {
				factor = Memory.factor();
				newBitmap = Bitmap.createBitmap(mRadius / factor,
						mBitmapNewHeight / factor, Bitmap.Config.ARGB_8888);
				// Bitmap bm = rotatedBitmap(mLocalAngle, mBitmap);
				drawIntoBitmap(newBitmap, mBitmap, mCurX, mCurY);
				x = mRadius * 2 / factor;
				rad = mRadius / factor;
			} else {
				x = mBitmapViewWidth * 2;
				rad = mBitmapViewWidth;
			}
			exportBitmap = Bitmap.createBitmap(x, x, g);

			Canvas c = new Canvas(exportBitmap);
			paint(c, newBitmap, rad);
			if (!mBlur)
				newBitmap.recycle();
			System.gc();
		} else {
			exportBitmap = bmp;
		}

		String path = preferences.getString(Prefs.KEY_FOLDER, "");
		if (path.equals("")) {
			path = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES).toString();
		}
		File directory = new File(path, "Kaleidoscope");
		directory.mkdirs();
		// File directory = new File(path);

		// MediaStore.Images.Media.insertImage(null, mBitmap2, null, null);
		SimpleDateFormat s = new SimpleDateFormat("MMddmmss");
		String stamp = s.format(new Date());

		File file = new File(directory,
				mContext.getString(R.string.png_save_name) + stamp + ext);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		exportBitmap.compress(cf, q, stream);
		byte[] byteArray = stream.toByteArray();
		stream = null;
		exportBitmap.recycle();
		System.gc();
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(byteArray);
		} catch (Exception e) {
			// Log.e(TAG, "Failed to write image", e);
		} finally {
			try {
				out.close();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}

		byteArray = null;
		System.gc();
		String toast1;
		if (file.exists()) {
			toast1 = mContext.getString(R.string.picture_saved_to) + " "
					+ file.toString();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				Intent mediaScanIntent = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri contentUri = Uri.fromFile(file);
				mediaScanIntent.setData(contentUri);
				mContext.sendBroadcast(mediaScanIntent);
			} else {
				mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));
			}
		} else {
			toast1 = mContext.getString(R.string.cant_save_picture_to) + " "
					+ directory.toString()
					+ mContext.getString(R.string.please_change_folder);
		}
		return toast1;
	}

	void toastString(String s, int duration) {
		Toast.makeText(mContext, s, duration).show();
	}

	public void setViewBitmapSizes(int width) {
		int diam = 2 * width;
		if (Memory.checkBitmapFitsInMemory(diam, diam)) {
			mBitmapViewWidth = width;
			mBitmapViewHeight = (int) Math.round((double) width
					* Math.sin(Math.PI / (double) mNumberOfMirrors));
			mScale = (float) mBitmapViewWidth / mRadius;
			mScaledHeight = (int) (mScale * mBitmapHeight);
			mScaledWidth = (int) (mScale * mBitmapWidth);
			mAlphaMark = false;
			mViewBitmap = Bitmap.createBitmap(mBitmapViewWidth,
					mBitmapViewHeight, Bitmap.Config.ARGB_8888);
			mX = mScaledWidth - mBitmapViewWidth;
			mY = mScaledHeight - mBitmapViewHeight;
			msgNextTime = true;
		} else if (msgNextTime) {
			toastString(mContext.getString(R.string.cant_zoom_in),
					Toast.LENGTH_SHORT);
			msgNextTime = false;
		}
	}

	public void setBitmap() {
		mBitmapWidth = mBitmap.getWidth();
		mBitmapHeight = mBitmap.getHeight();
		mRadius = (int) (mBitmapWidth / 2);
		mBitmapNewHeight = (int) Math.round((double) mRadius
				* Math.sin(Math.PI / (double) mNumberOfMirrors));
		if (mBitmapNewHeight > mBitmapHeight) {
			mBitmapNewHeight = mBitmapHeight / 2;
			mRadius = (int) Math.round((double) mBitmapNewHeight
					/ Math.sin(Math.PI / (double) mNumberOfMirrors));
		}
		mScale = (float) mBitmapViewWidth / mRadius;
		// Log.i(TAG,String.format("%d", mBitmapViewWidth));
		mBitmapViewHeight = Math.round((float) mBitmapNewHeight * mScale);
		mScaledHeight = (int) (mScale * mBitmapHeight);
		mScaledWidth = (int) (mScale * mBitmapWidth);
		// Log.i(TAG,String.format("%d %d", mRadius, mBitmapNewHeight));
		mAlphaMark = false;
		mViewBitmap = Bitmap.createBitmap(mBitmapViewWidth, mBitmapViewHeight,
				Bitmap.Config.ARGB_8888);
		mX = mScaledWidth - mBitmapViewWidth;
		mY = mScaledHeight - mBitmapViewHeight;
		mCurX = (int) (Math.random() * mX / mScale);
		mCurY = (int) (Math.random() * mY / mScale);
		mAlpha = mBitmap.hasAlpha();
		// Toast.makeText(mContext, Boolean.toString(mAlpha),
		// Toast.LENGTH_SHORT)
		// .show();
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	// public void setFormat(int format) {
	// mFormat = format;
	// }

	public void setBlur(boolean blur) {
		mBlur = blur;
		mAlphaMark = false;
	}

	public void setBlurValue(int blurValue) {
		mBlurVal = blurValue;
		mAlphaMark = false;
	}

	public int getNumberOfMirrors() {
		return mNumberOfMirrors;
	}

	public boolean isBlur() {
		return mBlur;
	}

	public int getBlurValue() {
		return mBlurVal;
	}

	private synchronized void paint(Canvas canvas, Bitmap bitmap) {
		// Paint p = new Paint();
		// p.setAntiAlias(true);
		// canvas.drawColor(Color.BLACK);
		// canvas.drawRGB(0, 0, 0);
		// canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		// mPaint.setTextSize(28);
		// Paint paint1 = new Paint();
		// paint1.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		// canvas.drawPaint(paint1);
		// paint1.setXfermode(new PorterDuffXfermode(Mode.SRC));
		// start your own drawing
		// canvas.save();
		//
		// // mPaint.setStyle(Paint.Style.FILL);
		// // // mPaint.setStrokeWidth(4);
		// // mPaint.setColor(Color.BLACK);
		// // canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
		// // mPaint);
		// canvas.drawRGB(255, 0, 0);
		// canvas.restore();
		float angle = mStartAngle;
		for (int i = 0; i < mNumberOfMirrors; i++) {
			canvas.save();// LayerAlpha(0, 0, mBitmapViewWidth,
							// mBitmapViewWidth,
							// 0xFF, LAYER_FLAGS);
			canvas.translate(mCenterX, mCenterY);
			canvas.rotate(angle);
			canvas.drawBitmap(bitmap, mOffset, mTopOffset, mPaint);
			// p.setColor(Color.RED);
			// canvas.drawText(Integer.toString(i), 100, 30, p);
			// canvas.drawArc(new RectF(-200, -200, 200, 200), 0, mAngle,
			// false, p);
			canvas.scale(1, -1);
			canvas.drawBitmap(bitmap, mOffset, mTopOffset, mPaint);
			// p.setColor(Color.GREEN);
			// canvas.drawText(Integer.toString(i), 100, 30, p);
			// canvas.drawArc(new RectF(-150, -150, 150, 150), 0, mAngle,
			// false, p);
			canvas.restore();
			angle += mAngle2;
		}
	}

	private synchronized void paint(Canvas canvas, Bitmap bitmap, int radius) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		float angle = mStartAngle;
		for (int i = 0; i < mNumberOfMirrors; i++) {
			canvas.save();
			canvas.translate(radius, radius);
			canvas.rotate(angle);
			canvas.drawBitmap(bitmap, mOffset, mTopOffset, mPaint);
			canvas.scale(1, -1);
			canvas.drawBitmap(bitmap, mOffset, mTopOffset, mPaint);
			canvas.restore();
			angle += mAngle2;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// setDrawingCacheEnabled(false);
		paint(canvas, mViewBitmap);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		int P = event.getPointerCount();
		// int N = event.getHistorySize();
		if (action != MotionEvent.ACTION_MOVE) {
			if (action == MotionEvent.ACTION_DOWN) {
				// startX = mCurX * mScale; // event.getX();
				// startY = mCurY * mScale; // event.getY();
				sX1 = mCurX * mScale - event.getX(0);
				sY1 = mCurY * mScale - event.getY(0);
				return true;
			}
			if (action == MotionEvent.ACTION_POINTER_DOWN) {
				// Toast.makeText(mContext,
				// Float.toString(sX1)+"apd",
				// Toast.LENGTH_SHORT).show();

				if (P == 2) {
					float sX2 = event.getX(0) - event.getX(1);
					float sY2 = event.getY(0) - event.getY(1);
					sD = (float) Math.sqrt(sX2 * sX2 + sY2 * sY2);
					mBitmapViewWidthInitial = mBitmapViewWidth;
					return true;
				} else if (P == 3) {
					sMx = (float) mCenterX - event.getX(2);
					sMy = (float) mCenterY - event.getY(2);
					return true;
				}
				return false;
			}
		} else {
			if (P == 1) {
				float a = Math.abs(sX1 + event.getX()) % mX;
				float b = Math.abs(sY1 + event.getY()) % mY;
				mCurX = (int) (a / mScale);// *(int)(event.getHistoricalX(0)-event.getHistoricalX(1));
				mCurY = (int) (b / mScale);// *(int)(event.getHistoricalY(0)-event.getHistoricalY(1));
			} else if (P == 2) {
				float sX2 = event.getX(0) - event.getX(1);
				float sY2 = event.getY(0) - event.getY(1);
				float sD2 = (float) Math.sqrt(sX2 * sX2 + sY2 * sY2);

				int r = mBitmapViewWidthInitial + Math.round(sD2 - sD);
				if (r < mScreenRadius)
					r = mScreenRadius;
				setViewBitmapSizes(r);
			} else if (P == 3) {
				mCenterX = (int) (sMx + event.getX(2));
				mCenterY = (int) (sMy + event.getY(2));
			} else {
				return false;
			}

			drawIntoBitmap();
			invalidate();
			if (use3D) {
				updateTexture();
			}
		}
		return true;
	}

	public void updateTexture() {
		setDrawingCacheEnabled(true);

		buildDrawingCache(true);
		try {
			mK3DRenderer.setBitmap(Bitmap.createBitmap(getDrawingCache(), texX,
					texY, texWidth, texWidth));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDrawingCacheEnabled(false);
	}

	public void resetSizes(int width, int height) {
		// mPictureWidth = pictureWidth;
		// mPictureHeight = pictureHeight;
		mDataLength = width * height;
		mPreviewWidth = width;
		mPreviewHeight = height;
		mBitmapWidth = width;
		mBitmapHeight = height;
		mRadius = (int) (mBitmapWidth / 2);
		mBitmapNewHeight = (int) ((double) mRadius * Math.sin(Math.PI
				/ (double) mNumberOfMirrors));
		if (mBitmapNewHeight > mBitmapHeight) {
			mBitmapNewHeight = mBitmapHeight / 2;
			mRadius = (int) Math.round((double) mBitmapNewHeight
					/ Math.sin(Math.PI / (double) mNumberOfMirrors));
		}
		mScale = (float) mBitmapViewWidth / mRadius;
		mScaledWidth = (int) (mScale * mBitmapWidth);
		mScaledHeight = (int) (mScale * mBitmapHeight);
		mX = mScaledWidth - mBitmapViewWidth;
		mY = mScaledHeight - mBitmapViewHeight;
		mCurX = (int) (Math.random() * mX / mScale);
		mCurY = (int) (Math.random() * mY / mScale);
	}

	private YUVProcessor mYUVProcessor = new SimplyRGB();

	void setYUVProcessor(YUVProcessor yuvProcessor) {
		mYUVProcessor = yuvProcessor;
	}

	YUVProcessor getYUVProcessor() {
		return mYUVProcessor;
	}

	int currentYUVProcessor() {
		for (int i = 0; i < YUVProcessor.YUV_PROCESSORS.length; i++) {
			YUVProcessor yp = YUVProcessor.YUV_PROCESSORS[i];
			if (yp == mYUVProcessor)
				return i;
		}
		throw new Error("");
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		int[] rgb = new int[mDataLength];
		mYUVProcessor.processYUV420SP(rgb, data, mPreviewWidth, mPreviewHeight);
		Bitmap bmp = Bitmap.createBitmap(rgb, mPreviewWidth, mPreviewHeight,
				Bitmap.Config.ARGB_8888);
		setBitmap(bmp);
		// n++;
		// if (n == 10) {
		// debugString = Double
		// .toString(10000 / (System.currentTimeMillis() - lastFrame))
		// + " FPS";
		// lastFrame = System.currentTimeMillis();
		// n = 0;
		// }
		drawIntoBitmap(0);
		invalidate();
		if (use3D) {
			updateTexture();
		}
		camera.addCallbackBuffer(data);
	}

	public void setNewSettings(int number_of_mirrors) {
		mNumberOfMirrors = number_of_mirrors;
		mAngle = (float) 180 / (float) mNumberOfMirrors;
		mAngle2 = mAngle * 2;
		mOffset = calculateOffset(mNumberOfMirrors);
		// double d = Math.sin(Math.PI / (double) mNumberOfMirrors);
		mBitmapNewHeight = (int) Math.round((double) mRadius
				* Math.sin(Math.PI / (double) mNumberOfMirrors));
		mBitmapViewHeight = Math.round((float) mBitmapNewHeight * mScale);
		mAlphaMark = false;
		mViewBitmap = Bitmap.createBitmap(mBitmapViewWidth, mBitmapViewHeight,
				Bitmap.Config.ARGB_8888);
		mY = mScaledHeight - mBitmapViewHeight;
		mCurY = (int) (Math.random() * mY / mScale);
	}

	private static int calculateOffset(int nom) {
		int offset = (int) (-1.0 / Math.tan(Math.PI / (double) nom));
		if (nom == 3 || nom == 5 || nom == 9 || nom == 14 || nom == 15
				|| nom == 19 || nom == 20 || nom == 21 || nom == 22
				|| nom == 24) {
			offset = offset - 1;
		} else if (nom == 25) {
			offset = offset - 2;
		}
		return offset;
	}

	void destroy() {
		if (mViewBitmap != null)
			mViewBitmap.recycle();
		if (mBitmap != null)
			mBitmap.recycle();
		if (mK3DRenderer != null) {
			mK3DRenderer.stop();
			mK3DRenderer = null;
		}
		try {
			System.gc();
			System.gc();
		} catch (NullPointerException e) {
		}
	}

	// public void reset(Size previewImageSize) {
	// processing = false;
	// rgb = new int[0];
	// data = new byte[0];
	// this.previewImageSize = previewImageSize;
	// }
}