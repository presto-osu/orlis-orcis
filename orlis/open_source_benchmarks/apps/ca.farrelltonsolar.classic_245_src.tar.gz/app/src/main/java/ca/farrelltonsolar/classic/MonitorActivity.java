/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import ca.farrelltonsolar.uicomponents.SlidingTabLayout;
import ca.farrelltonsolar.uicomponents.TabStripAdapter;

public class MonitorActivity extends ActionBarActivity {

    private NavigationDrawerFragment navigationDrawerFragment;
    private TabStripAdapter tabStripAdapter;
    private int currentChargeState = -1;
    private boolean isReceiverRegistered;
    private SlidingTabLayout stl;
    private ViewPager viewPager;
    public ArrayList<RecordEntry> record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        DrawerLayout layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, layout);
        stl = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        stl.setDividerColors(Color.RED);
        stl.setSelectedIndicatorColors(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW);
        viewPager = (ViewPager) findViewById(R.id.pager);
        setupActionBar();
        if(savedInstanceState != null && savedInstanceState.containsKey("record")) {
            record = savedInstanceState.getParcelableArrayList("record");
        }
        else {
            record = new ArrayList<>();
        }
        Log.d(getClass().getName(), "onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("record", record);
    }

    private void setupActionBar() {

        tabStripAdapter = new TabStripAdapter(getFragmentManager(), this, viewPager, stl, null);
        ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
        if (cc != null && cc.deviceType() == DeviceType.Classic) {
            if (cc.hasWhizbang()) {
                if (MonitorApplication.chargeControllers().showSystemView()) {
                    tabStripAdapter.addTab("StateOfCharge", StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
                    tabStripAdapter.addTab("System", SystemFragment.TabTitle, SystemFragment.class, null);
                    tabStripAdapter.addTab("Power", PowerFragment.TabTitle, PowerFragment.class, null);
                    tabStripAdapter.addTab("Energy", EnergyFragment.TabTitle, EnergyFragment.class, null);
                    tabStripAdapter.addTab("Capacity", CapacityFragment.TabTitle, CapacityFragment.class, null);
                }
                else {
                    tabStripAdapter.addTab("StateOfCharge", StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
                    tabStripAdapter.addTab("Load", LoadFragment.TabTitle, LoadFragment.class, null);
                    tabStripAdapter.addTab("Power", PowerFragment.TabTitle, PowerFragment.class, null);
                    tabStripAdapter.addTab("Energy", EnergyFragment.TabTitle, EnergyFragment.class, null);
                    tabStripAdapter.addTab("Capacity", CapacityFragment.TabTitle, CapacityFragment.class, null);
                }
            }
            else {
                tabStripAdapter.addTab("Power", PowerFragment.TabTitle, PowerFragment.class, null);
                tabStripAdapter.addTab("Energy", EnergyFragment.TabTitle, EnergyFragment.class, null);
            }
            tabStripAdapter.addTab("RealTimeChart", R.string.RealTimeChartTabTitle, RealTimeChartFragment.class, null);
            tabStripAdapter.addTab("Temperature", TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            tabStripAdapter.addTab("DayChart", R.string.DayChartTabTitle, DayLogChart.class, null);
            tabStripAdapter.addTab("HourChart", R.string.HourChartTabTitle, HourLogChart.class, null);
            addDayLogCalendar();
            tabStripAdapter.addTab("Info", R.string.InfoTabTitle, InfoFragment.class, null);
            tabStripAdapter.addTab("Messages", R.string.MessagesTabTitle, MessageFragment.class, null);
            tabStripAdapter.addTab("About", R.string.About, About.class, null);
        } else if (cc != null && cc.deviceType() == DeviceType.Kid) {
            tabStripAdapter.addTab("Power", PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab("Energy", EnergyFragment.TabTitle, EnergyFragment.class, null);
            if (cc.hasWhizbang()) {
                tabStripAdapter.addTab("StateOfCharge", StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
            }
            tabStripAdapter.addTab("RealTimeChart", R.string.RealTimeChartTabTitle, RealTimeChartFragment.class, null);
            tabStripAdapter.addTab("Info", R.string.InfoTabTitle, InfoFragment.class, null);
            tabStripAdapter.addTab("About", R.string.About, About.class, null);
        }
        else if (cc != null && cc.deviceType() == DeviceType.TriStar) {
            tabStripAdapter.addTab("Power", PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab("Energy", EnergyFragment.TabTitle, EnergyFragment.class, null);
            tabStripAdapter.addTab("RealTimeChart", R.string.RealTimeChartTabTitle, RealTimeChartFragment.class, null);
            tabStripAdapter.addTab("Temperature", TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            tabStripAdapter.addTab("About", R.string.About, About.class, null);
        }
        else {
            tabStripAdapter.addTab("Power", PowerFragment.TabTitle, PowerFragment.class, null);
            tabStripAdapter.addTab("Energy", EnergyFragment.TabTitle, EnergyFragment.class, null);
            tabStripAdapter.addTab("StateOfCharge", StateOfChargeFragment.TabTitle, StateOfChargeFragment.class, null);
            tabStripAdapter.addTab("RealTimeChart", R.string.RealTimeChartTabTitle, RealTimeChartFragment.class, null);
            tabStripAdapter.addTab("Temperature", TemperatureFragment.TabTitle, TemperatureFragment.class, null);
            tabStripAdapter.addTab("DayChart", R.string.DayChartTabTitle, DayLogChart.class, null);
            tabStripAdapter.addTab("HourChart",R.string.HourChartTabTitle, HourLogChart.class, null);
            addDayLogCalendar();
            tabStripAdapter.addTab("Info", R.string.InfoTabTitle, InfoFragment.class, null);
            tabStripAdapter.addTab("Messages", R.string.MessagesTabTitle, MessageFragment.class, null);
            tabStripAdapter.addTab("About", R.string.About, About.class, null);
        }
        tabStripAdapter.notifyTabsChanged();
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem pvOutput = menu.findItem(R.id.action_pvOutput);
        if(pvOutput != null)
        {
            pvOutput.setVisible(MonitorApplication.chargeControllers().uploadToPVOutput());
        }
        return true;
    }

    private void addDayLogCalendar() {
        if (Build.VERSION.SDK_INT >= 17) {
            tabStripAdapter.addTab("Calendar", R.string.DayLogTabTitle, MonthCalendarPager.class, null);
        }
        else {
            tabStripAdapter.addTab("Calendar", R.string.DayLogTabTitle, DayLogCalendar.class, null);

        }
    }

    protected BroadcastReceiver mMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
            boolean differentController = intent.getBooleanExtra("DifferentController", false);
            if (differentController) {
                MonitorActivity.this.finish();
                System.gc();
                MonitorActivity.this.startActivity(getIntent());
            }
            }
            catch (Throwable ex) {
                Log.e(getClass().getName(), "mMonitorReceiver failed ");
            }
        }
    };

    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle readings = intent.getBundleExtra("readings");
                int chargeState = readings.getInt(RegisterName.ChargeState.name());
                if (currentChargeState != chargeState) {
                    currentChargeState = chargeState;
                    String state = MonitorApplication.getChargeStateTitleText(chargeState);
                    String currentUnitName = "";
                    ChargeController cc = MonitorApplication.chargeControllers().getCurrentChargeController();
                    if (cc != null) {
                        currentUnitName = cc.deviceName();
                    }
                    if (state == null || state.isEmpty()) {
                        getSupportActionBar().setTitle(currentUnitName);
                    } else {
                        getSupportActionBar().setTitle(String.format("%s - (%s)", currentUnitName, MonitorApplication.getChargeStateTitleText(chargeState)));
                        if (MonitorApplication.chargeControllers().showPopupMessages()) {
                            Toast.makeText(context, MonitorApplication.getChargeStateText(chargeState), Toast.LENGTH_LONG).show();
                        }
                    }
                }
                DateTime now = DateTime.now();
                if (record.size() > 21600) {
                    record.remove(0);
                }
                record.add(new RecordEntry(readings.getFloat(RegisterName.BatVoltage.name()), readings.getFloat(RegisterName.BatCurrent.name()), readings.getFloat(RegisterName.WhizbangBatCurrent.name()), readings.getInt(RegisterName.ChargeState.name()), now.getMillis()));
            }
            catch (Throwable ex) {
                Log.e(getClass().getName(), "mReadingsReceiver failed ");
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean handled = false;
        switch (id) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                handled = true;
                break;
            case R.id.action_help:
                String helpContext = navigationDrawerFragment.isDrawerOpen() ? "NavigationBar" : tabStripAdapter.getItemBookmark(viewPager.getCurrentItem());
                helpContext = String.format("http://skyetracker.com/classicmonitor/help_%s.html#%s", Locale.getDefault().getLanguage(), helpContext);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(helpContext)));
                handled = true;
                break;
            case R.id.action_pvOutput:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pvoutput.org/")));
                handled = true;
                break;
            case R.id.action_share:
                try {
                    if (verifyStoragePermissions(this)) {
                        item.setActionView(new ProgressBar(this));
                        runOnUiThread(new Runnable() {
                            public void run() {
                                startActivity(Intent.createChooser(getScreenShot(), "Share Screenshot"));
                            }
                        });

                        item.getActionView().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                item.setActionView(null);
                            }
                        }, 2000);
                    }
                }
                catch (Exception e) {
                    item.setActionView(null);
                    e.printStackTrace();
                }
                handled = true;
                break;
        }
        return handled || super.onOptionsItemSelected(item);
    }


    private BroadcastReceiver receiveAToast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MonitorApplication.chargeControllers().showPopupMessages()) {
                try {
                    String message = intent.getStringExtra("message");
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                } catch (Throwable ex) {
                    Log.e(getClass().getName(), "receiveAToast failed ");
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMonitorReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_MONITOR_CHARGE_CONTROLLER));
            LocalBroadcastManager.getInstance(this).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
            LocalBroadcastManager.getInstance(this).registerReceiver(receiveAToast, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_TOAST));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMonitorReceiver);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mReadingsReceiver);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveAToast);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (navigationDrawerFragment == null || !navigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.shared_activity_menu, menu);
            currentChargeState = -1; // reload title
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public Intent getScreenShot() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_SUBJECT, tabStripAdapter.getPageTitle(viewPager.getCurrentItem()));
        View view = getWindow().getDecorView().findViewById(android.R.id.content);
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        String state = Environment.getExternalStorageState();
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "Classic.png");
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            Uri uri = Uri.fromFile(file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intent;
    }
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static boolean verifyStoragePermissions(AppCompatActivity activity) {
        boolean rVal = true;
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            rVal = false;
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        return rVal;
    }
}
