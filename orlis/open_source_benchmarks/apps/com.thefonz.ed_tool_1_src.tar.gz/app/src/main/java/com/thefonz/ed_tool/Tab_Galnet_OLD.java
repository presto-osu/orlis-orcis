package com.thefonz.ed_tool;

/**
 * Created by thefonz on 18/03/15.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.thefonz.ed_tool.rss.RssItem;
import com.thefonz.ed_tool.rss.RssReader;
import com.thefonz.ed_tool.utils.Constants;
import com.thefonz.ed_tool.utils.Utils;

public class Tab_Galnet_OLD extends Fragment {

    private ListView mList;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.tab_galnet_old, container,false);

        final Button button_refresh = (Button) myFragmentView
                .findViewById(R.id.button_refresh);

        // Set ListView, ArrayAdapter and RSS feed
        mList = (ListView) myFragmentView.findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.basic_list_item);
        new GetRssFeed().execute((Constants.RSSFEEDURL));

        button_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String msg = getString(R.string.refreshing);
                Utils.showToast_Short(getActivity(), msg);
                adapter = new ArrayAdapter<String>(getActivity(), R.layout.basic_list_item);
                new GetRssFeed().execute((Constants.RSSFEEDURL));
            }
        });
        return myFragmentView;
    }

    private class GetRssFeed extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                RssReader rssReader = new RssReader(params[0]);
                for (RssItem item : rssReader.getItems())
                    adapter.add(item.getContent());
            } catch (Exception e) {
                final String LOGMETHOD = " GetRssFeed ";
                final String LOGBODY = " Error Parsing Data ";
                Utils.LogError(getActivity(), Constants.TAG, LOGMETHOD, LOGBODY);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            mList.setAdapter(adapter);
        }
    }
}