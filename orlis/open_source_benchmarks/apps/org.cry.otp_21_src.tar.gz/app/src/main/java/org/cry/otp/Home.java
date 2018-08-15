package org.cry.otp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;
import java.util.TimeZone;

public class Home extends AppCompatActivity {
    public static final int OTP_TYPE_MOTP = 0;
    public static final int OTP_TYPE_HOTP = 1;
    public static final int OTP_TYPE_TOTP = 2;
    private static final int MENU_ABOUT = 1;
    private static final int MENU_TIME = 2;
    private String activeProfName = "";
    private String activeSeed = "";
    private int activeOTPType = OTP_TYPE_MOTP;
    private int activeCount = 0;
    private int activeRowId = -1;
    private int activeDigits = 6;
    private String activeZone = "GMT";
    private int activeTimeInterval = 30;
    private SharedPreferences preferences;
    private DBAdapter db;

    private final OnClickListener generateMOTPListener = new OnClickListener() {
        public void onClick(View v) {
            TextView pinEditText = (TextView) findViewById(R.id.motpPinEditText);
            if (pinEditText != null && pinEditText.getText().length() != 4) {
                return;
            }

            String key = mOTP.gen(pinEditText.getText().toString(), activeSeed, activeZone);
            TextView keyTextView = (TextView) findViewById(R.id.motpKeyTextView);
            if(keyTextView != null) {
                keyTextView.setText(key);
                keyTextView.setVisibility(View.VISIBLE);
            }

            pinEditText.setText("");
        }
    };

    private final OnClickListener generateHOTPListener = new OnClickListener() {

        public void onClick(View v) {
            HOTP hotp = new HOTP();
            String key = hotp.gen(activeSeed, activeCount, activeDigits);
            TextView keyTextView = (TextView) findViewById(R.id.hotpKeyTextView);
            if(keyTextView != null) {
                keyTextView.setText(key);
                keyTextView.setVisibility(View.VISIBLE);
            }

            SharedPreferences.Editor ed = preferences.edit();
            activeCount = activeCount + 1;
            ed.putInt("count", activeCount);
            ed.apply();
            db.open();
            db.updateCount(activeRowId, activeCount);
            db.close();
        }
    };

    private final OnClickListener generateTOTPListener = new OnClickListener() {
        public void onClick(View v) {
            Spinner SHATypeSpinner = (Spinner) findViewById(R.id.totpSHATypeSpinner);
            int shaType = SHATypeSpinner.getSelectedItemPosition();
            String key = TOTP.gen(activeSeed, activeDigits, shaType, activeZone, activeTimeInterval);
            TextView keyTextView = (TextView) findViewById(R.id.totpKeyTextView);
            if(keyTextView != null) {
                keyTextView.setText(key);
                keyTextView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBAdapter(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        activeProfName = preferences.getString(DBAdapter.KEY_PROF_NAME, "Profile Name");
        activeSeed = preferences.getString(DBAdapter.KEY_SEED, "0");
        activeOTPType = preferences.getInt(DBAdapter.KEY_OTP_TYPE, OTP_TYPE_MOTP);
        activeCount = preferences.getInt(DBAdapter.KEY_COUNT, 0);
        activeRowId = preferences.getInt(DBAdapter.KEY_ROW_ID, -1);
        activeDigits = preferences.getInt(DBAdapter.KEY_DIGITS, 6);
        activeZone = preferences.getString(DBAdapter.KEY_TIME_ZONE, "GMT");
        activeTimeInterval = preferences.getInt(DBAdapter.KEY_TIME_INTERVAL, 30);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activeOTPType == OTP_TYPE_HOTP) {
            // HOTP
            setContentView(R.layout.hotp_main);
            Button generateButton = (Button) findViewById(R.id.hotpGenerateButton);
            TextView profNameTextView = (TextView) findViewById(R.id.hotpProfileNameTextView);
            profNameTextView.setText(activeProfName);
            generateButton.setOnClickListener(generateHOTPListener);
            setTitle(getString(R.string.app_name) + " - HOTP");
        } else if (activeOTPType == OTP_TYPE_TOTP) {
            setContentView(R.layout.totp_main);
            Button generateButton = (Button) findViewById(R.id.totpGenerateButton);
            TextView profNameTextView = (TextView) findViewById(R.id.totpProfileNameTextView);
            profNameTextView.setText(activeProfName);
            Spinner SHATypeSpinner = (Spinner) findViewById(R.id.totpSHATypeSpinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.SHATypes, R.layout.spinner_layout);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            SHATypeSpinner.setAdapter(adapter);
            generateButton.setOnClickListener(generateTOTPListener);
            setTitle(getString(R.string.app_name) + " - TOTP");
        } else {
            // mOTP
            setContentView(R.layout.motp_main);
            Button generateButton = (Button) findViewById(R.id.motpGenerateButton);
            TextView profNameTextView = (TextView) findViewById(R.id.motpProfileNameTextView);
            EditText pinEditText = (EditText) findViewById(R.id.motpPinEditText);
            profNameTextView.setText(activeProfName);
            generateButton.setOnClickListener(generateMOTPListener);
            pinEditText.addTextChangedListener(new PinTextWatcher());
            setTitle(getString(R.string.app_name) + " - mOTP");
            TextView epoch = (TextView) findViewById(R.id.epochValue);
            long time = new Date().getTime();
            String epochValue = "" + time + TimeZone.getTimeZone(activeZone).getOffset(time);
            activeZone = preferences.getString(DBAdapter.KEY_TIME_ZONE, "GMT");
            epoch.setText(epochValue.substring(0, 9));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (activeOTPType == OTP_TYPE_MOTP || activeOTPType == OTP_TYPE_TOTP) {
            menu.add(0, MENU_TIME, 0, R.string.time).setIcon(
                    R.drawable.ic_menu_time);
        }
        menu.add(0, MENU_ABOUT, 0, R.string.about_info).setIcon(
                R.drawable.ic_menu_about);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
            case MENU_ABOUT:
                builder.setTitle(getString(R.string.about_dialog_title));
                builder.setMessage(getString(R.string.info));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return true;
            case MENU_TIME:
                builder.setTitle(getString(R.string.time));
                long time = new Date().getTime();
                String epoch = ""
                        + (time + TimeZone.getTimeZone(activeZone).getOffset(time));

                if (activeOTPType == OTP_TYPE_MOTP) {
                    epoch = epoch.substring(0, epoch.length() - 4);
                }
                builder.setMessage(getString(R.string.time) + ":  " + epoch + "\n"
                        + getString(R.string.time_zone) + ":  " + activeZone);
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return true;
        }
        return false;
    }

    private class PinTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            Button generateButton = (Button) findViewById(R.id.motpGenerateButton);
            if (s.length() == 4) {
                generateButton.setEnabled(true);
            } else {
                generateButton.setEnabled(false);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    }
}
