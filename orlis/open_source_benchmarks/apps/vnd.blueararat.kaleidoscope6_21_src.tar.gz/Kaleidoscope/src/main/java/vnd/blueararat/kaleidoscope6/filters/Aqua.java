package vnd.blueararat.kaleidoscope6.filters;

import vnd.blueararat.kaleidoscope6.YUVProcessor;
import android.hardware.Camera;

public class Aqua extends YUVProcessor {
	private static String s = "";

	@Override
	public void processYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		Normal.processYUV420SP(rgb, yuv420sp, width, height);
	}

	@Override
	public String getName() {
		return s;
	}

	@Override
	public String getEffect() {
		return Camera.Parameters.EFFECT_AQUA;
	}

	@Override
	public void setString(String st) {
		s = st;
	}
}
