/*
 * Oburo.O est un programme destinée à saisir son temps de travail sur un support Android.
 *
 *     This file is part of Oburo.O
 *     Oburo.O is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package fr.s3i.pointeuse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.IntentCompat;
import android.view.Menu;
import android.view.MenuItem;

import fr.s3i.pointeuse.adaptaters.ListeDesPointagesDeleteAdapter;

public class Parametre extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    ListeDesPointagesDeleteAdapter adapter;
    private static final int Aide = Menu.FIRST;

    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            String theme = preferences.getString("theme", "AppThemeNoir");

            if ("AppThemeNoir".equals(theme)) {
                setTheme(R.style.AppThemeNoir);
            }
        } catch (Exception All) {
            //Toast.makeText(this, "Echec=" + All.getMessage() , Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen root = getPreferenceScreen();
        PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
        inlinePrefCat.setTitle(getString(R.string.feedback));
        root.addPreference(inlinePrefCat);

        PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
        intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse("market://details?id=fr.s3i.pointeuse")));
        intentPref.setTitle(getString(R.string.noter));
        intentPref.setSummary(getString(R.string.noter2));
        inlinePrefCat.addPreference(intentPref);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!key.equals("theme")) {
            return;
        }

        finish();
        final Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, Aide, 0, getString(R.string.help))
                .setIcon(android.R.drawable.ic_menu_help);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case Aide:
                Aide();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void Aide() {
        Intent con = new Intent(this, fr.s3i.pointeuse.activite.Aide.class);
        con.putExtra("NumAide", 2);
        this.startActivity(con);
    }

}
