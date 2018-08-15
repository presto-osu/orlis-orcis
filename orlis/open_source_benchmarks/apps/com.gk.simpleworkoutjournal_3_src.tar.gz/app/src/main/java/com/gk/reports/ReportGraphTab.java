package com.gk.reports;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gk.datacontrol.DataPointParcel;
import com.gk.simpleworkoutjournal.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.ArrayList;

public class ReportGraphTab extends Fragment {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;

    public enum PointType {
        NONE(-1), MIN(0), MAX(1), AVG(2), SUM(3);


        PointType(int val) {}

        public static PointType fromInteger(int x) {
            switch(x) {
                case -1:
                    return NONE;
                case 0:
                    return MIN;
                case 1:
                    return MAX;
                case 2:
                    return AVG;
                case 3:
                    return SUM;
            }
            return null;
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: onCreateView()");

        if ( getActivity() == null ) {
            Log.e(APP_NAME, "ReportGraphTab :: onCreateView() failed since no atcivity is attached");
            throw new IllegalStateException("fragment is not attached to any activity");
        }

        View rootView = inflater.inflate(R.layout.fragment_ex_graph, container, false);

        //get passed data
        Bundle exBundle = getArguments();
        String exName = exBundle.getString("exName");

        ((TextView)rootView.findViewById( R.id.exercise_name_in_report )).setText(exName);

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ReportGraphTab :: first ex : "+exName);

        DataPointParcel parceledPoints;
        ArrayList<DataPoint> points;
        LineGraphSeries<DataPoint> series;

        //draw graph
        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        String[] parPoints = {"wPoints","rPoints"};
        String pointType;
        for ( String parPoint : parPoints )
        {
            pointType = parPoint.equals("wPoints")  ? "weightType" : "repsType";
            if ( PointType.fromInteger( exBundle.getInt( pointType  ) ) == PointType.NONE )
            {
                continue;
            }

            parceledPoints = exBundle.getParcelable( parPoint );
            points = parceledPoints.restoreData();
            if ( points.size() <= 1 )
            {
                if ( DEBUG_FLAG ) Log.d( APP_NAME, "ReportGraphTab :: not enough data points to draw.");
                (rootView.findViewById( R.id.noDataView )).setVisibility( View.VISIBLE );
                return rootView;
            }
            else
            {
                (rootView.findViewById( R.id.noDataView )).setVisibility( View.GONE );
            }

            series = new LineGraphSeries<DataPoint>( points.toArray( new DataPoint[ points.size() ] ) );
            series.setDataPointsRadius(4);
            series.setDrawDataPoints(true);

            if ( parPoint.equals("wPoints") ) {
                series.setColor(getResources().getColor(R.color.baseColor_complementary));
            } else {
                series.setColor(getResources().getColor(R.color.baseColor));
            }

            String legendTitle = (  parPoint.equals("wPoints") ) ? getString(R.string.weights) : getString(R.string.reps);
            graph.addSeries( series );
            series.setTitle( legendTitle );
        }

        graph.getViewport().setYAxisBoundsManual(true);
        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), DateFormat.getDateInstance(DateFormat.SHORT)));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        graph.getGridLabelRenderer().setNumVerticalLabels(7);

        graph.getViewport().setMaxY( exBundle.getDouble("extremum") + 5);
        graph.getViewport().setMinY(0);

        // legend
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graph.getLegendRenderer().setMargin( 10 );
        graph.getLegendRenderer().setBackgroundColor( Color.argb( 150 , 187 , 231, 247 ) ); // base color - lightest

        return rootView;
    }

}
