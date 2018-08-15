package com.guvery.notifyme;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;


/**
 * Things to check before updating:
 * Bump version code + name + string app_version
 *
 * Currently SettingsActivity accesses the static Notifier.ontoggle method
 * I need to somehow get settingsactivity its own notifier instance with context(!)
 * so i can add the L settings option when long pressing the create notif in notif drawer
 *
 */

public class MainActivity extends ActionBarActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Notifier mNotifier;
    private NotificationHistory mNotifHistory;
    private ArrayList<Notif> mDataSet;
    private TextView empty;
    private SharedPreferences mSharedPrefs;

    private final Context mContext = this;
    public static final String NOTIF_FOLDER = "notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        empty = (TextView) findViewById(R.id.empty);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up our notifier (to clear notifications etc..)
        mNotifier = new Notifier(this);

        // set up history
        mNotifHistory = NotificationHistory.getInstance(getFilesDir(), NOTIF_FOLDER);
        mDataSet = mNotifHistory.getHistory();

        // recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.history_view);
        mRecyclerView.setHasFixedSize(true); // improve performance if layout size no changie
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new HistoryAdapter(mDataSet, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int itemPosition = mRecyclerView.getChildPosition(view);
                final Notif n = mDataSet.get(itemPosition);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(n.getTitle()).setIcon(ImageAdapter.getDarkFromLight(n.getImageId()));
                builder.setItems(new CharSequence[]{"Notify Me", "Delete", "Share", "Copy"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case (0):
                                        mNotifier.notifyFromHistory(n);
                                        break;
                                    case (1):
                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                                        dialogBuilder.setMessage(R.string.delete_history_item);
                                        dialogBuilder.setNegativeButton(R.string.no, null);
                                        dialogBuilder.setPositiveButton(R.string.yes,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        removeNotification(n);
                                                    }
                                                });
                                        AlertDialog dialog1 = dialogBuilder.create();
                                        dialog1.show();
                                        break;
                                    case (2):
                                        shareNotification(n);
                                        break;
                                    case (3):
                                        copy(n);
                                        break;
                                }
                            }
                        }).show();
            }
        }));

        // Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_create);
        fab.attachToRecyclerView(mRecyclerView);

        // If we opened the app via a notification, or text was shared, or on a delete intent
        Intent intent = getIntent();
        if (intent.getBooleanExtra("com.guvery.notifyme.isNotification", false)) {
            onNewIntent(intent);
        }

        // Uhh...
        if (!mSharedPrefs.getBoolean("com.guvery.notifyme.iconsFixed14", false)) {
            // Find the icons that mess up on upgrade to 14
            ArrayList<Notif> messUps = new ArrayList<>();
            for (Notif n : mDataSet) {
                if (!ImageAdapter.isAnIcon(n.getImageId())) {
                    messUps.add(n);
                }
            }
            // Fix em
            for (Notif n : messUps) {
                n.setImageId(CreateActivity.DEFAULT_ICON);
                removeNotification(n);
                mNotifHistory.add(n);
                refreshList();
            }
            mSharedPrefs.edit().putBoolean("com.guvery.notifyme.iconsFixed14", true).apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Create it on start
        Notifier.toggleCreateNotification(
                mSharedPrefs.getBoolean("settings_create_notification", false));

        refreshList();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("com.guvery.notifyme.isNotification", false)) {
            int mId = intent.getIntExtra("com.guvery.notifyme.Id", 0);
            Notif o = mNotifHistory.findNotifById(mId);
            handleNotificationTapped(o);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_clear) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(R.string.alert_clear_notifications);
            dialogBuilder.setNegativeButton(R.string.action_no, null);
            dialogBuilder.setPositiveButton(R.string.action_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mNotifier.clearNotifs();
                        }
                    });
            dialogBuilder.create().show();
            return true;
        }
        if (id == R.id.action_clear_history) {
            AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
            dialogBuilder2.setMessage(R.string.alert_clear_history);
            dialogBuilder2.setNegativeButton(R.string.action_no, null);
            dialogBuilder2.setPositiveButton(R.string.action_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            clearAllNotifications();
                        }
                    });
            dialogBuilder2.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleNotificationTapped(final Notif n) {
        if (n != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(n.getTitle())
                    .setMessage(n.getBody())
                    .setIcon(ImageAdapter.getDarkFromLight(n.getImageId()));
            dialogBuilder.setPositiveButton(R.string.dismiss,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mNotifier.clearNotif(n.getId());
                        }
                    });
            dialogBuilder.setNeutralButton(R.string.delete,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                            dialogBuilder.setMessage(R.string.delete_history_item);
                            dialogBuilder.setNegativeButton(R.string.no, null);
                            dialogBuilder.setPositiveButton(R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            removeNotification(n);
                                        }
                                    });
                            AlertDialog dialog1 = dialogBuilder.create();
                            dialog1.show();
                        }
                    });
            dialogBuilder.setNegativeButton(R.string.copy,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            copy(n);
                        }
                    });
            dialogBuilder.create().show();
        }
    }

    private void shareNotification(Notif n) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, n.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                (n.getBody().length() == 0 ? n.getTitle() : n.getBody()));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share Notification"));
    }

    private void copy(Notif n) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip;
        if (n.getBody().trim().length() != 0) {
            clip = ClipData.newPlainText(n.getBody(),
                    n.getBody());
            Toast.makeText(getApplicationContext(),
                    R.string.message_copied,
                    Toast.LENGTH_SHORT).show();
        } else {
            clip = ClipData.newPlainText(n.getTitle(),
                    n.getTitle());
            Toast.makeText(getApplicationContext(),
                    R.string.title_copied,
                    Toast.LENGTH_SHORT).show();
        }
        clipboard.setPrimaryClip(clip);
    }

    private void refreshList() {
        mDataSet = mNotifHistory.getHistory();
        mAdapter.notifyDataSetChanged();

        // Show or hide the empty text
        if (mDataSet.size() > 0) {
            empty.setVisibility(View.INVISIBLE);
        } else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void removeNotification(Notif n) {
        int index = mDataSet.indexOf(mNotifHistory.findNotifById(n.getId()));
        mNotifHistory.remove(n);
        mDataSet = mNotifHistory.getHistory();
        mAdapter.notifyItemRemoved(index);
        mNotifier.clearNotif(n.getId());

        // Show or hide the empty text
        if (mDataSet.size() > 0) {
            empty.setVisibility(View.INVISIBLE);
        } else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void clearAllNotifications() {
        mNotifHistory.clear();
        mNotifier.clearNotifs();
        refreshList();
    }

    public void create(View v) {
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }
}
