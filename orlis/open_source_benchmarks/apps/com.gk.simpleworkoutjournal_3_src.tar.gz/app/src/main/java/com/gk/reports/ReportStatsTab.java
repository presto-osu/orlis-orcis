package com.gk.reports;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gk.simpleworkoutjournal.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ReportStatsTab extends Fragment {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportStatsTab :: onCreateView()");

        if ( getActivity() == null ) {
            Log.e(APP_NAME, "ReportStatsTab :: onCreateView() failed since no atcivity is attached");
            throw new IllegalStateException("fragment is not attached to any activity");
        }

        View rootView = inflater.inflate(R.layout.fragment_ex_stats, container, false);

        Bundle exBundle = getArguments();

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##", otherSymbols);

        ((TextView) rootView.findViewById(R.id.exercise_name_in_report)).setText(exBundle.getString("exName"));

        ((TextView)rootView.findViewById( R.id.oneRepMaxWeightNum )).setText( String.valueOf( df.format( exBundle.getDouble("wOneRepMax")) ) );
        ((TextView)rootView.findViewById( R.id.oneRepAvgWeightNum )).setText( String.valueOf( df.format(exBundle.getDouble("wOneRepAvg")) ) );

        ((TextView)rootView.findViewById( R.id.oneSetMaxWeightNum )).setText( String.valueOf( df.format(exBundle.getDouble("wOneSetMax")) ) );
        ((TextView)rootView.findViewById( R.id.oneSetAvgWeightNum )).setText( String.valueOf( df.format(exBundle.getDouble("wOneSetAvg")) ) );

        ((TextView)rootView.findViewById( R.id.oneDayMaxWeightNum )).setText( String.valueOf( df.format(exBundle.getDouble("wOneDayMax")) ) );
        ((TextView)rootView.findViewById( R.id.oneDayAvgWeightNum )).setText( String.valueOf( df.format(exBundle.getDouble("wOneDayAvg")) ) );

        ((TextView)rootView.findViewById( R.id.allTimeMaxWeightNum )).setText( String.valueOf( df.format(exBundle.getDouble("wTotal")) ) );

        return rootView;
    }

}
