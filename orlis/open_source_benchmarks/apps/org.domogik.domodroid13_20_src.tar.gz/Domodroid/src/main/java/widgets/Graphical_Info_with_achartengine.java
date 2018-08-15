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
 * 
 * SPECIAL
 * Thank's to http://wptrafficanalyzer.in/blog/android-combined-chart-using-achartengine-library/
 * 
 */
package widgets;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import Abstract.calcul;
import Abstract.display_sensor_info;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Graphics_Manager;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.Rest_com;

public class Graphical_Info_with_achartengine extends Basic_Graphical_widget implements OnClickListener {

    private LinearLayout chartContainer;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private int id;

    private Message msg;
    private static String mytag = "";
    private String url = null;

    public static FrameLayout container = null;
    private static FrameLayout myself = null;
    private final Boolean with_graph = true;
    private Boolean realtime = false;
    private GraphicalView mChart;

    private String step = "hour";
    private int limit = 6;        // items returned by Rinor on stats arrays when 'hour' average
    private long currentTimestamp = 0;
    private long startTimestamp = 0;
    private final Date time_start = new Date();
    private final Date time_end = new Date();
    private int period_type = 0;        // 0 = period defined by settings
    // 1 = 1 day
    // 8 = 1 week
    // 30 = 1 month
    // 365 = 1 year
    private int sav_period;

    private float size10;
    private float size5;
    private XYMultipleSeriesRenderer multiRenderer;
    private XYSeriesRenderer incomeRenderer;
    private XYSeriesRenderer emptyRenderer;
    private XYMultipleSeriesDataset dataset;
    private XYSeries nameSeries;
    private XYSeries EmptySeries;
    private final Entity_Feature feature;
    private String state_key;
    private String parameters;
    private int dev_id;
    private final int session_type;
    private final SharedPreferences params;
    private SimpleDateFormat format;
    private TextView state_key_view;
    private String stateS;
    private String test_unite;
    private Typeface typefaceweather;
    private Typeface typefaceawesome;
    private Float Float_graph_size;

