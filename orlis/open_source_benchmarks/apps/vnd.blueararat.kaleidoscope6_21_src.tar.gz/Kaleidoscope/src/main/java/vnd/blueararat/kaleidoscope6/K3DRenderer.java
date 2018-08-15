package vnd.blueararat.kaleidoscope6;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;

class K3DRenderer implements GLSurfaceView.Renderer, SensorEventListener {
	// private Square mSquare;
	private Sphere mSphere;
	// private Square mSquare;
	private Sensor mRotationVectorSensor;
	private final float[] mRotationMatrix = new float[16];
	private final float[] mRotationMatrix2 = new float[16];
	private Context mContext;
	//private GLSurfaceView mGLSurfaceView;
	private SensorManager mSensorManager;
	private volatile Bitmap mBitmap;
	private volatile boolean isNewBitmap;
	private volatile boolean shouldExport = false;
	private boolean isLandscape = false;
	private boolean useSensors = true;
	private float angle = 0;
	private float[] axis = new float[3];

	public void setShouldExport(boolean shouldExport) {
		this.shouldExport = shouldExport;
	}

	public K3DRenderer(Context ctx, SensorManager sm) {
		mContext = ctx;
		mSensorManager = sm;
		// find the rotation-vector sensor
		mRotationVectorSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		if (mRotationVectorSensor == null) {
			useSensors = false;
		}
		mSphere = new Sphere(ctx);
		// initialize the rotation matrix to identity

		mRotationMatrix[0] = 1;
		mRotationMatrix[4] = 1;
		mRotationMatrix[8] = 1;
		mRotationMatrix[12] = 1;

		int orientation = ctx.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isLandscape = true;
			// mSensorManager.remapCoordinateSystem(inR, X, Y, outR);
		}
	}

	public void start() {
		if (useSensors) {
			mSensorManager.registerListener(this, mRotationVectorSensor,
					SensorManager.SENSOR_DELAY_GAME);
		} else {
			axis[0] = (float) Math.random() - 1.f;
			axis[1] = (float) Math.random() - 1.f;
			axis[2] = (float) Math.random() - 1.f;

		}
	}

	public void stop() {
		// make sure to turn our sensor off when the activity is paused
		mSensorManager.unregisterListener(this);
	}

	public void onSensorChanged(SensorEvent event) {
		// we received a sensor event. it is a good practice to check
		// that we received the proper event
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			// convert the rotation-vector to a 4x4 matrix. the matrix
			// is interpreted by Open GL as the inverse of the
			// rotation-vector, which is what we want.
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					event.values);
			if (isLandscape) {
				SensorManager.remapCoordinateSystem(mRotationMatrix,
						SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
						mRotationMatrix2);
			}
		}
	}

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		if (useSensors) {
			if (isLandscape) {
				gl.glMultMatrixf(mRotationMatrix2, 0);
			} else {
				gl.glMultMatrixf(mRotationMatrix, 0);
			}
		} else {
			gl.glRotatef(angle++, axis[0], axis[1], axis[2]);
		}
		mSphere.draw(gl);

		if (shouldExport) {
			int width;
			if (mContext instanceof Kaleidoscope) {
				width = ((Kaleidoscope) mContext).getWidth();
			} else {
				width = ((KCamera) mContext).getWidth();
			}
			// int height = width;
			savePixels(0, 0, width, width, gl);
			shouldExport = false;
		}
	}

	public void savePixels(int x, int y, int w, int h, GL10 gl) {
		if (gl == null)
			return;

		int b[] = new int[w * (y + h)];
		int bt[] = new int[w * h];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		gl.glReadPixels(x, 0, w, y + h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

		for (int i = 0, k = 0; i < h; i++, k++) {// remember, that OpenGL bitmap
			// is incompatible with
			// Android bitmap
			// and so, some correction need.
			for (int j = 0; j < w; j++) {
				int pix = b[i * w + j];
				int pb = (pix >> 16) & 0xff;
				int pr = (pix << 16) & 0x00ff0000;
				int pix1 = (pix & 0xff00ff00) | pr | pb;
				bt[(h - k - 1) * w + j] = pix1;
			}
		}

		Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
		if (mContext instanceof Kaleidoscope) {
			((Kaleidoscope) mContext).new Export().execute(sb);
		} else {
			((KCamera) mContext).getCameraPreview().takePicture(sb);
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, width);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// float ratio = (float) height / width;
		gl.glOrthof(-1, 1, -1, 1, 0.1f, 100);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DITHER);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glClearColor(0, 0, 0, 0);

		gl.glEnable(GL10.GL_CULL_FACE); // glDisable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);

		// float lightPos[] = { 20.0f, -20.0f, -80.0f, 5.0f };
		// gl.glEnable(GL10.GL_COLOR_MATERIAL);
		// gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);
		// gl.glEnable(GL10.GL_LIGHTING);
		// gl.glEnable(GL10.GL_LIGHT0);

		// loading texture
		if (mBitmap == null) {
			mBitmap = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.transparent);
			isNewBitmap = true;
		}

		gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping ( NEW )
		// gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
		// gl.glClearColor(0, 0, 0, 0); // Black Background
		// gl.glClearDepthf(1.0f); //Depth Buffer Setup
		// gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
		// gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

		// Really Nice Perspective Calculations
		// gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
		isNewBitmap = true;
		mSphere.setBitmap(bitmap);
	}
}