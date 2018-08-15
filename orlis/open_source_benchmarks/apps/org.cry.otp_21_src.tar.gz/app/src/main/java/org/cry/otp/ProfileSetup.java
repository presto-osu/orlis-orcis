package org.cry.otp;

import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ProfileSetup extends AppCompatActivity {
    private SharedPreferences preferences;
    private boolean editing = false;
    private int editingRowID = -1;

    private Builder builder;
    private DBAdapter db;

    private final OnClickListener motpSaveButtonListener = new OnClickListener() {

        public void onClick(View v) {
            EditText profName = (EditText) findViewById(R.id.motpProfileEditText);
            EditText profSeed = (EditText) findViewById(R.id.motpSeedEditText);
            Spinner spinner = (Spinner) findViewById(R.id.motpTimeZoneSpinner);
            String name = profName.getText().toString();
            String seed = profSeed.getText().toString();
            int zone = spinner.getSelectedItemPosition();
            String time_zone = positionToZone(zone);
            if (seed.length() != 20) {
                builder.setTitle(getString(R.string.error_title));
                builder.setMessage(getString(R.string.error));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return;
            }
            if (checkIfInDatabase(name)) {
                builder.setTitle(getString(R.string.error_title));
                builder.setMessage(getString(R.string.error));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return;
            }

            if (!editing) {
                insertIntoDatabaseAndFinish(name, seed, Home.OTP_TYPE_MOTP, 0, time_zone, 30);
            } else {
                updateIntoDatabaseAndFinish(name, seed, Home.OTP_TYPE_MOTP, 0, time_zone, 30);
            }
        }
    };
    private final OnClickListener hotpSaveButtonListener = new OnClickListener() {
        public void onClick(View v) {
            EditText profName = (EditText) findViewById(R.id.hotpProfileEditText);
            EditText profSeed = (EditText) findViewById(R.id.hotpSeedEditText);
            EditText profDigit = (EditText) findViewById(R.id.hotpOutputSizeEditText);
            Spinner seedTypeSpinner = (Spinner) findViewById(R.id.hotpSeedTypeSpinner);
            String name = profName.getText().toString();
            String seed = profSeed.getText().toString();
            boolean hexadecimalSeed = seedTypeSpinner.getSelectedItemPosition() == 0;
            int digits = Integer.parseInt(profDigit.getText().toString());
            if (hexadecimalSeed) {
                try {
                    if (seed.length() % 2 != 0) {
                        throw new NumberFormatException();
                    }

                    if (seed.substring(0, 2).toLowerCase().compareTo("0x") == 0) {
                        seed = seed.substring(2);
                    }

                    for (int i = 0; i < seed.length(); i++) {
                        Integer.parseInt(seed.substring(i, i + 1), 16);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    builder.setTitle(R.string.error_title);
                    builder.setMessage(R.string.hex_seed_error);
                    builder.setPositiveButton(getString(R.string.ok), null);
                    builder.show();
                    return;
                }
            } else {
                String newSeed = "";
                for (int i = 0; i < seed.length(); i++) {
                    newSeed += Integer.toHexString((int) seed.charAt(i));
                }

                seed = newSeed;
            }

            if (digits <= 0 || digits >= 10) {
                builder.setTitle(R.string.error_title);
                builder.setMessage(R.string.digit_error);
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return;
            }

            if (checkIfInDatabase(name)) {
                builder.setTitle(getString(R.string.error_title));
                builder.setMessage(getString(R.string.error));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return;
            }

            if (!editing) {
                insertIntoDatabaseAndFinish(name, seed, Home.OTP_TYPE_HOTP, digits, "", 30);
            } else {
                updateIntoDatabaseAndFinish(name, seed, Home.OTP_TYPE_HOTP, digits, "", 30);
            }
        }
    };

    private final OnClickListener totpSaveButtonListener = new OnClickListener() {

        public void onClick(View v) {
            EditText profName = (EditText) findViewById(R.id.totpProfileEditText);
            EditText profSeed = (EditText) findViewById(R.id.totpSeedEditText);
            EditText profDigits = (EditText) findViewById(R.id.totpOutputSizeEditText);
            Spinner seedTypeSpinner = (Spinner) findViewById(R.id.totpSeedTypeSpinner);
            Spinner timeIntervalSpinner = (Spinner) findViewById(R.id.totpTimeIntervalSpinner);
            String name = profName.getText().toString();
            String seed = profSeed.getText().toString();
            int digits = Integer.parseInt(profDigits.getText().toString());
            boolean hexadecimalSeed = seedTypeSpinner.getSelectedItemPosition() == 0;
            int timeInterval = timeIntervalSpinner.getSelectedItemPosition() == 0 ? 30 : 60;
            if (digits <= 0 || digits >= 10) {
                builder.setTitle(R.string.error_title);
                builder.setMessage(R.string.digit_error);
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return;
            }

            if (hexadecimalSeed) {
                try {
                    if (seed.length() % 2 != 0) {
                        throw new NumberFormatException();
                    }

                    if (seed.substring(0, 2).toLowerCase().compareTo("0x") == 0) {
                        seed = seed.substring(2);
                    }

                    for (int i = 0; i < seed.length(); i++) {
                        Integer.parseInt(seed.substring(i, i + 1), 16);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    builder.setTitle(R.string.error_title);
                    builder.setMessage(R.string.hex_seed_error);
                    builder.setPositiveButton(getString(R.string.ok), null);
                    builder.show();
                    return;
                }
            } else {
                String newSeed = "";
                for (int i = 0; i < seed.length(); i++) {
                    newSeed += Integer.toHexString((int) seed.charAt(i));
                }

                seed = newSeed;
            }

            if (checkIfInDatabase(name)) {
                builder.setTitle(getString(R.string.error_title));
                builder.setMessage(getString(R.string.error));
                builder.setPositiveButton(getString(R.string.ok), null);
                builder.show();
                return;
            }

            if (!editing) {
                insertIntoDatabaseAndFinish(name, seed, Home.OTP_TYPE_TOTP, digits, "", timeInterval);
            } else {
                updateIntoDatabaseAndFinish(name, seed, Home.OTP_TYPE_TOTP, digits, "", timeInterval);
            }
        }

    };

    private static String positionToZone(int zone) {
        switch (zone) {
            case 0:
                return "GMT-12:00";
            case 1:
                return "GMT-11:00";
            case 2:
                return "GMT-10:00";
            case 3:
                return "GMT-09:30";
            case 4:
                return "GMT-09:00";
            case 5:
                return "GMT-08:00";
            case 6:
                return "GMT-07:00";
            case 7:
                return "GMT-06:00";
            case 8:
                return "GMT-05:00";
            case 9:
                return "GMT-04:30";
            case 10:
                return "GMT-04:00";
            case 11:
                return "GMT-03:30";
            case 12:
                return "GMT-03:00";
            case 13:
                return "GMT-02:00";
            case 14:
                return "GMT-01:00";
            case 15:
                return "GMT";
            case 16:
                return "GMT+01:00";
            case 17:
                return "GMT+02:00";
            case 18:
                return "GMT+03:00";
            case 19:
                return "GMT+03:30";
            case 20:
                return "GMT+04:00";
            case 21:
                return "GMT+04:30";
            case 22:
                return "GMT+05:00";
            case 23:
                return "GMT+05:30";
            case 24:
                return "GMT+05:45";
            case 25:
                return "GMT+06:00";
            case 26:
                return "GMT+06:30";
            case 27:
                return "GMT+07:00";
            case 28:
                return "GMT+08:00";
            case 29:
                return "GMT+08:45";
            case 30:
                return "GMT+09:00";
            case 31:
                return "GMT+09:30";
            case 32:
                return "GMT+10:00";
            case 33:
                return "GMT+10:30";
            case 34:
                return "GMT+11:00";
            case 35:
                return "GMT+11:30";
            case 36:
                return "GMT+12:00";
            case 37:
                return "GMT+12:45";
            case 38:
                return "GMT+13:00";
            case 39:
                return "GMT+14:00";
            default:
                return "GMT";
        }
    }

    private static int zoneToPosition(String zone) {
        if ("GMT-12:00".compareTo(zone) == 0) {
            return 0;
        } else if ("GMT-11:00".compareTo(zone) == 0) {
            return 1;
        } else if ("GMT-10:00".compareTo(zone) == 0) {
            return 2;
        } else if ("GMT-09:30".compareTo(zone) == 0) {
            return 3;
        } else if ("GMT-09:00".compareTo(zone) == 0) {
            return 4;
        } else if ("GMT-08:00".compareTo(zone) == 0) {
            return 5;
        } else if ("GMT-07:00".compareTo(zone) == 0) {
            return 6;
        } else if ("GMT-06:00".compareTo(zone) == 0) {
            return 7;
        } else if ("GMT-05:00".compareTo(zone) == 0) {
            return 8;
        } else if ("GMT-04:30".compareTo(zone) == 0) {
            return 9;
        } else if ("GMT-04:00".compareTo(zone) == 0) {
            return 10;
        } else if ("GMT-03:30".compareTo(zone) == 0) {
            return 11;
        } else if ("GMT-03:00".compareTo(zone) == 0) {
            return 12;
        } else if ("GMT-02:00".compareTo(zone) == 0) {
            return 13;
        } else if ("GMT-01:00".compareTo(zone) == 0) {
            return 14;
        } else if ("GMT".compareTo(zone) == 0) {
            return 15;
        } else if ("GMT+01:00".compareTo(zone) == 0) {
            return 16;
        } else if ("GMT+02:00".compareTo(zone) == 0) {
            return 17;
        } else if ("GMT+03:00".compareTo(zone) == 0) {
            return 18;
        } else if ("GMT+03:30".compareTo(zone) == 0) {
            return 19;
        } else if ("GMT+04:00".compareTo(zone) == 0) {
            return 20;
        } else if ("GMT+04:30".compareTo(zone) == 0) {
            return 21;
        } else if ("GMT+05:00".compareTo(zone) == 0) {
            return 22;
        } else if ("GMT+05:30".compareTo(zone) == 0) {
            return 23;
        } else if ("GMT+05:45".compareTo(zone) == 0) {
            return 24;
        } else if ("GMT+06:00".compareTo(zone) == 0) {
            return 25;
        } else if ("GMT+06:30".compareTo(zone) == 0) {
            return 26;
        } else if ("GMT+07:00".compareTo(zone) == 0) {
            return 27;
        } else if ("GMT+08:00".compareTo(zone) == 0) {
            return 28;
        } else if ("GMT+08:45".compareTo(zone) == 0) {
            return 29;
        } else if ("GMT+09:00".compareTo(zone) == 0) {
            return 30;
        } else if ("GMT+09:30".compareTo(zone) == 0) {
            return 31;
        } else if ("GMT+10:00".compareTo(zone) == 0) {
            return 32;
        } else if ("GMT+10:30".compareTo(zone) == 0) {
            return 33;
        } else if ("GMT+11:00".compareTo(zone) == 0) {
            return 34;
        } else if ("GMT+11:30".compareTo(zone) == 0) {
            return 35;
        } else if ("GMT+12:00".compareTo(zone) == 0) {
            return 36;
        } else if ("GMT+12:45".compareTo(zone) == 0) {
            return 37;
        } else if ("GMT+13:00".compareTo(zone) == 0) {
            return 38;
        } else if ("GMT+14:00".compareTo(zone) == 0) {
            return 39;
        } else {
            return 15;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DBAdapter(this);
        builder = new Builder(this);
        Bundle extras = getIntent().getExtras();
        editing = extras.getBoolean(Profiles.EDITING_KEY);
        if (editing) {
            editingRowID = extras.getInt(DBAdapter.KEY_ROW_ID);
            String name = extras.getString(DBAdapter.KEY_PROF_NAME);
            String seed = extras.getString(DBAdapter.KEY_SEED);
            int otpType = extras.getInt(DBAdapter.KEY_OTP_TYPE);
            int digits = extras.getInt(DBAdapter.KEY_DIGITS);
            String zone = extras.getString(DBAdapter.KEY_TIME_ZONE);
            int timeInterval = extras.getInt(DBAdapter.KEY_TIME_INTERVAL, 30);
            if (otpType == Home.OTP_TYPE_MOTP) {
                setContentView(R.layout.motp_setup);
                setTitle(getString(R.string.app_name) + " - mOTP");
                EditText profNameEditText = (EditText) findViewById(R.id.motpProfileEditText);
                EditText seedEditText = (EditText) findViewById(R.id.motpSeedEditText);
                Spinner spinner = (Spinner) findViewById(R.id.motpTimeZoneSpinner);
                Button saveButton = (Button) findViewById(R.id.motpSaveButton);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TimeZones, R.layout.spinner_layout);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setSelection(15);
                profNameEditText.setText(name);
                seedEditText.setText(seed);
                spinner.setSelection(zoneToPosition(zone));
                saveButton.setOnClickListener(motpSaveButtonListener);
                saveButton.setText(R.string.save);
            } else if (otpType == Home.OTP_TYPE_HOTP) {
                setContentView(R.layout.hotp_setup);
                setTitle(getString(R.string.app_name) + " - HOTP");
                EditText profNameEditText = (EditText) findViewById(R.id.hotpProfileEditText);
                EditText seedEditText = (EditText) findViewById(R.id.hotpSeedEditText);
                EditText digitEditText = (EditText) findViewById(R.id.hotpOutputSizeEditText);
                Button saveButton = (Button) findViewById(R.id.hotpSaveButton);
                profNameEditText.setText(name);
                seedEditText.setText(seed);
                Spinner seedTypeSpinner = (Spinner) findViewById(R.id.hotpSeedTypeSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.SeedTypes, R.layout.spinner_layout);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                seedTypeSpinner.setAdapter(adapter);
                digitEditText.setText(String.valueOf(digits));
                saveButton.setOnClickListener(hotpSaveButtonListener);
                saveButton.setText(R.string.save);
            } else {
                // TOTP
                setContentView(R.layout.totp_setup);
                setTitle(getString(R.string.app_name) + " - TOTP");
                EditText profNameEditText = (EditText) findViewById(R.id.totpProfileEditText);
                EditText seedEditText = (EditText) findViewById(R.id.totpSeedEditText);
                EditText digitEditText = (EditText) findViewById(R.id.totpOutputSizeEditText);
                Button saveButton = (Button) findViewById(R.id.totpSaveButton);
                profNameEditText.setText(name);
                seedEditText.setText(seed);
                Spinner seedTypeSpinner = (Spinner) findViewById(R.id.totpSeedTypeSpinner);
                Spinner timeIntervalSpinner = (Spinner) findViewById(R.id.totpTimeIntervalSpinner);
                ArrayAdapter<CharSequence> seedTypesAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.SeedTypes, R.layout.spinner_layout);
                seedTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                seedTypeSpinner.setAdapter(seedTypesAdapter);
                ArrayAdapter<CharSequence> timeIntervalAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TimeIntervals, R.layout.spinner_layout);
                timeIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeIntervalSpinner.setAdapter(timeIntervalAdapter);
                timeIntervalSpinner.setSelection(timeInterval == 30 ? 0 : 1);
                digitEditText.setText(String.valueOf(digits));
                saveButton.setOnClickListener(totpSaveButtonListener);
                saveButton.setText(R.string.save);
            }
        } else {
            int type = extras.getInt(DBAdapter.KEY_OTP_TYPE);
            if (type == Home.OTP_TYPE_MOTP) {
                setContentView(R.layout.motp_setup);
                setTitle(getString(R.string.app_name) + " - mOTP");
                Button saveButton = (Button) findViewById(R.id.motpSaveButton);
                saveButton.setOnClickListener(motpSaveButtonListener);
                Spinner timeZoneSpinner = (Spinner) findViewById(R.id.motpTimeZoneSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TimeZones, R.layout.spinner_layout);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeZoneSpinner.setAdapter(adapter);
                timeZoneSpinner.setSelection(15);
            } else if (type == Home.OTP_TYPE_HOTP) {
                setContentView(R.layout.hotp_setup);
                setTitle(getString(R.string.app_name) + " - HOTP");
                Spinner seedTypeSpinner = (Spinner) findViewById(R.id.hotpSeedTypeSpinner);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.SeedTypes, R.layout.spinner_layout);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                seedTypeSpinner.setAdapter(adapter);
                seedTypeSpinner.setSelection(0);
                Button saveButton = (Button) findViewById(R.id.hotpSaveButton);
                saveButton.setOnClickListener(hotpSaveButtonListener);
            } else {
                // TOTP
                setContentView(R.layout.totp_setup);
                setTitle(getString(R.string.app_name) + " - TOTP");
                Spinner seedTypeSpinner = (Spinner) findViewById(R.id.totpSeedTypeSpinner);

                ArrayAdapter<CharSequence> seedTypeAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.SeedTypes, R.layout.spinner_layout);
                seedTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                seedTypeSpinner.setAdapter(seedTypeAdapter);
                seedTypeSpinner.setSelection(0);
                Spinner timeIntervalSpinner = (Spinner) findViewById(R.id.totpTimeIntervalSpinner);
                ArrayAdapter<CharSequence> timeIntervalAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.TimeIntervals, R.layout.spinner_layout);
                timeIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeIntervalSpinner.setAdapter(timeIntervalAdapter);
                timeIntervalSpinner.setSelection(0);
                Button saveButton = (Button) findViewById(R.id.totpSaveButton);
                saveButton.setOnClickListener(totpSaveButtonListener);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private boolean checkIfInDatabase(String name) {
        db.open();
        Cursor c = db.getAllProfiles();
        int count = c.getCount();
        if (count != 0 && !editing) {
            c.moveToFirst();
            int profileNameIndex = c.getColumnIndex(DBAdapter.KEY_PROF_NAME);
            while (!c.isAfterLast()) {
                if (c.getString(profileNameIndex).equals(name)) {
                    c.close();
                    db.close();
                    return true;
                }
                c.moveToNext();
            }
        }

        c.close();
        db.close();
        return false;
    }

    private void insertIntoDatabaseAndFinish(String name, String seed,
                                             int otpType, int digits, String time_zone,
                                             int time_interval) {
        db.open();
        int rowId = (int) db.insertProfile(name, seed, otpType, digits, time_zone, time_interval);
        db.close();
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(DBAdapter.KEY_PROF_NAME, name);
        ed.putString(DBAdapter.KEY_SEED, seed);
        ed.putInt(DBAdapter.KEY_OTP_TYPE, otpType);
        ed.putInt(DBAdapter.KEY_COUNT, 0);
        ed.putInt(DBAdapter.KEY_DIGITS, digits);
        ed.putString(DBAdapter.KEY_TIME_ZONE, time_zone);
        ed.putInt(DBAdapter.KEY_ROW_ID, rowId);
        ed.putInt(DBAdapter.KEY_TIME_INTERVAL, time_interval);
        ed.apply();
        finish();
    }

    private void updateIntoDatabaseAndFinish(String name, String seed, int otpType, int digits, String time_zone, int time_interval) {
        db.open();
        Cursor current = db.getProfile(editingRowID);
        int count = current.getInt(current.getColumnIndex(DBAdapter.KEY_COUNT));
        db.deleteProfile(editingRowID);
        int rowId = (int) db.insertProfile(name, seed, otpType, digits, time_zone, time_interval);
        db.updateCount(rowId, count);
        current.close();
        db.close();
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(DBAdapter.KEY_PROF_NAME, name);
        ed.putString(DBAdapter.KEY_SEED, seed);
        ed.putInt(DBAdapter.KEY_OTP_TYPE, otpType);
        ed.putInt(DBAdapter.KEY_COUNT, count);
        ed.putInt(DBAdapter.KEY_DIGITS, digits);
        ed.putString(DBAdapter.KEY_TIME_ZONE, time_zone);
        ed.putInt(DBAdapter.KEY_ROW_ID, rowId);
        ed.putInt(DBAdapter.KEY_TIME_INTERVAL, time_interval);
        ed.apply();
        finish();
    }
}