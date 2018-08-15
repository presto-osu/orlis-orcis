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

package fr.s3i.pointeuse.adaptaters;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.EditText;

import fr.s3i.pointeuse.R;
import fr.s3i.pointeuse.framents.Calendrier;
import fr.s3i.pointeuse.persistance.DatabaseHelper;
import fr.s3i.pointeuse.utils.Calcul;

public class ListeDesPointagesAdapter extends BaseAdapter {
    Context leContext;
    //Un mécanisme pour gérer l'affichage graphique depuis un layout XML
    private LayoutInflater mInflater;
    Cursor curseur;

    private ArrayList<String> listeDebut;
    private ArrayList<String> listeFin;
    private ArrayList<String> listeCommentaires;

    ArrayList<String> listeId;

    Calendrier parent;
    private Calcul calcul;

    public ListeDesPointagesAdapter(Context context, Calendrier cal) {
        leContext = context;
        parent = cal;
    }

    public ArrayList<String> getListeDebut() {
        return listeDebut;
    }

    public ArrayList<String> getListeFin() {
        return listeFin;
    }

    public Calcul getCalcul() {
        return this.calcul;
    }

    public void suppression_confirmer(Context context, int position, int debutFin) {
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db;

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        Cursor constantsCursor = dbHelper.selectDatePointage(db, position);
//        String valeur;
//        if (debutFinEnCours==1)
//            valeur = constantsCursor.getString(1);
//        else
//            valeur = constantsCursor.getString(2);
//        constantsCursor.close();
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        String conditions= "strftime('%s',DATE_DEBUT) >= strftime('%s','"+valeur+"') " +
//                " or strftime('%s',DATE_FIN) >= strftime('%s','"+valeur+"') " ;
//
//
//        constantsCursor=dbHelper.getSomeDatePointage(db, conditions) ;
//        if(constantsCursor==null)
//        {
//            //android.util.Log.w("constantsCursor", "==null");
//            parent.refresh();
//            return ;
//        }
//
//        boolean premier_coup = true;
//        long old_position = -1;
//		long pos = -1;
//        while (constantsCursor.moveToNext() )
//        {
//            pos = constantsCursor.getLong(0);
//            if( premier_coup) {
//                if (debutFin == 1) {
//                    dbHelper.updateEnregistrementPointage(db, pos, dbHelper.DATE_DEBUT, constantsCursor.getString(2));
//                }
//                premier_coup = false;
//                old_position = constantsCursor.getLong(0);
//            }
//            else
//            {
//                dbHelper.updateEnregistrementPointage(db, old_position, dbHelper.DATE_FIN, constantsCursor.getString(1));
//                dbHelper.updateEnregistrementPointage(db, pos, dbHelper.DATE_DEBUT, constantsCursor.getString(2));
//
//                old_position = constantsCursor.getLong(0);
//
//            }
//            dbHelper.updateEnregistrementPointage(db, pos, dbHelper.DATE_FIN, "");
//
//        }
//
//        dbHelper.purge_pointage_vide(db);
        // modification pour supprimer un pointage complet
        dbHelper.deleteEnregistrementPointage(db, constantsCursor.getLong(0));


        dbHelper.close();
        parent.refresh();

    }

