/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pixmob.freemobile.netstat.MonitorService;
import org.pixmob.freemobile.netstat.R;
import org.pixmob.freemobile.netstat.content.NetstatContract.Events;
import org.pixmob.freemobile.netstat.content.Statistics;
import org.pixmob.freemobile.netstat.content.StatisticsLoader;
import org.pixmob.freemobile.netstat.util.DateUtils;

import static org.pixmob.freemobile.netstat.BuildConfig.DEBUG;
import static org.pixmob.freemobile.netstat.Constants.TAG;

/**
 * Fragment showing statistics using charts.
 * @author Pixmob
 */
public class StatisticsFragment extends Fragment implements LoaderCallbacks<Statistics> {
    private static final String STAT_NO_VALUE = "-";
    private static ExportTask exportTask;
    private ContentObserver contentMonitor;
    private View statisticsGroup;
    private ProgressBar progressBar;
    private MobileNetworkChart mobileNetworkChart;
    private BatteryChart batteryChart;
    private TextView onFreeMobileNetwork;
    private TextView onOrangeNetwork;
    private TextView statMobileNetwork;
    private TextView statMobileCode;
    private TextView statScreenOn;
    private TextView statWifiOn;
    private TextView statOnOrange;
    private TextView statOnFreeMobile;
    private TextView statOnFemtocell;
    private TextView statBattery;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (exportTask != null) {
            exportTask.setFragmentManager(getFragmentManager());
        }

        // Monitor database updates: when new data is available, this fragment
        // is updated with the new values.
        contentMonitor = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                Log.i(TAG, "Content updated: refresh statistics");
                refresh();
            }
        };

        // Get widgets.
        final Activity a = getActivity();
        statisticsGroup = a.findViewById(R.id.statistics);
        progressBar = (ProgressBar) a.findViewById(R.id.states_progress);
        mobileNetworkChart = (MobileNetworkChart) a.findViewById(R.id.mobile_network_chart);
        batteryChart = (BatteryChart) a.findViewById(R.id.battery_chart);
        onOrangeNetwork = (TextView) a.findViewById(R.id.on_orange_network);
        onFreeMobileNetwork = (TextView) a.findViewById(R.id.on_free_mobile_network);
        statMobileNetwork = (TextView) a.findViewById(R.id.stat_mobile_network);
        statMobileCode = (TextView) a.findViewById(R.id.stat_mobile_code);
        statScreenOn = (TextView) a.findViewById(R.id.stat_screen);
        statWifiOn = (TextView) a.findViewById(R.id.stat_wifi);
        statOnOrange = (TextView) a.findViewById(R.id.stat_on_orange);
        statOnFreeMobile = (TextView) a.findViewById(R.id.stat_on_free_mobile);
        statOnFemtocell = (TextView) a.findViewById(R.id.stat_on_femtocell);
        statBattery = (TextView) a.findViewById(R.id.stat_battery);
        
        // The fields are hidden the first time this fragment is displayed,
        // while statistics data are being loaded.
        statisticsGroup.setVisibility(View.INVISIBLE);

        setHasOptionsMenu(true);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_statistics, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.menu_export:
	            onMenuExport();
	            return true;
	        case R.id.menu_preferences:
	            onMenuPreferences();
	            return true;
	        case R.id.menu_quit:
	        	onMenuQuit();
	        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void onMenuExport() {
        if (ContextCompat.checkSelfPermission(getActivity(), Netstat.EXPORT_TASK_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            launchExportTask();
        }
        else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Netstat.EXPORT_TASK_PERMISSION},
                    Netstat.EXPORT_TASK_PERMISSION_CODE
            );
        }
    }

    public void launchExportTask() {
        exportTask = new ExportTask(getActivity().getApplicationContext(), getFragmentManager());
        exportTask.execute();
    }

    private void onMenuPreferences() {
        startActivity(new Intent(getActivity(), Preferences.class));
    }

    private void onMenuQuit() {
    	getActivity().stopService(new Intent(getActivity().getApplicationContext(), MonitorService.class));
    	getActivity().finish();
    	System.exit(0);
	}

    @Override
    public void onResume() {
        super.onResume();
        // Monitor database updates if the fragment is displayed.
        final ContentResolver cr = getActivity().getContentResolver();
        cr.registerContentObserver(Events.CONTENT_URI, true, contentMonitor);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop monitoring database updates.
        getActivity().getContentResolver().unregisterContentObserver(contentMonitor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics_fragment, container, false);
    }

    private void refresh() {
        if ((isDetached()) || (!isAdded())) { //handle case if fragment is not properly attached to the parent activity
            return;
        }
        if (getLoaderManager().hasRunningLoaders()) {
            if (DEBUG) {
                Log.d(TAG, "Skip statistics refresh: already running");
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "Refresh statistics");
        }
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Statistics> onCreateLoader(int id, Bundle args) {
        return new StatisticsLoader(getActivity());
    }

    @Override
    public void onLoaderReset(Loader<Statistics> loader) {
    }

    @Override
    public void onLoadFinished(Loader<Statistics> loader, Statistics s) {
        Log.i(TAG, "Statistics loaded: " + s);

        onOrangeNetwork.setText(s.orangeUsePercent + "%");
        onFreeMobileNetwork.setText(s.freeMobileUsePercent + "%");
        
        mobileNetworkChart.clear();
        PieChartView.PieChartComponent orange =
        		mobileNetworkChart.new PieChartComponent(R.color.orange_network_color1, R.color.orange_network_color2,
        				s.orangeUsePercent);
        PieChartView.PieChartComponent freeMobile =
        		mobileNetworkChart.new PieChartComponent(R.color.free_mobile_network_color1, R.color.free_mobile_network_color2,
        				s.freeMobileUsePercent);
        mobileNetworkChart.addPieChartComponent(freeMobile);
        mobileNetworkChart.addPieChartComponent(orange);

        final Activity a = getActivity();
        statMobileNetwork.setText(s.mobileOperator == null ? STAT_NO_VALUE : s.mobileOperator.toName(a));
        statMobileCode.setText(s.mobileOperatorCode == null ? STAT_NO_VALUE : s.mobileOperatorCode);
        setDurationText(statScreenOn, s.screenOnTime);
        setDurationText(statWifiOn, s.wifiOnTime);
        setDurationText(statOnOrange, s.orangeTime);
        setDurationText(statOnFreeMobile, s.freeMobileTime);
        setDurationText(statOnFemtocell, s.femtocellTime);

        statBattery.setText(s.battery == 0 ? STAT_NO_VALUE : String.valueOf(s.battery) + "%");

        batteryChart.setData(s.events);

        progressBar.setVisibility(View.INVISIBLE);
        statisticsGroup.setVisibility(View.VISIBLE);
        statisticsGroup.invalidate();
        mobileNetworkChart.invalidate();
        batteryChart.invalidate();
    }

    private void setDurationText(TextView tv, long duration) {
        if (duration < 1) {
            tv.setText(STAT_NO_VALUE);
        } else {
            tv.setText(formatDuration(duration));
        }
    }

    /**
     * Return a formatted string for a duration value.
     */
    private CharSequence formatDuration(long duration) {
        return DateUtils.formatDuration(duration, getActivity(), STAT_NO_VALUE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (exportTask != null) {
            exportTask.onSavedInstanceCalled();
        }
        super.onSaveInstanceState(outState);
    }
}
