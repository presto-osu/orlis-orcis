/**
 * Copyright (C) 2013 Damien Chazoule
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
 */

package com.doomy.overflow;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactActivity extends Activity implements OnItemClickListener {

	// Declare your view and variables
    private static final String TAG = "ContactActivity";
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<Contact> mList = new ArrayList<>();
    private static ContactActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        setContentView(R.layout.listview);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(this);

        ContentResolver mContentResolver = getContentResolver();
        Cursor mCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
                null, null);

        while (mCursor.moveToNext()) {

            String mFullName = mCursor
                    .getString(mCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String mPhoneNumber = mCursor
                    .getString(mCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (mPhoneNumber.startsWith("06")||mPhoneNumber.startsWith("07")||mPhoneNumber.startsWith("+336")||mPhoneNumber.startsWith("+337")) {
                Contact mContact = new Contact();
                mContact.setColorDark(randomColor());
                mContact.setFullName(mFullName);
                mContact.setPhoneNumber(mPhoneNumber);
                mList.add(mContact);
            }
        }
        mCursor.close();

        mAdapter = new ListViewAdapter(
                ContactActivity.this, R.layout.activity_contact, mList);
        mListView.setAdapter(mAdapter);

        if (null != mList && mList.size() != 0) {
            Collections.sort(mList, new Comparator<Contact>() {

                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return lhs.getFullName().compareTo(rhs.getFullName());
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.none), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        mSearchView.setSearchableInfo(mSearchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               mAdapter.getFilter().filter(query);
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               mAdapter.getFilter().filter(newText);
               return false;
           }
        });

        return true;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> listview, View v, int position, long id) {
    	Contact myContact = (Contact) listview.getItemAtPosition(position);

        String myName = myContact.getFullName().toString();
        String myPhoneNumber = myContact.getPhoneNumber().toString();
        int myColorDark = myContact.getColorDark();
        int myColor = normalColor(myColorDark);

        Intent mIntent = new Intent(ContactActivity.this, MessageActivity.class);
        mIntent.putExtra("fullname", myName);
        mIntent.putExtra("phonenumber", myPhoneNumber);
        mIntent.putExtra("colordark", myColorDark);
        mIntent.putExtra("color", myColor);
        setResult(Activity.RESULT_OK, mIntent);
        startActivity(mIntent);
    }

    public int randomColor() {

        int mTab [] = { R.color.redDark, R.color.pinkDark, R.color.purpleDark, R.color.deepPurpleDark, R.color.indigoDark,
                R.color.blueDark, R.color.lightBlueDark, R.color.cyanDark, R.color.tealDark, R.color.greenDark,
                R.color.lightGreenDark, R.color.limeDark, R.color.yellowDark, R.color.amberDark, R.color.orangeDark,
                R.color.deepOrangeDark, R.color.brownDark, R.color.greyDark, R.color.blueGreyDark };

        int myColor = mTab[(int) Math.floor(Math.random() * mTab.length)];

        return myColor;
    }

    public int normalColor(int myDarkColor) {
        int myColor = R.color.indigo;
        switch (myDarkColor) {
            case R.color.redDark :
                myColor = R.color.red;
                break;
            case R.color.pinkDark :
                myColor = R.color.pink;
                break;
            case R.color.purpleDark :
                myColor = R.color.purple;
                break;
            case R.color.deepPurpleDark :
                myColor = R.color.deepPurple;
                break;
            case R.color.indigoDark :
                myColor = R.color.indigo;
                break;
            case R.color.blueDark :
                myColor = R.color.blue;
                break;
            case R.color.lightBlueDark :
                myColor = R.color.lightBlue;
                break;
            case R.color.cyanDark :
                myColor = R.color.cyan;
                break;
            case R.color.tealDark :
                myColor = R.color.teal;
                break;
            case R.color.greenDark :
                myColor = R.color.green;
                break;
            case R.color.lightGreenDark :
                myColor = R.color.lightGreen;
                break;
            case R.color.limeDark :
                myColor = R.color.lime;
                break;
            case R.color.yellowDark :
                myColor = R.color.yellow;
                break;
            case R.color.amberDark :
                myColor = R.color.amber;
                break;
            case R.color.orangeDark :
                myColor = R.color.orange;
                break;
            case R.color.deepOrangeDark :
                myColor = R.color.deepOrange;
                break;
            case R.color.brownDark :
                myColor = R.color.brown;
                break;
            case R.color.greyDark :
                myColor = R.color.grey;
                break;
            case R.color.blueGreyDark :
                myColor = R.color.blueGrey;
                break;
        }
        return myColor;
    }

    public static ContactActivity getInstance() {
        return mActivity;
    }
}
