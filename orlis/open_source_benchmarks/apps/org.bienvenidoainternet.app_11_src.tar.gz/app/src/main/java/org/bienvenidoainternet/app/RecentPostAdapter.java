package org.bienvenidoainternet.app;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.bienvenidoainternet.app.structure.BoardItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *   BaiApp - Bienvenido a internet Android Application
 *   Copyright (C) 2016 Renard1911(https://github.com/Renard1911)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class RecentPostAdapter extends ArrayAdapter<BoardItem> {

    public RecentPostAdapter(Context context, List<BoardItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItemView = convertView;
        if (null == convertView) {
            listItemView = inflater.inflate(
                    R.layout.recentpost_item,
                    parent,
                    false);
        }
        final BoardItem postItem = getItem(position);
        TextView rp_message = (TextView) listItemView.findViewById(R.id.rp_message);
        TextView rp_title = (TextView) listItemView.findViewById(R.id.rp_title);
        TextView rp_timediff = (TextView) listItemView.findViewById(R.id.rp_timediff);
        if (postItem.getParentBoard() != null){
            rp_title.setText(postItem.getParentBoard().getBoardName() + ": " + postItem.getSubject());
        }else{
            rp_title.setText(postItem.getSubject());
        }
        rp_message.setText(Html.fromHtml(postItem.getMessage()));
        Map<TimeUnit,Long> timeDiff = computeDiff(new Date(postItem.getTimeStamp() * 1000L), new Date(System.currentTimeMillis()));
        String strTimeDiff = "";
        if (timeDiff.get(TimeUnit.SECONDS) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.SECONDS) + (timeDiff.get(TimeUnit.SECONDS) == 1 ? " segundo" : " segundos");
        }

        if (timeDiff.get(TimeUnit.MINUTES) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.MINUTES) + (timeDiff.get(TimeUnit.MINUTES) == 1 ? " minuto" : " minutos");
        }

        if (timeDiff.get(TimeUnit.HOURS) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.HOURS) + (timeDiff.get(TimeUnit.HOURS) == 1 ? " hora" : " horas");
        }

        if (timeDiff.get(TimeUnit.DAYS) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.DAYS) + (timeDiff.get(TimeUnit.DAYS) == 1 ? " día" : " días");
        }
        rp_timediff.setText(strTimeDiff);


        return listItemView;
    }


    public static Map<TimeUnit,Long> computeDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
        long milliesRest = diffInMillies;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit,diff);
        }
        return result;
    }
}