    public void supprimer_pointage(final Context context, final int position, final int debutFin)//1 debut 2 fin
    {

        Cursor constantsCursor = null;
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db;

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        constantsCursor = dbHelper.selectDatePointage(db, position);
        if (constantsCursor == null) {
            //android.util.Log.w("Return", "constantsCursor is null");
            //constantsCursor.close();
            dbHelper.close();
            return;
        }
        if (constantsCursor.getCount() == 0) {
            //android.util.Log.w("Return", "constantsCursor == 0");
            constantsCursor.close();
            dbHelper.close();
            return;
        }
        String valeur = "";
        if (debutFin == 1)
            valeur = constantsCursor.getString(1); // 0 is the first column
        else valeur = constantsCursor.getString(2);

        constantsCursor.close();
        dbHelper.close();
        if (valeur == null) return;
        if (valeur == "") return;

        SimpleDateFormat newdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = olddateFormat.parse(valeur);
            valeur = (String) newdateFormat.format(date);
        } catch (ParseException e) {
            return;
            //Toast.makeText(LeContext, "Erreur=" +e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getString(R.string.confirmation));

        //android.util.Log.w("titre", "titre="+titre);
        String titre = context.getString(R.string.suppression_pointage_unitaire_message) + ": " + valeur;
        alert.setMessage(titre);

        alert.setNegativeButton(context.getString(R.string.cancel), null);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        suppression_confirmer(context, position, debutFin);
                    }
                }
        );
        alert.show();


    }

    /*Permet de creer la fenetre de modification*/
    public void creerFenetreModification(Context context, int position, int debutFin)//1 debut 2 fin
    {
        Cursor constantsCursor = null;
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db;
        String debut, fin;
        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int iheureDebut = -1, iheureFin = -1, iminDebut = -1, iminFin = -1;

        Date date = new Date();
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        dateFinEnCours = null;
        dateDebutEnCours = null;

        positionEnCours = position;
        debutFinEnCours = debutFin;

        constantsCursor = dbHelper.selectDatePointage(db, position);
        if (constantsCursor == null) {
            //android.util.Log.w("Return", "constantsCursor is null");
            //constantsCursor.close();
            dbHelper.close();
            return;
        }
        if (constantsCursor.getCount() == 0) {
            //android.util.Log.w("Return", "constantsCursor == 0");
            constantsCursor.close();
            dbHelper.close();
            return;
        }
        debut = "";
        debut = constantsCursor.getString(1); // 0 is the first column
        fin = "";
        fin = constantsCursor.getString(2); // 0 is the first column
        if (debut.length() > 0) {
            try {
                date = olddateFormat.parse(debut);
                iheureDebut = date.getHours();
                iminDebut = date.getMinutes();
                dateDebutEnCours = date;
            } catch (Exception e) {

            }
        } else if (debutFin == 1) {
            constantsCursor.close();
            dbHelper.close();
            return;
        }

        if (fin.length() > 0) {
            try {
                date = olddateFormat.parse(fin);
                iheureFin = date.getHours();
                iminFin = date.getMinutes();
                dateFinEnCours = date;
            } catch (Exception e) {

            }
        }

        if ((debutFin == 2 && fin.length() <= 0) || (debutFin == 1 && debut.length() <= 0)) {
            constantsCursor.close();
            dbHelper.close();
            return;
        }

        constantsCursor.close();
        dbHelper.close();

        Dialog timepick;
        if (debutFin == 1) timepick = onCreateDialogTime(context, iheureDebut, iminDebut);
        else timepick = onCreateDialogTime(context, iheureFin, iminFin);

        timepick.show();
    }

    int positionEnCours = -1;
    int debutFinEnCours = -1;
    Date dateFinEnCours, dateDebutEnCours;

    // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Date dateEnCours;
            SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //android.util.Log.w("onTimeSet", "hourOfDay="+hourOfDay+" - "+ "minute="+minute);

            if (debutFinEnCours == 1) {
                dateEnCours = dateDebutEnCours;
            } else {
                dateEnCours = dateFinEnCours;
            }
            dateEnCours.setHours(hourOfDay);
            dateEnCours.setMinutes(minute);

            if (debutFinEnCours == 1 && dateFinEnCours != null) {
                if (dateEnCours.after(dateFinEnCours)) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.erreurchangementdate1), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (debutFinEnCours == 2 && dateDebutEnCours != null) {
                if (dateEnCours.before(dateDebutEnCours)) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.erreurchangementdate2), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Cursor constantsCursor = null;
            DatabaseHelper dbHelper = null;
            SQLiteDatabase db;

            // android.util.Log.w("commentaire=", commentaire);
            dbHelper = new DatabaseHelper(view.getContext());
            db = dbHelper.getWritableDatabase();

            constantsCursor = dbHelper.selectDatePointage(db, positionEnCours);
            if (constantsCursor == null) {
                //android.util.Log.w("constantsCursor=", "null");
                //constantsCursor.close();
                dbHelper.close();
                return;
            }
            if (constantsCursor.getCount() == 0) {
                //android.util.Log.w("constantsCursor=", "0");
                constantsCursor.close();
                dbHelper.close();
                return;
            }
            if (debutFinEnCours == 1)
                dbHelper.updateEnregistrementPointage(db, positionEnCours, dbHelper.DATE_DEBUT, (String) olddateFormat.format(dateEnCours));
            else
                dbHelper.updateEnregistrementPointage(db, positionEnCours, dbHelper.DATE_FIN, (String) olddateFormat.format(dateEnCours));

            constantsCursor.close();
            dbHelper.close();
            parent.refresh();
        }
    };


    protected Dialog onCreateDialogTime(Context context, int heure, int min) {
        TimePickerDialog temp = new TimePickerDialog(context, mTimeSetListener, heure, min, true);

        return temp;
    }

    /* Commentaire */
    public void creerFenetreCommentaire(Context context, int position) {
        Cursor constantsCursor = null;
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db;
        String commentaire;
        String debut, fin;
        SimpleDateFormat newdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = new Date();
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();

        //android.util.Log.w("textString", "creerFenetreCommentaire position="+position);
        constantsCursor = dbHelper.selectDatePointage(db, position);
        if (constantsCursor == null) {
            //android.util.Log.w("Return", "constantsCursor is null");
            //constantsCursor.close();
            dbHelper.close();
            return;
        }
        if (constantsCursor.getCount() == 0) {
            //android.util.Log.w("Return", "constantsCursor == 0");
            constantsCursor.close();
            dbHelper.close();
            return;
        }


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
            } catch (Exception e) {

            }
        }

        if (fin.length() > 0) {
            try {
                date = olddateFormat.parse(fin);
                fin = (String) newdateFormat.format(date);
            } catch (Exception e) {

            }
        }

        constantsCursor.close();
        dbHelper.close();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getString(R.string.addcomment));
        String titre;
        titre = debut;

        if (fin != null) {
            if (fin.length() > 0) {
                titre = titre + "   " + context.getString(R.string.dateto) + " " + fin;
            }
        }
        //android.util.Log.w("titre", "titre="+titre);
        alert.setMessage(titre);
        final EditText editText = new EditText(context);

        editText.setTag(position);
        alert.setView(editText);

        editText.setText(commentaire);

        alert.setNegativeButton(context.getString(R.string.cancel), null);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Cursor constantsCursor = null;
                        DatabaseHelper dbHelper = null;
                        SQLiteDatabase db;
                        int position = (Integer) editText.getTag();

                        String commentaire = editText.getText().toString();
                        // android.util.Log.w("commentaire=", commentaire);
                        dbHelper = new DatabaseHelper(editText.getContext());
                        db = dbHelper.getWritableDatabase();


                        constantsCursor = dbHelper.selectDatePointage(db, position);
                        if (constantsCursor == null) {
                            //android.util.Log.w("constantsCursor=", "null");
                            //constantsCursor.close();
                            dbHelper.close();
                            return;
                        }
                        if (constantsCursor.getCount() == 0) {
                            //android.util.Log.w("constantsCursor=", "0");
                            constantsCursor.close();
                            dbHelper.close();
                            return;
                        }
                        dbHelper.updateEnregistrementPointage(db, position, dbHelper.COMMENTAIRE, commentaire);
                        constantsCursor.close();
                        dbHelper.close();
                        parent.refresh();
                    }
                }
        );
        alert.show();
    }


    public void setList(Cursor CursorBase) {
        curseur = CursorBase;
        listeDebut = new ArrayList<String>();
        listeFin = new ArrayList<String>();
        listeId = new ArrayList<String>();
        listeCommentaires = new ArrayList<String>();

        while (curseur.moveToNext()) {
            listeId.add(curseur.getString(0)); // 0 is the first column
            listeDebut.add(curseur.getString(1)); // 0 is the first column
            listeFin.add(curseur.getString(2)); // 0 is the first column
            listeCommentaires.add(curseur.getString(3));
        }
        mInflater = LayoutInflater.from(leContext);
        calcul = new Calcul(leContext);
    }

    @Override
    public int getCount() {
        return listeId.size();
    }

    @Override
    public Object getItem(int position) {
        return listeId.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layoutItem;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(leContext);

        int Id;

        //(1) : Réutilisation des layouts
        if (convertView == null) {
            //Initialisation de notre item à partir du  layout XML "personne_layout.xml"
            layoutItem = (LinearLayout) mInflater.inflate(R.layout.listepointage, parent, false);
        } else {
            layoutItem = (LinearLayout) convertView;
        }

        TextView debut = (TextView) layoutItem.findViewById(R.id.datedebut);
        TextView fin = (TextView) layoutItem.findViewById(R.id.datefin);
        TextView commentaireActivite = (TextView) layoutItem.findViewById(R.id.commentaireActivite);
        TextView lecumul = (TextView) layoutItem.findViewById(R.id.cumulunitaire);

        long cumul;


        SimpleDateFormat newdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String option_a_la_seconde = "0";
        try {
            option_a_la_seconde = preferences.getString("pointagealaseconde", "0");
        } catch (Exception All) {
            //Toast.makeText(this, "Echec=" + All.getMessage() , Toast.LENGTH_SHORT).show();
        }
        //android.util.Log.w("refresh", "option_a_la_seconde="+option_a_la_seconde);

        if ("0".equals(option_a_la_seconde)) {
            olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        Id = -1;
        Id = Integer.parseInt(listeId.get(position));
        // android.util.Log.w("ListeId=", "Id=" + Id);
        debut.setTag(Id);
        debut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                creerFenetreModification(v.getContext(), position, 1);
            }

        });

        debut.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Integer position = (Integer) v.getTag();
                supprimer_pointage(v.getContext(), position, 1);
                return true;
            }
        });

        fin.setTag(Id);
        fin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                creerFenetreModification(v.getContext(), position, 2);
            }

        });

        fin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Integer position = (Integer) v.getTag();
                supprimer_pointage(v.getContext(), position, 2);
                return true;
            }
        });


        commentaireActivite.setTag(Id);
        commentaireActivite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                creerFenetreCommentaire(v.getContext(), position);
            }

        });

        //debut.setText((String)ListeDebut.get(position) );
        fin.setText((String) listeFin.get(position));

        if (listeDebut.get(position).length() > 0) {

            try {
                Date date = olddateFormat.parse((String) listeDebut.get(position));
                debut.setText((String) newdateFormat.format(date));
            } catch (ParseException e) {
                debut.setText("");
                lecumul.setText("");
                //Toast.makeText(LeContext, "Erreur=" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else {
            lecumul.setText("");
            debut.setText("");
        }

        if (listeFin.get(position).length() > 0) {
            try {
                Date d_debut = olddateFormat.parse((String) listeDebut.get(position));
                Date d_fin = olddateFormat.parse((String) listeFin.get(position));

                GregorianCalendar c_debut = new GregorianCalendar();
                c_debut.setTime(d_debut);
                GregorianCalendar c_fin = new GregorianCalendar();
                c_fin.setTime(d_fin);

                fin.setText((String) newdateFormat.format(d_fin));

                Calcul.Spointage s = calcul.CalculTemps(c_debut, c_fin, 0);
                cumul = s.temps_pointage;
                cumul = cumul / 60;//Min

                String format = "0";
                try {
                    format = preferences.getString("formataffichage", "0");
                } catch (Exception All) {
                    //Toast.makeText(this, "Echec=" + All.getMessage() , Toast.LENGTH_SHORT).show();
                }
                if (cumul < 60) {
                    lecumul.setText(cumul + "min");
                } else if (cumul < 1440) {
                    lecumul.setText((int) (cumul / 60) + "h " +
                            (int) (cumul % 60) + "min");
                } else {
                    if (format.equals("0")) {
                        lecumul.setText((int) (cumul / 60) + "h " +
                                (int) (cumul % 60) + "min");
                    } else {
                        int min = (int) (cumul % 1440);
                        int nbjour = (int) (cumul / 1440);
                        lecumul.setText(
                                (int) (nbjour) + leContext.getString(R.string.jourarrondi) + " " +
                                        (int) (min / 60) + "h " +
                                        (int) (min % 60) + "min");
                    }
                }
            } catch (ParseException e) {
                fin.setText("");
                lecumul.setText("");
                //Toast.makeText(LeContext, "Erreur=" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            fin.setText("");
            lecumul.setText("");
        }

        if (listeCommentaires.get(position).length() > 0) {

            commentaireActivite.setText(listeCommentaires.get(position));
        } else {
            commentaireActivite.setText("");
        }

        return layoutItem;
    }


}
