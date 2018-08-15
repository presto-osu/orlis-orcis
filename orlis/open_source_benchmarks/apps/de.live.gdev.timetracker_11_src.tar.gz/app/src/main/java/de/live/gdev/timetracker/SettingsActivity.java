package de.live.gdev.timetracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {
    static final int ACTIVITY_ID = 10;

    static class RESULT {
        public static final int NOCHANGE = -1;
        public static final int CHANGED = 1;
    }

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.radio_webapp_selector)
    RadioGroup profileSelector;

    @Bind(R.id.edit_webapp_path)
    EditText editPath;

    @Bind(R.id.edit_webapp_filename)
    EditText editFilename;

    @Bind(R.id.edit_webapp_username)
    EditText editUsername;

    @Bind(R.id.edit_webapp_password)
    EditText editPassword;

    @Bind(R.id.check_webapp_accept_all_ssl)
    CheckBox checkAcceptAllSsl;

    @Bind(R.id.check_webapp_auto_login)
    CheckBox checkAutoLogin;

    Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile = Profile.getDefaultProfile(this);
        if (profile.getId() < profileSelector.getChildCount())
            ((RadioButton) profileSelector.getChildAt(profile.getId())).setChecked(true);
        loadSettings();
        profileSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int profileNr = group.indexOfChild(findViewById(checkedId));
                profile = new Profile(getApplicationContext(), profileNr);
                loadSettings();
            }
        });
    }

    @OnClick({R.id.action_done, R.id.action_discard})
    public void onFloatingActionClicked(View v) {
        switch (v.getId()) {
            case R.id.action_done:
                applySettings();
                profile.saveSettings();
                Profile.setDefaultProfile(this, profile);
                setResult(RESULT.CHANGED);
                finish();
                break;
            case R.id.action_discard:
                setResult(RESULT.NOCHANGE);
                finish();
                break;
        }
    }

    public void loadSettings() {
        editFilename.setText(profile.getFilename());
        editPassword.setText(profile.getPassword());
        editUsername.setText(profile.getUsername());
        editPath.setText(profile.getPath());
        checkAcceptAllSsl.setChecked(profile.isAcceptAllSsl());
        checkAutoLogin.setChecked(profile.isAutoLogin());
    }

    public void applySettings() {
        profile.setFilename(editFilename.getText().toString());
        profile.setPassword(editPassword.getText().toString());
        profile.setUsername(editUsername.getText().toString());
        profile.setPath(editPath.getText().toString());
        profile.setAcceptAllSsl(checkAcceptAllSsl.isChecked());
        profile.setAutoLogin(checkAutoLogin.isChecked());
    }
}
