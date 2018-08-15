package com.markusborg.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;

import java.util.ArrayList;

/**
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class ResultsActivity extends AppCompatActivity {

    private ListView mLstView;
    private SessionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mLstView = (ListView) findViewById(R.id.listView);
        displayHistory();
    }

    /**
     * List a number of previous ghosting sessions.
     */
    private void displayHistory() {
        LogHandler logger = new LogHandler(getApplicationContext());
        ArrayList<Setting> theList = logger.getSettingList();
        mAdapter = new SessionAdapter(this, R.layout.list_item, theList);
        mLstView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}
