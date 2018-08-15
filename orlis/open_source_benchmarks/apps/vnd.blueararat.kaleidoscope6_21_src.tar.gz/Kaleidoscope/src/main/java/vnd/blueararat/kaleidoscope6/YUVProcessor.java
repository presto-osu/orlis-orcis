package vnd.blueararat.kaleidoscope6;

import vnd.blueararat.kaleidoscope6.filters.Aqua;
import vnd.blueararat.kaleidoscope6.filters.Negative;
import vnd.blueararat.kaleidoscope6.filters.RGB2gray;
import vnd.blueararat.kaleidoscope6.filters.SimplyRGB;

public abstract class YUVProcessor {
	final static YUVProcessor[] YUV_PROCESSORS = new YUVProcessor[] {
			new SimplyRGB(), new RGB2gray(), new Aqua(), new Negative() };// new
																			// Solarize(),
																			// new
																			// Sepia(),
	// new Blackboard(), new Whiteboard(), new Posterize()
	final static YUVProcessor DEFAULT = YUV_PROCESSORS[0];

	public abstract String getEffect();

	// public static YUVProcessor find(String processorName) {
	// if (processorName == null) return DEFAULT;
	//
	// for (YUVProcessor p : Arrays.asList(YUV_PROCESSORS))
	// if (p.getName().equals(processorName)) return p;
	//
	// return DEFAULT;
	// }

	public abstract void processYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height);

	@Override
	public String toString() {
		return getName();
	}

	public abstract String getName();
	
	public abstract void setString(String s);

}