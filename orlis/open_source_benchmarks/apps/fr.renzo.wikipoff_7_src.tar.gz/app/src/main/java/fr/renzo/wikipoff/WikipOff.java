/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.renzo.wikipoff;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.renzo.wikipoff.Database.DatabaseException;

public class WikipOff extends Application {

	@SuppressWarnings("unused")
	private static final String TAG = "WikipOff";
	public static SharedPreferences config;
	public File DBDir;
	private Database dbHandler;

	public void onCreate(){
		super.onCreate();
		config = PreferenceManager.getDefaultSharedPreferences(this);  	
	}

	public Database getDatabaseHandler(Context context) {

		ArrayList<String> seldb = ConfigManager.getSelectedDBFilesAsList(context);
		
		if (seldb==null) {
			return null;
		} else {
			boolean new_db = config.getBoolean(s(R.string.config_key_should_update_db), false);
			if ((this.dbHandler == null ) || (new_db)){
				if (this.dbHandler!=null)
					this.dbHandler.close();
				try {
					this.dbHandler = new Database(this,seldb);
				} catch (DatabaseException e) {
					Builder b = e.alertUser(context);
					b.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {}
					});
				}
				config.edit().remove(s(R.string.config_key_should_update_db)).commit();
			}
		}
		
		return this.dbHandler;
	}

	private  String s(int i) {
		return this.getString(i);
	}
	
}
