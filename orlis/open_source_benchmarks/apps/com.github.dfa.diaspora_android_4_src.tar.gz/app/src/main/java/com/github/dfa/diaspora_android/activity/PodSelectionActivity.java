/*
    This file is part of the Diaspora for Android.

    Diaspora for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora for Android.

    If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.dfa.diaspora_android.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.R;
import com.github.dfa.diaspora_android.task.GetPodsService;
import com.github.dfa.diaspora_android.util.Helpers;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;


public class PodSelectionActivity extends AppCompatActivity {
    private App app;

    @BindView(R.id.podselection__edit_filter)
    public EditText editFilter;

    @BindView(R.id.podselection__listpods)
    public ListView listPods;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.podselection__activity);
        ButterKnife.bind(this);
        app = (App) getApplication();
        setSupportActionBar(toolbar);


        listPods.setTextFilterEnabled(true);
        setListedPods(app.getSettings().getPreviousPodlist());
        LocalBroadcastManager.getInstance(this).registerReceiver(podListReceiver, new IntentFilter(GetPodsService.MESSAGE_PODS_RECEIVED));

        if (!Helpers.isOnline(PodSelectionActivity.this)) {
            Snackbar.make(listPods, R.string.no_internet, Snackbar.LENGTH_LONG).show();
        }
    }


    private final BroadcastReceiver podListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("pods")) {
                Bundle extras = intent.getExtras();
                String[] pods = extras.getStringArray("pods");
                if (pods != null && pods.length > 0) {
                    app.getSettings().setPreviousPodlist(pods);
                    setListedPods(pods);
                } else {
                    setListedPods(app.getSettings().getPreviousPodlist());
                    Snackbar.make(listPods, R.string.podlist_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    };

    @OnClick(R.id.podselection__button_select_pod)
    public void onButtonSelectPodClicked(View view) {
        if (editFilter.getText().length() > 4 && editFilter.getText().toString().contains("")) {
            showPodConfirmationDialog(editFilter.getText().toString());
        } else {
            Snackbar.make(listPods, R.string.valid_pod, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(PodSelectionActivity.this, GetPodsService.class);
        startService(i);
    }


    private void setListedPods(String[] listedPodsArr) {
        final ArrayList<String> listedPodsList = new ArrayList<>();
        for (String pod : listedPodsArr) {
            listedPodsList.add(pod.toLowerCase());
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                PodSelectionActivity.this,
                android.R.layout.simple_list_item_1,
                listedPodsList);

        // save index and top position
        int index = listPods.getFirstVisiblePosition();
        View v = listPods.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - listPods.getPaddingTop());
        listPods.setAdapter(adapter);
        listPods.setSelectionFromTop(index, top);

        adapter.getFilter().filter(editFilter.getText());
        editFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (adapter).getFilter().filter(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    @OnItemClick(R.id.podselection__listpods)
    public void onListPodsItemClicked(int position) {
        showPodConfirmationDialog((String) listPods.getAdapter().getItem(position));
    }

    private void showPodConfirmationDialog(final String selectedPod) {
        // Make a clickable link
        final SpannableString dialogMessage = new SpannableString(getString(R.string.confirm_pod, selectedPod));
        Linkify.addLinks(dialogMessage, Linkify.ALL);

        // Check if online
        if (!Helpers.isOnline(PodSelectionActivity.this)) {
            Snackbar.make(listPods, R.string.no_internet, Snackbar.LENGTH_LONG).show();
            return;
        }

        // Show dialog
        new AlertDialog.Builder(PodSelectionActivity.this)
                .setTitle(getString(R.string.confirmation))
                .setMessage(dialogMessage)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onPodSelectionConfirmed(selectedPod);
                            }
                        })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public void onPodSelectionConfirmed(String selectedPod) {
        app.getSettings().setPodDomain(selectedPod);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().removeSessionCookies(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                CookieManager.getInstance().removeAllCookie();
                CookieManager.getInstance().removeSessionCookie();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Helpers.animateToActivity(this, MainActivity.class, true);
    }


    @Override
    public void onBackPressed() {
        Snackbar.make(listPods, R.string.confirm_exit, Snackbar.LENGTH_LONG)
                .setAction(android.R.string.yes, new View.OnClickListener() {
                    public void onClick(View view) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(podListReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pods__menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload: {
                if (Helpers.isOnline(PodSelectionActivity.this)) {
                    Intent i = new Intent(PodSelectionActivity.this, GetPodsService.class);
                    startService(i);
                    return true;
                } else {
                    Snackbar.make(listPods, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}


