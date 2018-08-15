package io.github.phora.androptpb.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import io.github.phora.androptpb.DBHelper;
import io.github.phora.androptpb.R;
import io.github.phora.androptpb.adapters.PasteHintFilter;
import io.github.phora.androptpb.adapters.PasteHintsCursorAdapter;
import io.github.phora.androptpb.network.NetworkUtils;

public class PasteHintsActivity extends Activity {
    private static final String LOG_TAG = "PasteHintsActivity";

    public static final String EXTRA_PASTE_HINT = "EXTRA_PASTE_HINT";
    public static final String EXTRA_SERVER = "EXTRA_SERVER";
    public static final String EXTRA_PASTE_ID = "EXTRA_PASTE_ID";

    private Switch mIsHighlight;
    private TextView mPHintContent;
    private PasteHintsCursorAdapter pasteHintsCursorAdapter;
    private ExpandableListView mExpandableListView;

    private String server;
    private long pasteId = -1;
    private TextView mNoHighlights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIsDark()) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paste_hints);

        mIsHighlight = (Switch)findViewById(R.id.PasteHints_IsHighlight);
        mPHintContent = (TextView)findViewById(R.id.PasteHints_Content);
        mExpandableListView = (ExpandableListView)findViewById(R.id.PasteHints_Highlights);
        mNoHighlights = (TextView)findViewById(R.id.PasteHints_NoHighlights);

        pasteHintsCursorAdapter = new PasteHintsCursorAdapter(this, null,
                R.layout.paste_hint_group,
                new String[]{"longest_name", "naliases"}, new int[]{R.id.PasteHintGroup_Name, R.id.PasteHintGroup_Size},
                R.layout.paste_hint_item,
                new String[]{DBHelper.PASTE_HINTS_NAME}, new int[]{R.id.PasteHintItem_Name});

        mExpandableListView.setAdapter(pasteHintsCursorAdapter);

        mPHintContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pasteHintsCursorAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mIsHighlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandableListView.setEnabled(mIsHighlight.isChecked());
                if (mIsHighlight.isChecked()) {
                    if (mExpandableListView.getAdapter().isEmpty()) {
                        mExpandableListView.setVisibility(View.GONE);
                        mNoHighlights.setVisibility(View.VISIBLE);
                    }
                    else {
                        mExpandableListView.setVisibility(View.VISIBLE);
                        mNoHighlights.setVisibility(View.GONE);
                    }

                } else {
                    mExpandableListView.setVisibility(View.INVISIBLE);
                    mNoHighlights.setVisibility(View.INVISIBLE);
                }
            }
        });

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPos, int childPos, long id) {
                long packedPos = ExpandableListView.getPackedPositionForChild(groupPos, childPos);
                int flatPos = expandableListView.getFlatListPosition(packedPos);
                Cursor c = (Cursor) expandableListView.getItemAtPosition(flatPos);
                String pasteName = c.getString(c.getColumnIndex(DBHelper.PASTE_HINTS_NAME));
                mPHintContent.setText(pasteName);
                return true;
            }
        });

        View waiting = findViewById(R.id.PasteHints_Waiting);
        waiting.setVisibility(View.VISIBLE);
        mExpandableListView.setEmptyView(waiting);

        Intent intent = getIntent();
        if (intent != null) {
            server = intent.getStringExtra(EXTRA_SERVER);
            String pasteHint = intent.getStringExtra(EXTRA_PASTE_HINT);
            pasteId = intent.getLongExtra(EXTRA_PASTE_ID, -1);
            if (pasteHint != null && pasteHint.startsWith("/")) {
                mIsHighlight.setChecked(true);
                mPHintContent.setText(pasteHint.substring(1));
                mExpandableListView.setEnabled(true);
                mExpandableListView.setVisibility(View.VISIBLE);
            }
            else if (pasteHint != null && pasteHint.startsWith(".")) {
                mIsHighlight.setChecked(false);
                mPHintContent.setText(pasteHint.substring(1));
                mExpandableListView.setEnabled(false);
                mExpandableListView.setVisibility(View.GONE);
            }
            else {
                mIsHighlight.setChecked(false);
                mExpandableListView.setEnabled(false);
                mExpandableListView.setVisibility(View.GONE);
            }

            new PasteHintsRefreshTask(server, false).execute();
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
        getMenuInflater().inflate(R.menu.menu_paste_hints, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == R.id.action_purge_refresh) {
            new PasteHintsRefreshTask(server, true).execute();
        }

        return super.onOptionsItemSelected(item);
    }

    public void cancelSubmission(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void finishSubmission(View view) {
        Intent output = new Intent();
        String fmt = "%1$s%2$s";
        String putHere;
        String hintString = mPHintContent.getText().toString();
        if (!TextUtils.isEmpty(hintString)) {
            if (mIsHighlight.isChecked()) {
                putHere = String.format(fmt, "/", hintString);
            } else {
                putHere = String.format(fmt, ".", hintString);
            }
        }
        else {
            putHere = null;
        }
        output.putExtra(EXTRA_PASTE_ID, pasteId);
        output.putExtra(EXTRA_PASTE_HINT, putHere);
        setResult(RESULT_OK, output);
        finish();
    }

    private class PasteHintsRefreshTask extends AsyncTask<Void, Void, Void> {
        private static final String LOG_TAG = "PasteHintsRefreshTask";

        String server;
        long serverId = -1;
        DBHelper sqlhelper = DBHelper.getInstance(getApplicationContext());
        boolean purge;

        public PasteHintsRefreshTask(String server, boolean purgeBeforeRetrieve) {
            this.server = server;
            serverId = sqlhelper.getServerByURL(server);
            purge = purgeBeforeRetrieve;
        }

        @Override
        protected void onPreExecute() {
            if (!purge)
                return;

            pasteHintsCursorAdapter.changeCursor(null);
            View noHighlights = findViewById(R.id.PasteHints_NoHighlights);
            View waiting = findViewById(R.id.PasteHints_Waiting);
            waiting.setVisibility(View.GONE);
            mExpandableListView.setEmptyView(noHighlights);

            if (mIsHighlight.isChecked()) {
                Log.d(LOG_TAG, "Showing widgets");
                if (mExpandableListView.getAdapter().isEmpty()) {
                    Log.d(LOG_TAG, "There's nothing! Show waiting message");
                    mExpandableListView.setVisibility(View.GONE);
                    mNoHighlights.setVisibility(View.VISIBLE);
                }
                else {
                    mExpandableListView.setVisibility(View.VISIBLE);
                    mNoHighlights.setVisibility(View.GONE);
                }

            } else {
                Log.d(LOG_TAG, "Hiding widgets");
                mExpandableListView.setVisibility(View.INVISIBLE);
                mNoHighlights.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils nm = NetworkUtils.getInstance(getApplicationContext());
            String fmt = "%1$s/l";
            HttpURLConnection connection = nm.openConnection(String.format(fmt, server),
                    NetworkUtils.METHOD_GET);

            List<String[]> groups = null;
            try {
                groups = nm.getHintGroups(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (groups != null) {
                if (purge) {
                    sqlhelper.clearHintGroups(serverId);
                }
                for (String[] hintGroup: groups) {
                    if (!sqlhelper.hasHighlighter(serverId, hintGroup)) {
                        Log.d(LOG_TAG, "Found new hints, adding them");
                        sqlhelper.addHintGroup(serverId, hintGroup);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void ignoreme) {
            pasteHintsCursorAdapter.changeCursor(sqlhelper.getHintGroups(serverId));
            pasteHintsCursorAdapter.setFilterQueryProvider(new PasteHintFilter(serverId, pasteHintsCursorAdapter));

            View noHighlights = findViewById(R.id.PasteHints_NoHighlights);
            View waiting = findViewById(R.id.PasteHints_Waiting);
            waiting.setVisibility(View.GONE);
            mExpandableListView.setEmptyView(noHighlights);

            pasteHintsCursorAdapter.getFilter().filter(mPHintContent.getText().toString());

            if (mIsHighlight.isChecked()) {
                Log.d(LOG_TAG, "Showing widgets");
                if (mExpandableListView.getAdapter().isEmpty()) {
                    Log.d(LOG_TAG, "There's nothing! Show waiting message");
                    mExpandableListView.setVisibility(View.GONE);
                    mNoHighlights.setVisibility(View.VISIBLE);
                }
                else {
                    mExpandableListView.setVisibility(View.VISIBLE);
                    mNoHighlights.setVisibility(View.GONE);
                }

            } else {
                Log.d(LOG_TAG, "Hiding widgets");
                mExpandableListView.setVisibility(View.INVISIBLE);
                mNoHighlights.setVisibility(View.INVISIBLE);
            }
        }
    }
}
