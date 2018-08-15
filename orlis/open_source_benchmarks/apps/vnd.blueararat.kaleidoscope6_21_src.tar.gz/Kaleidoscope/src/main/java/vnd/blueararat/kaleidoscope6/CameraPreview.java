package vnd.blueararat.kaleidoscope6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private final SurfaceHolder mHolder;
	private Camera mCamera;
	private KView mKView;
	private int previewWidth, previewHeight;
	Camera.Parameters mParameters;
	private static String sCameraEffect = Camera.Parameters.EFFECT_NONE;
	private int numberOfCameras;
	int cameraCurrentlyLocked = -1;
	int defaultCameraId;
	private int bufsize;
	private boolean sdk11plus = true;

	CameraPreview(Context context, KView kaleidoscopeView) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1) {
			sdk11plus = false;
			try {
				mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
			} catch (Exception e) {
				mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}
		}
		// mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// mContext = context;
		numberOfCameras = Camera.getNumberOfCameras();

		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}

		mKView = kaleidoscopeView;
	}

	@SuppressLint("NewApi")
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (cameraCurrentlyLocked == -1) {
			cameraCurrentlyLocked = defaultCameraId;
		}
		mCamera = Camera.open(cameraCurrentlyLocked);
		if (sdk11plus) {
			try {
				android.graphics.SurfaceTexture st = new android.graphics.SurfaceTexture(
						0);
				mCamera.setPreviewTexture(st);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mParameters = mCamera.getParameters();
		supportedPreviewSizes = getSupportedPreviewSizes();
		guessPreviewSize(0);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.setPreviewCallbackWithBuffer(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	void setPreviewSize(Size previewSize) {
		setPreviewSize(previewSize.width, previewSize.height);
	}

	void setPreviewSize(int width, int height) {
		previewWidth = width;
		previewHeight = height;
	}

	int getPreviewWidth() {
		return previewWidth;
	}

	int getPreviewHeight() {
		return previewHeight;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		mCamera.setPreviewCallbackWithBuffer(null);
		mCamera.stopPreview();

		int j = 0;
		try {
			while (j != -1) {

				mKView.resetSizes(previewWidth, previewHeight);
				mParameters.setPreviewSize(previewWidth, previewHeight);

				mParameters.setColorEffect(sCameraEffect);
				mCamera.setParameters(mParameters);

				mCamera.setPreviewCallbackWithBuffer(mKView);
				mCamera.startPreview();
				j = -1;
			}
		} catch (RuntimeException e) {
			j = guessPreviewSize(j);
		}

		int imgformat = mParameters.getPreviewFormat();
		int bitsperpixel = ImageFormat.getBitsPerPixel(imgformat);
		bufsize = (previewWidth * previewHeight * bitsperpixel) / 8 + 1; // +1
		mCamera.addCallbackBuffer(new byte[bufsize]);
	}

	@SuppressLint("NewApi")
	public void switchCamera() {
		if (numberOfCameras == 1)
			return;
		if (mCamera != null) {
			mCamera.setPreviewCallbackWithBuffer(null);
			mCamera.stopPreview();
			// mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
		if (sdk11plus) {
			try {
				android.graphics.SurfaceTexture st = new android.graphics.SurfaceTexture(
						0);
				mCamera.setPreviewTexture(st);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
		mParameters = mCamera.getParameters();
		supportedPreviewSizes = getSupportedPreviewSizes();
		int j = 0;
		while (j != -1) {
			try {
				j = guessPreviewSize(j);
				mParameters.setPreviewSize(previewWidth, previewHeight);
				// requestLayout();
				mKView.resetSizes(previewWidth, previewHeight);
				mCamera.setParameters(mParameters);
				// mPreview.switchCamera(mCamera);
				mCamera.setPreviewCallbackWithBuffer(mKView);

				int imgformat = mParameters.getPreviewFormat();
				int bitsperpixel = ImageFormat.getBitsPerPixel(imgformat);

				mCamera.addCallbackBuffer(new byte[(previewWidth
						* previewHeight * bitsperpixel) / 8 + 1]);
				mCamera.startPreview();
				j = -1;
			} catch (RuntimeException e) {
			}
		}

	}

	boolean canAutoFocus() {
		String focusMode = mParameters.getFocusMode();
		if (focusMode != null) {
			return focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)
					|| focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO);
		}
		return false;
	}

	public void setCameraEffect() {
		mParameters.setColorEffect(sCameraEffect);
		mCamera.setParameters(mParameters);
	}

	public static void setEffect(String string) {
		sCameraEffect = string;
	}

	private List<Size> supportedPreviewSizes;

	List<Size> getSupportedPreviewSizes() {
		List<Size> rawSupportedSizes = mParameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			return null;
		}

		// sort descending
		List<Size> supportedPreviewSizes = new ArrayList<Size>(
				rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Size a, Size b) {
				int sa = a.height * a.width;
				int sb = b.height * b.width;
				if (sb < sa) {
					return -1;
				}
				if (sb > sa) {
					return 1;
				}
				return 0;
			}
		});
		return supportedPreviewSizes;
	}

	private int guessPreviewSize(int i) {
		Size s = null;
		if (supportedPreviewSizes == null) {
			s = mParameters.getPreviewSize();
			previewWidth = s.width;
			previewHeight = s.height;
			return -1;
		}
		try {
			s = supportedPreviewSizes.get(i);
		} catch (IndexOutOfBoundsException e) {
			return -1;
		}
		previewWidth = s.width;
		previewHeight = s.height;
		return i + 1;
	}

	// private void guessPictureSize() {
	// supportedPictureSizes = mParameters.getSupportedPictureSizes();
	// int w = previewWidth;
	// int h = previewHeight;
	// for (Size sps : supportedPictureSizes) {
	// Log.i(TAG, String.format("%d===%d", sps.width, sps.height));
	// if (sps.width >= previewWidth) {
	// if (sps.height >= previewHeight) {
	// w = sps.width;
	// h = sps.height;
	// //break;
	// }
	// }
	// }
	// pictureWidth = w;
	// pictureHeight = h;
	// Log.i(TAG, String.format("%d+++%d", w, h));
	// }

	// private boolean validFocusMode(String mode) {
	// for (String m : supportedFocusModes)
	// if (m.equals(mode)) return true;
	// return false;
	// }

	void autoFocus(AutoFocusCallback cb) {
		mCamera.autoFocus(cb);
	}

	public int getNumberOfCameras() {
		return numberOfCameras;
	}

	public void takePicture(Bitmap bmp) {
		(new AsyncTask<Bitmap, Void, String>() {

			@Override
			protected String doInBackground(Bitmap... params) {
				if (params.length == 0)
					return mKView.exportImage(null);
				return mKView.exportImage(params[0]);
			}

			@Override
			protected void onPostExecute(String result) {
				mKView.toastString(result, 1);
			}
		}).execute(bmp);
	}
}
