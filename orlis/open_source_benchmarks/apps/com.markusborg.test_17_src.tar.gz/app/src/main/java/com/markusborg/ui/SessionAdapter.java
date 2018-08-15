package com.markusborg.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.markusborg.logic.Setting;

/**
 * Created by Markus Borg on 2015-11-22.
 */
public class SessionAdapter extends ArrayAdapter<Setting> {

    private ArrayList<Setting> objects;

    public SessionAdapter(Context context, int textViewResourceId, ArrayList<Setting> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    /**
     * Describes how an individual Session is presented in the list view.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item, null);
        }

		// the variable position is sent in as an argument to this method.
		// The variable simply refers to the position of the current object in the list,
		// the ArrayAdapter iterates through the list we sent it.
        Setting i = objects.get(position);

        if (i != null) {
            ImageView iv = (ImageView) v.findViewById(R.id.sessionImageView);
            TextView tv = (TextView) v.findViewById(R.id.sessionTextView);

            // Display the corresponding ball icon
            Drawable ballIcon = null;
            if (i.isSquash()) {
                // getDrawable changed with API 22
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ballIcon = getBallIconNew(0);
                } else {
                    ballIcon = getBallIconOld(0);
                }
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ballIcon = getBallIconNew(1);
                } else {
                    ballIcon = getBallIconOld(1);
                }
            }
            iv.setImageDrawable(ballIcon);
            tv.setText(i.getRestrictedString());
        }

        return v;
    }

    /**
     * Return a ball icon from Lollipop and later Android versions
     *
     * @param type 0 = squash otherwise = badminton
     * @return The ball icon
     */
    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    private Drawable getBallIconNew(int type) {
        return type == 0 ? getContext().getResources().getDrawable(R.drawable.squashball,
                getContext().getApplicationContext().getTheme()) :
                getContext().getResources().getDrawable(R.drawable.shuttlecock,
                        getContext().getApplicationContext().getTheme());
    }

    /**
     * Return a ball icon using deprecated method.
     *
     * @param type 0 = squash otherwise = badminton
     * @return The ball icon
     */
    @SuppressWarnings("deprecation")
    private Drawable getBallIconOld(int type) {
        return type == 0 ? getContext().getResources().getDrawable(R.drawable.squashball) :
                getContext().getResources().getDrawable(R.drawable.shuttlecock);
    }
}
