package info.staticfree.SuperGenPass;

/*
 Android SuperGenPass
 Copyright (C) 2009-2015  Steve Pomeroy <steve@staticfree.info>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import info.staticfree.SuperGenPass.hashes.DomainBasedHash;
import info.staticfree.SuperGenPass.hashes.HotpPin;
import info.staticfree.SuperGenPass.hashes.PasswordComposer;
import info.staticfree.SuperGenPass.hashes.SuperGenPass;

// TODO Wipe generated password from clipboard after delay.

// the check below is a nice reminder, however this activity uses no delayed messages,
// so nothing should be holding onto references past this activity's lifetime.
@SuppressLint("HandlerLeak")
public class Super_Gen_Pass extends Activity
        implements OnClickListener, OnLongClickListener, OnCheckedChangeListener,
        OnEditorActionListener, FilterQueryProvider {
    private static final String TAG = Super_Gen_Pass.class.getSimpleName();
    private static final int MINUTE_MS = 60000;

    private DomainBasedHash mDomainBasedHash;

    private static final int REQUEST_CODE_PREFERENCES = 200;
    private static final String STATE_LAST_STOPPED_TIME =
            "info.staticfree.SuperGenPass.STATE_LAST_STOPPED_TIME";
    private static final String STATE_SHOWING_PASSWORD =
            "info.staticfree.SuperGenPass.STATE_SHOWING_PASSWORD";
    private int mPwLength;
    @Nullable
    private String mPwSalt;
    private boolean mCopyToClipboard;
    private boolean mRememberDomains;
    private boolean mDomainCheck = true;

    private GeneratedPasswordView mGenPwView;
    private AutoCompleteTextView mDomainEdit;
    private VisualHashEditText mMasterPwEdit;

    private long mLastStoppedTime;
    private int mPwClearTimeout;

    private static final int MSG_UPDATE_PW_VIEW = 100;

    private static final int MIN_PIN_LENGTH = 3;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PW_VIEW:
                    generateIfValid();
                    break;
            }
        }
    };

    private CompoundButton mShowGenPassword;

    private GeneratedPasswordView mGenPinView;

    private Spinner mPinDigitsSpinner;

    private final BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            clearEditTexts();
            unregisterReceiver(this);
        }
    };

    private boolean mClearDomain = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        final Intent intent = getIntent();
        final Uri data = intent.getData();

        if (savedInstanceState != null) {
            mLastStoppedTime = savedInstanceState.getLong(STATE_LAST_STOPPED_TIME, 0);
            mShowingPassword = savedInstanceState.getBoolean(STATE_SHOWING_PASSWORD, false);
        }

        initPinWidgets();

        initDomainPasswordEntry();
        initGenPassword();
        bindTextWatchers();
        initMasterPasswordHide();

        loadFromPreferences();

        // sometimes the domain doesn't have focus when first started. Perhaps because of the tabs?
        mDomainEdit.requestFocus();

        // check for the "share page" intent. If present, pre-fill.

        if (data == null) {

            final String maybeUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (maybeUrl != null) {
                try {
                    // populate the URL and give the password entry focus
                    final Uri uri = Uri.parse(maybeUrl);

                    mDomainEdit.setText(mDomainBasedHash.getDomain(uri.getHost()));
                    mMasterPwEdit.requestFocus();
                    mClearDomain = false;
                } catch (@NonNull final PasswordGenerationException e) {
                    // nothing much to be done here.
                    // Let the user figure it out.
                    Log.e(TAG, "Could not find valid URI in shared text", e);
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initDomainPasswordEntry() {
        mDomainEdit = (AutoCompleteTextView) findViewById(R.id.domain_edit);

        mMasterPwEdit = (VisualHashEditText) findViewById(R.id.password_edit);

        mMasterPwEdit.setOnEditorActionListener(this);

        final SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line, null,
                        new String[] { "domain" }, new int[] { android.R.id.text1 },
                        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        adapter.setFilterQueryProvider(this);
        adapter.setStringConversionColumn(DOMAIN_COLUMN);

        // initialize the autocompletion
        mDomainEdit.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
                    final long arg3) {
                mMasterPwEdit.requestFocus();
            }
        });
        mDomainEdit.setAdapter(adapter);
    }

    private void initGenPassword() {

        mGenPwView = (GeneratedPasswordView) findViewById(R.id.password_output);
        mGenPwView.setOnLongClickListener(this);

        // hook in our buttons
        mShowGenPassword = (CompoundButton) findViewById(R.id.show_gen_password);
        mShowGenPassword.setOnCheckedChangeListener(this);
    }

    private void initPinWidgets() {
        mGenPinView = (GeneratedPasswordView) findViewById(R.id.pin_output);
        mGenPinView.setOnLongClickListener(this);

        mPinDigitsSpinner = (Spinner) findViewById(R.id.pin_length);
        mPinDigitsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                mPinDigits = position + MIN_PIN_LENGTH;

                final SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(Super_Gen_Pass.this);
                prefs.edit().putInt(Preferences.PREF_PIN_DIGITS, mPinDigits).apply();
                generateIfValid();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    private void initMasterPasswordHide() {
        final CompoundButton masterPasswordHide =
                (CompoundButton) findViewById(R.id.hide_master_password);
        masterPasswordHide.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                Log.d(TAG, "toggle master password hide");
                final int selStart = mMasterPwEdit.getSelectionStart();
                final int selEnd = mMasterPwEdit.getSelectionEnd();
                mMasterPwEdit
                        .setTransformationMethod(b ? null : new PasswordTransformationMethod());
                mMasterPwEdit.setSelection(selStart, selEnd);
            }
        });
    }

    @Override
    protected void onPause() {

        // listen for a screen off event and clear everything if received.
        if (mShowingPassword) {
            registerReceiver(mScreenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }

        super.onPause();

        // this is overly cautious to avoid memory leaks
        mHandler.removeMessages(MSG_UPDATE_PW_VIEW);

        mLastStoppedTime = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onResume() {
        super.onResume();

        findViewById(R.id.tab_pin).setVisibility(mShowPin ? View.VISIBLE : View.GONE);
        // when the user has left the app for more than mPwClearTimeout minutes,
        // wipe master password and generated password.
        if (mClearDomain &&
                SystemClock.elapsedRealtime() - mLastStoppedTime > mPwClearTimeout * MINUTE_MS) {
            clearEditTexts();
        }
        mClearDomain = true;
    }

    private void clearEditTexts() {
        mDomainEdit.getText().clear();
        mMasterPwEdit.getText().clear();
        clearGenPassword();
        mDomainEdit.requestFocus();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_LAST_STOPPED_TIME, mLastStoppedTime);
        outState.putBoolean(STATE_SHOWING_PASSWORD, mShowingPassword);
    }

    private boolean mShowingPassword;

    @Nullable
    private HotpPin mPinGen;

    private int mPinDigits;

    private boolean mShowPin;

    private void generateIfValid() {
        try {
            if (mMasterPwEdit.length() > 0 && mDomainEdit.length() > 0) {
                generateAndDisplay();
            } else {
                clearGenPassword();
            }
        } catch (@NonNull final PasswordGenerationException e) {

            clearGenPassword();
        }
    }

    private void clearGenPassword() {
        if (mShowingPassword) {
            mGenPwView.setText(null);
            mGenPinView.setText(null);
            mShowingPassword = false;
            invalidateOptionsMenu();
        }
    }

    @Nullable
    private String generateAndDisplay() throws PasswordGenerationException {
        String domain = getDomain();

        if (mDomainCheck) {
            domain = extractDomain(domain);
        }
        final String masterPw = getMasterPassword() + mPwSalt;
        final String genPw = mDomainBasedHash.generate(masterPw, domain, mPwLength);

        mGenPwView.setDomainName(domain);
        mGenPwView.setText(genPw);

        if (mPinGen != null && mShowPin) {
            final String pin = mPinGen.generate(masterPw, domain, mPinDigits);
            mGenPinView.setDomainName(domain);
            mGenPinView.setText(pin);
        }
        mShowingPassword = true;
        invalidateOptionsMenu();

        return genPw;
    }

    private void postGenerate(final boolean copyToClipboard) {

        if (mRememberDomains) {
            RememberedDomainProvider.addRememberedDomain(getContentResolver(), getDomain());
        }

        if (copyToClipboard) {
            mGenPwView.copyToClipboard();

            if (Intent.ACTION_SEND.equals(getIntent().getAction()) &&
                    mGenPwView.getHidePassword()) {
                finish();
            }
        }
    }

    /**
     * Go! Validates the forms, computes the password, displays it, remembers the domain, and copies
     * to clipboard.
     */
    boolean go() {
        try {
            if (mMasterPwEdit.length() == 0) {
                clearGenPassword();
                mMasterPwEdit.setError(getText(R.string.err_empty_master_password));
                mMasterPwEdit.requestFocus();
                return false;
            }

            generateAndDisplay();

            postGenerate(mCopyToClipboard);
            return true;
        } catch (@NonNull final IllegalDomainException e) {
            clearGenPassword();
            mDomainEdit.setError(e.getLocalizedMessage());
            mDomainEdit.requestFocus();
        } catch (@NonNull final PasswordGenerationException e) {
            clearGenPassword();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * @return the domain entered into the text box
     */
    @NonNull
    String getDomain() {
        final AutoCompleteTextView txt = (AutoCompleteTextView) findViewById(R.id.domain_edit);
        return txt.getText().toString().trim();
    }

    @NonNull
    String getMasterPassword() {
        return mMasterPwEdit.getText().toString();
    }

    /**
     * Returns the hostname portion of the supplied URL.
     *
     * @param maybeUrl : either a hostname or a URL.
     * @return the hostname portion of maybeUrl, or null if maybeUrl was null.
     */
    String extractDomain(@NonNull final String maybeUrl) {
        try {
            final Uri uri = Uri.parse(maybeUrl);
            final String host = uri.getHost();
            return mDomainBasedHash.getDomain(host != null ? uri.getHost() : "");
        } catch (@NonNull final PasswordGenerationException e) {
            return maybeUrl;
        }
    }

    @Override
    public void onClick(@NonNull final View v) {
        switch (v.getId()) {
            case R.id.go:
                go();
                break;
        }
    }

    @Override
    public boolean onLongClick(final View v) {
        return false;
    }

    @Override
    public void onCheckedChanged(@NonNull final CompoundButton buttonView,
            final boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.show_gen_password: {

                mGenPwView.setHidePassword(!isChecked);
                mGenPinView.setHidePassword(!isChecked);

                final SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(Super_Gen_Pass.this);
                prefs.edit().putBoolean(Preferences.PREF_SHOW_GEN_PW, isChecked).apply();
            }
        }
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        return !go();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull final Menu menu) {
        final MenuItem verify = menu.findItem(R.id.verify);
        verify.setEnabled(getMasterPassword().length() != 0);
        menu.findItem(R.id.copy).setEnabled(mShowingPassword);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:

                final Intent preferencesIntent =
                        new Intent().setClass(this, SgpPreferencesActivity.class);
                startActivityForResult(preferencesIntent, REQUEST_CODE_PREFERENCES);

                return true;

            case R.id.about:
                new AboutFragment().show(getFragmentManager(), "about");
                return true;

            case R.id.verify:
                VerifyFragment.showVerifyFragment(getFragmentManager(), getMasterPassword());
                return true;

            case R.id.go:
                go();
                return true;

            case R.id.copy:
                postGenerate(true);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PREFERENCES) {
            loadFromPreferences();
        }
    }

    private void bindTextWatchers() {
        mDomainEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(final Editable s) {
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                    final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                    final int count) {
                mHandler.sendEmptyMessage(MSG_UPDATE_PW_VIEW);
            }
        });

        mMasterPwEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                    final int count) {
                mHandler.sendEmptyMessage(MSG_UPDATE_PW_VIEW);
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                    final int after) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });
    }

    /**
     * Loads the preferences and updates the program state based on them.
     */
    protected void loadFromPreferences() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // when adding items here, make sure default values are in sync with the xml file
        final String pwType = prefs.getString(Preferences.PREF_PW_TYPE, SuperGenPass.TYPE);
        mPwLength = Preferences.getStringAsInteger(prefs, Preferences.PREF_PW_LENGTH, 10);
        mPwSalt = prefs.getString(Preferences.PREF_PW_SALT, "");
        mCopyToClipboard = prefs.getBoolean(Preferences.PREF_CLIPBOARD, true);
        mRememberDomains = prefs.getBoolean(Preferences.PREF_REMEMBER_DOMAINS, true);

        if (prefs.contains("domain_nocheck")) {
            // Double negatives are so confusing
            final boolean domainCheckDefault = !prefs.getBoolean("domain_nocheck", false);
            prefs.edit().remove("domain_nocheck")
                    .putBoolean(Preferences.PREF_DOMAIN_CHECK, domainCheckDefault).apply();
        }

        mDomainCheck = prefs.getBoolean(Preferences.PREF_DOMAIN_CHECK, true);
        mPwClearTimeout =
                Preferences.getStringAsInteger(prefs, Preferences.PREF_PW_CLEAR_TIMEOUT, 2);

        // PIN
        mPinDigits = prefs.getInt(Preferences.PREF_PIN_DIGITS, 4);
        mPinDigitsSpinner.setSelection(mPinDigits - MIN_PIN_LENGTH);
        mShowPin = prefs.getBoolean(Preferences.PREF_SHOW_PIN, true);

        try {
            switch (pwType) {
                case SuperGenPass.TYPE:
                    mDomainBasedHash = new SuperGenPass(this, SuperGenPass.HASH_ALGORITHM_MD5);

                    break;
                case SuperGenPass.TYPE_SHA_512:
                    mDomainBasedHash = new SuperGenPass(this, SuperGenPass.HASH_ALGORITHM_SHA512);

                    break;
                case PasswordComposer.TYPE:
                    mDomainBasedHash = new PasswordComposer(this);

                    break;
                default:
                    mDomainBasedHash = new SuperGenPass(this, SuperGenPass.HASH_ALGORITHM_MD5);
                    Log.e(TAG, "password type was set to unknown algorithm: " + pwType);
                    break;
            }

            mPinGen = new HotpPin(this);
        } catch (@NonNull final NoSuchAlgorithmException e) {
            Log.e(TAG, "could not find MD5", e);
            Toast.makeText(getApplicationContext(),
                    String.format(getString(R.string.err_no_md5), e.getLocalizedMessage()),
                    Toast.LENGTH_LONG).show();
            finish();
        } catch (@NonNull final IOException e) {
            Toast.makeText(this, getString(R.string.err_json_load, e.getLocalizedMessage()),
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, getString(R.string.err_json_load), e);
            finish();
        }

        mDomainBasedHash.setCheckDomain(mDomainCheck);
        if (mPinGen != null) {
            mPinGen.setCheckDomain(mDomainCheck);
        }

        if (mDomainCheck) {
            mDomainEdit.setHint(R.string.domain_hint);
        } else {
            mDomainEdit.setHint(R.string.domain_hint_no_checking);
        }

        mMasterPwEdit.setShowVisualHash(prefs.getBoolean(Preferences.PREF_VISUAL_HASH, true));

        if (mCopyToClipboard) {
            mMasterPwEdit.setImeActionLabel(getText(android.R.string.copy), R.id.go);
        } else {
            mMasterPwEdit.setImeActionLabel(getText(R.string.done), R.id.go);
        }

        final boolean showPassword = prefs.getBoolean(Preferences.PREF_SHOW_GEN_PW, false);

        mGenPwView.setHidePassword(!showPassword);
        mGenPinView.setHidePassword(!showPassword);
        mShowGenPassword.setChecked(showPassword);
    }

    private static final String[] PROJECTION = { Domain.DOMAIN, Domain._ID };
    private static final int DOMAIN_COLUMN = 0;

    // a filter that searches for domains starting with the given constraint
    @Nullable
    @Override
    public Cursor runQuery(@Nullable final CharSequence constraint) {
        final Cursor c;
        if (constraint == null || constraint.length() == 0) {
            c = getContentResolver()
                    .query(Domain.CONTENT_URI, PROJECTION, null, null, Domain.SORT_ORDER);
        } else {
            c = getContentResolver()
                    .query(Domain.CONTENT_URI, PROJECTION, Domain.DOMAIN + " GLOB ?",
                            new String[] { constraint + "*" }, Domain.SORT_ORDER);
        }

        return c;
    }

    /**
     * A Dialog that verifies that the master password was typed correctly.
     */
    public static class VerifyFragment extends DialogFragment {
        private static final String ARG_PASSWORD = "password";
        @NonNull
        private String mPasswordToCheck = "";

        /**
         * Shows the password verification dialog
         *
         * @param fragmentManager Activity's fragment manager
         * @param passwordToVerify the password that must be entered to dismiss the dialog
         */
        public static void showVerifyFragment(@NonNull final FragmentManager fragmentManager,
                @NonNull final String passwordToVerify) {
            final VerifyFragment vf = new VerifyFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_PASSWORD, passwordToVerify);
            vf.setArguments(args);
            vf.show(fragmentManager, VerifyFragment.class.getSimpleName());
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mPasswordToCheck = getArguments().getString(ARG_PASSWORD, "");
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_verify_title);
            builder.setCancelable(true);
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View pwVerifyLayout =
                    inflater.inflate(R.layout.master_pw_verify, (ViewGroup) getView());
            final EditText pwVerify = (EditText) pwVerifyLayout.findViewById(R.id.verify);

            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                final int which) {
                            dialog.cancel();
                        }
                    });

            pwVerify.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before,
                        final int count) {
                }

                @Override
                public void beforeTextChanged(final CharSequence s, final int start,
                        final int count, final int after) {
                }

                @Override
                public void afterTextChanged(@NonNull final Editable s) {
                    if (mPasswordToCheck.length() > 0 && mPasswordToCheck.equals(s.toString())) {
                        getDialog().dismiss();
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.toast_verify_success, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setView(pwVerifyLayout);
            final Dialog d = builder.create();
            // This is added below to ensure that the soft input doesn't get hidden if it's
            // showing, which seems to be the default for dialogs.
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);

            return d;
        }
    }

    /**
     * The about dialog
     */
    public static class AboutFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.about_title);
            builder.setIcon(R.drawable.icon);

            // using this instead of setMessage lets us have clickable links.
            final LayoutInflater factory = LayoutInflater.from(getActivity());
            builder.setView(factory.inflate(R.layout.about, (ViewGroup) getView()));

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    getDialog().dismiss();
                }
            });
            return builder.create();
        }
    }
}
