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

import java.util.Timer;
import java.util.TimerTask;

import activities.Gradients_Manager;

import org.domogik.domodroid13.R;

import rinor.Stats_Com;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import misc.tracerengine;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class Com_Stats extends FrameLayout {


    private FrameLayout imgPan;
    private LinearLayout featurePan;
    private LinearLayout featurePan2;
    private View featurePan2_buttons;
    private LinearLayout infoPan;
    private ImageView img;
    private int id;
    private final Handler handler;
    private Message msg;
    public FrameLayout container = null;

    private final Stats_Com stats = null;
    private final TextView elapsed_period;
    private final TextView cumul_period;
    private final TextView cum_statsPR;
    private final TextView cum_statsBR;
    private final TextView cum_statsPS;
    private final TextView cum_statsBS;
    private final TextView cum_eventsPR;
    private final TextView cum_eventsBR;
    private final TextView cum_eventsPS;
    private final TextView cum_eventsBS;

    private final TextView period_statsPR;
    private final TextView period_statsBR;
    private final TextView period_statsPS;
    private final TextView period_statsBS;
    private final TextView period_eventsPR;
    private final TextView period_eventsBR;
    private final TextView period_eventsPS;
    private final TextView period_eventsBS;

    public Com_Stats(tracerengine Tracer, Activity context, int widgetSize) {
        super(context);

        String mytag = "Com_Stats";
        this.setPadding(5, 5, 5, 5);
        Tracer.i(mytag, "New instance");

        //panel with border
        LinearLayout background = new LinearLayout(context);
        background.setOrientation(LinearLayout.VERTICAL);
        if (widgetSize == 0)
            background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        else
            background.setLayoutParams(new LayoutParams(widgetSize, LayoutParams.WRAP_CONTENT));
        background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white", background.getHeight()));

        //panel with border
        LinearLayout topPan = new LinearLayout(context);
        topPan.setOrientation(LinearLayout.HORIZONTAL);
        topPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        //topPan.setTextColor(Color.parseColor("#333333"));

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.com_stats_2, null);

        topPan.addView(view);
        background.addView(topPan);

        this.addView(background);
        elapsed_period = (TextView) findViewById(R.id.textPeriodValue);
        cumul_period = (TextView) findViewById(R.id.textCumulValue);
        cum_statsPR = (TextView) findViewById(R.id.statsPR);
        cum_statsBR = (TextView) findViewById(R.id.statsBR);
        cum_statsPS = (TextView) findViewById(R.id.statsPS);
        cum_statsBS = (TextView) findViewById(R.id.statsBS);
        cum_eventsPR = (TextView) findViewById(R.id.eventsPR);
        cum_eventsBR = (TextView) findViewById(R.id.eventsBR);
        cum_eventsPS = (TextView) findViewById(R.id.eventsPS);
        cum_eventsBS = (TextView) findViewById(R.id.eventsBS);

        period_statsPR = (TextView) findViewById(R.id.PstatsPR);
        period_statsBR = (TextView) findViewById(R.id.PstatsBR);
        period_statsPS = (TextView) findViewById(R.id.PstatsPS);
        period_statsBS = (TextView) findViewById(R.id.PstatsBS);
        period_eventsPR = (TextView) findViewById(R.id.PeventsPR);
        period_eventsBR = (TextView) findViewById(R.id.PeventsBR);
        period_eventsPS = (TextView) findViewById(R.id.PeventsPS);
        period_eventsBS = (TextView) findViewById(R.id.PeventsBS);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    //Message from timer expired
                    if (stats != null) {
                        cumul_period.setText(stats.get_cumul_period());
                        elapsed_period.setText(stats.get_elapsed_period());
                        cum_statsPR.setText(Integer.toString(Stats_Com.cumul_stats_recv_packets));
                        cum_statsBR.setText(Integer.toString(Stats_Com.cumul_stats_recv_bytes));
                        cum_statsPS.setText(Integer.toString(Stats_Com.cumul_stats_sent_packets));
                        cum_statsBS.setText(Integer.toString(Stats_Com.cumul_stats_sent_bytes));
                        cum_eventsPR.setText(Integer.toString(Stats_Com.cumul_events_recv_packets));
                        cum_eventsBR.setText(Integer.toString(Stats_Com.cumul_events_recv_bytes));
                        cum_eventsPS.setText(Integer.toString(Stats_Com.cumul_events_sent_packets));
                        cum_eventsBS.setText(Integer.toString(Stats_Com.cumul_events_sent_bytes));

                        period_statsPR.setText(Integer.toString(Stats_Com.periodic_stats_recv_packets));
                        period_statsBR.setText(Integer.toString(Stats_Com.periodic_stats_recv_bytes));
                        period_statsPS.setText(Integer.toString(Stats_Com.periodic_stats_sent_packets));
                        period_statsBS.setText(Integer.toString(Stats_Com.periodic_stats_sent_bytes));
                        period_eventsPR.setText(Integer.toString(Stats_Com.periodic_events_recv_packets));
                        period_eventsBR.setText(Integer.toString(Stats_Com.periodic_events_recv_bytes));
                        period_eventsPS.setText(Integer.toString(Stats_Com.periodic_events_sent_packets));
                        period_eventsBS.setText(Integer.toString(Stats_Com.periodic_events_sent_bytes));
                    }
                }
            }

        };
        Tracer.i(mytag, "Instance created");
        Timer();


    }

    private void Timer() {
        Timer timer = new Timer();

        TimerTask doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    //Tracer.e(mytag,"Update statistics.."+stats.elapsed_period);
                    handler.sendEmptyMessage(0);

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }

        };
        if (timer != null) {
            timer.schedule(doAsynchronousTask, 0, 5000);    // Once per 5 seconds
            doAsynchronousTask.run();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

        }
    }


}



