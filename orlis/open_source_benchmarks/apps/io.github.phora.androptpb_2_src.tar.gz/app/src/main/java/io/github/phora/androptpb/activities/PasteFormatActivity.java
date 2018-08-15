package io.github.phora.androptpb.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import io.github.phora.androptpb.DBHelper;
import io.github.phora.androptpb.R;
import io.github.phora.androptpb.adapters.FormatsCursorAdapter;
import io.github.phora.androptpb.network.NetworkUtils;

public class PasteFormatActivity extends Activity {

    private String server;
    private long pasteId;

    public static final String EXTRA_PASTE_FORMAT = "EXTRA_PASTE_FORMAT";
    public static final String EXTRA_SERVER = "EXTRA_SERVER";
    public static final String EXTRA_PASTE_ID = "EXTRA_PASTE_ID";
    
    private Switch mIsDefault;
    private ExpandableListView mExpandableListView;
    private TextView mNoFormats;
    private FormatsCursorAdapter formatsCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIsDark()) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formatter);

        mIsDefault = (Switch)findViewById(R.id.PasteFormat_IsDefault);
        mExpandableListView = (ExpandableListView)findViewById(R.id.PasteFormat_Formats);
        mNoFormats = (TextView)findViewById(R.id.PasteFormat_NoFormats);

        mIsDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandableListView.setEnabled(mIsDefault.isChecked());
                if (mIsDefault.isChecked()) {
                    if (mExpandableListView.getAdapter().isEmpty()) {
                        mExpandableListView.setVisibility(View.GONE);
                        mNoFormats.setVisibility(View.VISIBLE);
                    }
                    else {
                        mExpandableListView.setVisibility(View.VISIBLE);
                        mNoFormats.setVisibility(View.GONE);
                    }

                } else {
                    mExpandableListView.setVisibility(View.INVISIBLE);
                    mNoFormats.setVisibility(View.INVISIBLE);
                }
            }
        });
        
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                long packedPos = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
                int index = parent.getFlatListPosition(packedPos);
                parent.setItemChecked(index, true);

                return true;
            }
        });
        
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        formatsCursorAdapter = new FormatsCursorAdapter(this, null,
                R.layout.formatter_group,
                new String[]{"longest_name", "naliases"},
                new int[]{R.id.FormatterGroup_Name, R.id.FormatterGroup_Size},
                R.layout.formatter_item,
                new String[]{DBHelper.FORMATTERS_NAME},
                new int[]{R.id.FormatterItem_Name});
        mExpandableListView.setAdapter(formatsCursorAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            server = intent.getStringExtra(EXTRA_SERVER);
            String pasteFormat = intent.getStringExtra(EXTRA_PASTE_FORMAT);
            pasteId = intent.getLongExtra(EXTRA_PASTE_ID, -1);
            if (pasteFormat != null) {
                mIsDefault.setChecked(true);
                mExpandableListView.setEnabled(true);
                mExpandableListView.setVisibility(View.VISIBLE);
            }
            else {
                mIsDefault.setChecked(false);
                mExpandableListView.setEnabled(false);
                mExpandableListView.setVisibility(View.GONE);
            }

            new FormatterRefreshTask(server, false).execute();
        }
    }

    private boolean getIsDark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isdark = preferences.getBoolean("isDark", false);
        return isdark;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_formatters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_purge_refresh) {
            new FormatterRefreshTask(server, true).execute();
        }

        return super.onOptionsItemSelected(item);
    }

    public void cancelSubmission(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private String getPasteFormat() {
        if (mIsDefault.isChecked()) {
            int itemPos = mExpandableListView.getCheckedItemPosition();
            if (itemPos == AbsListView.INVALID_POSITION) {
                return null;
            }
            else {
                Cursor c = (Cursor) mExpandableListView.getItemAtPosition(itemPos);
                return c.getString(c.getColumnIndex(DBHelper.FORMATTERS_NAME));
            }
        }
        else {
            return null;
        }
    }

    public void finishSubmission(View view) {
        Intent output = new Intent();

        String pasteFormat = getPasteFormat();

        output.putExtra(EXTRA_PASTE_ID, pasteId);
        output.putExtra(EXTRA_PASTE_FORMAT, pasteFormat);
        setResult(RESULT_OK, output);
        finish();
    }

    private class FormatterRefreshTask extends AsyncTask<Void, Void, Void> {
        private static final String LOG_TAG = "FormatterRefreshTask";

        String server;
        long serverId = -1;
        DBHelper sqlhelper = DBHelper.getInstance(getApplicationContext());
        boolean purge;

        public FormatterRefreshTask(String server, boolean purgeBeforeRetrieve) {
            this.server = server;
            serverId = sqlhelper.getServerByURL(server);
            purge = purgeBeforeRetrieve;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils nm = NetworkUtils.getInstance(getApplicationContext());
            String fmt = "%1$s/lf";
            HttpURLConnection connection = nm.openConnection(String.format(fmt, server),
                    NetworkUtils.METHOD_GET);

            List<String[]> groups = null;
            try {
                groups = nm.getFormatters(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (groups != null) {
                if (purge) {
                    sqlhelper.clearFormatterGroups(serverId);
                }
                for (String[] formatGroup: groups) {
                    if (!sqlhelper.hasFormatter(serverId, formatGroup)) {
                        Log.d(LOG_TAG, "Found new formats, adding them");
                        sqlhelper.addFormatterGroup(serverId, formatGroup);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            formatsCursorAdapter.changeCursor(sqlhelper.getAllFormatters(serverId));

            View noHighlights = findViewById(R.id.PasteFormat_NoFormats);
            View waiting = findViewById(R.id.PasteFormat_Waiting);
            waiting.setVisibility(View.GONE);
            mExpandableListView.setEmptyView(noHighlights);

            if (mIsDefault.isChecked()) {
                Log.d(LOG_TAG, "Showing widgets");
                if (mExpandableListView.getAdapter().isEmpty()) {
                    Log.d(LOG_TAG, "There's nothing! Show waiting message");
                    mExpandableListView.setVisibility(View.GONE);
                    mNoFormats.setVisibility(View.VISIBLE);
                }
                else {
                    mExpandableListView.setVisibility(View.VISIBLE);
                    mNoFormats.setVisibility(View.GONE);
                }

            } else {
                Log.d(LOG_TAG, "Hiding widgets");
                mExpandableListView.setVisibility(View.INVISIBLE);
                mNoFormats.setVisibility(View.INVISIBLE);
            }
        }
    }
}
