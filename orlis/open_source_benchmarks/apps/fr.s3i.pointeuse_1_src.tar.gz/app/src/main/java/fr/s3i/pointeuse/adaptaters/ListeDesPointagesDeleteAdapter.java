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


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import fr.s3i.pointeuse.R;

public class ListeDesPointagesDeleteAdapter extends BaseAdapter {
    Context LeContext;
    //Un mécanisme pour gérer l'affichage graphique depuis un layout XML
    private LayoutInflater mInflater;
    Cursor curseur;

    public ArrayList<String> ListeDebut;
    public ArrayList<String> ListeFin;
    public ArrayList<String> ListeId;
    public ArrayList<CheckBox> ListeCheckBox;
    public ArrayList<Boolean> ListeEtat;

    public ListeDesPointagesDeleteAdapter(Context context) {
        LeContext = context;
    }

    public void setList(Cursor CursorBase) {
        curseur = CursorBase;
        ListeFin = new ArrayList<String>();
        ListeDebut = new ArrayList<String>();
        ListeId = new ArrayList<String>();
        ListeCheckBox = new ArrayList<CheckBox>();
        ListeEtat = new ArrayList<Boolean>();

        while (curseur.moveToNext()) {
            if (curseur.getString(1).length() > 0) {
                ListeId.add(curseur.getString(0)); // 0 is the first column
                ListeDebut.add(curseur.getString(1)); // 0 is the first column
                ListeFin.add(curseur.getString(2)); // 0 is the first column
            }
        }
        mInflater = LayoutInflater.from(LeContext);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return ListeId.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return ListeId.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layoutItem;

        layoutItem = (LinearLayout) mInflater.inflate(R.layout.listepointagedelete, parent, false);

        TextView fin = (TextView) layoutItem.findViewById(R.id.datefinD);
        TextView debut = (TextView) layoutItem.findViewById(R.id.datedebutD);
        CheckBox checkBox = (CheckBox) layoutItem.findViewById(R.id.checkboxD);
        checkBox.setTag(position);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //android.util.Log.w("CHECK","position=" + Integer.parseInt(buttonView.getTag().toString()));
                coche(Integer.parseInt(buttonView.getTag().toString()), isChecked);
            }

        });
        SimpleDateFormat newdateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat olddateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (ListeCheckBox.size() > position) {
            checkBox.setChecked(ListeEtat.get(position));
        } else {
            ListeEtat.add(false);
            ListeCheckBox.add(checkBox);

        }

        if (ListeDebut.get(position).length() > 0) {

            try {

                Date date = olddateFormat.parse((String) ListeDebut.get(position));
                debut.setText((String) newdateFormat.format(date));

            } catch (ParseException e) {
                debut.setText("");
                //Toast.makeText(LeContext, "Erreur=" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        } else {
            debut.setText("");
        }

        if (ListeFin.get(position).length() > 0) {

            try {
                Date date = olddateFormat.parse((String) ListeFin.get(position));
                fin.setText((String) newdateFormat.format(date));
            } catch (ParseException e) {
                fin.setText("");
                //Toast.makeText(LeContext, "Erreur=" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            fin.setText("");
        }


        fin.setTag(position);
        debut.setTag(position);

        return layoutItem;
    }

    public void coche(int position, boolean isChecked) {
        ListeEtat.set(position, isChecked);
    }

    public void coche_all(boolean isChecked) {
        for (int i = 0; i < ListeCheckBox.size(); i++) {
            ListeCheckBox.get(i).setChecked(isChecked);
        }

    }

}
