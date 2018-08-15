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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.json.JSONObject;

import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Graphics_Manager;
import database.WidgetUpdate;
import misc.tracerengine;

@SuppressWarnings("ALL")
public class Graphical_Boolean extends Basic_Graphical_widget {

    private TextView state;
    private RelativeTimeTextView TV_Timestamp;
    private String value0;
    private String value1;
    private String Value_0;
    private String Value_1;
    private ImageView bool;
    private static String mytag;
    private Message msg;
    private String stateS = "";
    public static FrameLayout container = null;
    private static FrameLayout myself = null;
    private Entity_Feature feature;
    private String state_key;
    private String parameters;
    private int dev_id;
    private final int session_type;
    private final SharedPreferences params;
    private final String url;
    private String usage;
    private String address;
    private Boolean realtime = false;

    public Graphical_Boolean(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Boolean(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Map feature_map, Handler handler) {
        super(params, context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.url = url;
        this.session_type = session_type;
        this.params = params;
        onCreate();
    }

    public void onCreate() {
        myself = this;
        this.address = feature.getAddress();
        this.usage = feature.getIcon_name();
        this.state_key = feature.getState_key();
        this.dev_id = feature.getDevId();
        this.parameters = feature.getParameters();
        mytag = "Graphical_Boolean(" + dev_id + ")";

        try {
            this.stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            this.stateS = state_key;
        }

        try {
            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            value0 = jparam.getString("value0");
            value1 = jparam.getString("value1");
        } catch (Exception e) {
            value0 = "0";
            value1 = "1";
        }

        if (usage.equals("light")) {
            this.Value_0 = getResources().getText(R.string.light_stat_0).toString();
            this.Value_1 = getResources().getText(R.string.light_stat_1).toString();
        } else if (usage.equals("shutter")) {
            this.Value_0 = getResources().getText(R.string.shutter_stat_0).toString();
            this.Value_1 = getResources().getText(R.string.shutter_stat_1).toString();
        } else {
            this.Value_0 = value0;
            this.Value_1 = value1;
        }

        //state
        state = new TextView(context);
        state.setTextColor(Color.BLACK);
        try {
            Tracer.d(mytag, "Try to get value translate from R.STRING");
            state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_0.toLowerCase())));
        } catch (Exception e1) {
            Tracer.d(mytag, "no translation for: " + Value_0);
            state.setText(stateS + " : " + Value_0);
        }

        TV_Timestamp = new RelativeTimeTextView(context, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;

        //boolean on/off
        bool = new ImageView(context);
        bool.setImageResource(R.drawable.boolean_off);
        bool.setLayoutParams(params);

        super.LL_infoPan.addView(state);
        super.LL_featurePan.addView(bool);
        super.LL_featurePan.addView(TV_Timestamp);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                 if (msg.what == 9999) {
                    if (session == null)
                        return;
                     String status = session.getValue();
                     String Value_timestamp = session.getTimestamp();

                    if (status != null) {
                        Tracer.d(mytag, "Handler receives a new TV_Value <" + status + "> at " + Value_timestamp);

                        //Value_timestamp = timestamp_to_relative_time.get_relative_time(Value_timestamp);
                        Long Value_timestamplong = null;
                        Value_timestamplong = Value_timestamplong.valueOf(Value_timestamp) * 1000;

                        TV_Timestamp.setReferenceTime(Value_timestamplong);

                        try {
                            if (status.equals(value0) || status.equals("0")) {
                                bool.setImageResource(R.drawable.boolean_off);
                                //change color if statue=low to (usage, o) means off
                                //note sure if it must be kept as set previously as default color.
                                change_this_icon(0);
                                try {
                                    Tracer.d(mytag, "Try to get value translate from R.STRING");
                                    state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_0.toLowerCase())));
                                } catch (Exception e1) {
                                    Tracer.d(mytag, "no translation for: " + Value_0);
                                    state.setText(stateS + " : " + Value_0);
                                }
                            } else if (status.equals(value1) || status.equals("1")) {
                                bool.setImageResource(R.drawable.boolean_on);
                                //change color if statue=high to (usage, 2) means on
                                change_this_icon(2);
                                try {
                                    Tracer.d(mytag, "Try to get value translate from R.STRING");
                                    state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_1.toLowerCase())));
                                } catch (Exception e1) {
                                    Tracer.d(mytag, "no translation for: " + Value_1);
                                    state.setText(stateS + " : " + Value_1);
                                }
                            }
                        } catch (Exception e) {
                            Tracer.e(mytag, "handler error device " + name);
                            e.printStackTrace();
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
                    }    //kill the handler thread itself
                }

            }

        };
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
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
                    //each time our value change, the engine will call handler
                    handler.sendEmptyMessage(9999);    //Force to consider current value in session
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....
    }

}



