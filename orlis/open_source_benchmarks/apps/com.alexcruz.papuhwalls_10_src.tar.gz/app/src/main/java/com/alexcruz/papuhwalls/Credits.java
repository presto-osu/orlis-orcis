package com.alexcruz.papuhwalls;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Credits extends Fragment {

    private Context context;
    Preferences Preferences;

    public static Fragment newInstance(Context context) {
        Credits f = new Credits();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.credits, null);
        this.Preferences = new Preferences(getContext());

        context = getActivity();

        ActionBar toolbar = ((ActionBarActivity)context).getSupportActionBar();
        toolbar.setTitle(R.string.section_aboutapp);

        //Credits
        TextView credit_title = (TextView) root.findViewById(R.id.credit_title);
        credit_title.setTextColor(Preferences.PrimaryText());

        TextView credit_desc = (TextView) root.findViewById(R.id.credit_desc);
        credit_desc.setText(Html.fromHtml(getString(R.string.credit_desc)));
        credit_desc.setTextColor(Preferences.SecondaryText());

        //Free and open source text
        TextView freeandopensource_text = (TextView) root.findViewById(R.id.freeandopensource_text);
        freeandopensource_text.setTextColor(Preferences.PrimaryText());

        //Library number two
        TextView libtwo_title = (TextView) root.findViewById(R.id.libtwo_title);
        libtwo_title.setTextColor(Preferences.PrimaryText());

        TextView libtwo_content = (TextView) root.findViewById(R.id.libtwo_content);
        libtwo_content.setText(Html.fromHtml(getString(R.string.fab_desc)));
        libtwo_content.setTextColor(Preferences.SecondaryText());

        TextView libtwo_license = (TextView) root.findViewById(R.id.libtwo_license);
        libtwo_license.setTextColor(Preferences.PrimaryText());

        TextView libtwo_author = (TextView) root.findViewById(R.id.libtwo_author);
        libtwo_author.setTextColor(Preferences.SecondaryText());

        //Library number three
        TextView libthree_title = (TextView) root.findViewById(R.id.libthree_title);
        libthree_title.setTextColor(Preferences.PrimaryText());

        TextView libthree_content = (TextView) root.findViewById(R.id.libthree_content);
        libthree_content.setText(Html.fromHtml(getString(R.string.materialdialogs_desc)));
        libthree_content.setTextColor(Preferences.SecondaryText());

        TextView libthree_license = (TextView) root.findViewById(R.id.libthree_license);
        libthree_license.setTextColor(Preferences.PrimaryText());

        TextView libthree_author = (TextView) root.findViewById(R.id.libthree_author);
        libthree_author.setTextColor(Preferences.SecondaryText());

        //Library number four
        TextView libfour_title = (TextView) root.findViewById(R.id.libfour_title);
        libfour_title.setTextColor(Preferences.PrimaryText());

        TextView libfour_content = (TextView) root.findViewById(R.id.libfour_content);
        libfour_content.setText(Html.fromHtml(getString(R.string.materialdrawer_desc)));
        libfour_content.setTextColor(Preferences.SecondaryText());

        TextView libfour_license = (TextView) root.findViewById(R.id.libfour_license);
        libfour_license.setTextColor(Preferences.PrimaryText());

        TextView libfour_author = (TextView) root.findViewById(R.id.libfour_author);
        libfour_author.setTextColor(Preferences.SecondaryText());

        //Library number five
        TextView libfive_title = (TextView) root.findViewById(R.id.libfive_title);
        libfive_title.setTextColor(Preferences.PrimaryText());

        TextView libfive_content = (TextView) root.findViewById(R.id.libfive_content);
        libfive_content.setText(Html.fromHtml(getString(R.string.picasso_desc)));
        libfive_content.setTextColor(Preferences.SecondaryText());

        TextView libfive_license = (TextView) root.findViewById(R.id.libfive_license);
        libfive_license.setTextColor(Preferences.PrimaryText());

        TextView libfive_author = (TextView) root.findViewById(R.id.libfive_author);
        libfive_author.setTextColor(Preferences.SecondaryText());

        //Library number six
        TextView libsix_title = (TextView) root.findViewById(R.id.libsix_title);
        libsix_title.setTextColor(Preferences.PrimaryText());

        TextView libsix_content = (TextView) root.findViewById(R.id.libsix_content);
        libsix_content.setText(Html.fromHtml(getString(R.string.okhttp_desc)));
        libsix_content.setTextColor(Preferences.SecondaryText());

        TextView libsix_license = (TextView) root.findViewById(R.id.libsix_license);
        libsix_license.setTextColor(Preferences.PrimaryText());

        TextView libsix_author = (TextView) root.findViewById(R.id.libsix_author);
        libsix_author.setTextColor(Preferences.SecondaryText());

        //Library number seven
        TextView libseven_title = (TextView) root.findViewById(R.id.libseven_title);
        libseven_title.setTextColor(Preferences.PrimaryText());

        TextView libseven_content = (TextView) root.findViewById(R.id.libseven_content);
        libseven_content.setText(Html.fromHtml(getString(R.string.snackbar_desc)));
        libseven_content.setTextColor(Preferences.SecondaryText());

        TextView libseven_license = (TextView) root.findViewById(R.id.libseven_license);
        libseven_license.setTextColor(Preferences.PrimaryText());

        TextView libseven_author = (TextView) root.findViewById(R.id.libseven_author);
        libseven_author.setTextColor(Preferences.SecondaryText());

        //Library number eight
        TextView libeight_title = (TextView) root.findViewById(R.id.libeight_title);
        libeight_title.setTextColor(Preferences.PrimaryText());

        TextView libeight_content = (TextView) root.findViewById(R.id.libeight_content);
        libeight_content.setText(Html.fromHtml(getString(R.string.crash_desc)));
        libeight_content.setTextColor(Preferences.SecondaryText());

        TextView libeight_license = (TextView) root.findViewById(R.id.libeight_license);
        libeight_license.setTextColor(Preferences.PrimaryText());

        TextView libeight_author = (TextView) root.findViewById(R.id.libeight_author);
        libeight_author.setTextColor(Preferences.SecondaryText());

        //Library number nine
        TextView libnine_title = (TextView) root.findViewById(R.id.libnine_title);
        libnine_title.setTextColor(Preferences.PrimaryText());

        TextView libnine_content = (TextView) root.findViewById(R.id.libnine_content);
        libnine_content.setText(Html.fromHtml(getString(R.string.appintro_desc)));
        libnine_content.setTextColor(Preferences.SecondaryText());

        TextView libnine_license = (TextView) root.findViewById(R.id.libnine_license);
        libnine_license.setTextColor(Preferences.PrimaryText());

        TextView libnine_author = (TextView) root.findViewById(R.id.libnine_author);
        libnine_author.setTextColor(Preferences.SecondaryText());

        //Library number ten
        TextView libten_title = (TextView) root.findViewById(R.id.libten_title);
        libten_title.setTextColor(Preferences.PrimaryText());

        TextView libten_content = (TextView) root.findViewById(R.id.libten_content);
        libten_content.setText(Html.fromHtml(getString(R.string.materialripple_desc)));
        libten_content.setTextColor(Preferences.SecondaryText());

        TextView libten_license = (TextView) root.findViewById(R.id.libten_license);
        libten_license.setTextColor(Preferences.PrimaryText());

        TextView libten_author = (TextView) root.findViewById(R.id.libten_author);
        libten_author.setTextColor(Preferences.SecondaryText());

        //Library number eleven
        TextView libeleven_title = (TextView) root.findViewById(R.id.libeleven_title);
        libeleven_title.setTextColor(Preferences.PrimaryText());

        TextView libeleven_content = (TextView) root.findViewById(R.id.libeleven_content);
        libeleven_content.setText(Html.fromHtml(getString(R.string.materialpreference_desc)));
        libeleven_content.setTextColor(Preferences.SecondaryText());

        TextView libeleven_license = (TextView) root.findViewById(R.id.libeleven_license);
        libeleven_license.setTextColor(Preferences.PrimaryText());

        TextView libeleven_author = (TextView) root.findViewById(R.id.libeleven_author);
        libeleven_author.setTextColor(Preferences.SecondaryText());

        //Library number twelve
        TextView libtwelve_title = (TextView) root.findViewById(R.id.libtwelve_title);
        libtwelve_title.setTextColor(Preferences.PrimaryText());

        TextView libtwelve_content = (TextView) root.findViewById(R.id.libtwelve_content);
        libtwelve_content.setText(Html.fromHtml(getString(R.string.licensesdialog_desc)));
        libtwelve_content.setTextColor(Preferences.SecondaryText());

        TextView libtwelve_license = (TextView) root.findViewById(R.id.libtwelve_license);
        libtwelve_license.setTextColor(Preferences.PrimaryText());

        TextView libtwelve_author = (TextView) root.findViewById(R.id.libtwelve_author);
        libtwelve_author.setTextColor(Preferences.SecondaryText());

        //Library source buttons
        TextView credit = (TextView) root.findViewById(R.id.github_button);
        credit.setTextColor(Preferences.Accent());
        credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent credit = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.jahir_link)));
                startActivity(credit);
            }
        });

        TextView fab_web = (TextView) root.findViewById(R.id.floating_github_button);
        fab_web.setTextColor(Preferences.Accent());
        fab_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fab_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.fab_web)));
                startActivity(fab_web);
            }
        });

        TextView materialdialogs_web = (TextView) root.findViewById(R.id.dialogs_github_button);
        materialdialogs_web.setTextColor(Preferences.Accent());
        materialdialogs_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent materialdialogs_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.materialdialogs_web)));
                startActivity(materialdialogs_web);
            }
        });

        TextView materialdrawer_web = (TextView) root.findViewById(R.id.materialdrawer_github_button);
        materialdrawer_web.setTextColor(Preferences.Accent());
        materialdrawer_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent materialdrawer_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.materialdrawer_web)));
                startActivity(materialdrawer_web);
            }
        });

        TextView picasso_web = (TextView) root.findViewById(R.id.picasso_github_button);
        picasso_web.setTextColor(Preferences.Accent());
        picasso_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent picasso_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.picasso_web)));
                startActivity(picasso_web);
            }
        });

        TextView okhttp_web = (TextView) root.findViewById(R.id.okhttp_github_button);
        okhttp_web.setTextColor(Preferences.Accent());
        okhttp_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent okhttp_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.okhttp_web)));
                startActivity(okhttp_web);
            }
        });

        TextView snackbar_web = (TextView) root.findViewById(R.id.snackbar_github_button);
        snackbar_web.setTextColor(Preferences.Accent());
        snackbar_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent snackbar_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.snackbar_web)));
                startActivity(snackbar_web);
            }
        });

        TextView crash_web = (TextView) root.findViewById(R.id.crash_github_button);
        crash_web.setTextColor(Preferences.Accent());
        crash_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent crash_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.crash_web)));
                startActivity(crash_web);
            }
        });

        TextView appintro_web = (TextView) root.findViewById(R.id.appintro_github_button);
        appintro_web.setTextColor(Preferences.Accent());
        appintro_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appintro_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.appintro_web)));
                startActivity(appintro_web);
            }
        });

        TextView materialripple_web = (TextView) root.findViewById(R.id.materialripple_github_button);
        materialripple_web.setTextColor(Preferences.Accent());
        materialripple_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent materialripple_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.materialripple_web)));
                startActivity(materialripple_web);
            }
        });

        TextView materialpreference_web = (TextView) root.findViewById(R.id.materialpreference_github_button);
        materialpreference_web.setTextColor(Preferences.Accent());
        materialpreference_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent materialpreference_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.materialpreference_web)));
                startActivity(materialpreference_web);
            }
        });

        TextView licensesdialog_web = (TextView) root.findViewById(R.id.licensesdialog_github_button);
        licensesdialog_web.setTextColor(Preferences.Accent());
        licensesdialog_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent licensesdialog_web = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.licensesdialog_web)));
                startActivity(licensesdialog_web);
            }
        });

        return root;
    }

}
