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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.display_sensor_info;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Graphics_Manager;
import database.WidgetUpdate;
import misc.tracerengine;

public class Graphical_Info extends Basic_Graphical_widget implements OnClickListener {


    private LinearLayout featurePan2;
    private View featurePan2_buttons;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private Graphical_Info_View canvas;
    private Message msg;
    private static String mytag;
    public static FrameLayout container = null;
    private static FrameLayout myself = null;
    public Boolean with_graph = true;
    private Boolean realtime = false;
    private final Entity_Feature feature;
    private String state_key;
    private String timestamp;
    private String parameters;
    private final int session_type;
    private final SharedPreferences params;
    private String url = null;
    private int dpiClassification;
    private final int update;
    private TextView state_key_view;
    private String stateS;
    private String test_unite;
    private Typeface typefaceweather;
    private Typeface typefaceawesome;
    private float Float_graph_size;

    public Graphical_Info(tracerengine Trac,
                          final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params, final int update,
                          final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        this.update = update;
        onCreate();
    }

    public Graphical_Info(tracerengine Trac,
                          final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params, final int update,
                          final Entity_Map feature_map, Handler handler) {
        super(params, context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.url = url;
        this.session_type = session_type;
        this.params = params;
        this.update = update;
        onCreate();
    }

    private void onCreate() {
        this.parameters = feature.getParameters();
        int dev_id = feature.getDevId();
        this.state_key = feature.getState_key();
        mytag = "Graphical_Info (" + dev_id + ")";
        String graph_size = params.getString("graph_size", "262.5");
        this.Float_graph_size = Float.valueOf(graph_size);
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
        float size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        float size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);

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

        TV_Timestamp = new RelativeTimeTextView(context, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        typefaceweather = Typeface.createFromAsset(context.getAssets(), "fonts/weathericons-regular-webfont.ttf");
        typefaceawesome = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");

        if (with_graph) {

            //feature panel 2 which will contain graphic
            featurePan2 = new LinearLayout(context);
            featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            featurePan2.setGravity(Gravity.CENTER_VERTICAL);
            featurePan2.setPadding(5, 10, 5, 10);
            //canvas
            canvas = new Graphical_Info_View(Tracer, context, params, parameters);
            canvas.dev_id = dev_id;
            canvas.id = feature.getId();
            canvas.state_key = state_key;
            canvas.url = url;
            canvas.update = update;

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            featurePan2_buttons = layoutInflater.inflate(R.layout.graph_buttons, null);
            View v = null;

            v = featurePan2_buttons.findViewById(R.id.bt_prev);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_next);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_year);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_month);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_week);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_day);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.period);
            if (v != null)
                canvas.dates = (TextView) v;

            //background_stats.addView(canvas);
            featurePan2.addView(canvas);
        }

        LL_featurePan.addView(TV_Value);
        LL_featurePan.addView(TV_Timestamp);

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

                    String loc_Value = session.getValue();
                    String Value_timestamp = session.getTimestamp();
                    Tracer.d(mytag, "Handler receives a new TV_Value <" + loc_Value + "> at " + Value_timestamp);

                    //Value_timestamp = timestamp_to_relative_time.get_relative_time(Value_timestamp);
                    Long Value_timestamplong = null;
                    Value_timestamplong = Value_timestamplong.valueOf(Value_timestamp) * 1000;

                    display_sensor_info.display(Tracer, loc_Value, Value_timestamplong, mytag, parameters, TV_Value, TV_Timestamp, context, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, test_unite);
                    //Todo display timestamp

                    //Change icon if in %
                    if ((state_key.equalsIgnoreCase("humidity")) || (state_key.equalsIgnoreCase("percent")) || (test_unite.equals("%"))) {
                        if (Float.parseFloat(loc_Value) >= 60) {
                            //To have the icon colored if TV_Value beetwen 30 and 60
                            change_this_icon(2);
                        } else if (Float.parseFloat(loc_Value) >= 30) {
                            //To have the icon colored if TV_Value >30
                            change_this_icon(1);
                        } else {
                            //To have the icon colored if TV_Value <30
                            change_this_icon(0);
                        }
                    } else {
                        // #93
                        if (loc_Value.equals("off") || loc_Value.equals("false") || loc_Value.equals("0") || loc_Value.equals("0.0")) {
                            change_this_icon(0);
                            //set featuremap.state to 1 so it could select the correct icon in entity_map.get_ressources
                        } else change_this_icon(2);
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
        LL_infoPan.addView(state_key_view);
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our TV_Value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            if (api_version <= 0.6f) {
                session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            } else if (api_version >= 0.7f) {
                session = new Entity_client(feature.getId(), "", mytag, handler, session_type);
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // View is now detached, and about to be destroyed
        try {
            Tracer.get_engine().unsubscribe(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(View arg0) {
        if (with_graph) {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = Float_graph_size * context.getResources().getDisplayMetrics().density + 0.5f;
            int sizeint = (int) size;
            if (LL_background.getHeight() != sizeint) {
                try {
                    LL_background.removeView(featurePan2_buttons);
                    LL_background.removeView(featurePan2);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
                LL_background.addView(featurePan2_buttons);
                LL_background.addView(featurePan2);
                canvas.activate = true;
                canvas.updateTimer();
            } else {
                LL_background.removeView(featurePan2_buttons);
                LL_background.removeView(featurePan2);
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                canvas.activate = false;    //notify Graphical_Info_View to stop its UpdateTimer
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

        }
    }

}



