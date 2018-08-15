package de.baumann.sieben.helper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import de.baumann.sieben.R;

public class UserSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_settings);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.action_settings);

        getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new SettingsFragment())
                            .commit();
    }

    @Override
    public void onBackPressed() {
        if( !getFragmentManager().popBackStackImmediate() ) super.onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private void addChangelogListener() {
            Preference reset = findPreference("changelog");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference pref)
                {

                    final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.changelog_text)));
                    Linkify.addLinks(s, Linkify.WEB_URLS);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.action_changelog)
                            .setMessage( s )
                            .setPositiveButton(getString(R.string.about_yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                    d.show();
                    ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                    return true;
                }
            });
        }

        private void addHelpListener() {
            Preference reset = findPreference("help");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference pref)
                {

                    final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.help_text)));
                    Linkify.addLinks(s, Linkify.WEB_URLS);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setMessage(s)
                            .setPositiveButton(getString(R.string.about_yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                    d.show();
                    ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                    return true;
                }
            });
        }

        private void addLicenseListener() {
            Preference reset = findPreference("license");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                public boolean onPreferenceClick(Preference pref)
                {

                    final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.about_text)));
                    Linkify.addLinks(s, Linkify.WEB_URLS);

                    final AlertDialog d = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.about_title)
                            .setMessage(s)
                            .setPositiveButton(getString(R.string.about_yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                    d.show();
                    ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                    return true;
                }
            });
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.user_settings);
            addHelpListener();
            addLicenseListener();
            addChangelogListener();
        }
    }
}
