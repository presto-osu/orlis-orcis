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

package fr.s3i.pointeuse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import fr.s3i.pointeuse.persistance.DatabaseHelper;

public class Calcul {
    public class Spointage {
        public long temps_pause;
        public long temps_pointage;

        public Spointage(long temps_pointage, long temps_pause) {
            this.temps_pause = temps_pause;
            this.temps_pointage = temps_pointage;
        }
    }

    static List pause_debut_brute = new LinkedList();
    static List pause_fin_brute = new LinkedList();
    Context leContext;


    public Calcul(Context leContext) {
        this.leContext = leContext;
    }

    public Spointage somme(List listeDebut, List listeFin, int arrondi) {
        Date debut = null, fin = null;

        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.leContext);
        String option_a_la_seconde = "0";

        Spointage retour = new Spointage(0, 0);

        try {
            option_a_la_seconde = preferences.getString("pointagealaseconde", "0");
        } catch (Exception All) {
            //Toast.makeText(this, "Echec=" + All.getMessage() , Toast.LENGTH_SHORT).show();
        }
        //android.util.Log.w("refresh", "option_a_la_seconde="+option_a_la_seconde);

        if ("0".equals(option_a_la_seconde)) {
            olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        for (int i = 0; i < listeDebut.size(); i++) {
            try {

                debut = olddateFormat.parse((String) listeDebut.get(i));
                if (!"".equals(listeFin.get(i))) {
                    fin = olddateFormat.parse((String) listeFin.get(i));
                } else {
                    fin = new Date();
                }
                GregorianCalendar c_debut = new GregorianCalendar();
                c_debut.setTime(debut);
                GregorianCalendar c_fin = new GregorianCalendar();
                c_fin.setTime(fin);

                Spointage s = CalculTemps(c_debut, c_fin, arrondi);
                retour.temps_pointage += s.temps_pointage;
                retour.temps_pause += s.temps_pause;
                //	Toast.makeText(LeContext, debut.toString() + "+" +fin.toString()+"="+cumul, Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                System.out.println("Enregistrement " + i + " parcouru =" + debut + " / " + fin);

                //Toast.makeText(LeContext, "Erreur=" +e.getMessage(), Toast.LENGTH_SHORT).show();
                break;

            }
        }

        return retour;
    }

    public Spointage CalculTemps(GregorianCalendar debut, GregorianCalendar fin, int arrondi) {

        long diff;

        diff = fin.getTimeInMillis() - debut.getTimeInMillis();

        //diff = diff / (60 * 1000);//Conversion en minutes
        diff = diff / (1000);//Conversion en secondes

        //Utilisation de l'arrondi
        if (arrondi > 0) {
            long arrondissement = diff % (arrondi * 60);
            diff = diff - arrondissement;

            //android.util.Log.w("Arrondissement", "Val=" + arrondi + " diff="+ diff + " arrondissement = "+arrondissement);
        }

        //android.util.Log.w("CalculTemps", "Val=" +diff);
        Spointage s = new Spointage(diff, 0);
        return s;
    }

}

