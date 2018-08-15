package com.alexcruz.papuhwalls;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BaseActivity extends Fragment {

    Preferences Preferences;

    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=com.alexcruz.papuhwalls";

    private String GooglePlusCommunity;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.home, container, false);
        this.Preferences = new Preferences(getContext());

        GooglePlusCommunity = getResources().getString(R.string.googleplus_link);

        TextView welcometext = (TextView) root.findViewById(R.id.welcometext);
        welcometext.setTextColor(Preferences.PrimaryText());

        TextView welcometextsummary = (TextView) root.findViewById(R.id.welcometext_summary);
        welcometextsummary.setTextColor(Preferences.SecondaryText());

        TextView joincommunitytext = (TextView) root.findViewById(R.id.join_community_text);
        joincommunitytext.setTextColor(Preferences.PrimaryText());

        TextView joincommunitytextsummary = (TextView) root.findViewById(R.id.join_community_text_summary);
        joincommunitytextsummary.setTextColor(Preferences.SecondaryText());

        TextView ratebtn = (TextView) root.findViewById(R.id.rate_button);
        ratebtn.setTextColor(Preferences.Accent());
        ratebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL));
                startActivity(rate);
            }
        });

        TextView playbtn = (TextView) root.findViewById(R.id.googleplus_button);
        playbtn.setTextColor(Preferences.Accent());
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent devPlay = new Intent(Intent.ACTION_VIEW, Uri.parse(GooglePlusCommunity));
                startActivity(devPlay);
            }
        });

        return root;
    }
}