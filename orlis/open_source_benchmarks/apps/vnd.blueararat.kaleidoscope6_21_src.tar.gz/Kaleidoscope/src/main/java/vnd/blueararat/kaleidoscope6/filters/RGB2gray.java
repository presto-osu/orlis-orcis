package vnd.blueararat.kaleidoscope6.filters;

import vnd.blueararat.kaleidoscope6.YUVProcessor;
import android.hardware.Camera;

public class RGB2gray extends YUVProcessor {
	private static String s = "";
	
	@Override
	public void processYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp += 2]) - 128;
					// u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				// int g = (y1192 - 833 * v - 400 * u);
				// int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				// if (g < 0) g = 0; else if (g > 262143) g = 262143;
				// if (b < 0) b = 0; else if (b > 262143) b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((r >> 2) & 0xff00) | ((r >> 10) & 0xff);
			}
		}
	}

	@Override
	public String getName() {
		return s;
	}

	@Override
	public String getEffect() {
		return Camera.Parameters.EFFECT_MONO;
	}
	
	@Override
	public void setString(String st) {
		s = st;
	}
}
