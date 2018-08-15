package io.github.phora.androptpb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;

import io.github.phora.androptpb.DBHelper;
import io.github.phora.androptpb.CheckableLinearLayout;
import io.github.phora.androptpb.R;

/**
 * Created by phora on 8/21/15.
 */
public class ServerChooserAdapter extends ResourceCursorAdapter {
    private final static String LOG_TAG = "ServerChooserAdapter";

    //column field caches
    private static int URL_IDX = -1;
    private static int DEF_IDX = -1;

    private long curId = -1;


    public ServerChooserAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, R.layout.server_item_choose, c, autoRequery);
    }

    public ServerChooserAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.server_item_choose, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckableLinearLayout checkme = (CheckableLinearLayout)view;
        CheckedTextView base_url_view = (CheckedTextView) view.findViewById(R.id.ServerItem_Url);

        if (URL_IDX == -1) {
            URL_IDX = cursor.getColumnIndex(DBHelper.BASE_URL);
        }
        if (DEF_IDX == -1) {
            DEF_IDX = cursor.getColumnIndex(DBHelper.SERVER_DEFAULT);
        }

        boolean isChecked = (cursor.getInt(DEF_IDX) == 1);
        String base_url = cursor.getString(URL_IDX);

        Log.d(LOG_TAG, "def flag: "+cursor.getInt(DEF_IDX));

        checkme.setChecked(isChecked);
        base_url_view.setText(base_url);

        if (isChecked) {
            Log.d(LOG_TAG, base_url + " is the default");
            curId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
        }
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = super.getView(pos, convertView, parent);
        ListView lv = (ListView)parent;
        CheckableLinearLayout cll = (CheckableLinearLayout)v;

        //move this outside of the adapter, somehow...
        if (cll.isChecked()) {
            lv.setSelection(pos);
            lv.setItemChecked(pos, true);
        }

        return v;
    }

    public long getCurId() {
        return curId;
    }

    public void setCurId(long curId) {
        this.curId = curId;
    }
}
