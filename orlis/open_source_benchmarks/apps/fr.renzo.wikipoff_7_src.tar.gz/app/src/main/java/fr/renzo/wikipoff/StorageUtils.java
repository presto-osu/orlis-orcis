/**
 * This has been salvaged from
 * http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location#answer-19982451
 */

package fr.renzo.wikipoff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class StorageUtils {

	@SuppressWarnings("unused")
	private static final String TAG = "StorageUtils";

	public static class StorageInfo {

		public final String path;
		public final boolean readonly;
		public final boolean removable;     
		public final int number;

		StorageInfo(String path, boolean readonly, boolean removable, int number) {
			this.path = path;
			this.readonly = readonly;
			this.removable = removable;         
			this.number = number;
		}
		
		@Override
		public boolean equals(Object st) {
			return ((StorageInfo) st).path.equals(this.path);
		}

		public String getDisplayName(Context c) {
			StringBuilder res = new StringBuilder();
			if (!removable) {
				if (path.endsWith("/files")) {
					res.append(c.getString(R.string.message_internal_android_storage));
				} else {
					res.append(c.getString(R.string.message_internal_sd_card));
				}
			} else if (number > 1) {
				res.append(c.getString(R.string.message_sd_card_n,number));
			} else {
				if (path.contains("emulated")) {
					if (path.endsWith("/files")) {
						res.append(c.getString(R.string.message_internal_android_storage));
					} else {
						res.append(c.getString(R.string.message_internal_sd_card));
					}
				} else if (path.endsWith("/files")) {
					res.append(c.getString(R.string.message_external_android_storage));
				} else {
					res.append(c.getString(R.string.message_external_sd_card));
				}
			}
			if (readonly) {
				res.append(c.getString(R.string.message_read_only_sd_card));
			}
			return res.toString();
		}
	}

	@SuppressLint("NewApi") public static String getDefaultStorage(Context ctx) {	
		String path=null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			File[] array = ctx.getExternalFilesDirs(null);
			for (int i = 0; i < array.length; i++) {
                if (array[i]!=null){
                    path = array[i].getAbsolutePath();
                    if (path.contains("sdcard_ext"))
                        break;
                }
			}
		} else {
			path=ctx.getExternalFilesDir(null).getAbsolutePath();
		}
		return path;
	}

	@SuppressLint("NewApi") public static ArrayList<StorageInfo> getDefaultStorageInfo(Context ctx) {
		ArrayList<StorageInfo> list = new ArrayList<StorageInfo>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			File[] array = ctx.getExternalFilesDirs(null);
			for (int i = 0; i < array.length; i++) {
				if (array[i]==null)	continue;
				String path = array[i].getAbsolutePath();
				list.add(new StorageInfo(path, false, true, 0 - i));
			}
		} else {
			String path = ctx.getExternalFilesDir(null).getAbsolutePath();
			list.add(new StorageInfo(path, false, true, -1));
		}
		return list;
	}

	public static List<StorageInfo> getStorageList() {

		List<StorageInfo> list = new ArrayList<StorageInfo>();
		String def_path = Environment.getExternalStorageDirectory().getPath();
		boolean def_path_removable = Environment.isExternalStorageRemovable();
		String def_path_state = Environment.getExternalStorageState();
		boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
				|| def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

		HashSet<String> paths = new HashSet<String>();
		int cur_removable_number = 0;



		BufferedReader buf_reader = null;
		try {
			buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
			String line;
			// Log.d(TAG, "/proc/mounts");
			while ((line = buf_reader.readLine()) != null) {
				//    Log.d(TAG, line);
				if (line.contains("vfat") || line.contains("/mnt")) {
					StringTokenizer tokens = new StringTokenizer(line, " ");
					@SuppressWarnings("unused")
					String unused = tokens.nextToken(); //device
					String mount_point = tokens.nextToken(); //mount point
					if (paths.contains(mount_point)) {
						continue;
					}
					unused = tokens.nextToken(); //file system
					List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
					boolean readonly = flags.contains("ro");

					if (line.contains("/dev/block/vold")) {
						if (!line.contains("/mnt/secure")
								&& !line.contains("/mnt/asec")
								&& !line.contains("/mnt/obb")
								&& !line.contains("/dev/mapper")
								&& !line.contains("tmpfs")) {
							paths.add(mount_point);
							list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
						}
					}
				}
			}
			if (def_path_available) {
				paths.add(def_path);
				list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (buf_reader != null) {
				try {
					buf_reader.close();
				} catch (IOException ex) {}
			}
		}
		return list;
	}
}
