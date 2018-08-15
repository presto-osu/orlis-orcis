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

package fr.s3i.pointeuse.framents;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import fr.s3i.pointeuse.adaptaters.ListeDesPointagesAdapter;
import fr.s3i.pointeuse.R;
import fr.s3i.pointeuse.persistance.DatabaseHelper;
import fr.s3i.pointeuse.utils.Calcul;
import fr.s3i.pointeuse.utils.Utilitaire;

/*
 * 
 * Classe Calendrier 
 * Permet de liste les pointages selon la sélection du calendrier
 * 
 */
public class Calendrier extends Fragment {
    private Cursor constantsCursor = null;
    private DatabaseHelper dbHelper = null;
    SQLiteDatabase db;
    public static final String ID = "_ID";
    public static final String DATE_DEBUT = "DATE_DEBUT";
    public static final String DATE_FIN = "DATE_FIN";

    public static final String PREF_FILE_NAME = "preferences";


    private static final int DELETE = Menu.FIRST;
    private static final int PARAMETRE = Menu.FIRST + 1;
    private static final int BACKUP = Menu.FIRST + 2;
    private static final int HELP = Menu.FIRST + 3;

    ListeDesPointagesAdapter adapter;
    ListView maListe;
    TextView message;
    DatePicker dp;
    RadioGroup rg;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.calendrier, container, false);

        dp = (DatePicker) v.findViewById(R.id.calendrier);
        dp.init(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), new OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {

                refresh();

            }
        });

        message = (TextView) v.findViewById(R.id.message);
        message.setGravity(Gravity.CENTER_HORIZONTAL);

        rg = (RadioGroup) v.findViewById(R.id.radiogroup);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                refresh();
            }
        });

        dbHelper = new DatabaseHelper(this.getContext());
        db = dbHelper.open();

        maListe = (ListView) v.findViewById(R.id.MaListeView);

        return v;
    }

    public void refresh() {

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

        if (Id == R.id.trijour) {
            conditions = "( strftime('%j',DATE_DEBUT) = strftime('%j','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) " +

                    " or ( strftime('%j',DATE_FIN) = strftime('%j','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;
        } else if (Id == R.id.trisemaine) {
            conditions = "( strftime('%W',DATE_DEBUT) = strftime('%W','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) " +

                    " or ( strftime('%W',DATE_FIN) = strftime('%W','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;

        } else if (Id == R.id.trimois) {
            conditions = "( strftime('%m',DATE_DEBUT) = strftime('%m','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) " +

                    " or ( strftime('%m',DATE_FIN) = strftime('%m','" + dateFormat.format(LaDate) + "') " +
                    " and strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ) ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;
        } else if (Id == R.id.triannee) {
            conditions = "strftime('%Y',DATE_DEBUT) = strftime('%Y','" + dateFormat.format(LaDate) + "') " +
                    " or strftime('%Y',DATE_FIN) = strftime('%Y','" + dateFormat.format(LaDate) + "') ";

            constantsCursor = dbHelper.getSomeDatePointage(db, conditions);
            if (constantsCursor == null) return;
        } else {
            constantsCursor = dbHelper.getAllPointage(db);
        }

        if (constantsCursor.getCount() == 0) {
            message.setText(getString(R.string.pasdepointage));
            // return;
        }

        adapter = new ListeDesPointagesAdapter(this.getContext(), this);
        adapter.setList(constantsCursor);
        //Toast.makeText(this, "constantsCursor=" + constantsCursor.getCount() , Toast.LENGTH_LONG).show();
        maListe.setAdapter(adapter);

        /*adapter.setList(constantsCursor);
        MaListe.setSelection(0);
        adapter.notifyDataSetChanged();*/

        constantsCursor.close();
        String arrondi = "0";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        try {
            arrondi = preferences.getString("listedesarrondis", "0");
        } catch (Exception All) {
            //Toast.makeText(this, "Echec=" + All.getMessage() , Toast.LENGTH_SHORT).show();
        }

        String format = "0";
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            format = preferences.getString("formataffichage", "0");
        } catch (Exception All) {
            //Toast.makeText(this, "Echec=" + All.getMessage() , Toast.LENGTH_SHORT).show();
        }
        Calcul.Spointage s = adapter.getCalcul().somme(adapter.getListeDebut(), adapter.getListeFin(), Integer.parseInt(arrondi));
        long lasomme = s.temps_pointage / 60;
        message.setText(getString(R.string.somme) + Utilitaire.formatAffichage(getContext(), lasomme));
    }

    @Override
    public void onResume() {
        super.onResume();

        //Toast.makeText(this, "onResume" , Toast.LENGTH_SHORT).show();
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (constantsCursor != null)
            constantsCursor.close();

        if (dbHelper != null)
            dbHelper.close();
    }

}
