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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import fr.s3i.pointeuse.R;
import fr.s3i.pointeuse.persistance.DatabaseHelper;
import fr.s3i.pointeuse.utils.Calcul;
import fr.s3i.pointeuse.utils.Utilitaire;
import fr.s3i.pointeuse.widget.PointageWidgetProvider;


public class Pointer extends Fragment {
    int heure;
    int minute;
    int sec;
    int annee, mois, jour;

    public Button actionPointer, btnAddPointage;
    public TimePicker nouvelleHeure;
    public DatePicker nouvelleDate;
    public Date nouvelleDateDebut;
    public EditText commentaire;
    public TextView etatEnCours;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    private String TAG = "Pointer";
    private static final int DELETE = Menu.FIRST;
    private static final int PARAMETRE = Menu.FIRST + 1;
    private static final int BACKUP = Menu.FIRST + 2;
    private static final int HELP = Menu.FIRST + 3;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pagepointage, container, false);

        etatEnCours = (TextView) v.findViewById(R.id.encours);

        actionPointer = (Button) v.findViewById(R.id.btnPointer);
        actionPointer.setSingleLine(false);
        actionPointer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Pointe();
            }

        });
        btnAddPointage = (Button) v.findViewById(R.id.btnAddPointage);
        btnAddPointage.setSingleLine(false);
        btnAddPointage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                creerFenetreDateHeure();
                //AjoutePointage();
            }

        });
        // Ajout du listener de click sur l'image banniere_S3i pour aller sur le site
        ImageView img = (ImageView) v.findViewById(R.id.imgS3i);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://s3i.fr"));
                startActivity(intent);
            }
        });

        dbHelper = new DatabaseHelper(this.getContext());
        db = dbHelper.getWritableDatabase();

        refresh_etat();

        return v;
    }

    public void refresh_etat() {
        Cursor dernierEnregistrement;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = new Date();
        Date debut, fin = null;

        String date1 = dateFormat.format(date);
        try {
            fin = dateFormat.parse(date1);
        } catch (ParseException e1) {
            android.util.Log.d("Exception e1", e1.getMessage());
        }

        dernierEnregistrement = dbHelper.getLastEnregistrementPointage(db);
        etatEnCours.setText(getString(R.string.aucunpointage));

        if (dernierEnregistrement != null) {
            try {
                if (!(dernierEnregistrement.getString(2).length() > 0)) {
                    //android.util.Log.w("debut=", (String)dernierEnregistrement.getString(1));
                    debut = dateFormat.parse((String) dernierEnregistrement.getString(1));

                    GregorianCalendar c_debut = new GregorianCalendar();
                    c_debut.setTime(debut);
                    GregorianCalendar c_fin = new GregorianCalendar();
                    c_fin.setTime(fin);

                    Calcul calcul = new Calcul(this.getContext());
                    Calcul.Spointage s = calcul.CalculTemps(c_debut, c_fin, 0);
                    long temps = s.temps_pointage / 60;
                    String affichageTemps = Utilitaire.formatAffichage(this.getContext(), temps);
                    etatEnCours.setText(getString(R.string.tempstravail1) + affichageTemps);
                }
            } catch (Exception e) {
                android.util.Log.w("Exception Refresh", e.getMessage());
            }
        }
        //   Toast.makeText(this, "Update!", Toast.LENGTH_SHORT).show();

        dernierEnregistrement.close();
        dernierEnregistrement.deactivate();

        AlarmManager alarmManager = (AlarmManager) this.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this.getContext(), PointageWidgetProvider.class);
        intent.setAction(PointageWidgetProvider.ACTION_START_REFRESH_WIDGET);
        PendingIntent pi = PendingIntent.getBroadcast(this.getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pi);
    }


    public void creerFenetreDateHeure() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(getString(R.string.ajouter_pointage));
        String titre;
        titre = "Date de début";

        alert.setMessage(titre);


        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View dialoglayout = inflater.inflate(R.layout.framealertedateheure, (ViewGroup) getView().findViewById(R.id.framealertedateheure));

        nouvelleHeure = (TimePicker) dialoglayout.findViewById(R.id.heure);
        nouvelleHeure.setIs24HourView(true);
        nouvelleHeure.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        nouvelleHeure.setIs24HourView(true);
        commentaire = (EditText) dialoglayout.findViewById(R.id.commentaire);
        commentaire.setVisibility(View.INVISIBLE);
        dialoglayout.findViewById(R.id.titreCommentaire).setVisibility(View.INVISIBLE);
        nouvelleDate = (DatePicker) dialoglayout.findViewById(R.id.date);
        alert.setView(dialoglayout);

        alert.setNegativeButton(getString(R.string.cancel), null);
        alert.setPositiveButton(getString(R.string.oui1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                saisirDateFin();
            }
        });
        alert.show();

    }

    public void saisirDateFin() {
        jour = nouvelleDate.getDayOfMonth();
        mois = nouvelleDate.getMonth();//Commence a 0
        annee = nouvelleDate.getYear();
        heure = nouvelleHeure.getCurrentHour();
        minute = nouvelleHeure.getCurrentMinute();

        // converting the datestring from the picker to a long:
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, jour);
        c.set(Calendar.MONTH, mois);
        c.set(Calendar.YEAR, annee);
        c.set(Calendar.HOUR_OF_DAY, heure);
        c.set(Calendar.MINUTE, minute);

        nouvelleDateDebut = c.getTime();

        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(getString(R.string.ajouter_pointage));
        String titre;
        titre = "Saisir date de fin";

        alert.setMessage(titre);


        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View dialoglayout = inflater.inflate(R.layout.framealertedateheure, (ViewGroup) getView().findViewById(R.id.framealertedateheure));

        nouvelleHeure = (TimePicker) dialoglayout.findViewById(R.id.heure);
        nouvelleHeure.setIs24HourView(true);
        nouvelleHeure.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        nouvelleHeure.setIs24HourView(true);

        nouvelleDate = (DatePicker) dialoglayout.findViewById(R.id.date);
        commentaire = (EditText) dialoglayout.findViewById(R.id.commentaire);
        alert.setView(dialoglayout);

        alert.setNegativeButton(getString(R.string.cancel), null);
        alert.setPositiveButton(getString(R.string.oui1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                insereNouveauPointage();
            }
        });
        alert.show();

    }

    public void insereNouveauPointage() {
        //Recupere la date et l'heure
        Cursor constantsCursor = null;
        int jour, mois, annee, heure, minute;
        String conditions;
        String oldDebut = "", oldFin = "";
        boolean first;
        jour = nouvelleDate.getDayOfMonth();
        mois = nouvelleDate.getMonth();//Commence a 0
        annee = nouvelleDate.getYear();
        heure = nouvelleHeure.getCurrentHour();
        minute = nouvelleHeure.getCurrentMinute();

        // converting the datestring from the picker to a long:
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, jour);
        c.set(Calendar.MONTH, mois);
        c.set(Calendar.YEAR, annee);
        c.set(Calendar.HOUR_OF_DAY, heure);
        c.set(Calendar.MINUTE, minute);

        Date nouvelleDateFin = c.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        dbHelper.insereNouveauPointage(db, dateFormat.format(nouvelleDateDebut), dateFormat.format(nouvelleDateFin), commentaire.getText().toString());

        String message = getString(R.string.insertiontermine);
        Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
        refresh_etat();
    }

    public void Pointe() {
        //1  On recupere le dernier pointage
        Cursor dernierEnregistrement = dbHelper.getLastEnregistrementPointage(db);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String message;

    	/*Toast.makeText(this, "dernierEnregistrement =  " + dernierEnregistrement.getLong(0) +
                    " - Debut = " + dernierEnregistrement.getString(2) + " Fin= " +
    				dernierEnregistrement.getString(3), Toast.LENGTH_SHORT).show();*/
        if (dernierEnregistrement.getCount() == 0) {
            dbHelper.insereNouveauPointage(db, dateFormat.format(date), "");
            dateFormat = new SimpleDateFormat("HH:mm");
            message = getString(R.string.debutpointage) + " " + dateFormat.format(date);
            Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
        } else if (dernierEnregistrement.getString(2).length() > 0) {
            dbHelper.insereNouveauPointage(db, dateFormat.format(date), "");
            dateFormat = new SimpleDateFormat("HH:mm");
            message = getString(R.string.debutpointage) + " " + dateFormat.format(date);
            Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateEnregistrementPointage(db, dernierEnregistrement.getLong(0), dbHelper.DATE_FIN, dateFormat.format(date));
            dateFormat = new SimpleDateFormat("HH:mm");
            message = getString(R.string.finpointage) + " " + dateFormat.format(date);
            Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
        }
        refresh_etat();
    }

    @Override
    public void onDestroy() {
        if (dbHelper != null)
            dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh_etat();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
