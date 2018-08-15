package com.luk.timetable2.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.luk.timetable2.R;
import com.luk.timetable2.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by luk on 5/12/15.
 */
public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext = null;
    private Integer[] mWidgetColors;
    private ArrayList<String[]> mLessons = new ArrayList<>();

    public WidgetViewsFactory(Context mContext, String variant) {
        this.mContext = mContext;
        this.mWidgetColors = Utils.getWidgetColorsForVariant(variant);
    }

    @Override
    public void onCreate() {
        loadLessons();
    }

    private void loadLessons() {
        mLessons.clear();

        // load mLessons for current day
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (day == -1 || day == 5) day = 0; // set monday

        ArrayList<List<String>> hours = Utils.getHours(mContext, day);

        if (hours == null) return;

        for (List<String> hour : hours) {
            ArrayList<List<String>> mLessons = Utils.getLessonsForHour(mContext, day, hour.get(0));

            if (mLessons == null) return;

            String _lesson = "";
            String _room = "";
            String _hour = hour.get(0);

            for (List<String> l : mLessons) {
                // set lesson names
                _lesson += l.get(0) + "\n";

                // set rooms
                _room += l.get(1) + " / ";
            }

            this.mLessons.add(new String[]{
                    _lesson.substring(0, _lesson.length() - 1),
                    _hour + "\n" + _room.substring(0, _room.length() - 3)
            });
        }
    }

    @Override
    public void onDataSetChanged() {
        loadLessons();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mLessons.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews lesson = new RemoteViews(mContext.getPackageName(), R.layout.widget_lesson);

        // set lesson name
        lesson.setTextViewText(R.id.lesson, mLessons.get(position)[0]);

        // set lesson additional info { hours, classroom }
        lesson.setTextViewText(R.id.info, mLessons.get(position)[1]);

        // set colors
        lesson.setInt(R.id.background, "setBackgroundResource", mWidgetColors[0]);
        lesson.setTextColor(R.id.lesson, mContext.getResources().getColor(mWidgetColors[2]));
        lesson.setTextColor(R.id.info, mContext.getResources().getColor(mWidgetColors[2]));

        return lesson;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
