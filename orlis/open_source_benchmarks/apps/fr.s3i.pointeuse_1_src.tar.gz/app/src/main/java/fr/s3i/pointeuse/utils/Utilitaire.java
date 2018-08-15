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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import fr.s3i.pointeuse.persistance.DatabaseHelper;
import fr.s3i.pointeuse.R;

public class Utilitaire {
    public final static String FILENAME = "oburoS3I.csv";


    public static void sendbymail(Context leContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(leContext);
        String email = "inconnu";
        try {
            email = preferences.getString("email", "inconnu");
        } catch (Exception All) {
            android.util.Log.w("Echec", All.getMessage());
            return;

        }

        if (email.equals("inconnu")) {
            Toast.makeText(leContext, leContext.getString(R.string.err_email2), Toast.LENGTH_SHORT).show();
            return;
        }

        String[] arrayEmail = {email};

        if (genereLaListe(leContext) != 0) {
            Toast.makeText(leContext, leContext.getString(R.string.err_email5), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("text/plain");

        //emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse
        //       ("file://"+ leContext.getFilesDir().getAbsolutePath() +"/" + Utilitaire.FILENAME));

        try {
            copyFile(new File(leContext.getFilesDir(), Utilitaire.FILENAME), new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), Utilitaire.FILENAME));
            //emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse
            //           ("file://"+leContext.getFilesDir()+"/"+Utilitaire.FILENAME));
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse
                    ("file://" + Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + Utilitaire.FILENAME));
        } catch (Exception e) {
            //   android.util.Log.w("erreur", "Erreur = " + e.getMessage() );
            //	return;
        }

        if (!email.equals("inconnu"))
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayEmail);

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, leContext.getString(R.string.savepointage));
        emailIntent.putExtra(Intent.EXTRA_TEXT, leContext.getString(R.string.savepointage));
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        leContext.startActivity(Intent.createChooser(emailIntent, leContext.getString(R.string.choisir)));

    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public static int genereLaListe(Context leContext) {

        int erreur;
        String Separateur = ";";

        int numero = 1;
        erreur = 0;
        SQLiteDatabase db;
        Cursor constantsCursor = null;
        DatabaseHelper dbHelper = null;
        SimpleDateFormat newdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String debut = "", fin = "";
        Date date;
        FileOutputStream fos;
        String buffer;
        String commentaire = "";

        //String myPath = leContext.getFilesDir().getAbsolutePath();
        //Log.e("myPath",myPath);

        date = new Date();

        dbHelper = new DatabaseHelper(leContext);
        db = dbHelper.getWritableDatabase();

        constantsCursor = dbHelper.getAllPointage(db);
        if (constantsCursor == null) {
            erreur = 1;
            android.util.Log.d("Erreur", " Erreur 1 base vide");
            return erreur;
        }

        android.util.Log.d("EXTRA_STREAM", leContext.getFilesDir() + "  " + leContext.getFilesDir().getAbsolutePath());

        try {

            fos = leContext.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);

            //buffer = "\"Liste des pointages du " +  (String)newdateFormat.format(date) +"\""+Separateur ;
            buffer = "\"" + leContext.getString(R.string.listeP) + " " + (String) newdateFormat.format(date) + "\"" + Separateur;
            fos.write(buffer.getBytes());
            fos.write("\n".getBytes());
            buffer = "\"" +

                    leContext.getString(R.string.numero) + "\"" + Separateur + "\"" +
                    leContext.getString(R.string.debut) + "\"" + Separateur + "\"" +
                    leContext.getString(R.string.fin) + "\"" + Separateur + "\"" +
                    leContext.getString(R.string.commentaire)
                    + "\"";


            fos.write(buffer.getBytes());
            fos.write("\n".getBytes());

            numero = 1;

            while (constantsCursor.moveToNext()) {
                if (constantsCursor.getString(1).length() > 0) {
                    debut = "";
                    debut = constantsCursor.getString(1); // 0 is the first column
                    fin = "";
                    fin = constantsCursor.getString(2); // 0 is the first column
                    commentaire = "";
                    commentaire = constantsCursor.getString(3);

                    if (debut.length() > 0) {
                        try {
                            date = olddateFormat.parse(debut);
                            debut = (String) newdateFormat.format(date);
                        } catch (ParseException ex) {
                            debut = "";
                        }
                    }

                    if (fin.length() > 0) {
                        try {
                            date = olddateFormat.parse(fin);
                            fin = (String) newdateFormat.format(date);
                        } catch (ParseException ex) {
                            fin = "";
                        }
                    }
                    if (commentaire == null) {
                        commentaire = " ";
                    }

                    buffer = "\"" + String.valueOf(numero) + "\"" + Separateur + "\"" +
                            debut + "\"" + Separateur + "\"" + fin + "\"" + Separateur + "\"" + commentaire + "\"";

                    fos.write(buffer.getBytes());
                    fos.write("\n".getBytes());
                    numero++;
                }


            }
            fos.close();
            constantsCursor.close();
            dbHelper.close();
            db.close();
        } catch (Exception e) {
            constantsCursor.close();
            dbHelper.close();
            db.close();
            erreur = 2;
            android.util.Log.d("Erreur", " Erreur 2 Exception " + e.getMessage());
            return erreur;
        }
        return 0;
    }

    static public String formatAffichage(Context leContext, Long temps) {
        Double tempsReel = new Double(temps);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2); //arrondi à 2 chiffres apres la virgules
        df.setMinimumFractionDigits(2);
        df.setDecimalSeparatorAlwaysShown(true);
        String format = "0";
        String texteAffichage = new String();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(leContext);
        try {
            format = preferences.getString("formataffichage", "0");
        } catch (Exception All) {
            android.util.Log.w("Echec", All.getMessage());
        }

        if (temps < 60) {
            if (format.equals("2")) {
                texteAffichage = (df.format(tempsReel / 60)) + " H";
            } else {
                texteAffichage = temps + "Min";

            }

        } else if (temps < 1440) {
            if (format.equals("2")) {
                texteAffichage = (df.format(tempsReel / 60)) + " H";
            } else {
                texteAffichage = temps / 60 + "H" + temps % 60 + "Min";
            }
        } else {
            android.util.Log.d("format", format);

            if (format.equals("0")) {
                texteAffichage = temps / 60 + "H" + temps % 60 + "Min";
            } else if (format.equals("1")) {
                int min = (int) (temps % 1440);
                int nbjour = (int) (temps / 1440);
                texteAffichage = (int) (nbjour) + leContext.getString(R.string.jourarrondi) + " " +
                        (int) (min / 60) + "h " +
                        (int) (min % 60) + "min";
            } else {
                texteAffichage = (df.format(tempsReel / 60)) + " H";
            }
        }
        return texteAffichage;
    }

}
