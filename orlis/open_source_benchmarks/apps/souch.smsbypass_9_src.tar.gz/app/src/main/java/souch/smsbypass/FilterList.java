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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FilterList extends Activity
{
    //private static final String TAG = "Filters";

    private Settings mSettings;

    private CheckableFilterListArrayAdapter
            mAdapter;

    private ListView mListView;

    private class CheckableFilterListArrayAdapter extends ArrayAdapter<Filter>
    {
        public CheckableFilterListArrayAdapter(Context context, List<Filter> filters)
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
                v = inflater.inflate(R.layout.filter_list_item, null);
                holder = new ViewHolder();
                holder.nameEditText = (TextView) v.findViewById(android.R.id.title);
                holder.addressEditText = (TextView) v.findViewById(android.R.id.summary);
                v.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) v.getTag();
            }

            Filter filter = getItem(position);
            holder.nameEditText.setText(filter.name);
            holder.addressEditText.setText(filter.address);

            return v;
        }

        private class ViewHolder
        {
            TextView nameEditText;
            TextView addressEditText;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.messageFilters));
        setContentView(R.layout.filter_list);

        mSettings = new Settings(this);

        List<Filter> filters = mSettings.getFilters();

        mListView = (ListView) findViewById(R.id.messageFilterList);

        mAdapter = new CheckableFilterListArrayAdapter(this, filters);
        mListView.setAdapter(mAdapter);

        registerForContextMenu(mListView);
    }


    @Override
    protected void onStop() {
        super.onStop();

        // force exiting in order to not come back to this view if the app was only put in background
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // force going to previous menu as the previous one has been destroyed
                Intent intent = new Intent(this, UI.class);
                startActivity(intent);

                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (!(v instanceof ListView))
        {
            throw new AssertionError();
        }
        else
        {
            menu.setHeaderTitle(getFilterNameFromMenuInfo(menuInfo));
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.message_filter_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int adapterPosition = info.position;
        int menuItemID = item.getItemId();
        if (menuItemID == android.R.id.edit)
        {
            Intent intent = new Intent(this, FilterForm.class);
            String filterName = getFilterNameFromMenuInfo(item.getMenuInfo());
            intent.putExtra(FilterForm.FILTER_NAME_EXTRA, filterName);
            startActivityForResult(intent, FilterForm.REQUEST_CODE_MUTATED);
            return true;
        }
        else if (menuItemID == R.id.delete)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete)
                .setMessage(R.string.filterWillBeDeleted)
                .setPositiveButton(
                    R.string.delete,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Filter filter = mAdapter.getItem(adapterPosition);
                            mSettings.deleteFilter(filter.name);
                            removeAdapterItem(adapterPosition);
                        }
                    })
                .setNegativeButton(android.R.string.cancel, null);
            builder.show();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == FilterForm.REQUEST_CODE_MUTATED)
        {
            if (resultCode == FilterForm.RESULT_CODE_MUTATED)
                refreshList();
        }
    }

    private String getFilterNameFromMenuInfo(ContextMenuInfo menuInfo)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        TextView textView = (TextView) info.targetView.findViewById(android.R.id.title);
        return textView.getText().toString();
    }

    private void refreshList()
    {
        List<Filter> filters = mSettings.getFilters();
        replaceListItems(filters);
    }

    private Filter getListItem(int position)
    {
        return (Filter) mListView.getItemAtPosition(position);
    }

    public void onAdd(View v)
    {
        Intent intent = new Intent(this, FilterForm.class);
        startActivityForResult(intent, FilterForm.REQUEST_CODE_MUTATED);
    }

    public void onConfirmDelete(View v)
    {
        if (getCheckedItems().size() == 0)
        {
            Toast.makeText(this, getString(R.string.noItemsSelected), Toast.LENGTH_SHORT).show();
            return;
        }

        int messageResourceID = R.string.filterWillBeDeleted;
        if (getCheckedItems().size() > 1)
            messageResourceID = R.string.filtersWillBeDeleted;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.delete)
            .setMessage(messageResourceID)
            .setPositiveButton(
                R.string.delete,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        delete();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void delete()
    {
        // We shouldn't remove items while iterating over the items. Hence,
        // keep track of items that should be kept, remove all items after
        // iterating, then restore items that should be kept.
        List<Filter> uncheckedItems = new ArrayList<Filter>();
        for (int i = 0; i < mListView.getCount(); ++i)
        {
            Filter filter = getListItem(i);
            if (mListView.isItemChecked(i))
                mSettings.deleteFilter(filter.name);
            else
                uncheckedItems.add(filter);
        }

        replaceListItems(uncheckedItems);
    }

    private List<Filter> getCheckedItems()
    {
        List<Filter> items = new ArrayList<Filter>();
        for (int i = 0; i < mListView.getCount(); ++i)
        {
            if (mListView.isItemChecked(i))
            {
                Filter item = getListItem(i);
                items.add(item);
            }
        }
        return items;
    }

    private void removeAdapterItem(int position)
    {
        // HACK:
        // We have to uncheck the item before removing it, otherwise
        // mListView.getCheckedItemPositions() will still return 'true'
        // for the item.
        mListView.setItemChecked(position, false);

        mAdapter.remove(mAdapter.getItem(position));
        mAdapter.notifyDataSetChanged();
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
}
