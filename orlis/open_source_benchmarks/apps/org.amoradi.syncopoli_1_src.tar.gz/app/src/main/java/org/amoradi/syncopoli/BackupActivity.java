package org.amoradi.syncopoli;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class BackupActivity extends AppCompatActivity implements IBackupHandler {
    private static final String TAG = "BackupActivity";

    private static final String SYNC_AUTHORITY = "org.amoradi.syncopoli.provider";
    private static final String SYNC_ACCOUNT_NAME = "Syncopoli Sync Account";
    private static final String SYNC_ACCOUNT_TYPE = "org.amoradi.syncopoli";

    Account mAccount;
    BackupHandler mBackupHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        mAccount = createSyncAccount(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long freq = Long.parseLong(prefs.getString(SettingsFragment.KEY_FREQUENCY, "8"));
        freq = freq * 3600; // hours to seconds

        ContentResolver.addPeriodicSync(mAccount, SYNC_AUTHORITY, new Bundle(), freq);

        copyExecutables();

        mBackupHandler = new BackupHandler(this);

        BackupListFragment f = new BackupListFragment();
        f.setBackupHandler(this);
        setCurrentFragment(f, false);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(layoutResId);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
    }

    public static Account createSyncAccount(Context ctx) {
        Account acc = new Account(SYNC_ACCOUNT_NAME, SYNC_ACCOUNT_TYPE);
        AccountManager accman = AccountManager.get(ctx);

        if (accman.addAccountExplicitly(acc, null, null)) {
            ContentResolver.setIsSyncable(acc, SYNC_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(acc, SYNC_AUTHORITY, true);
        }

        return acc;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.backup, menu);
        menu.findItem(R.id.action_done).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(true);
        menu.findItem(R.id.action_run).setVisible(true);

        return true;
    }

    public void syncBackups() {
        Snackbar.make(findViewById(R.id.backuplist_coordinator), "Running all sync tasks", Snackbar.LENGTH_SHORT).show();
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mAccount, SYNC_AUTHORITY, settingsBundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_run) {
            syncBackups();
        } else if (id == R.id.menu_settings) {
            setCurrentFragment(new SettingsFragment(), true);
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void addBackup(BackupItem item) {
        mBackupHandler.addBackup(item);
        BackupListFragment f = new BackupListFragment();
        f.setBackupHandler(this);
        setCurrentFragment(f, true);
    }

    public void updateBackupList() {
        mBackupHandler.updateBackupList();
    }

    public List<BackupItem> getBackups() {
        return mBackupHandler.getBackups();
    }

    public void updateBackupTimestamp(BackupItem b) {
        mBackupHandler.updateBackupTimestamp(b);
    }

    public int runBackup(BackupItem b) {
        syncBackups();
        return 0;
    }

    public void showLog(BackupItem b) {
        BackupLogFragment f = new BackupLogFragment();
        f.setBackupItem(b);
        setCurrentFragment(f, true);
    }


    private void setCurrentFragment(Fragment f, boolean stack) {
        FragmentTransaction tr = getFragmentManager().beginTransaction().replace(R.id.content_container, f);

        if (stack) {
            tr.addToBackStack(null);
        }

        tr.commit();
    }

    public void copyExecutables() {
        copyExecutable("rsync");
        copyExecutable("ssh");
    }

    public void copyExecutable(String filename) {
        File file = getFileStreamPath(filename);

        if (file.exists()) {
            return;
        }

        new DownloadBinaryTask(this).execute(filename);
    }

    private class DownloadBinaryTask extends AsyncTask<String, Void, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String filePath;

        public DownloadBinaryTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... filenames) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL("https://amoradi.org/public/android/arm/" + filenames[0]);
                filePath = context.getFileStreamPath(filenames[0]).getAbsolutePath();
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // download the file
                input = connection.getInputStream();
                output = context.openFileOutput(filenames[0], Context.MODE_PRIVATE);

                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        File f = new File(filePath);
                        f.delete();
                        return null;
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();

            if (result != null) {
                Toast.makeText(context, "Download Error: " + result, Toast.LENGTH_LONG).show();
                File f = new File(filePath);
                f.delete();
            } else {
                File f = new File(filePath);
                f.setExecutable(true);
            }
        }
    }

    public static class AddBackupItemFragment extends Fragment {
        IBackupHandler mHandler;

        @Override
        public void onAttach(Activity acc) {
            super.onAttach(acc);
            mHandler = (IBackupHandler) acc;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_addbackupitem, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.findItem(R.id.action_done).setVisible(true);
            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.action_run).setVisible(false);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.action_done) {
                BackupItem i = new BackupItem();

                EditText t = (EditText) getView().findViewById(R.id.addbackupitem_source);
                i.source = t.getText().toString();

                t = (EditText) getView().findViewById(R.id.addbackupitem_destination);
                i.destination = t.getText().toString();

                t = (EditText) getView().findViewById(R.id.addbackupitem_name);
                i.name = t.getText().toString();

                mHandler.addBackup(i);
            } else {
                return super.onOptionsItemSelected(item);
            }

            return true;
        }
    }

    public static class BackupLogFragment extends Fragment {
        private BackupItem mBackupItem;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        public void setBackupItem(BackupItem b) {
            mBackupItem = b;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_backuplog, container, false);
            if (mBackupItem != null) {
                ((TextView) v.findViewById(R.id.backuplog_textview)).setText(getLogString(mBackupItem.logFileName));
            } else {
                ((TextView) v.findViewById(R.id.backuplog_textview)).setText("mBackupItem is null");
            }

            return v;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.findItem(R.id.action_done).setVisible(false);
            menu.findItem(R.id.action_refresh).setVisible(true);
            menu.findItem(R.id.action_run).setVisible(false);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.action_refresh) {
                ((TextView) getView().findViewById(R.id.backuplog_textview)).setText(getLogString(mBackupItem.logFileName));
            } else {
                return super.onOptionsItemSelected(item);
            }

            return true;
        }

        public String getLogString(String filename) {
            try {
                FileInputStream ins = getActivity().getApplicationContext().openFileInput(filename);

                BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
                char[] buffer = new char[4096];

                StringBuilder output = new StringBuilder();
                while (reader.read(buffer) > 0) {
                    output.append(new String(buffer));
                }
                reader.close();
                ins.close();

                return output.toString();
            } catch (FileNotFoundException e) {
                return "Log file not found.";
            } catch (IOException e) {
                e.printStackTrace();
                return "An error occurred while trying to read log file.";
            }
        }
    }

    public static class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.ViewHolder> implements IBackupItemClickHandler {
        IBackupHandler mBackupHandler;

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            IBackupItemClickHandler mBackupClickHandler;

            public TextView mProfileTextView;
            public TextView mSrcTextView;
            public View mView;

            public ViewHolder(View v, IBackupItemClickHandler handler) {
                super(v);
                LinearLayout l = (LinearLayout) v.findViewById(R.id.backup_item_info);
                l.setOnClickListener(this);

                mView = l;
                mBackupClickHandler = handler;
                mProfileTextView = (TextView) v.findViewById(R.id.backup_item_profile_text);
                mSrcTextView = (TextView) v.findViewById(R.id.backup_item_source);
            }

            @Override
            public void onClick(View v) {
                if (v instanceof LinearLayout) {
                    mBackupClickHandler.onBackupShowLog(getAdapterPosition());
                }
            }
        }

        public BackupAdapter(IBackupHandler handler) {
            mBackupHandler = handler;
        }

        @Override
        public BackupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.backup_item, parent, false);
            ViewHolder vh = new ViewHolder(v.findViewById(R.id.backup_item), this);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int pos) {
            holder.mProfileTextView.setText(mBackupHandler.getBackups().get(pos).name);

            if (mBackupHandler.getBackups().get(pos).lastUpdate == null) {
                holder.mSrcTextView.setText("This backup has never run");
            } else {
                holder.mSrcTextView.setText("Last update: " + mBackupHandler.getBackups().get(pos).lastUpdate.toString());
            }

            holder.mView.setTranslationX(holder.mView.getTranslationX() -50f);
            holder.mView.setAlpha(0f);
            holder.mView.animate()
                    .setDuration(200)
                    .setStartDelay(holder.getLayoutPosition() * 50)
                    .translationXBy(50f)
                    .alpha(1f)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        @Override
        public int getItemCount() {
            return mBackupHandler.getBackups().size();
        }

        public void onBackupShowLog(int pos) {
            mBackupHandler.showLog(mBackupHandler.getBackups().get(pos));
        }
    }

    public static class BackupListFragment extends Fragment {
        private List<BackupItem> mBackupItems;
        private IBackupHandler mBackupHandler;
        private BackupAdapter mAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_backuplist, container, false);

            RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_backup);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

            mAdapter = new BackupAdapter(mBackupHandler);
            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

            FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
            if (fab != null) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AddBackupItemFragment f = new AddBackupItemFragment();
                        getFragmentManager().beginTransaction().replace(R.id.content_container, f).addToBackStack(null).commit();
                    }
                });
            }
            return v;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.findItem(R.id.action_done).setVisible(false);
            menu.findItem(R.id.action_refresh).setVisible(true);
            menu.findItem(R.id.action_run).setVisible(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                mBackupHandler.updateBackupList();
                mBackupItems = mBackupHandler.getBackups();
                mAdapter.notifyDataSetChanged();
            } else {
                super.onOptionsItemSelected(item);
            }

            return true;
        }

        public void setBackupHandler(IBackupHandler handler) {
            mBackupHandler = handler;
        }
    }
}
