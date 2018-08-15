/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import net.sf.times.R;

import java.util.Calendar;

/**
 * Date picker dialog with a "Today" button.
 *
 * @author Moshe Waisberg
 */
public class TodayDatePickerDialog extends DatePickerDialog {

    public TodayDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        init(context);
    }

    public TodayDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, theme, callBack, year, monthOfYear, dayOfMonth);
        init(context);
    }

    private void init(Context context) {
        setButton(BUTTON_NEUTRAL, context.getText(R.string.today), this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_NEUTRAL) {
            setToday();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                onClick(dialog, BUTTON_POSITIVE);
        }
        super.onClick(dialog, which);
    }

    private void setToday() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int monthOfYear = today.get(Calendar.MONTH);
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        updateDate(year, monthOfYear, dayOfMonth);
    }
}
