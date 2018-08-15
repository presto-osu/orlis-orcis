package fr.renzo.wikipoff;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;

public class DownloadUtils {

	@SuppressWarnings("unused")
	private static String TAG = "DownloadUtils";


	public static HashMap<Long,String> getCurrentDownloads(Context ctx) {
		HashMap<Long,String> res = new HashMap<Long,String>();
		DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
		Query query = new DownloadManager.Query();
		if(query!=null) 
		{
			query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|
					DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
		} 
		Cursor c = dm.query(query);
		while (c.moveToNext()) {
			int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
			if (status == DownloadManager.STATUS_PAUSED || 
					status == DownloadManager.STATUS_RUNNING) {
				res.put(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)), c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)));
			}
		}
		return res;
	}

	public static boolean isInCurrentDownloads(Wiki w, Context ctx){
		Collection<String> cur = getCurrentDownloads(ctx).values();
		return cur.containsAll(w.getDBFilesnamesAsList());
	}

	public static void delete(Wiki wiki, Context ctx) {
		DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
		HashMap<Long, String> pute = getCurrentDownloads(ctx);
		for (Map.Entry<Long, String> entry : pute.entrySet()) {
			long lid = (long) entry.getKey();
			String f = (String) entry.getValue();
			if (wiki.getFilenamesAsString().contains(f)) {
				dm.remove(lid);
			}
		}

	}
}