    public Graphical_Info_with_achartengine(tracerengine Trac,
                                            final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                                            final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Info_with_achartengine(tracerengine Trac,
                                            final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                                            final Entity_Map feature_map, Handler handler) {
        super(params, context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.url = url;
        this.session_type = session_type;
        this.params = params;

        onCreate();
    }

    private void onCreate() {
        this.state_key = feature.getState_key();
        this.dev_id = feature.getDevId();
        this.parameters = feature.getParameters();
        this.id = feature.getId();
        String graph_size = params.getString("graph_size", "262.5");
        this.Float_graph_size = Float.valueOf(graph_size);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mytag = "Graphical_Info_with_achartengine (" + dev_id + ")";

        try {
            stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            stateS = state_key;
        }
        myself = this;
        setOnClickListener(this);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //Label Text size according to the screen size
        float size12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, metrics);
        size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);
        size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, metrics);
        float size2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, metrics);

        //Design the graph
        //Creating a XYMultipleSeriesRenderer to customize the whole chart
        multiRenderer = new XYMultipleSeriesRenderer();
        //Creating XYSeriesRenderer to customize incomeSeries
        incomeRenderer = new XYSeriesRenderer();
        emptyRenderer = new XYSeriesRenderer();
        //Creating a dataset to hold each series
        dataset = new XYMultipleSeriesDataset();
        //Creating an  XYSeries for Income
        nameSeries = new TimeSeries(name);
        EmptySeries = new TimeSeries(getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), "no_value")));
        incomeRenderer.setColor(0xff0B909A);
        emptyRenderer.setColor(0xffff0000);
        incomeRenderer.setPointStyle(PointStyle.CIRCLE);
        //emptyRenderer.setPointStyle(PointStyle.CIRCLE);
        incomeRenderer.setFillPoints(true);
        emptyRenderer.setFillPoints(true);
        incomeRenderer.setLineWidth(4);
        emptyRenderer.setLineWidth(4);
        incomeRenderer.setDisplayChartValues(true);
        emptyRenderer.setDisplayChartValues(false);
        incomeRenderer.setChartValuesTextSize(size12);

        //Change the type of line between point
        //incomeRenderer.setStroke(BasicStroke.DASHED);
        //Remove default X axis label
        //multiRenderer.setXLabels(0);
        //Set X title
        multiRenderer.setXTitle(getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), "time")));
        //Remove default Y axis label
        multiRenderer.setYLabels(0);
        //Set X label text color
        multiRenderer.setXLabelsColor(Color.BLACK);
        //Set Y label text color
        multiRenderer.setYLabelsColor(0, Color.BLACK);
        //Set X label text size
        multiRenderer.setLabelsTextSize(size10);
        //Set X label text angle
        multiRenderer.setXLabelsAngle(-15);
        //Set Y label text angle
        multiRenderer.setYLabelsAngle(-10);
        //Set X label text alignement
        multiRenderer.setXLabelsAlign(Align.CENTER);
        //Set to make TV_Value of y axis left aligned
        multiRenderer.setYLabelsAlign(Align.LEFT);
        //Disable zoom button
        multiRenderer.setZoomButtonsVisible(false);
        //get background transparent
        multiRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        //Disable Zoom in Y axis
        multiRenderer.setZoomEnabled(true, false);
        //Disable Pan in Y axis
        multiRenderer.setPanEnabled(true, false);
        //Limits pan mouvement
        //[panMinimumX, panMaximumX, panMinimumY, panMaximumY]
        //double[] panLimits={-5,26,0,0};
        //multiRenderer.setPanLimits(panLimits);
        //Sets the selectable radius TV_Value around clickable points.
        multiRenderer.setSelectableBuffer(10);
        //Add grid
        multiRenderer.setShowGrid(true);
        //Set color for grid
        multiRenderer.setGridColor(Color.BLACK, 0);
        //To allow on click method (called when pan or zoom aplied)
        multiRenderer.setClickEnabled(true);

        Tracer.i(mytag, "New instance for name = " + name + " state_key = " + state_key);

        //state key
        state_key_view = new TextView(context);
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //TV_Value
        TV_Value = new TextView(context);
        TV_Value.setTextSize(28);
        TV_Value.setTextColor(Color.BLACK);
        TV_Value.setGravity(Gravity.RIGHT);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        typefaceweather = Typeface.createFromAsset(context.getAssets(), "fonts/weathericons-regular-webfont.ttf");
        typefaceawesome = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");

        TV_Timestamp = new RelativeTimeTextView(context, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        super.LL_featurePan.addView(TV_Value);
        super.LL_featurePan.addView(TV_Timestamp);
        super.LL_infoPan.addView(state_key_view);

        test_unite = "";
        try {
            //Basilic add, number feature has a unit parameter
            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            test_unite = jparam.getString("unit");
        } catch (JSONException jsonerror) {
            Tracer.i(mytag, "No unit for this feature");
        }

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 9999) {
                    //Message from widgetupdate
                    //state_engine send us a signal to notify TV_Value changed
                    if (session == null)
                        return;

                    String new_val = session.getValue();
                    String Value_timestamp = session.getTimestamp();
                    Tracer.d(mytag, "Handler receives a new TV_Value <" + new_val + "> at " + Value_timestamp);

                    //Value_timestamp = timestamp_to_relative_time.get_relative_time(Value_timestamp);
                    Long Value_timestamplong = null;
                    Value_timestamplong = Value_timestamplong.valueOf(Value_timestamp) * 1000;

                    display_sensor_info.display(Tracer, new_val, Value_timestamplong, mytag, parameters, TV_Value, TV_Timestamp, context, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, test_unite);

                    //Change icon if in %
                    if ((state_key.equalsIgnoreCase("humidity")) || (state_key.equalsIgnoreCase("percent")) || (test_unite.equals("%"))) {
                        if (Float.parseFloat(new_val) >= 60) {
                            //To have the icon colored if TV_Value beetwen 30 and 60
                            change_this_icon(2);
                        } else if (Float.parseFloat(new_val) >= 30) {
                            //To have the icon colored if TV_Value >30
                            change_this_icon(1);
                        } else {
                            //To have the icon colored if TV_Value <30
                            change_this_icon(0);
                        }
                    } else {
                        // #93
                        if (new_val.equals("off") || new_val.equals("false") || new_val.equals("0") || new_val.equals("0.0")) {
                            change_this_icon(0);
                            //set featuremap.state to 1 so it could select the correct icon in entity_map.get_ressources
                        } else {
                            change_this_icon(2);
                        }
                    }
                } else if (msg.what == 9998) {
                    // state_engine send us a signal to notify it'll die !
                    Tracer.d(mytag, "state engine disappeared ===> Harakiri !");
                    session = null;
                    realtime = false;
                    removeView(LL_background);
                    myself.setVisibility(GONE);
                    if (container != null) {
                        container.removeView(myself);
                        container.recomputeViewAttributes(myself);
                    }
                    try {
                        finalize();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }    //kill the handler thread itself
                }
            }

        };

        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our TV_Value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null)

        {
            if (api_version <= 0.6f) {
                session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            } else if (api_version >= 0.7f) {
                session = new Entity_client(id, "", mytag, handler, session_type);
            }
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    realtime = true;        //we're connected to engine
                    //each time our TV_Value change, the engine will call handler
                    handler.sendEmptyMessage(9999);    //Force to consider current TV_Value in session
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....

    }




    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

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


    private void drawgraph() throws JSONException {
        float minf = 0;
        float maxf = 0;
        float avgf = 0;
        //Clear to avoid crash on multiple redraw
        EmptySeries.clear();
        nameSeries.clear();
        dataset.clear();
        //Clear all labels
        multiRenderer.clearXTextLabels();
        multiRenderer.clearYTextLabels();
        multiRenderer.removeAllRenderers();
        //Set position of graph to 0
        //multiRenderer.setXAxisMin(0);
        //Set max position of graph to now
        //multiRenderer.setXAxisMax(new Date().getTime());
        //Adding nameSeries Series to the dataset
        dataset.addSeries(nameSeries);
        dataset.addSeries(EmptySeries);
        //Adding incomeRenderer and emptyRenderer to multipleRenderer
        //Note: The order of adding dataseries to dataset and renderers to multipleRenderer
        //should be same
        multiRenderer.addSeriesRenderer(incomeRenderer);
        multiRenderer.addSeriesRenderer(emptyRenderer);

        Vector<Vector<Float>> values = new Vector<>();
        chartContainer = new LinearLayout(context);
        // Getting a reference to LinearLayout of the MainActivity Layout
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        chartContainer.setGravity(Gravity.CENTER_VERTICAL);
        chartContainer.setPadding((int) size5, (int) size10, (int) size5, (int) size10);

        JSONObject json_GraphValues = null;
        try {
            if (api_version <= 0.6f) {
                Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/from/" + startTimestamp + "/to/" + currentTimestamp + "/interval/" + step + "/selector/avg");
                json_GraphValues = Rest_com.connect_jsonobject(Tracer, url + "stats/" + dev_id + "/" + state_key + "/from/" + startTimestamp + "/to/" + currentTimestamp + "/interval/" + step + "/selector/avg", login, password, 10000, SSL);
            } else if (api_version >= 0.7f) {
                Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/from/" + startTimestamp + "/to/" + currentTimestamp + "/interval/" + step + "/selector/avg");
                //Don't forget old "dev_id"+"state_key" is replaced by "id"
                json_GraphValues = Rest_com.connect_jsonobject(Tracer, url + "sensorhistory/id/" + id + "/from/" + startTimestamp + "/to/" + currentTimestamp + "/interval/" + step + "/selector/avg", login, password, 10000, SSL);
            }

        } catch (Exception e) {
            //return null;
            Tracer.e(mytag, "Error with json");
        }

        JSONArray itemArray = null;
        JSONArray valueArray = new JSONArray();
        if (api_version <= 0.6f) {
            itemArray = json_GraphValues.getJSONArray("stats");
            valueArray = itemArray.getJSONObject(0).getJSONArray("values");
        } else if (api_version >= 0.7f) {
            try {
                valueArray = json_GraphValues.getJSONArray("values");
            } catch (Exception e) {
                //return null;
                Tracer.e(mytag, "Error with json TV_Value");
            }
        }

        int j = 0;
        Boolean ruptur = false;
        Double real_val;
        if (limit == 6) {
            // range between 1 to 8 days (average per hour)
            for (int i = 0; i < valueArray.length() - 1; i++) {
                real_val = valueArray.getJSONArray(i).getDouble(limit - 1);
                real_val = calcul.Round_double(real_val);
                int year = valueArray.getJSONArray(i).getInt(0);
                int month = valueArray.getJSONArray(i).getInt(1);
                int week = valueArray.getJSONArray(i).getInt(2);
                int day = valueArray.getJSONArray(i).getInt(3);
                int hour = valueArray.getJSONArray(i).getInt(4);
                int hour_next = valueArray.getJSONArray(i + 1).getInt(4);
                //String date=String.valueOf(hour)+"'";
                Date date1 = new Date();
                try {
                    date1 = format.parse(String.valueOf(year) + "-"
                            + String.valueOf(month) + "-"
                            + String.valueOf(day) + " "
                            + String.valueOf(hour) + ":00");
                    Tracer.d(mytag, "date1=" + date1);
                    Tracer.d(mytag, "Value=" + real_val);
                } catch (ParseException e) {
                    Tracer.d(mytag, "Error converting date");
                    Tracer.d(mytag, e.toString());
                }
                if (hour != 23 && (hour < hour_next)) {
                    //no day change
                    if ((hour + 1) != hour_next) {
                        //ruptur : simulate next missing steps
                        EmptySeries.add(date1.getTime(), real_val);
                        nameSeries.add(date1.getTime(), real_val);
                        for (int k = 1; k < (hour_next - hour); k++) {
                            nameSeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                            EmptySeries.add(date1.getTime(), real_val);
                        }
                        j = j + (hour_next - hour);
                        ruptur = true;
                    } else {
                        if (ruptur) {
                            EmptySeries.add(date1.getTime(), real_val);
                        } else {
                            EmptySeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                        }
                        ruptur = false;
                        nameSeries.add(date1.getTime(), real_val); //change to j to avoid missing TV_Value
                        j++;
                    }
                } else if (hour == 23) {
                    if (ruptur) {
                        EmptySeries.add(date1.getTime(), real_val);
                    } else {
                        EmptySeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                    }
                    ruptur = false;
                    nameSeries.add(date1.getTime(), real_val); //change to j to avoid missing TV_Value
                    j++;
                }
                if (minf == 0)
                    minf = real_val.floatValue();
                avgf += real_val;    // Get the real 'TV_Value'

                if (real_val > maxf) {
                    maxf = real_val.floatValue();

                }
                if (real_val < minf) {
                    minf = real_val.floatValue();

                }
            }
        } else if (limit == 5) {
            // range between 9 to 32 days (average per day)
            for (int i = 0; i < valueArray.length() - 1; i++) {
                real_val = valueArray.getJSONArray(i).getDouble(limit - 1);
                real_val = calcul.Round_double(real_val);
                int year = valueArray.getJSONArray(i).getInt(0);
                int month = valueArray.getJSONArray(i).getInt(1);
                int day = valueArray.getJSONArray(i).getInt(3);
                int day_next = valueArray.getJSONArray(i + 1).getInt(3);
                //String date=String.valueOf(hour)+"'";
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.DAY_OF_MONTH, day);
                //JAVA calendar month his very strange but start from 0
                //find a way to always get the right month this way
                month = (month - 1);
                //set to 12h because it's an average and much more nice like this.
                calendar.set(Calendar.HOUR, 12);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);
                Date date1 = new Date();
                date1 = calendar.getTime();
                if ((day + 1) != day_next) {
                    //ruptur : simulate next missing steps
                    EmptySeries.add(date1.getTime(), real_val);
                    nameSeries.add(date1.getTime(), real_val);
                    for (int k = 1; k < (day_next - day); k++) {
                        nameSeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                        EmptySeries.add(date1.getTime(), real_val);
                    }
                    j = j + (day_next - day);
                    ruptur = true;
                } else {
                    if (ruptur) {
                        EmptySeries.add(date1.getTime(), real_val);
                        Tracer.d(mytag, "date1=" + date1);
                        Tracer.d(mytag, "Value=" + real_val);
                    } else {
                        EmptySeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                        Tracer.d(mytag, "date1=" + date1);
                        Tracer.d(mytag, "Value=" + real_val);
                    }
                    ruptur = false;
                    nameSeries.add(date1.getTime(), real_val); //change to j to avoid missing TV_Value
                    j++;
                }
                if (minf == 0)
                    minf = real_val.floatValue();
                avgf += real_val;    // Get the real 'TV_Value'

                if (real_val > maxf) {
                    maxf = real_val.floatValue();

                }
                if (real_val < minf) {
                    minf = real_val.floatValue();

                }
            }
        } else if (limit == 3) {
            // (average per week)
            for (int i = 0; i < valueArray.length() - 1; i++) {
                real_val = valueArray.getJSONArray(i).getDouble(limit - 1);
                real_val = calcul.Round_double(real_val);
                int year = valueArray.getJSONArray(i).getInt(0);
                int week = valueArray.getJSONArray(i).getInt(1);
                int week_next = valueArray.getJSONArray(i + 1).getInt(1);
                //String date=String.valueOf(hour)+"'";

                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                //set to thursday because it's an average and much more nice like this.
                calendar.set(Calendar.DAY_OF_WEEK, 5);
                calendar.set(Calendar.WEEK_OF_YEAR, week);
                calendar.set(Calendar.YEAR, year);
                Date date1 = new Date();
                date1 = calendar.getTime();
                if (week != 52 && (week < week_next)) {
                    //no day change
                    if ((week + 1) != week_next) {
                        //ruptur : simulate next missing steps
                        EmptySeries.add(date1.getTime(), real_val);
                        nameSeries.add(date1.getTime(), real_val);
                        for (int k = 1; k < (week_next - week); k++) {
                            nameSeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                            EmptySeries.add(date1.getTime(), real_val);
                        }
                        j = j + (week_next - week);
                        ruptur = true;
                    } else {
                        if (ruptur) {
                            EmptySeries.add(date1.getTime(), real_val);
                            Tracer.d(mytag, "date1=" + date1);
                            Tracer.d(mytag, "Value=" + real_val);
                        } else {
                            EmptySeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                            Tracer.d(mytag, "date1=" + date1);
                            Tracer.d(mytag, "Value=" + real_val);
                        }
                        ruptur = false;
                        nameSeries.add(date1.getTime(), real_val); //change to j to avoid missing TV_Value
                        j++;
                    }
                } else if (week == 52) {
                    if (ruptur) {
                        EmptySeries.add(date1.getTime(), real_val);
                    } else {
                        EmptySeries.add(date1.getTime(), MathHelper.NULL_VALUE);
                    }
                    ruptur = false;
                    nameSeries.add(date1.getTime(), real_val); //change to j to avoid missing TV_Value
                    j++;
                }
                if (minf == 0)
                    minf = real_val.floatValue();
                avgf += real_val;    // Get the real 'TV_Value'

                if (real_val > maxf) {
                    maxf = real_val.floatValue();

                }
                if (real_val < minf) {
                    minf = real_val.floatValue();

                }
            }
        }
        avgf = avgf / values.size();
        multiRenderer.addYTextLabel(((double) minf) - 1, ("" + minf));
        multiRenderer.addYTextLabel(((double) avgf), ("" + avgf));
        multiRenderer.addYTextLabel(((double) maxf), ("" + maxf));
        //SET limit up and down on Y axis
        multiRenderer.setYAxisMin(minf - 1);
        multiRenderer.setYAxisMax(maxf + 1);
        Tracer.d(mytag, "minf (" + dev_id + ")=" + minf);
        Tracer.d(mytag, "maxf (" + dev_id + ")=" + maxf);
        Tracer.d(mytag, "avgf (" + dev_id + ")=" + avgf);
        Tracer.d(mytag, "UpdateThread (" + dev_id + ") Refreshing graph");

        // Specifying chart types to be drawn in the graph
        // Number of data series and number of types should be same
        // Order of data series and chart type will be same
        String types = "dd-MM HH:mm";
        // Creating a Timed chart with the chart types specified in types array
        mChart = ChartFactory.getTimeChartView(context, dataset, multiRenderer, types);
        mChart.setOnClickListener(new OnClickListener() {
                                      //on click is called when pan or zoom movement id ended
                                      public void onClick(View v) {
                                          Tracer.i(mytag + "Pan or zoom", "New X range=[" + multiRenderer.getXAxisMin() + ", " + multiRenderer.getXAxisMax()
                                                  + "]");
                                          //To get the start of the graph after a move and grab new TV_Value
                                          startTimestamp = ((new Date((long) multiRenderer.getXAxisMin())).getTime()) / 1000;
                                          currentTimestamp = ((new Date((long) multiRenderer.getXAxisMax())).getTime()) / 1000;
                                          Tracer.i(mytag, "Period from " + startTimestamp + " to " + currentTimestamp);
                                          Tracer.i(mytag, "Differcence= " + (currentTimestamp - startTimestamp));
                                          //period_type=1;
                                          long difference = currentTimestamp - startTimestamp;
                                          //Avoid graph to go in the future.
                                          if (currentTimestamp > (System.currentTimeMillis() / 1000)) {
                                              multiRenderer.setXAxisMax(System.currentTimeMillis());
                                              multiRenderer.setXAxisMin(System.currentTimeMillis() - (difference * 1000));
                                              startTimestamp = ((new Date((long) multiRenderer.getXAxisMin())).getTime()) / 1000;
                                              currentTimestamp = ((new Date((long) multiRenderer.getXAxisMax())).getTime()) / 1000;
                                          }
                                          if (difference < 604800) {
                                              period_type = 8;
                                          } else if (difference < 2419200) {
                                              period_type = 31;
                                          } else {
                                              period_type = 33;
                                          }
                                          compute_period();
                                          try {
                                              drawgraph();
                                          } catch (JSONException e) {
                                              e.printStackTrace();
                                          }
                                          mChart.refreshDrawableState();
                                      }
                                  }
        );

        // Adding the Combined Chart to the LinearLayout
        chartContainer.addView(mChart);
    }

    public void onClick(View arg0) {
        if (with_graph) {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = Float_graph_size * context.getResources().getDisplayMetrics().density + 0.5f;
            int sizeint = (int) size;
            if (LL_background.getHeight() != sizeint) {
                try {
                    LL_background.removeView(chartContainer);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {

                    period_type = 1;    //by default, display 24 hours
                    compute_period();    //To initialize time_start & time_end
                    sav_period = period_type;        //Save the current graph period
                    startTimestamp = time_start.getTime() / 1000;
                    currentTimestamp = time_end.getTime() / 1000;
                    drawgraph();
                } catch (JSONException e) {
                    Tracer.d(mytag, "Acharengine failed" + e.toString());
                }
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
                LL_background.addView(chartContainer);

            } else {
                LL_background.removeView(chartContainer);
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            }
        }
    }
}
