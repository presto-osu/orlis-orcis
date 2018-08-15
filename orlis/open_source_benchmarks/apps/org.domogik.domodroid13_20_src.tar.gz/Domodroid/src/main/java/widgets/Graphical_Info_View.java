/*
 * This file is part of Domodroid.
 *
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 *
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import Abstract.calcul;
import misc.tracerengine;
import rinor.Rest_com;


public class Graphical_Info_View extends View implements OnClickListener {

    private final String parameter;
    private int width;
    private Canvas can;
    private Canvas can2;
    private final Vector<Vector<Float>> values;

    private float gridStartX;
    private float gridStartY;
    private float gridStopX;
    private float gridStopY;
    private float gridOffset;
    private float valueOffset;

    private float minf;
    private float maxf;
    private float avgf;

    public int dev_id;
    public int id;
    public String state_key;
    public String url;
    public Thread thread;
    //public int period;
    public int update;
    private final Handler handler;
    public boolean activate = false;
    private boolean loaded = false;
    private final String mytag = "Graphical_Info_View";
    private final FrameLayout container = null;
    private View myself = null;
    private Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final Date time_start = new Date();
    private final Date time_end = new Date();
    public TextView dates = null;
    private int period_type = 0;        // 0 = period defined by settings
    // 1 = 1 day
    // 8 = 1 week
    // 30 = 1 month
    // 365 = 1 year
    private int sav_period;
    private Button Prev = null;
    private Button Next = null;
    private Button Year = null;
    private Button Month = null;
    private Button Week = null;
    private Button Day = null;

    private String step = "hour";
    private int limit = 6;        // items returned by Rinor on stats arrays when 'hour' average
    private OnClickListener listener = null;
    private tracerengine Tracer = null;
    private final String login;
    private final String password;
    private final float size15;
    private final float size10;
    private final float size5;
    private final float size7;
    private final float api_version;
    private Boolean SSL;
    private String unit;

    public Graphical_Info_View(tracerengine Trac, Context context, SharedPreferences params, String parameters) {
        super(context);
        invalidate();
        this.Tracer = Trac;
        this.parameter = parameters;
        login = params.getString("http_auth_username", null);
        password = params.getString("http_auth_password", null);
        api_version = params.getFloat("API_VERSION", 0);
        SSL = params.getBoolean("ssl_activate", false);

        try {
            //Basilic add, number feature has a unit parameter
            JSONObject jparam = new JSONObject(parameter.replaceAll("&quot;", "\""));
            unit = jparam.getString("unit");
        } catch (JSONException jsonerror) {
            Tracer.i(mytag, "No unit for this feature");
        }

        values = new Vector<>();
        activate = true;
        this.myself = this;
        period_type = 1;    //by default, display 24 hours
        compute_period();    //To initialize time_start & time_end

        //correct kitkat problem
        //DisplayMetrics metrics = getResources().getDisplayMetrics();
        //width= metrics.widthPixels;
        //height= metrics.heightPixels;
        //fixed 15 float text size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics);
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        //Label Text size according to the screen size
        size15 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics);
        size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);
        size7 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 7, metrics);
        size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, metrics);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!activate) {
                    Tracer.d(mytag, "Handler receives a request to die ");
                    //That seems to be a zombie
                    myself.setVisibility(GONE);
                    if (container != null) {
                        container.removeView(myself);
                        container.recomputeViewAttributes(myself);
                    }
                    invalidate();
                    try {
                        finalize();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }    //kill the handler thread itself
                } else {
                    invalidate();
                }
            }
        };
    }

    public void onClick(View v) {
        String tag = (String) v.getTag();
        sav_period = period_type;        //Save the current graph period
        switch (tag) {
            case "Prev":
                Prev = (Button) v;
                period_type = -1;
                break;
            case "Next":
                Next = (Button) v;
                period_type = 0;
                break;
            case "Year":
                Year = (Button) v;
                period_type = 365;
                break;
            case "Month":
                Month = (Button) v;
                period_type = 31;
                break;
            case "Week":
                Week = (Button) v;
                period_type = 8;
                break;
            case "Day":
                Day = (Button) v;
                period_type = 1;
                break;
        }
        force_aspect(period_type);
        compute_period();
        updateTimer();
    }

    private void force_aspect(int which) {

        if (Prev != null)
            if (which != -1)
                Prev.setTypeface(null, Typeface.NORMAL);
            else
                Prev.setTypeface(null, Typeface.BOLD);
        if (Next != null)
            if (which != 0)
                Next.setTypeface(null, Typeface.NORMAL);
            else
                Next.setTypeface(null, Typeface.BOLD);
        if (Year != null)
            if (which != 365)
                Year.setTypeface(null, Typeface.NORMAL);
            else
                Year.setTypeface(null, Typeface.BOLD);
        if (Month != null)
            if (which != 31)
                Month.setTypeface(null, Typeface.NORMAL);
            else
                Month.setTypeface(null, Typeface.BOLD);
        if (Week != null)
            if (which != 8)
                Week.setTypeface(null, Typeface.NORMAL);
            else
                Week.setTypeface(null, Typeface.BOLD);
        if (Day != null)
            if (which != 1)
                Day.setTypeface(null, Typeface.NORMAL);
            else
                Day.setTypeface(null, Typeface.BOLD);
    }

    public void onWindowVisibilityChanged(int visibility) {
        Tracer.i(mytag, "Visibility changed to : " + visibility);
        if (visibility == View.VISIBLE) {
            this.activate = true;
            display_dates();
        } else
            activate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getMeasuredWidth();
        int height = getMeasuredHeight();
        gridStartY = height - size15;
        gridStopY = size15;
        gridOffset = size15;
        valueOffset = size10;


        Bitmap buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Bitmap text = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        can = new Canvas(buffer);
        can2 = new Canvas(text);

        drawMessage();

        try {
            drawGrid();
            drawValue();
            drawGraph();
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }
        if (loaded) {
            canvas.drawBitmap(buffer, 0, 0, new Paint());
            buffer.recycle();
        } else {
            canvas.drawBitmap(text, 0, 0, new Paint());
            text.recycle();
        }
    }

    private void drawMessage() {
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setAntiAlias(true);
        paint.setTextSize(size15);
        can2.drawText("Loading Data ...", size10, size15, paint);
    }

    private void drawGrid() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);

        can.drawColor(Color.TRANSPARENT);
        can.drawLine(gridStartX, gridStartY, gridStopX, gridStartY, paint);
        can.drawLine(gridStartX, gridStartY, gridStartX, gridStopY, paint);
    }

    private void drawValue() {
        //min - max - avg lines
        float gridSize_values = (gridStartY - gridOffset) - (gridStopY + gridOffset);
        float scale_values = gridSize_values / (maxf - minf);

        DashPathEffect dashPath = new DashPathEffect(new float[]{10, 5}, 1);
        Paint paint = new Paint();
        paint.setPathEffect(dashPath);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        can.drawLine(gridStartX, gridStartY - gridOffset, gridStopX, gridStartY - gridOffset, paint);
        can.drawLine(gridStartX, gridStopY + gridOffset, gridStopX, gridStopY + gridOffset, paint);
        paint.setColor(Color.parseColor("#993300"));
        can.drawLine(gridStartX, (gridStartY - gridOffset) - ((avgf - minf) * scale_values), gridStopX, (gridStartY - gridOffset) - ((avgf - minf) * scale_values), paint);


        //min - max - avg values
        paint.setPathEffect(null);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(size10);
        paint.setColor(Color.BLACK);
        can.drawText(minf + unit, gridStartX - valueOffset - (Float.toString(minf).length() * size5), gridStartY - gridOffset, paint);
        can.drawText(maxf + unit, gridStartX - valueOffset - (Float.toString(maxf).length() * size5), gridStopY + gridOffset, paint);
        can.drawText(avgf + unit, gridStartX - valueOffset - (Float.toString(avgf).length() * size5), (gridStartY - gridOffset) - ((avgf - minf) * scale_values), paint);

        //temp values
        DashPathEffect dashPath2 = new DashPathEffect(new float[]{3, 8}, 1);
        paint.setStyle(Paint.Style.FILL);
        float temp_step = (maxf - minf) / 6;

        for (int i = 1; i < 6; i++) {
            paint.setPathEffect(dashPath2);
            paint.setAntiAlias(false);
            paint.setColor(Color.parseColor("#0B909A"));
            can.drawLine(gridStartX, (gridStartY - gridOffset) - ((temp_step * i) * scale_values), gridStopX, (gridStartY - gridOffset) - ((temp_step * i) * scale_values), paint);
            paint.setPathEffect(null);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            float right_value = minf + temp_step * i;
            String s = String.format("%.2f", right_value);
            //Todo add unit but they are displayed out of screen
            //can.drawText(s + unit, gridStopX + size5, (gridStartY - gridOffset) - ((temp_step * i) * scale_values), paint);
            can.drawText(s , gridStopX + size5, (gridStartY - gridOffset) - ((temp_step * i) * scale_values), paint);
        }
    }

    private void drawGraph() {
        float gridSize_values = (gridStartY - gridOffset) - (gridStopY + gridOffset);
        float scale_values = gridSize_values / (maxf - minf);
        float step = (gridStopX - gridStartX) / (values.size() - 1);
        int top = 0;
        String top_txt = "";
        int rythm = 1;
        int curr = 0;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        //Tracer.i(mytag,"drawGraph limit = "+limit+" , step = "+step+"Array size = "+values.size());
        for (int i = 0; i < values.size(); i++) {
            if (limit == 6) {
                top = values.get(i).get(4).intValue();    //get the hour
                top_txt = top + "'";
            }
            //top texts on X separators
            if (step > 24) {
                paint.setStrokeWidth(1);
                paint.setColor(Color.LTGRAY);
                paint.setAntiAlias(false);
                can.drawLine(gridStartX + (i * step),
                        gridStartY,
                        gridStartX + (i * step),
                        gridStopY,
                        paint);
                can.drawLine(gridStartX + (i * step),
                        gridStartY,
                        gridStartX + (i * step),
                        gridStartY - 4,
                        paint);

                paint.setAntiAlias(true);
                paint.setStrokeWidth(0);
                paint.setColor(Color.parseColor("#157C9E"));
                paint.setTextSize(size10);
                can.drawText(top_txt,
                        gridStartX + (i * step) - size7,
                        gridStopY + gridOffset - size15,
                        paint);
            } else if (step > 8 && (top == 3 ||
                    top == 6 ||
                    top == 9 ||
                    top == 12 ||
                    top == 15 ||
                    top == 18 ||
                    top == 21 ||
                    top == 0)) {
                paint.setStrokeWidth(1);
                paint.setColor(Color.LTGRAY);
                paint.setAntiAlias(false);
                can.drawLine(gridStartX + (i * step),
                        gridStartY, gridStartX + (i * step),
                        gridStopY,
                        paint);
                can.drawLine(gridStartX + (i * step),
                        gridStartY, gridStartX + (i * step),
                        gridStartY - 4,
                        paint);

                paint.setAntiAlias(true);
                paint.setStrokeWidth(0);
                paint.setColor(Color.parseColor("#157C9E"));
                can.drawText(top_txt,
                        gridStartX + (i * step) - 8,
                        gridStopY + gridOffset - 20,
                        paint);
            } else if (step > 4 && (top == 6 ||
                    top == 12 ||
                    top == 18 ||
                    top == 0)) {
                paint.setStrokeWidth(1);
                paint.setColor(Color.LTGRAY);
                paint.setAntiAlias(false);
                can.drawLine(gridStartX + (i * step),
                        gridStartY, gridStartX + (i * step),
                        gridStopY,
                        paint);
                can.drawLine(gridStartX + (i * step),
                        gridStartY,
                        gridStartX + (i * step),
                        gridStartY - 4,
                        paint);

                paint.setAntiAlias(true);
                paint.setStrokeWidth(0);
                paint.setColor(Color.parseColor("#157C9E"));
                can.drawText(top_txt,
                        gridStartX + (i * step) - 8,
                        gridStopY + gridOffset - 20,
                        paint);
            } else if (step > 2 && (top == 12 ||
                    top == 0)) {
                paint.setStrokeWidth(1);
                paint.setColor(Color.LTGRAY);
                paint.setAntiAlias(false);
                can.drawLine(gridStartX + (i * step),
                        gridStartY,
                        gridStartX + (i * step),
                        gridStopY, paint);
                can.drawLine(gridStartX + (i * step),
                        gridStartY,
                        gridStartX + (i * step),
                        gridStartY - 4,
                        paint);

                paint.setAntiAlias(true);
                paint.setStrokeWidth(0);
                paint.setColor(Color.parseColor("#157C9E"));
                can.drawText(top_txt,
                        gridStartX + (i * step) - 8,
                        gridStopY + gridOffset - 20,
                        paint);
            }
            if (top == 0) {
                paint.setAntiAlias(false);
                paint.setColor(Color.parseColor("#AEB255"));
                can.drawLine(gridStartX + (i * step),
                        gridStartY,
                        gridStartX + (i * step),
                        gridStopY,
                        paint);
            }

            // bottom texts
            int bottom_val1 = 0;
            int bottom_val2 = 0;
            int hour = values.get(i + 1).get(4).intValue();
            String bottom_txt = "";

            if (limit == 6) {
                //day or week
                rythm = 1;
                bottom_val1 = values.get(i + 1).get(3).intValue();    //day
                bottom_val2 = values.get(i).get(1).intValue();        //month
                bottom_txt = Integer.toString(bottom_val1) + "/" + Integer.toString(bottom_val2);
            } else if (limit == 5) {
                //one month
                rythm = 3;
                bottom_val1 = values.get(i + 1).get(3).intValue();    //day
                bottom_val2 = values.get(i).get(1).intValue();        //month
                bottom_txt = Integer.toString(bottom_val1) + "/" + Integer.toString(bottom_val2);
            } else {
                //one year (by weeks )
                if (values.size() < 18)    //17 weeks
                    rythm = 1;
                else if (values.size() < 36)    //35 weeks
                    rythm = 2;
                else
                    rythm = 3;        //full year

                bottom_val1 = values.get(i).get(0).intValue();    //year
                bottom_val2 = values.get(i + 1).get(2).intValue(); // week
                String tmp = Integer.toString(bottom_val1);
                tmp = tmp.substring(2);    //Only keep last 2 digits
                bottom_txt = tmp + "/" + Integer.toString(bottom_val2);
            }


            // Intermediate points
            if (values.get(i).get(0).intValue() == 0 || values.get(i + 1).get(0).intValue() == 0) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.parseColor("#157C9E"));
                paint.setStrokeWidth(4);
                can.drawPoint(gridStartX + (step * (i)),
                        (gridStartY - gridOffset) - ((values.get(i).get(5)) - minf) * scale_values,
                        paint);
                can.drawPoint(gridStartX + (step * (i + 1)),
                        (gridStartY - gridOffset) - ((values.get(i + 1).get(5)) - minf) * scale_values,
                        paint);
            }
            paint.setStrokeWidth(2);
            can.drawLine(gridStartX + (step * (i)),
                    (gridStartY - gridOffset) - ((values.get(i).get(5)) - minf) * scale_values,
                    gridStartX + (step * (i + 1)),
                    (gridStartY - gridOffset) - ((values.get(i + 1).get(5)) - minf) * scale_values,
                    paint);

            //text on X Axis
            if ((curr == 0) || (curr == rythm)) {
                if (hour == 0) {
                    paint.setColor(Color.parseColor("#157C9E"));
                    can.drawText(bottom_txt,
                            gridStartX + ((i + 1) * step),
                            gridStartY + gridOffset,
                            paint);
                    curr = 1;    //to skip next draw
                }
            } else {
                curr++;    //draw text skipped
            }
        }
    }

    public void updateTimer() {

        TimerTask doAsynchronousTask;
        final Timer timer = new Timer();
        doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {
                Runnable myTH = new Runnable() {
                    //handler.post(new Runnable() {	//Doume : to avoid Exception on ICS
                    public void run() {
                        try {
                            if (activate) {
                                new UpdateThread().execute();
                            } else {
                                //Tracer.i(mytag+"("+dev_id+")","update Timer : Destroy runnable");
                                timer.cancel();
                                this.finalize();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
                        //)
                        ;
                //Tracer.i(mytag,"TimerTask.run : Queuing Runnable for Device : "+dev_id);
                try {
                    handler.post(myTH);        //Doume : to avoid Exception on ICS
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } //TimerTask Run method
        }; //TimerTask
        //timer.schedule(doAsynchronousTask, 0, update*10000);
        timer.schedule(doAsynchronousTask, 0, update * 1000);    // Fix Doume
    }

    /*
     * Graphs are refreshed using a direct request to Domogik server
     */
    private class UpdateThread extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (!activate)
                return null;

            loaded = false;
            try {
                avgf = 0;
                values.clear();
                long currentTimestamp = time_end.getTime() / 1000;
                long startTimestamp = time_start.getTime() / 1000;

                JSONObject json_GraphValues = null;
                try {
                    if (api_version <= 0.6f) {
                        Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/from/" + startTimestamp + "/to/" + currentTimestamp + "/interval/" + step + "/selector/avg");
                        json_GraphValues = Rest_com.connect_jsonobject(Tracer, url + "stats/" + dev_id + "/" +
                                state_key +
                                "/from/" +
                                startTimestamp +
                                "/to/" +
                                currentTimestamp +
                                "/interval/" + step + "/selector/avg", login, password, 10000, SSL);
                    } else if (api_version >= 0.7f) {
                        Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/from/" + startTimestamp + "/to/" + currentTimestamp + "/interval/" + step + "/selector/avg");
                        //Don't forget old "dev_id"+"state_key" is replaced by "id"
                        json_GraphValues = Rest_com.connect_jsonobject(Tracer, url + "sensorhistory/id/" + id +
                                "/from/" +
                                startTimestamp +
                                "/to/" +
                                currentTimestamp +
                                "/interval/" + step + "/selector/avg", login, password, 10000, SSL);
                    }
                } catch (Exception e) {
                    Tracer.d(mytag, "Could not get sensor history.");
                    Tracer.d(mytag, e.toString());
                    return null;
                }
                //Tracer.d(mytag,"UpdateThread ("+dev_id+") Rinor result: "+json_GraphValues.toString());

                //TODO
                //replace json_GraphValues.getJSONArray("stats") != null by something else as it may crash on a 0.4.
                //if(! ((json_GraphValues != null) && (json_GraphValues.getJSONArray("stats") != null))) {
                if (json_GraphValues == null) {
                    //That seems to be a zombie
                    loaded = false;
                    handler.sendEmptyMessage(0);    // To force a close of this instance
                    return null;
                }
                Tracer.d(mytag, "UpdateThread (" + dev_id + ") Refreshing graph");
                JSONArray itemArray = null;
                JSONArray valueArray = null;
                if (api_version <= 0.6f) {
                    itemArray = json_GraphValues.getJSONArray("stats");
                    valueArray = itemArray.getJSONObject(0).getJSONArray("values");
                } else if (api_version >= 0.7f) {
                    valueArray = json_GraphValues.getJSONArray("values");
                }

                //minf=(float)valueArray.getJSONArray(0).getDouble(limit-1);
                maxf = minf = 0;
                //Tracer.i(mytag,"UpdateThread ("+dev_id+") : array size "+valueArray.length());

                for (int i = 0; i < valueArray.length(); i++) {
                    // Create a vector with all entry components

                    Vector<Float> vect = new Vector<>();
                    Double real_val = valueArray.getJSONArray(i).getDouble(limit - 1);    // Get the real 'value'
                    real_val = calcul.Round_double(real_val);

                    if (limit == 6) {
                        // stats per hour return [ year, month, week, day, hour, value]
                        for (int j = 0; j < 6; j++) {
                            vect.addElement((float) valueArray.getJSONArray(i).getDouble(j));
                        }
                    } else if (limit == 5) {
                        // stats per day return [year, month, week, day, value]
                        for (int j = 0; j < 4; j++) {
                            vect.addElement((float) valueArray.getJSONArray(i).getDouble(j));
                        }
                        vect.addElement(0f);    //null hour
                        vect.addElement(real_val.floatValue());
                    } else {
                        // stats per week return [ year,  week, value ]
                        Calendar date_value = Calendar.getInstance();
                        date_value.setTimeInMillis(0);
                        date_value.set(Calendar.YEAR, (int) valueArray.getJSONArray(i).getDouble(0));
                        date_value.set(Calendar.WEEK_OF_YEAR, (int) valueArray.getJSONArray(i).getDouble(1));
                        Date loc_date = new Date();
                        loc_date.setTime(date_value.getTimeInMillis());
                        //Tracer.d(mytag,"Case week : Inserting value at "+sdf.format(loc_date));
                        vect.addElement((float) valueArray.getJSONArray(i).getDouble(0));    //year
                        vect.addElement((float) loc_date.getMonth());    // month
                        vect.addElement((float) valueArray.getJSONArray(i).getDouble(1));    //week
                        vect.addElement((float) loc_date.getDay());    // day
                        vect.addElement(0f);    //null hour
                        vect.addElement(real_val.floatValue());
                    }
                    // each vector contains 6  floats
                    values.add(vect);
                    if (minf == 0)
                        minf = real_val.floatValue();

                    avgf += real_val;    // Get the real 'value'
                    if (real_val > maxf) {
                        maxf = real_val.floatValue();
                    }
                    if (real_val < minf) {
                        minf = real_val.floatValue();
                    }

                    if (i < valueArray.length() - 1) {
                        // It's not the last component
                        int loc_hour, loc_hour_next = 0;
                        int loc_day, loc_day_next = 0;
                        int loc_week, loc_week_next = 0;
                        int loc_year = 0;
                        int loc_month = 0;
                        float loc_value = 0;

                        if (limit == 6) {
                            // range between 1 to 8 days (average per hour)
                            loc_year = (int) valueArray.getJSONArray(i).getDouble(0);
                            loc_month = (int) valueArray.getJSONArray(i).getDouble(1);
                            loc_week = (int) valueArray.getJSONArray(i).getDouble(2);
                            loc_day = (int) valueArray.getJSONArray(i).getDouble(3);
                            loc_hour = (int) valueArray.getJSONArray(i).getDouble(4);
                            loc_hour_next = (int) valueArray.getJSONArray(i + 1).getDouble(4);
                            loc_value = (float) valueArray.getJSONArray(i).getDouble(5);
                            //Tracer.d(mytag,"Case hour : "+loc_year+" - "+loc_month+" - "+loc_week+" - "+loc_day+" - "+loc_hour+" - "+loc_value);

                            if (loc_hour != 23 && (loc_hour < loc_hour_next)) {
                                //no day change
                                //Tracer.d(mytag,"Case hour 1 ");

                                if ((loc_hour + 1) != loc_hour_next) {
                                    //ruptur : simulate next missing steps
                                    for (int k = 1; k < (loc_hour_next - loc_hour); k++) {
                                        Vector<Float> vect2 = new Vector<>();
                                        vect2.addElement(0f);
                                        vect2.addElement((float) loc_month);    //month
                                        vect2.addElement((float) loc_week);    //week
                                        vect2.addElement((float) loc_day);    //day
                                        vect2.addElement((float) loc_hour + k);    //hour
                                        vect2.addElement(loc_value);    //value
                                        values.add(vect2);
                                        //Tracer.e(mytag,"Case 1 : added : 0 - "+loc_month+" - "+loc_week+" - "+loc_day+" - "+(loc_hour+k)+" - "+loc_value);
                                        avgf += loc_value;    //value
                                    }
                                }
                            } else {
                                //Tracer.d(mytag,"Case hour 2 ");
                                //if((loc_hour ==23) && (loc_hour_next != 0) ){
                                if (loc_hour == 23) {
                                    // day change : simulate missing steps
                                    //Tracer.d(mytag,"Case hour 2a ");

                                    for (int k = 0; k < loc_hour_next; k++) {
                                        Vector<Float> vect2 = new Vector<>();
                                        vect2.addElement(0f);
                                        vect2.addElement((float) loc_month + 1);    //month
                                        vect2.addElement((float) loc_week);    //week
                                        vect2.addElement((float) loc_day + 1);    // next day
                                        vect2.addElement((float) k);            //simulated hour
                                        vect2.addElement(loc_value);    //value
                                        values.add(vect2);
                                        //Tracer.e(mytag,"Case 2 : added : 0 - "+loc_month+" - "+loc_week+" - "+(loc_day+1)+" - "+k+" - "+loc_value);
                                        avgf += loc_value;    //value
                                    }
                                }
                            }
                        } else if (limit == 5) {
                            // range between 9 to 32 days (average per day)
                            loc_day_next = (int) valueArray.getJSONArray(i + 1).getDouble(limit - 2);
                            loc_year = (int) valueArray.getJSONArray(i).getDouble(0);
                            loc_month = (int) valueArray.getJSONArray(i).getDouble(1);
                            loc_week = (int) valueArray.getJSONArray(i).getDouble(2);
                            loc_day = (int) valueArray.getJSONArray(i).getDouble(3);
                            loc_day_next = (int) valueArray.getJSONArray(i + 1).getDouble(3);
                            loc_hour = 0;
                            loc_value = (float) valueArray.getJSONArray(i).getDouble(4);
                            //Tracer.d(mytag,"Case day : "+loc_year+" - "+loc_month+" - "+loc_week+" - "+loc_day+" - "+loc_value);

                            //if(loc_day != 31 && (loc_day < loc_day_next) ) {
                            if (loc_day < loc_day_next) {
                                // month continues...
                                //Tracer.d(mytag,"Case day 1 ");

                                if ((loc_day + 1) != loc_day_next) {
                                    // but a hole exists : simulate missing days
                                    //Tracer.d(mytag,"Case day 1a ");
                                    for (int k = 1; k < (loc_day_next - loc_day); k++) {
                                        Vector<Float> vect2 = new Vector<>();
                                        vect2.addElement(0f);
                                        vect2.addElement((float) loc_month);    //month
                                        vect2.addElement((float) loc_week);    //week
                                        vect2.addElement((float) (loc_day + k));    //day
                                        vect2.addElement((float) loc_hour); // = 0
                                        vect2.addElement(loc_value);    //value
                                        values.add(vect2);
                                        avgf += loc_value;    //value
                                    }
                                }
                            } else {
                                // next day being less than current day, month has changed !
                                //Tracer.d(mytag,"Case day 2 ");

                                //if((loc_day == 31) && (loc_day_next != 0) ){
                                if (loc_day_next != 0) {
                                    //Tracer.d(mytag,"Case day 2a ");

                                    for (int k = 0; k < loc_day_next; k++) {
                                        Vector<Float> vect2 = new Vector<>();
                                        vect2.addElement(0f);
                                        vect2.addElement((float) loc_month);    //month
                                        vect2.addElement((float) loc_week);    //week
                                        //vect2.addElement(0f);				// no week in scale
                                        vect2.addElement((float) k);            //day
                                        vect2.addElement((float) loc_hour); // = 0
                                        vect2.addElement(loc_value);        //value
                                        values.add(vect2);
                                        //Tracer.d(mytag,"Case day 2a with k="+k);

                                        avgf += loc_value;    //value
                                    }
                                }
                            }
                        } else if (limit == 3) {
                            Calendar date_value = Calendar.getInstance();
                            date_value.setTimeInMillis(0);
                            date_value.set(Calendar.YEAR, (int) valueArray.getJSONArray(i).getDouble(0));
                            date_value.set(Calendar.WEEK_OF_YEAR, (int) valueArray.getJSONArray(i).getDouble(1));
                            Date loc_date = new Date();
                            loc_date.setTime(date_value.getTimeInMillis());
                            // range of 1 year (average per week)
                            loc_year = (int) valueArray.getJSONArray(i).getDouble(0);
                            loc_month = loc_date.getMonth();    // month
                            loc_week = (int) valueArray.getJSONArray(i).getDouble(1);
                            loc_week_next = (int) valueArray.getJSONArray(i + 1).getDouble(1);
                            loc_day = loc_date.getDate();
                            loc_day_next = 0;
                            loc_hour = loc_date.getHours();
                            loc_value = (float) valueArray.getJSONArray(i).getDouble(2);
                            //Tracer.d(mytag,"Case week : "+loc_year+" - "+loc_week+" - "+loc_value);

                            if (loc_week != 52 && (loc_week < loc_week_next)) {
                                if ((loc_week + 1) != loc_week_next) {
                                    // Changing year
                                    for (int k = 1; k < (loc_week_next - loc_week); k++) {
                                        Vector<Float> vect2 = new Vector<>();
                                        //vect2.addElement((float)loc_year);	//year
                                        vect2.addElement(0f);
                                        vect2.addElement((float) loc_month);    //month
                                        vect2.addElement((float) (loc_week + k));    //week
                                        vect2.addElement((float) loc_day);    //day
                                        vect2.addElement(0f);    //hour
                                        vect2.addElement(loc_value);    //value
                                        values.add(vect2);
                                        avgf += loc_value;    //value
                                    }
                                }
                            }
                            if ((loc_week == 52) && (loc_week_next != 0)) {
                                //last week of the year
                                for (int k = 0; k < loc_week_next; k++) {
                                    Vector<Float> vect2 = new Vector<>();
                                    //vect2.addElement((float)loc_year);	//year
                                    vect2.addElement(0f);
                                    vect2.addElement((float) loc_month);    //month
                                    vect2.addElement((float) k);    //week
                                    vect2.addElement((float) loc_day);    //day
                                    vect2.addElement(0f);    //hour
                                    vect2.addElement(loc_value);    //value
                                    values.add(vect2);
                                    avgf += valueArray.getJSONArray(i).getDouble(limit - 1);    //value
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            avgf = avgf / values.size();
            avgf = calcul.Round_float(avgf);

            gridStartX = Float.toString(maxf).length() * size7;
            if (Float.toString(minf).length() * size7 > gridStartX)
                gridStartX = Float.toString(minf).length() * size7;
            if (Float.toString(avgf).length() * size7 > gridStartX)
                gridStartX = Float.toString(avgf).length() * size7;
            gridStopX = width - gridStartX;
            loaded = true;
            handler.sendEmptyMessage(0);
            return null;
        }
    }

    private void compute_period() {
        long duration = 0;
        //Calendar cal = Calendar.getInstance(); // The 'now' time

        switch (period_type) {
            case -1:
                //user requires the 'Prev' period
                period_type = sav_period;
                duration = 86400l * 1000l * period_type;
                if (time_end != null) {
                    long new_end = time_end.getTime();
                    new_end -= duration;
                    time_end.setTime(new_end);
                    new_end -= duration;
                    time_start.setTime(new_end);

                }
                //Tracer.i(mytag,"type prev on "+period_type+" Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
                break;
            case 0:
                //user requires the 'Next' period
                period_type = sav_period;
                duration = 86400l * 1000l * period_type;
                if (time_start != null) {
                    long new_start = time_start.getTime();
                    new_start += duration;
                    time_start.setTime(new_start);
                    new_start += duration;
                    time_end.setTime(new_start);
                }
                long new_start = time_start.getTime();
                long new_end = time_end.getTime();
                long now = System.currentTimeMillis();
                if (new_end > now) {
                    time_end.setTime(now);
                    double new_timestamp = now - duration;
                    new_start = (long) new_timestamp;
                    time_start.setTime(new_start);
                }
                //Tracer.i(mytag,"type next on "+period_type+" Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
                break;
            default:
                //period_type indicates the number of days to graph
                // relative to 'now' date
                duration = 86400l * 1000l * period_type;
                long new_end_time = System.currentTimeMillis();
                time_end.setTime(new_end_time);    //Get actual system time
                new_end_time -= duration;
                time_start.setTime(new_end_time);
                //Tracer.i(mytag,"type = "+period_type+" Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
                break;
        }
        // time_start & time_end are set....
        display_dates();

        if (period_type < 9) {
            step = "hour";
            limit = 6;
        } else if (period_type < 32) {
            step = "day";
            limit = 5;
        } else {
            step = "week";
            limit = 3;
        }

    }

    private void display_dates() {
        if (dates != null) {
            dates.setText(getContext().getText(R.string.from) + "  " + sdf.format(time_start) + "   " + getContext().getText(R.string.to) + "  " + sdf.format(time_end));
        }
    }
}