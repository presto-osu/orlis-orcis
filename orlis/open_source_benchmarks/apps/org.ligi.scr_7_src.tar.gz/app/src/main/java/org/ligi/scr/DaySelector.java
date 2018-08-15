package org.ligi.scr;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.ligi.scr.model.Day;

import java.util.ArrayList;
import java.util.List;

public class DaySelector extends Spinner {
    public DaySelector(Context context) {
        super(context);

        final List<String> res = new ArrayList<>();
        for (Day day : App.conference.days) {
            res.add("#" + App.conference.days.indexOf(day) + " " + day.date);
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.day_spinner_item, android.R.id.text1,
                res.toArray(new String[res.size()]));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        setAdapter(adapter);
    }
}
