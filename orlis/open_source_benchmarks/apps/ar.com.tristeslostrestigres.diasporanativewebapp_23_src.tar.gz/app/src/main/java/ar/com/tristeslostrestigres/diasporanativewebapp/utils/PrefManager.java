package ar.com.tristeslostrestigres.diasporanativewebapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefManager {

    private final Context context;
	private boolean loadImages = true;
    private int minimumFontSize = 0;

    public PrefManager(Context ctx) {
		SharedPreferences sp = null;
		this.context = ctx;
		try {
			sp = PreferenceManager.getDefaultSharedPreferences(context);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (sp != null) {
			loadImages = sp.getBoolean("loadImages", true);
            minimumFontSize = sp.getInt("minimumFontSize", 8);
		}
	}
	





	public boolean getLoadImages() {
		return loadImages;
	}


	public void setLoadImages(boolean loadImages) {
		this.loadImages = loadImages;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = sp.edit();
        edit.putBoolean("loadImages", loadImages);
        edit.commit();
	}


    public int getMinimumFontSize() {
        return minimumFontSize;
    }

    public void setMinimumFontSize(int minimumFontSize) {
        this.minimumFontSize = minimumFontSize;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = sp.edit();
        edit.putInt("minimumFontSize", minimumFontSize);
        edit.commit();
    }
}
