package com.guvery.notifyme;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;


public class CreateActivity extends ActionBarActivity {
    private EditText mTitle;
    private EditText mBody;
    private CheckBox mOngoing;
    private Spinner mPriority;
    private Notifier mNotifier;
    private NotificationHistory mNotifHistory;
    private SharedPreferences mPrefs;
    private ImageButton mIconPicker;
    private AlertDialog mImagePickerDialog;
    private int mIconId;

    public static final int DEFAULT_ICON = R.drawable.ic_lightbulb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Work around having the label name in manifest being "Notify Me" for sharing purposes
        getSupportActionBar().setTitle("Create");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mNotifier = new Notifier(getApplicationContext());
        mTitle = (EditText) findViewById(R.id.create_title);
        mBody = (EditText) findViewById(R.id.create_body);
        mOngoing = (CheckBox) findViewById(R.id.create_ongoing);
        mPriority = (Spinner) findViewById(R.id.create_spinner);

        mPriority = (Spinner) findViewById(R.id.create_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.priority_spinner_names,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPriority.setAdapter(adapter);

        mIconPicker = (ImageButton) findViewById(R.id.icon_picker);
        mIconId = DEFAULT_ICON;

        resetForm();

        mNotifHistory = NotificationHistory.getInstance(getFilesDir(), MainActivity.NOTIF_FOLDER);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                // Handle text being sent
                handleSendText(intent);
            } else if (type.startsWith("image/")) {
                // Handle single image being sent
                // do nothing
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                //handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_done) {
            sendToNotifier();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSendText(Intent intent) {
        String titleText = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String bodyText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (bodyText != null) {
            // Update UI to reflect text being shared
            mBody.setText(bodyText);
        }
        if (titleText != null) {
            // Update UI to reflect text being shared
            mTitle.setText(titleText);
        }
    }

    public void iconPicker(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.icon_picker, (ViewGroup) findViewById(R.id.icon_picker_grid_view));

        GridView gridView = (GridView) layout.findViewById(R.id.icon_picker_grid_view);
        gridView.setAdapter(new ImageAdapter(CreateActivity.this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                mIconPicker.setImageResource(v.getId()); // ignore this error it is CORRECT
                mImagePickerDialog.hide();
                mIconId = ImageAdapter.mThumbIds[position];
            }
        });

        builder.setView(layout);
        mImagePickerDialog = builder.create();
        mImagePickerDialog.show();
    }

    private void sendToNotifier() {
        String title = mTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "You must enter a title!", Toast.LENGTH_SHORT).show();
            return;
        }

        String body = mBody.getText().toString().trim();

        boolean mIsList = false;
        if (body.length() > 40) {
            mIsList = true;
        } else {
            for (int i = 0; i < body.length(); i++) {
                if (body.charAt(i) == '\n')
                    mIsList = true;
            }
        }

        // Priority
        String sp = (String) mPriority.getSelectedItem();
        int p = 0;
        if (sp.equals("High")) {
            p = 2;
        } else if (sp.equals("Low")) {
            p = -2;
        }

        int[] time = {0, 0};
        Notif n = new Notif(title, body, mOngoing.isChecked(), mIsList, p,
                mIconId, time);

        mNotifier.notifyMe(n);
        mNotifHistory.add(n);
        finish();
    }

    private void resetForm() {
        mTitle.setText("");
        mTitle.requestFocus();
        mBody.setText("");
        mOngoing.setChecked(mPrefs.getBoolean("settings_always_ongoing", false));
        if (mPrefs.getString("default_priority", "Default").equals("2")) {
            mPriority.setSelection(1);
        } else if (mPrefs.getString("default_priority", "Default").equals("-2"))
            mPriority.setSelection(2);
        else
            mPriority.setSelection(0);

        mIconId = DEFAULT_ICON;
        mIconPicker.setImageResource(ImageAdapter.getDarkFromLight(DEFAULT_ICON));
    }
}
