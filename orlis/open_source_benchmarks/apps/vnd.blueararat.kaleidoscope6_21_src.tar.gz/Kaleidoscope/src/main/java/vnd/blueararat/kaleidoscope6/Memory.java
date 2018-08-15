package vnd.blueararat.kaleidoscope6;

import android.graphics.BitmapFactory.Options;
import android.os.Debug;

public class Memory {

	private static final int BYTES_PER_PIXEL_ARGB_8888 = 4;
	private static final long HEAP_PAD = 4000000;

	private static long sReqsize;

	// static final int BYTES_PER_PIXEL_RGB_565 = 2;

	public static boolean checkBitmapFitsInMemory(Options opts) {
		// int bpp = (opts.inPreferredConfig == Bitmap.Config.ARGB_8888) ? 4 :
		// 2;
		long reqsize = opts.outWidth * opts.outHeight
				* BYTES_PER_PIXEL_ARGB_8888;
		setReqSize(reqsize);
		long allocNativeHeap = Debug.getNativeHeapAllocatedSize();
		// final long heapPad = (long) (Runtime.getRuntime().maxMemory() *
		// 0.05);
		if (reqsize + allocNativeHeap + HEAP_PAD >= Runtime.getRuntime()
				.maxMemory())
			return false;
		return true;
	}

	public static boolean checkBitmapFitsInMemory(int width, int height) {
		long reqsize = width * height * BYTES_PER_PIXEL_ARGB_8888;
		long allocNativeHeap = Debug.getNativeHeapAllocatedSize();

		if (reqsize + allocNativeHeap + sReqsize >= Runtime.getRuntime()
				.maxMemory())
			return false;
		return true;
	}

	static void setReqSize(long i) {
		sReqsize = i;
	}

	// static void addReqSize (long i) {
	// sReqsize += i;
	// }

	static int factor() {
		long allocNativeHeap = Debug.getNativeHeapAllocatedSize();
		if (allocNativeHeap + 2 * sReqsize >= Runtime.getRuntime().maxMemory())
			return 2;
		return 1;
	}
}
