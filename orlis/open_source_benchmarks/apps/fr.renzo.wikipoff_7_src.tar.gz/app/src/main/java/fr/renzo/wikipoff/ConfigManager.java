package fr.renzo.wikipoff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class ConfigManager {

	public static String getSelectedDBFiles(Context ctx) {
		String key = ctx.getString(R.string.config_key_selecteddbfiles);
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(ctx);
		return config.getString(key, null);
	}

	public static ArrayList<String> getSelectedDBFilesAsList(Context ctx) {
		String key =ctx.getString(R.string.config_key_selecteddbfiles);
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String seldbs=null;
		try {
			// This is ugly, but prevent crashs on app upgrade
			seldbs = config.getString(key, null);
		} catch (ClassCastException e) {
			config.edit().remove(key).commit();
		}
		
		if (seldbs == null ) {
			return null;
		} else {
			return new ArrayList<String>(Arrays.asList(seldbs.split(",")));
		}
	}

	public static HashSet<String> getSelectedDBFilesAsSet(Context ctx) {
		String key =ctx.getString(R.string.config_key_selecteddbfiles);
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(ctx);
		String seldbs = config.getString(key, null);
		if (seldbs == null ) {
			return null;
		} else {
			return new HashSet<String>(Arrays.asList(seldbs.split(",")));
		}	
	}

	public static boolean isInSelectedDBs(Context ctx, Wiki wiki) {
		HashSet<String> res = getSelectedDBFilesAsSet(ctx);
		if (res == null) {
			return false;
		} else { 
			return res.containsAll(wiki.getDBFilesnamesAsList());
		}
	}


	public static void removeFromSelectedDBs(Context ctx, Wiki wiki) {
		String key = ctx.getString(R.string.config_key_selecteddbfiles);
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(ctx);

		HashSet<String> cur = getSelectedDBFilesAsSet(ctx);
		if (cur != null) {
			cur.removeAll(wiki.getDBFilesnamesAsList());

			String newseldbs = TextUtils.join("," , cur);

			if ( newseldbs.equals("") || newseldbs.equals(",")){
				config.edit().remove(key).commit();
			} else {
				config.edit().putString(key,newseldbs).commit();
			}

			config.edit().putBoolean(ctx.getString(R.string.config_key_should_update_db), true).commit();
		}
	}

	public static void addToSelectedDBs(Context ctx, Wiki wiki) {
		String key = ctx.getString(R.string.config_key_selecteddbfiles);
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(ctx);

		HashSet<String> cur = getSelectedDBFilesAsSet(ctx);
		if (cur != null) {
			cur.addAll(wiki.getDBFilesnamesAsList());
		} else {
			cur = new HashSet<String>(wiki.getDBFilesnamesAsList());
		}

		String newseldbs = TextUtils.join("," , cur);

		config.edit().putString(key,newseldbs).commit();

		config.edit().putBoolean(ctx.getString(R.string.config_key_should_update_db), true).commit();

	}

	public static void clearSelectedDBFiles(Context ctx) {
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(ctx);
		config.edit().remove(ctx.getString(R.string.config_key_selecteddbfiles)).commit();
		config.edit().putBoolean(ctx.getString(R.string.config_key_should_update_db), true).commit();
	}


}
