package io.github.phora.androptpb.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import io.github.phora.androptpb.DBHelper;
import io.github.phora.androptpb.R;
import io.github.phora.androptpb.network.NetworkUtils;

public class PasteStyleActivity extends Activity {

    public static final String EXTRA_PASTE_STYLE = "EXTRA_PASTE_STYLE";
    public static final String EXTRA_SERVER = "EXTRA_SERVER";
    public static final String EXTRA_PASTE_ID = "EXTRA_PASTE_ID";

    private Switch mIsDefault;
    private ListView mListView;
    private TextView mNoStyles;
    private SimpleCursorAdapter stylesCursorAdapter;
    private String server;
    private long pasteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIsDark()) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_styles);

        mIsDefault = (Switch)findViewById(R.id.PasteStyle_IsDefault);
        mListView = (ListView)findViewById(R.id.PasteStyle_Styles);
        mNoStyles = (TextView)findViewById(R.id.PasteStyle_NoStyles);
        
        mIsDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView.setEnabled(mIsDefault.isChecked());
                if (mIsDefault.isChecked()) {
                    if (mListView.getAdapter().isEmpty()) {
                        mListView.setVisibility(View.GONE);
                        mNoStyles.setVisibility(View.VISIBLE);
                    } else {
                        mListView.setVisibility(View.VISIBLE);
                        mNoStyles.setVisibility(View.GONE);
                    }

                } else {
                    mListView.setVisibility(View.INVISIBLE);
                    mNoStyles.setVisibility(View.INVISIBLE);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                mListView.setItemChecked(pos, true);
            }
        });
        
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        stylesCursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_activated_1,
                null,
                new String[]{DBHelper.STYLES_NAME},
                new int[]{android.R.id.text1}, 0);

        mListView.setAdapter(stylesCursorAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            server = intent.getStringExtra(EXTRA_SERVER);
            String pasteStyle = intent.getStringExtra(EXTRA_PASTE_STYLE);
            pasteId = intent.getLongExtra(EXTRA_PASTE_ID, -1);
            if (pasteStyle != null) {
                mIsDefault.setChecked(true);
                mListView.setEnabled(true);
                mListView.setVisibility(View.VISIBLE);
            }
            else {
                mIsDefault.setChecked(false);
                mListView.setEnabled(false);
                mListView.setVisibility(View.GONE);
            }

            new StyleRefreshTask(server, false).execute();
        }
    }

    private boolean getIsDark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isdark = preferences.getBoolean("isDark", false);
        return isdark;
    }

    public void cancelSubmission(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void finishSubmission(View view) {
        Intent output = new Intent();
        
        String pasteStyle = getPasteStyle();

        output.putExtra(EXTRA_PASTE_ID, pasteId);
        output.putExtra(EXTRA_PASTE_STYLE, pasteStyle);
        setResult(RESULT_OK, output);
        finish();
    }

    private String getPasteStyle() {
        if (mIsDefault.isChecked()) {
            int itemPos = mListView.getCheckedItemPosition();
            if (itemPos == AbsListView.INVALID_POSITION) {
                return null;
            }
            else {
                Cursor c = (Cursor) mListView.getItemAtPosition(itemPos);
                return c.getString(c.getColumnIndex(DBHelper.STYLES_NAME));
            }
        }
        else {
            return null;
        }
    }

    private class StyleRefreshTask extends AsyncTask<Void, Void, Void> {
        private static final String LOG_TAG = "StyleRefreshTask";
        DBHelper sqlhelper = DBHelper.getInstance(getApplicationContext());

        String server;
        long serverId;
        boolean purge;

        public StyleRefreshTask(String server, boolean purgeBeforeRetrieve) {
            this.server = server;
            serverId = sqlhelper.getServerByURL(server);
            purge = purgeBeforeRetrieve;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils nm = NetworkUtils.getInstance(getApplicationContext());
            String fmt = "%1$s/ls";
            HttpURLConnection connection = nm.openConnection(String.format(fmt, server),
                    NetworkUtils.METHOD_GET);

            List<String> styles = null;
            try {
                styles = nm.getStyles(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (styles != null) {
                if (purge) {
                    sqlhelper.clearStyles(serverId);
                }
                for (String style: styles) {
                    if (!sqlhelper.hasHighlighter(serverId, style)) {
                        Log.d(LOG_TAG, "Found new style, adding them");
                        sqlhelper.addStyle(serverId, style);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            stylesCursorAdapter.changeCursor(sqlhelper.getAllStyles(serverId));

            View noHighlights = findViewById(R.id.PasteStyle_NoStyles);
            View waiting = findViewById(R.id.PasteStyle_Waiting);
            waiting.setVisibility(View.GONE);
            mListView.setEmptyView(noHighlights);

            if (mIsDefault.isChecked()) {
                Log.d(LOG_TAG, "Showing widgets");
                if (mListView.getAdapter().isEmpty()) {
                    Log.d(LOG_TAG, "There's nothing! Show waiting message");
                    mListView.setVisibility(View.GONE);
                    mNoStyles.setVisibility(View.VISIBLE);
                }
                else {
                    mListView.setVisibility(View.VISIBLE);
                    mNoStyles.setVisibility(View.GONE);
                }

            } else {
                Log.d(LOG_TAG, "Hiding widgets");
                mListView.setVisibility(View.INVISIBLE);
                mNoStyles.setVisibility(View.INVISIBLE);
            }
        }
    }
}
