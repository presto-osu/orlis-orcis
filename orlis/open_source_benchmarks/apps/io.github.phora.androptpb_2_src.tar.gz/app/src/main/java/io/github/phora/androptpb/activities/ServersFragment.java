package io.github.phora.androptpb.activities;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import io.github.phora.androptpb.DBHelper;
import io.github.phora.androptpb.R;
import io.github.phora.androptpb.adapters.ServerChooserAdapter;

public class ServersFragment extends ListFragment {

    private final static String LOG_TAG = "ServersFragment";

    private ServerChooserAdapter servAdap;
    private TextView mUrlEdit;
    private Spinner mPrefixSpinner;
    private ImageButton mSubmitServer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_server_settings, null);

        servAdap = new ServerChooserAdapter(getActivity(),
                 DBHelper.getInstance(getActivity()).getAllServers(false), false);
        setListAdapter(servAdap);

        mUrlEdit = (TextView) view.findViewById(R.id.ServersFragment_UrlBox);
        mPrefixSpinner = (Spinner) view.findViewById(R.id.spinner);
        mSubmitServer = (ImageButton) view.findViewById(R.id.ServersFragment_AddServer);
        mSubmitServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitNewServer();
            }
        });

        //Log.d(LOG_TAG, "_id " + servAdap.getCurId() + ", pos " + servAdap.getCurPos() + ", getCount() " + servAdap.getCount());
        //uncomment these to see what I mean
        //setSelection(servAdap.getCurPos());
        //getListView().setItemChecked(servAdap.getCurPos(), true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Cursor c = (Cursor) getListView().getItemAtPosition(pos);

                DBHelper sqlhelper = DBHelper.getInstance(getActivity().getApplicationContext());
                sqlhelper.deleteServer(c.getLong(c.getColumnIndex(DBHelper.COLUMN_ID)));

                servAdap.swapCursor(sqlhelper.getAllServers(false));
                return true;
            }
        });
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Cursor currentItem = (Cursor) getListView().getItemAtPosition(pos);
                DBHelper sqlhelper = DBHelper.getInstance(getActivity().getApplicationContext());

                long newId = currentItem.getLong(currentItem.getColumnIndex(DBHelper.COLUMN_ID));
                sqlhelper.setDefaultServer(newId, servAdap.getCurId());
                servAdap.setCurId(newId);
                //servAdap.setCurPos(pos);

                //do we need to swap cursors?
                //servAdap.swapCursor(sqlhelper.getAllServers(false));
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_server_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }*/

    private void submitNewServer()
    {
        String prefix = mPrefixSpinner.getSelectedItem().toString();

        DBHelper sqlhelper = DBHelper.getInstance(getActivity().getApplicationContext());
        //add support for changing duration when transfer.sh and similar sites support it
        try {
            sqlhelper.addServer(prefix + mUrlEdit.getText().toString());
            servAdap.swapCursor(sqlhelper.getAllServers(false));
        } catch (SQLiteConstraintException e) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(getString(R.string.ServersFragment_Exists));
            b.setMessage(getString(R.string.ServersFragment_Exists_Msg));
            b.setPositiveButton(R.string.OK, null);
            b.create().show();
        }
    }
}
