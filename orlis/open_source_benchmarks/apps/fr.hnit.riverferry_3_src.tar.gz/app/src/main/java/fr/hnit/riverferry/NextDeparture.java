package fr.hnit.riverferry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NextDeparture extends AppCompatActivity {

    TextView helloTextView;
    private Schedules schedules;

    private ListView previousList;
    private ListView currentList;
    private ListView nextList;
    private TextView previousText;
    private TextView currentText;
    private TextView nextText;

    private Spinner leftSpinner, rightSpinner;

    private File workDir;
    private File tsvFile;
    private File binFile;

    private String horaireFileName;
    private String defaultStation;
    private SharedPreferences sharedPref;

    private ArrayList<String> previousTimesUsed;

    private int previousIDUsed;

    String HMformat = "%dh et %d min";

    String Mformat = "%d min";
    private int readyL = 0;
    private int readyR = 0;

    /**
     * Creates a Schedules object, serialize it in the binFile file using tsvFile file
     * Initialize schedules object
     *
     * @return
     * @throws Exception
     */
    private boolean createHorairesBin() throws Exception {
        if (!tsvFile.exists()) {
            if (tsvFile.getName().equals("schedules.tsv")) { // default schedules of Normandie !
                Toast.makeText(this, "Copy raw resource schedules.tsv to "+tsvFile, Toast.LENGTH_LONG).show();
                byte[] readData = new byte[1024*500];
                InputStream fis = getResources().openRawResource(fr.hnit.riverferry.R.raw.schedules);
                FileOutputStream fos = new FileOutputStream(tsvFile);
                int i = fis.read(readData);
                while (i != -1) {
                    fos.write(readData, 0, i);
                    i = fis.read(readData);
                }

                fos.close();
                fis.close();
            } else {
                show("No TSV file found at "+tsvFile.getAbsolutePath()+"\n This file is mandatory see README file.");
                return false;
            }
        }

        BuildSchedules bh = new BuildSchedules(tsvFile, "UTF-8");
        schedules = bh.createHoraires();
        if (schedules == null) {
            show("Error: while creating schedules: null");
            return false;
        } else {
            Schedules.write(binFile, schedules);
        }
        return true;
    }

    /**
     * Creates binFile file if it does not exists, using the tsvFile
     * @return
     * @throws Exception
     */
    private boolean createHorairesBinIfNeeded() throws Exception {
        if (!binFile.exists() || binFile.lastModified() < tsvFile.lastModified()) {
            return createHorairesBin();
        }

        return true;
    }

    /**
     * Test if today is a French public holiday \o/
     *
     * @param day
     * @param month
     * @return
     */
    private boolean ferie(int day, int month) {
        if (day == 1 && month== 1) return true;
        else if (day == 1 && month == 5) return true;
        else if (day == 8 && month == 5) return true;
        else if (day == 14 && month == 7) return true;
        else if (day == 15 && month == 8) return true;
        else if (day == 1 && month == 11) return true;
        else if (day == 25 && month == 12) return true;
        else return false;
    }

    /**
     * Go to next departure time of the list view
     *
     * @param textfield
     * @param list
     * @param values
     * @param site
     * @param time
     */
    private void findNextDepartureTime(TextView textfield, ListView list, List<DepartureTime> values, String site, int time) {
        if (values.size() == 0) {
            notimes(textfield, list, site);
        } else {
            int idx = getNextTimePosition(time, values);

            int nextTime = values.get(idx).time;
            textfield.setText(site + "\n"+remainingTime(nextTime, time));
            list.setAdapter(new ArrayAdapter<DepartureTime>(this, android.R.layout.simple_list_item_1,values));
            list.setSelection(idx);
        }
    }

    /**
     * Find next departure time in a DepartureTimeList
     * @param minutes
     * @param previousValues
     * @return
     */
    int getNextTimePosition(int minutes, List<DepartureTime> previousValues) {
        for (int i = 0 ; i < previousValues.size() ; i++) {
            if (previousValues.get(i).time > minutes) {
                return i;
            }
        }
        return 0;
    }


    /**
     * Initialize schedules object using Schedules serialized in the binFile file
     *
     * @throws Exception
     */
    private void loadHoraires() throws Exception {
        if (schedules == null) {
            schedules = Schedules.read(binFile);
        }
    }
    /**
     * Fill a ListView with no departure time
     *
     * @param textfield
     * @param list
     */
    private void notimes(TextView textfield, ListView list, String station) {
        if (station == null) {
            textfield.setText("");
            String[] tmp = {};
            list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,tmp));
        } else {
            textfield.setText(station);
            String[] tmp = {"closed"};
            list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,tmp));
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(fr.hnit.riverferry.R.layout.activity_next_departure);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        View helloView = this.findViewById(fr.hnit.riverferry.R.id.Hell);
        if (helloView != null && helloView instanceof TextView) {
            helloTextView = (TextView)helloView;
        } else {
            return;
        }

        try {
            String sdcardpath = Environment.getExternalStorageDirectory().getPath();

            workDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "riverferry");
            workDir.mkdirs();
            if (!workDir.exists()) {
                show("could not create directory "+workDir+" using app file dires instead: "+this.getFilesDir());
            }

            workDir = new File(this.getFilesDir(), "riverferry");
            workDir.mkdirs();
            if (!workDir.exists()) {
                show("Working directory was not created: "+workDir);
                return;
            }


            reloadPreferences();

            if (!createHorairesBinIfNeeded()) {
                Toast.makeText(this, fr.hnit.riverferry.R.string.noschedulesfile, Toast.LENGTH_LONG).show();
                return;
            }
            loadHoraires();
            if (schedules == null) {
                show("Error: no schedules created");
                return;
            } else {
                //show("LEFTS: "+schedules.lefts+"\n"+"RIGHTS: "+schedules.rights+"\n"+schedules.get("Dieppedal"));
                //return;
            }

            rightSpinner = (Spinner) this.findViewById(fr.hnit.riverferry.R.id.rightSpinner);
            leftSpinner = (Spinner) this.findViewById(fr.hnit.riverferry.R.id.leftSpinner);

            previousList = (ListView) this.findViewById(fr.hnit.riverferry.R.id.previousList);
            currentList = (ListView) this.findViewById(fr.hnit.riverferry.R.id.currentList);
            currentList.setBackgroundColor(Color.LTGRAY);
            nextList = (ListView) this.findViewById(fr.hnit.riverferry.R.id.nextList);

            previousText = (TextView) this.findViewById(fr.hnit.riverferry.R.id.previousText);
            previousText.setGravity(Gravity.FILL);
            currentText = (TextView) this.findViewById(fr.hnit.riverferry.R.id.currentText);
            currentText.setGravity(Gravity.FILL);
            nextText = (TextView) this.findViewById(fr.hnit.riverferry.R.id.nextText);
            nextText.setGravity(Gravity.FILL);

            leftSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                    if (readyL > 0) // don't want to exec this at ListView initialization...
                        updateGridValues(schedules.lefts, leftSpinner.getSelectedItemPosition(), "Left spinner");
                    readyL++;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) { }
            });
            rightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                    if (readyR > 0) // don't want to exec this at ListView initialization...
                        updateGridValues(schedules.rights, rightSpinner.getSelectedItemPosition(), "Right spinner");
                    readyR++;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) { }
            });

            reloadWidgets();
        } catch(Exception e) {
            show(e.getMessage() + e.getStackTrace()[0]);
        }
    }

    /**
     * Create menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(fr.hnit.riverferry.R.menu.next_bac, menu);
        return true;
    }

    /**
     * Item selection listener method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case fr.hnit.riverferry.R.id.action_settings:
                showSettings();
                return true;
            case fr.hnit.riverferry.R.id.action_refresh:
                Toast.makeText(this, fr.hnit.riverferry.R.string.refreshing, Toast.LENGTH_SHORT).show();
                updateGridValues(previousTimesUsed, previousIDUsed, "Refresh");
                return true;
            case fr.hnit.riverferry.R.id.action_reinitialize:
                Toast.makeText(this, fr.hnit.riverferry.R.string.reloadingall, Toast.LENGTH_SHORT).show();
                try {
                    reloadPreferences();
                    if (createHorairesBin()) {
                        reloadPreferences();
                        reloadWidgets();
                    }
                } catch (Exception e) {
                    show(e.toString());
                }

                return true;
            case fr.hnit.riverferry.R.id.action_about:
                showAbout();
                return true;
            case fr.hnit.riverferry.R.id.action_help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     *
     * @return true if need update
     */
    private boolean reloadDefaultStation() {
        if (schedules == null) return false;

        if (defaultStation != null) {
            DepartureTimeList dtl = schedules.get(defaultStation);

            if (dtl != null) {
                //Toast.makeText(this, "DTL "+dtl.bank, Toast.LENGTH_LONG).show();
                if (Bank.LEFT == dtl.bank) {
                    int idx = schedules.lefts.indexOf(defaultStation);
                    if (idx > 0) {
                        leftSpinner.setSelection(idx, true);
                        //leftSpinner.seta
                        previousIDUsed = idx;
                        previousTimesUsed = schedules.lefts;
                        return true;
                    }
                } else {
                    int idx = schedules.rights.indexOf(defaultStation);
                    if (idx > 0) {
                        rightSpinner.setSelection(idx, true);
                        previousIDUsed = idx;
                        previousTimesUsed = schedules.rights;
                        return true;
                    }
                }
                Toast.makeText(this, "Default station: "+defaultStation+ " sites="+previousTimesUsed+" idx="+previousIDUsed, Toast.LENGTH_LONG).show();
            } else {
                previousIDUsed = 0;
                if (schedules.lefts.size() > 0)
                    previousTimesUsed = schedules.lefts;
                else
                    previousTimesUsed = schedules.rights;
            }
        } else {
            previousIDUsed = 0;
            if (schedules.lefts.size() > 0)
                previousTimesUsed = schedules.lefts;
            else
                previousTimesUsed = schedules.rights;
        }
        return false;
    }

    /**
     * Search again the next DepartureTime
     *
     * @throws Exception
     */
    public void reloadHoraires() throws Exception {
        if (!binFile.delete()) {
            throw new IllegalStateException("reloadHoraires: Could not delete binary schedules: "+binFile);
        }
        createHorairesBin();
    }

    private void reloadPreferences() {
        horaireFileName = sharedPref.getString("TSVFILENAME", "schedules.tsv");
        defaultStation = sharedPref.getString("DEFAULTSTATION", null);

        tsvFile = new File(workDir, horaireFileName);
        binFile = new File(workDir, horaireFileName+".bin");

    }

    /**
     * Reload Lists using the schedules object
     */
    private void reloadWidgets() {
        readyL = readyR = 0;
        leftSpinner.setAdapter(new ArrayAdapter<String>(this, fr.hnit.riverferry.R.layout.mylist, schedules.lefts));
        rightSpinner.setAdapter(new ArrayAdapter<String>(this, fr.hnit.riverferry.R.layout.mylist, schedules.rights));

        if (reloadDefaultStation()) {
            updateGridValues(previousTimesUsed, previousIDUsed, "Default");
        } else {
            updateGridValues(schedules.lefts, 0, "No Default");
        }
    }

    /**
     * Format remaining time
     *
     * @param current
     * @param next
     * @return
     */
    public String remainingTime(int current, int next) {
        int diff = Math.abs(next - current);
        int h = (diff/60);
        int m = (diff%60);
        if (h > 0) {
            return String.format(HMformat, h, m);
        } else {
            return String.format(Mformat, m);
        }
    }

    /**
     * Show debug message
     *
     * @param mess
     */
    public void show(String mess) {
        helloTextView.setText(mess);
        helloTextView.setGravity(Gravity.FILL);
    }

    /**
     * Opens About activity
     */
    public void showAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    private void showHelp() {
        Intent i = new Intent(this, HelpActivity.class);
        startActivity(i);
    }

    public void showSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }


    /**
     * Search next DepartureTime
     * Refresh lists
     * Select the closer departure time in the center list
     *
     * @param stations
     * @param id
     */
    protected synchronized void updateGridValues(ArrayList<String> stations, int id, String event) {
        if (schedules == null) {
            show("Schedules are not set. Aborting.");
            return;
        }
        try {
            //Toast.makeText(this, "Update: "+stations+" sel="+id, Toast.LENGTH_LONG).show();
            previousTimesUsed = stations;
            previousIDUsed = id;

            // get current time in minutes
            Calendar c = Calendar.getInstance();
            int hours = c.get(Calendar.HOUR);
            if (hours <= 12 && c.get(Calendar.AM_PM) == Calendar.PM) hours += 12;
            int time = (hours*60) + c.get(Calendar.MINUTE);

            // get current day
            int day = c.get(Calendar.DAY_OF_WEEK) - 1;

            // test if day is free
            int daym = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH) + 1;
            if (ferie(daym, month)) day = 8;
            if (day == 0) day = 7; // fix sunday

            if (id > 0) {
                //Toast.makeText(this, "update 1st list", Toast.LENGTH_LONG).show();
                String previousSite = stations.get(id-1);
                List<DepartureTime> previousValues = schedules.get(previousSite).get(day);
                findNextDepartureTime(previousText, previousList, previousValues, previousSite, time);
            } else {
                notimes(previousText, previousList, null);
            }

            String station = stations.get(id);
            List<DepartureTime> currentValues = schedules.get(station).get(day);
            findNextDepartureTime(currentText, currentList, currentValues, station, time);

            if (id != stations.size() - 1) {
                String nextSite = stations.get(id+1);
                List<DepartureTime> nextValues = schedules.get(nextSite).get(day);
                findNextDepartureTime(nextText, nextList, nextValues, nextSite, time);
            } else {
                notimes(nextText, nextList, null);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}