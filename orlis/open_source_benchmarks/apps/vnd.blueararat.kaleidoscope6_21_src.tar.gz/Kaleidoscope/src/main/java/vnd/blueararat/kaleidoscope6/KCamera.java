package vnd.blueararat.kaleidoscope6;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class KCamera extends Activity {
	static final String KEY_COLOR_FILTER = "color_filter";

	// private static final String TAG = "KCamera";
	private FrameLayout mFrame;
	private KView mKView;
	private CameraPreview mCameraPreview;

	public CameraPreview getCameraPreview() {
		return mCameraPreview;
	}

	// private Camera mCamera;
	private int numberOfCameras;
	SharedPreferences preferences;
	// private ListView lv;
	private boolean inMenu = false;
	private ListView mLv;
	// private LinearLayout mLl;
	private FrameLayout mFl;
	private YUVProcessor mYUVProcessor;

	private GLSurfaceView mGLSurfaceView;
	private K3DRenderer mK3DRenderer;
	private boolean use3D = false;
	private View mOverlayView;

	public int getWidth() {
		return mFrame.getWidth();
	}

	// private boolean mAlpha;

	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// //super.onConfigurationChanged(newConfig);
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Memory.setReqSize(0);
		// Bitmap bitmap = Bitmap.createBitmap(640, 480,
		// Bitmap.Config.ARGB_8888);
		Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.transparent, options);
		mKView = new KView(this, bitmap);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		Integer processorIndex = preferences.getInt(KEY_COLOR_FILTER, 0);
		mYUVProcessor = YUVProcessor.YUV_PROCESSORS[processorIndex];
		mKView.setYUVProcessor(mYUVProcessor);
		// mAlpha = preferences.getBoolean(KView.KEY_BLUR, false);
		// mKView.setAlpha(mAlpha);
		CameraPreview.setEffect(mYUVProcessor.getEffect());
		// sStringUri = preferences.getString(KEY_IMAGE_URI, "");
		mCameraPreview = new CameraPreview(this, mKView);
		// mCamera = mCameraPreview.getCamera();

		// Log.v(TAG, processorIndex.toString());
		// SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		// int width = preferences.getInt("previewWidth", 0);
		// int height = preferences.getInt("previewHeight", 0);
		// mCameraPreview.setPreviewSize(640, 480);
		// mCameraPreview.setFocusMode(preferences.getString("focusMode",
		// null));

		setContentView(R.layout.main);
		mFrame = (FrameLayout) findViewById(R.id.frame);

		// ViewGroup.LayoutParams params = new
		// ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		// mCameraPreview.setLayoutParams(params);
		mFrame.addView(mCameraPreview);
		// mCameraPreview.setVisibility(View.GONE);
		mFrame.addView(mKView);
		mOverlayView = new View(this);
		mOverlayView.setBackgroundColor(Color.BLACK);

		numberOfCameras = mCameraPreview.getNumberOfCameras();
	}

	// private OrientationEventListener mOrientationEventListener;
	// private int mOrientation = -1;
	// private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
	// private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
	// private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
	// private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

	@Override
	protected void onResume() {
		super.onResume();

		// String s = preferences.getString(Kaleidoscope.KEY_IMAGE_URI, "");
		// boolean b = !s.equals(sStringUri);
		// Log.v(TAG, "123231234234234");
		// Log.v(TAG, s);
		// Log.v(TAG, b);

		int numberOfMirrors = KView.MIN_NOM
				+ preferences.getInt(KView.KEY_NUMBER_OF_MIRRORS,
						6 - KView.MIN_NOM);
		// Editor edit = preferences.edit();
		// edit.putInt(KEY_NUMBER_OF_MIRRORS, 2);
		// edit.commit();
		// Toast.makeText(this, Integer.toString(numberOfMirrors) +
		if (numberOfMirrors != mKView.getNumberOfMirrors()) {
			mKView.setNewSettings(numberOfMirrors);
			// mNumberOfMirrors = numberOfMirrors;
			// Intent intent = getIntent();
			// finish();
			// mKView = null;
			// mCameraPreview = null;
			// mFrame = null;
			// mYUVProcessor = null;
			// startActivity(intent);
			// Toast.makeText(
			// this,
			// getString(R.string.toast_preference)
			// + Integer.toString(mNumberOfMirrors),
			// Toast.LENGTH_LONG).show();
		}
		boolean blur = preferences.getBoolean(KView.KEY_BLUR, true);
		if (blur != mKView.isBlur())
			mKView.setBlur(blur);

		if (blur) {
			int blurValue = (int) (2.55 * (99.0 - preferences.getInt(
					KView.KEY_BLUR_VALUE, 49)));
			if (blurValue != mKView.getBlurValue())
				mKView.setBlurValue(blurValue);
		}

		// sAngle = (float)180/(float)mNumberOfMirrors;
		// mBitmapNewHeight =
		// (int)((double)sRadius*Math.sin(sAngle*Math.PI/180));
		// mBitmapViewHeight = (int)((float)mBitmapNewHeight*sScale);
		// sNewBitmap = Bitmap.createBitmap(sRadius, mBitmapNewHeight,
		// Bitmap.Config.ARGB_8888);
		// sViewBitmap = Bitmap.createBitmap(mBitmapViewWidth,
		// mBitmapViewHeight,
		// Bitmap.Config.ARGB_8888);
		// KaleidoscopeView.drawIntoBitmap(sViewBitmap, mBitmap, sRadius/2,
		// sRadius/2);
		// Intent intent = getIntent();
		// finish();
		// startActivity(intent);
	}

	// mCameraPreview.setCameraEffect(mYUVProcessor.getEffect());
	//
	// if (mOrientationEventListener == null) {
	// mOrientationEventListener = new OrientationEventListener(this,
	// SensorManager.SENSOR_DELAY_NORMAL) {
	//
	// @Override
	// public void onOrientationChanged(int orientation) {
	//
	// // determine our orientation based on sensor response
	// int lastOrientation = mOrientation;
	//
	// if (orientation >= 315 || orientation < 45) {
	// if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
	// mOrientation = ORIENTATION_PORTRAIT_NORMAL;
	// }
	// }
	// else if (orientation < 315 && orientation >= 225) {
	// if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
	// mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
	// }
	// }
	// else if (orientation < 225 && orientation >= 135) {
	// if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
	// mOrientation = ORIENTATION_PORTRAIT_INVERTED;
	// }
	// }
	// else { // orientation <135 && orientation > 45
	// if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
	// mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
	// }
	// }
	//
	// if (lastOrientation != mOrientation) {
	// // changeRotation(mOrientation, lastOrientation);
	// }
	// }
	// };
	// }
	// if (mOrientationEventListener.canDetectOrientation()) {
	// mOrientationEventListener.enable();
	// }

	@Override
	protected void onPause() {
		super.onPause();
		// mOrientationEventListener.disable();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.camera_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem focus = menu.findItem(R.id.focus);
		if (!mCameraPreview.canAutoFocus()) {
			focus.setVisible(false);
		} else {
			focus.setVisible(true);
		}
		if (numberOfCameras == 1) {
			menu.removeItem(R.id.switch_camera);
		}
		MenuItem k3d = menu.findItem(R.id.K3D);
		if (use3D) {
			k3d.setIcon(R.drawable.ic_menu_2d);
			k3d.setTitle("2D");
		} else {
			k3d.setIcon(R.drawable.ic_menu_3d);
			k3d.setTitle("3D");
		}
		return true;
	}

	// private boolean inMenu = false;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.switch_camera:
			// check for availability of multiple cameras
			if (numberOfCameras == 1) {
				// AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// builder.setMessage(this.getString(R.string.camera_alert))
				// .setNeutralButton("Close", null);
				// AlertDialog alert = builder.create();
				// alert.show();
				return true;
			}
			mCameraPreview.switchCamera();
			return true;
		case R.id.focus:
			mCameraPreview.autoFocus(null);
			return true;
		case R.id.take_picture:
			if (!use3D) {
				mCameraPreview.takePicture(null);
			} else {
				mK3DRenderer.setShouldExport(true);
			}

			return true;
		case R.id.color_mode:
			if (inMenu == true)
				return true;
			showEffectsMenu();
			return true;
		case R.id.settings_c:
			startActivity(new Intent(this, Prefs.class));
			return true;
		case R.id.K3D:
			toggle3D(use3D = !use3D);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// switch (item.getItemId()) {
	// case R.id.set_preview_size:
	// showPreviewSizeMenu();
	// return true;
	// case R.id.set_preview_processing_mode:
	// showEffectsMenu();
	// return true;
	// case R.id.set_focus_mode:
	// showFocusModeMenu();
	// return true;
	// case R.id.focus_now:
	// mCameraPreview.autoFocus(null);
	// return true;
	// case R.id.quit:
	// finish();
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }

	private void showEffectsMenu() {
		inMenu = true;
		mFrame.addView(getEffectsMenu());
		goIn(mFl);
		// setContentView(getEffectsMenu());
	}

	private FrameLayout getEffectsMenu() {
		if (mLv == null)
			buildEffectsMenu();
		return mFl;
	}

	// public void goIn(View view) {
	// Animation goInAnimation = AnimationUtils.loadAnimation(this,
	// android.R.anim.fade_in);
	// // Now Set your animation
	// view.startAnimation(goInAnimation);
	// // if (view.getVisibility() == View.VISIBLE) return;
	// //
	// // view.setVisibility(View.VISIBLE);
	// // Animation animation = new AlphaAnimation(0F, 1F);
	// // animation.setDuration(400);
	// // view.startAnimation(animation);
	// }

	public void goIn(View view) {
		Animation goInAnimation = AnimationUtils.loadAnimation(this,
				R.anim.translation_menu_in);
		view.startAnimation(goInAnimation);
	}

	public void goOut(View view) {
		Animation goOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.translation_menu_out);
		goOutAnimation.reset();
		view.startAnimation(goOutAnimation);
	}

	private void buildEffectsMenu() {
		ImageView iv = new ImageView(this);
		iv.setImageResource(R.drawable.shape_menu);
		// Log.i(TAG,
		// Float.toString(getResources().getDisplayMetrics().density));
		// Image img;// = DecodeRes
		final float SCALE = getResources().getDisplayMetrics().density;

		FrameLayout fl = new FrameLayout(this);
		int w = Math.round(SCALE * 200.f);
		int h = Math.round(SCALE * 170.f);
		int pd = Math.round(10.f * SCALE);

		// DisplayMetrics displaymetrics = new DisplayMetrics();
		// int dp = (int) TypedValue.applyDimension(
		// TypedValue.COMPLEX_UNIT_DIP, 200, displaymetrics );
		// Log.v(TAG, Integer.toString(dp));
		FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(w, h); // (
		// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// flp.height = 150;//iv.getHeight();
		// flp.width = 200;//iv.getWidth();
		fl.setLayoutParams(flp);
		fl.setPadding(pd, pd, 0, 0);

		// LayoutParams params = new FrameLayout.LayoutParams;
		// Changes the height and width to the specified *pixels*

		// fl.setLayoutParams(params);

		fl.addView(iv);
		// LinearLayout ll = new LinearLayout(this);
		// LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// ll.setLayoutParams(lp);
		// ll.setGravity(Gravity.RIGHT);
		// fl.addView(ll);

		// ll.setBackgroundResource(R.drawable.background);

		ListView lv = new ListView(this);

		TextView header = new TextView(this);
		header.setText(getString(R.string.color_effect));
		lv.addHeaderView(header);
		// lv.setBackgroundColor(Color.TRANSPARENT);//R.color.popup_background_color
		// ListView.LayoutParams p = new ListView.LayoutParams(
		// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// MarginLayoutParams mp = new MarginLayoutParams(p);

		lv.setPadding(pd, pd, pd, pd);
		// lv.setPadding(10, 10, 10, 10);
		// lv.set
		// lv.setLayoutParams(p);
		final int positionOffset = 1;

		YUVProcessor.YUV_PROCESSORS[0].setString(getString(R.string.normal));
		YUVProcessor.YUV_PROCESSORS[1].setString(getString(R.string.gray));
		YUVProcessor.YUV_PROCESSORS[2].setString(getString(R.string.aqua));
		YUVProcessor.YUV_PROCESSORS[3].setString(getString(R.string.negative));

		lv.setAdapter(new ArrayAdapter<YUVProcessor>(this,
				R.layout.simple_list_item_single_choice,
				YUVProcessor.YUV_PROCESSORS));
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// lv.setCacheColorHint(Color.TRANSPARENT);
		lv.setVerticalFadingEdgeEnabled(false);
		lv.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
		lv.setSelector(R.drawable.list_selector);
		lv.setItemChecked(mKView.currentYUVProcessor() + positionOffset, true);
		// lv.getAdapter()

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) // header clicked
					return;
				// TextView label = (TextView)view;
				// label.setTextColor(Color.BLUE);
				mYUVProcessor = YUVProcessor.YUV_PROCESSORS[position
						- positionOffset];
				CameraPreview.setEffect(mYUVProcessor.getEffect());
				mCameraPreview.setCameraEffect();
				mKView.setYUVProcessor(mYUVProcessor);
				// mKView.setAlpha((mYUVProcessor.getName().equals("Blur") ? 10
				// : 255));
				Editor edit = preferences.edit();
				edit.putInt(KEY_COLOR_FILTER, position - positionOffset); // String(KEY_COLOR_FILTER,
																			// y.getName());
				edit.commit();
				// exitMenu();
			}
		});
		fl.addView(lv);
		mLv = lv;
		// mLl = ll;
		// fl.setVisibility(View.GONE);
		mFl = fl;
	}

	private void exitMenu() {
		// setContentView(mFrame);
		goOut(mFl);
		mFrame.removeView(mFl);
		inMenu = false;
	}

	@Override
	public void onBackPressed() {
		if (inMenu) {
			exitMenu();
		} else {
			finish();
			startActivity(new Intent(this, Kaleidoscope.class));
		}
	}

	private void toggle3D(boolean use) {
		if (use) {
			SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
			mK3DRenderer = new K3DRenderer(this, sm);
			mGLSurfaceView = new GLSurfaceView(this);
			mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			mGLSurfaceView.setRenderer(mK3DRenderer);
			mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			mGLSurfaceView.setZOrderOnTop(true);
			FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
					mFrame.getLayoutParams());
			int margin = (mFrame.getHeight() - mFrame.getWidth()) / 2;
			flp.gravity = Gravity.CENTER;
			flp.setMargins(0, margin, 0, margin);
			mFrame.addView(mOverlayView);
			mFrame.addView(mGLSurfaceView, flp);
			mKView.setK3DMode(true, mK3DRenderer);
			mKView.updateTexture();
			mK3DRenderer.start();
		} else if (mK3DRenderer != null) {
			mKView.setK3DMode(false, null);
			mFrame.removeView(mOverlayView);
			mFrame.removeView(mGLSurfaceView);
			mK3DRenderer.stop();
			mGLSurfaceView = null;
			mK3DRenderer = null;
		}
	}
}