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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Activity_Cam;
import activities.Activity_Main;
import database.WidgetUpdate;
import misc.tracerengine;

public class Graphical_Cam extends Basic_Graphical_widget implements OnClickListener {

    private String url;
    private final Context context;
    private static String mytag;
    private tracerengine Tracer = null;
    public static FrameLayout container = null;
    private static FrameLayout myself = null;
    private String name_cam;
    private final Entity_Feature feature;
    private final SharedPreferences params;
    private final int session_type;
    private Boolean realtime = false;
    Activity activity;

    public Graphical_Cam(tracerengine Trac,
                         final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                         final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.Tracer = Trac;
        this.context = context;
        this.activity = context;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Cam(tracerengine Trac,
                         final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                         final Entity_Map feature_map, Handler handler) {
        super(params, context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.Tracer = Trac;
        this.context = context;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        myself = this;
        this.url = feature.getAddress();
        int dev_id = feature.getDevId();
        this.name_cam = feature.getName();
        String state_key = feature.getState_key();
        mytag = "Graphical_Cam(" + dev_id + ")";
        setOnClickListener(this);
        //To have the icon colored as it has no state
        change_this_icon(2);
        //handler to listen value change
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String status;
                if (msg.what == 9999) {
                    if (session == null)
                        return;
                    status = session.getValue();
                    if (status != null) {
                        Tracer.d(mytag, "Handler receives a new status <" + status + ">");
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
                        Tracer.e(mytag, "Error in deleting container");
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
        if (cache_engine != null)

        {
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

    public void onClick(View v) {
        try {
            if (url.equals("Mjpeg video url") || url.equals("Virtual Video"))
                url = session.getValue();
            if (!url.equals(null)) {
                Intent intent = new Intent(context, Activity_Cam.class);
                Bundle b = new Bundle();
                b.putString("url", url);
                //Tracer.i(mytag, "Opening camera at: " + url);
                b.putString("name", name_cam);
                intent.putExtras(b);
                int requestCode = 1;
                if (context.toString().contains("Main")) {
                    activity.startActivityForResult(intent, requestCode);
                } else if (context.toString().contains("Map")) {
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }
    }
}
