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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import Abstract.display_sensor_info;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Graphics_Manager;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.Rest_com;

public class Graphical_History extends Basic_Graphical_widget implements OnClickListener {


    private ListView listeChoices;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private TextView state;
    private int id;
    private static String mytag;
    private Message msg;
    private String url = null;

    public static FrameLayout container = null;
    private static FrameLayout myself = null;

    private Boolean realtime = false;
    private Animation animation;
    private final Entity_Feature feature;
    private String state_key;
    private int dev_id;
    private final int session_type;
    private final SharedPreferences params;

    public Graphical_History(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_History(tracerengine Trac,
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
        String parameters = feature.getParameters();
        this.dev_id = feature.getDevId();
        this.state_key = feature.getState_key();
        this.id = feature.getId();
        myself = this;
        String stateS = "";
        mytag = "Graphical_History(" + dev_id + ")";
        try {
            stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            stateS = state_key;
        }
        setOnClickListener(this);

        //state key
        TextView state_key_view = new TextView(context);
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

        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        super.LL_featurePan.addView(TV_Value);
        super.LL_featurePan.addView(TV_Timestamp);
        super.LL_infoPan.addView(state_key_view);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String status;
                if (msg.what == 9999) {
                    if (session == null)
                        return;
                    String new_val = session.getValue();
                    String Value_timestamp = session.getTimestamp();
                    Tracer.d(mytag, "Handler receives a new TV_Value <" + new_val + "> at " + Value_timestamp);
                    TV_Value.setAnimation(animation);

                    //Value_timestamp = timestamp_to_relative_time.get_relative_time(Value_timestamp);
                    Long Value_timestamplong = null;
                    Value_timestamplong = Value_timestamplong.valueOf(Value_timestamp) * 1000;

                    display_sensor_info.display(Tracer, new_val, Value_timestamplong, mytag, feature.getParameters(), TV_Value, TV_Timestamp, context, LL_featurePan, null, null, state_key, null, null, null);

                    //To have the icon colored as it has no state
                    change_this_icon(2);

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
        if (cache_engine != null) {
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

    }

    private void getlastvalue() {
        JSONObject json_LastValues = null;
        JSONArray itemArray = null;
        listeChoices = new ListView(context);
        ArrayList<HashMap<String, String>> listItem = new ArrayList<>();
        try {
            if (api_version <= 0.6f) {
                Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/last/5/");
                json_LastValues = Rest_com.connect_jsonobject(Tracer, url + "stats/" + dev_id + "/" + state_key + "/last/5/", login, password, 10000, SSL);
            } else if (api_version >= 0.7f) {
                Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/last/5");
                //Don't forget old "dev_id"+"state_key" is replaced by "id"
                JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(Tracer, url + "sensorhistory/id/" + id + "/last/5", login, password, 10000, SSL);
                json_LastValues = new JSONObject();
                json_LastValues.put("stats", json_LastValues_0_4);

            }
            itemArray = json_LastValues.getJSONArray("stats");
            if (api_version <= 0.6f) {
                for (int i = itemArray.length(); i >= 0; i--) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("TV_Value", itemArray.getJSONObject(i).getString("TV_Value"));
                        map.put("date", itemArray.getJSONObject(i).getString("date"));
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json TV_Value");
                    }
                }
            } else if (api_version == 0.7f) {
                for (int i = 0; i < itemArray.length(); i++) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("TV_Value", itemArray.getJSONObject(i).getString("value_str"));
                        map.put("date", itemArray.getJSONObject(i).getString("date"));
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json TV_Value");
                    }
                }
            } else if (api_version >= 0.8f) {
                //Prepare timestamp conversion
                Calendar calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getDefault();
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date currenTimeZone;
                for (int i = 0; i < itemArray.length(); i++) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("TV_Value", itemArray.getJSONObject(i).getString("value_str"));
                        currenTimeZone = new java.util.Date((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                        map.put("date", sdf.format(currenTimeZone));
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json TV_Value");
                    }
                }
            }

        } catch (Exception e) {
            //return null;
            Tracer.e(mytag, "Error fetching json object");
        }

        SimpleAdapter adapter_feature = new SimpleAdapter(this.context, listItem,
                R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.phone_value, R.id.phone_date});
        listeChoices.setAdapter(adapter_feature);
        listeChoices.setScrollingCacheEnabled(false);
    }

    public void onClick(View arg0) {
        //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
        float size = 262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
        int sizeint = (int) size;
        if (LL_background.getHeight() != sizeint) {
            Tracer.d(mytag, "on click");
            try {
                LL_background.removeView(listeChoices);
                Tracer.d(mytag, "removeView(listeChoices)");

            } catch (Exception e) {
                e.printStackTrace();
            }
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
            getlastvalue();
            Tracer.d(mytag, "addView(listeChoices)");
            LL_background.addView(listeChoices);
        } else {
            LL_background.removeView(listeChoices);
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }


}



