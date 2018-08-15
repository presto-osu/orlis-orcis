package ch.ihdg.calendarcolor;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.HashMap;


public class CalendarListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;

    HashMap<Integer, Integer> calIds = new HashMap<Integer, Integer>();

    static final String[] CALENDAR_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.CALENDAR_COLOR                 // 3
    };

    // This is the select criteria
    static final String SELECTION = "((" +
            CalendarContract.Calendars.ACCOUNT_NAME + " NOTNULL) AND (" +
            CalendarContract.Calendars.ACCOUNT_TYPE + " != '' ))";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.calendar_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_list);

        // set a progress bar to display while the list loads
        getListView().setEmptyView(findViewById(R.id.progress));

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars._ID
        };
        int[] toViews = {
                R.id.displayname,
                R.id.accountname,
                R.id.color,
                R.id.displayname
        };

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.calender_list_item, null,
                fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                String name = cursor.getColumnName(columnIndex);
                if (CalendarContract.Calendars.CALENDAR_COLOR.equals(name)) {
                    int color = cursor.getInt(columnIndex);
                    view.setBackgroundColor( color | 0xFF << 24 );
                    return true;
                }
                else if (CalendarContract.Calendars._ID.equals(name)) {
                    calIds.put(cursor.getPosition(), cursor.getInt(columnIndex));
                    return true;
                }
                return false;
            }
        };
        mAdapter.setViewBinder(binder);

        // Prepare the loader.  Either re-connect with an existing one or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, CalendarContract.Calendars.CONTENT_URI,
                CALENDAR_PROJECTION, SELECTION, null, null);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // open color picker
        int color = ((ColorDrawable) v.findViewById(R.id.color).getBackground()).getColor();
        String name = ((TextView) v.findViewById(R.id.displayname)).getText().toString();
        int cal_id = calIds.get(position);

        Intent intent = new Intent(this, ColorPickerActivity.class);

        intent.putExtra(ColorPickerActivity.ARG_NAME, name);
        intent.putExtra(ColorPickerActivity.ARG_ID, cal_id);
        intent.putExtra(ColorPickerActivity.ARG_COLOR, color);

        startActivity(intent);
    }
}
