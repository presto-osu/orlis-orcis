/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FilterListPicker extends Activity
{
    //private static final String TAG = "Filters";
    public static final int PICK_CONTACT = 3;
    public static final int REQUEST_PICK_FILTER = 2;
    public static final int RESULT_CODE_CHOOSED = 4;

    private FilterListArrayAdapter mAdapter;

    private ListView mListView;


    private class ViewHolder
    {
        TextView filterNameTextView;
    }

    private class FilterListArrayAdapter extends ArrayAdapter<Filter>
    {
        public FilterListArrayAdapter(Context context, List<Filter> filters)
        {
            super(context, 0, filters);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            View v = convertView;

            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.filter_list_picker_item, null);
                holder = new ViewHolder();
                holder.filterNameTextView = (TextView) v.findViewById(android.R.id.title);
                v.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) v.getTag();
            }

            Filter filter = getItem(position);
            holder.filterNameTextView.setText(filter.name + " (" + filter.address + ")");

            return v;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.pickMessageFilters));
        setContentView(R.layout.filter_list_picker);

        Settings settings = new Settings(this);
        List<Filter> filters = settings.getFilters();

        mListView = (ListView) findViewById(R.id.messageFilterList);

        mAdapter = new FilterListArrayAdapter(this, filters);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Filter filter = mAdapter.getItem(position);
                        returnFilterName(filter.name);
                    }
                });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // force going to previous menu as the previous one has been destroyed
                Intent intent = new Intent(this, MessageList.class);
                startActivity(intent);

                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshList()
    {
        Settings settings = new Settings(this);
        List<Filter> filters = settings.getFilters();
        replaceListItems(filters);
    }

    private void replaceListItems(List<Filter> filters)
    {
        // HACK:
        // Checkbox states may be remembered even after clearing the adapter's
        // items and adding new ones. Hence, we work around this Android bug.
        mListView.clearChoices();

        mAdapter.clear();
        for (Filter filter : filters)
            mAdapter.add(filter);
        mAdapter.notifyDataSetChanged();
    }

    public void onChooseContact(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private void returnFilterName(String filterName) {
        if (filterName != null) {
            Intent intent = new Intent(this, MessageListFilter.class);
            intent.putExtra(MessageListFilter.FILTER_NAME_EXTRA, filterName);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (FilterForm.REQUEST_CODE_MUTATED) :
                if (resultCode == FilterForm.RESULT_CODE_MUTATED)
                    refreshList();
                break;
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor =  getContentResolver().query(contactData, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String filterName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        cursor.close();
                        Settings settings = new Settings(getApplicationContext());
                        Filter filter = settings.findFilterByName(filterName);
                        // create filter if it does not exists
                        if (filter == null) {
                            String filterAddress = settings.getContactAddress(filterName);
                            if (filterAddress != null) {
                                filter = new Filter(filterName, filterAddress, new ArrayList<String>());
                                settings.saveFilter(filter);
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.newMessageFilter) +
                                                " " + filterName +
                                                " (" + filterAddress + ") " +
                                                getString(R.string.newMessageFilter2),
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.chooseContactFailed),
                                        Toast.LENGTH_LONG).show();
                                filterName = null;
                            }
                        }
                        returnFilterName(filterName);
                    }
                }
                break;
        }
    }
}
