package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DnsListActivity extends ListActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor sped;
    private ArrayList<String> dnsList;
    private ListView mListView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();
        mListView = getListView();

        dnsList = new ArrayList<>(sp.getStringSet("dnslist", new HashSet<String>()));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dnsList);
        setListAdapter(adapter);


        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int count = mListView.getCheckedItemCount();
                mode.setTitle(String.format("%d", count));
                View entry = mListView.getChildAt(position);
                if(checked) {
                    entry.setBackgroundResource(android.R.color.holo_blue_dark);
                }else{
                    entry.setBackgroundResource(android.R.color.transparent);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.item_longclick, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        SparseBooleanArray selectedItems = mListView.getCheckedItemPositions();
                        ArrayList<String> deletingString = new ArrayList<>();
                        //Get will be deleted entry
                        for(int i = 0; i != selectedItems.size(); i++){
                            if(selectedItems.valueAt(i)){
                                int j = selectedItems.keyAt(i);
                                String s = adapter.getItem(j);
                                deletingString.add(s);
                            }
                        }
                        //Delete phase
                        for(String s: deletingString) {
                            adapter.remove(s);
                        }
                        mode.finish();

                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                SparseBooleanArray selectedItems = mListView.getCheckedItemPositions();
                for(int i = 0; i != selectedItems.size(); i++){
                    if(selectedItems.valueAt(i)){
                        int j = selectedItems.keyAt(i);
                        mListView.getChildAt(j).setBackgroundResource(android.R.color.transparent);
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dnslist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_add:
                addItem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause(){
        super.onPause();
        sped = sp.edit();
        Set<String> toSavedDNS = new HashSet<>(dnsList);
        sped.putStringSet("dnslist", toSavedDNS);
        sped.apply();
    }

    private void addItem(){
        AlertDialog.Builder dnsDialog = new AlertDialog.Builder(this);
        final DnsEditText dnsEditText = new DnsEditText(this);
        dnsDialog.setView(dnsEditText);
        dnsDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dns = dnsEditText.getText().toString();
                if (!dns.equals("") &&
                        (IPChecker.IPv4Checker(dns)
                                || IPChecker.IPv6Checker(dns))) {
                    adapter.add(dns);
                }
            }
        });
        dnsDialog.setNegativeButton(android.R.string.cancel, null);
        dnsDialog.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final String focusedString = adapter.getItem(position);

        AlertDialog.Builder dnsDialog = new AlertDialog.Builder(this);
        final DnsEditText dnsEditText = new DnsEditText(this);
        dnsEditText.setText(focusedString);

        dnsDialog.setView(dnsEditText);
        dnsDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dns = dnsEditText.getText().toString();
                if (!dns.equals("") &&
                        (IPChecker.IPv4Checker(dns)
                                || IPChecker.IPv6Checker(dns))) {
                    adapter.add(dns);
                    adapter.remove(focusedString);
                }
            }
        });
        dnsDialog.setNegativeButton(android.R.string.cancel, null);
        dnsDialog.show();

    }



}
