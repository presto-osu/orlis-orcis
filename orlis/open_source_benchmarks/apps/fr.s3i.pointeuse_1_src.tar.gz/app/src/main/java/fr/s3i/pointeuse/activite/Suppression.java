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

package fr.s3i.pointeuse.activite;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import fr.s3i.pointeuse.R;
import fr.s3i.pointeuse.adaptaters.ListeDesPointagesDeleteAdapter;
import fr.s3i.pointeuse.persistance.DatabaseHelper;

public class Suppression extends ActionBarActivity {
    ListeDesPointagesDeleteAdapter adapter;
    ListView MaListe;
    SQLiteDatabase db;
    private Cursor constantsCursor = null;
    private DatabaseHelper dbHelper = null;
    DatePicker dp;
    RadioGroup rg;

    private static final int SUPPRESSION = Menu.FIRST;

    Button btnDelete;

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
        setContentView(R.layout.suppression);

        dp = (DatePicker) this.findViewById(R.id.calendrierD);
        dp.init(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), new OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {

                refresh();

            }
        });

        rg = (RadioGroup) this.findViewById(R.id.radiogroupD);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                refresh();

            }
        });

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        MaListe = (ListView) findViewById(R.id.MaListeViewDelete);
        refresh();
    }

    void refresh() {
        int Id = rg.getCheckedRadioButtonId();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String conditions;

        int jour, mois, annee;
        jour = dp.getDayOfMonth();
        mois = dp.getMonth();//Commence a 0
        annee = dp.getYear();

        // converting the datestring from the picker to a long:
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, jour);
        c.set(Calendar.MONTH, mois);
        c.set(Calendar.YEAR, annee);

        Date LaDate = c.getTime();

        if (Id == R.id.trijourD) {
            conditions = "( strftime('%j',DATE_DEBUT) = strftime('%j','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) " +

                    " or ( strftime('%j',DATE_FIN) = strftime('%j','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;
        } else if (Id == R.id.trisemaineD) {
            conditions = "( strftime('%W',DATE_DEBUT) = strftime('%W','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) " +

                    " or ( strftime('%W',DATE_FIN) = strftime('%W','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;

        } else if (Id == R.id.trimoisD) {
            conditions = "( strftime('%m',DATE_DEBUT) = strftime('%m','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) " +

                    " or ( strftime('%m',DATE_FIN) = strftime('%m','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;
        } else if (Id == R.id.trianneeD) {
            conditions = "strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') " +
                    " or strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;
        } else {
            constantsCursor = dbHelper.getAllPointage(db);
        }

        if (constantsCursor.getCount() == 0) {
            //message.setText("Il n'y a aucun pointeuse");
            // return;
        }

        if (constantsCursor == null) return;

        adapter = new ListeDesPointagesDeleteAdapter(this);
        adapter.setList(constantsCursor);
        MaListe.setAdapter(adapter);

        constantsCursor.close();
    }

    public void lance_suppression() {
        long row;

        // android.util.Log.w("Constants", "Taille="+adapter.ListeCheckBox.size());

        for (int i = 0; i < adapter.ListeEtat.size(); i++) {

            if (adapter.ListeEtat.get(i)) {
                try {
                    row = Long.parseLong(adapter.ListeId.get(i));

                    if (row >= 0) {
                        dbHelper.deleteEnregistrementPointage(db, row);
                    }
                } catch (Exception e) {
                    //android.util.Log.w("Constants", "Exception capture ="+e.getMessage());
                }
            }

        }
        refresh();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);


        menu.add(0, SUPPRESSION, 0, getString(R.string.validersuppressionPointage))
                .setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
            case SUPPRESSION:
                lance_suppression();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        constantsCursor.close();
        dbHelper.close();
    }

}
