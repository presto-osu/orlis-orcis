package org.pixmob.freemobile.netstat.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pixmob.freemobile.netstat.R;
import org.pixmob.freemobile.netstat.content.NetstatContract.Events;
import org.pixmob.freemobile.netstat.content.Statistics;
import org.pixmob.freemobile.netstat.content.StatisticsLoader;

import static org.pixmob.freemobile.netstat.BuildConfig.DEBUG;
import static org.pixmob.freemobile.netstat.Constants.TAG;

/**
 * Application chartFragment.
 * @author gilbsgilbs
 */
public class MobileNetworkChartFragment extends Fragment implements LoaderCallbacks<Statistics> {

    private ContentObserver contentMonitor;
    private View statisticsWrapperLayout;
    private ProgressBar progressBar;
    private MobileNetworkChart mobileNetworkChart;
    private TextView onOrangeNetworkTextView;
    private TextView onOrange2GnetworkTextView;
    private TextView onOrange3GnetworkTextView;
    private TextView onFreeMobileNetworkTextView;
    private TextView onFreeMobile3GnetworkTextView;
    private TextView onFreeMobileFemtocellTextView;
    private TextView onFreeMobile4GnetworkTextView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Monitor database updates: when new data is available, this fragment
        // is updated with the new values.
        contentMonitor = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                Log.i(TAG, "Content updated: refresh statistics");
                refresh();
            }
        };

        // Get widgets.
        final Activity a = getActivity();
        statisticsWrapperLayout = a.findViewById(R.id.statistics_wrapper_layout);
        progressBar = (ProgressBar) a.findViewById(R.id.states_progress);
        mobileNetworkChart = (MobileNetworkChart) a.findViewById(R.id.mobile_network_chart);
        onOrangeNetworkTextView = (TextView) a.findViewById(R.id.on_orange_network);
        onOrange2GnetworkTextView = (TextView) a.findViewById(R.id.on_orange_2G_network);
        onOrange3GnetworkTextView = (TextView) a.findViewById(R.id.on_orange_3G_network);
        onFreeMobileNetworkTextView = (TextView) a.findViewById(R.id.on_free_mobile_network);
        onFreeMobile3GnetworkTextView = (TextView) a.findViewById(R.id.on_free_mobile_3G_network);
        onFreeMobileFemtocellTextView = (TextView) a.findViewById(R.id.on_free_mobile_femtocell);
        onFreeMobile4GnetworkTextView = (TextView) a.findViewById(R.id.on_free_mobile_4G_network);
        
        // The fields are hidden the first time this fragment is displayed,
        // while statistics data are being loaded.
        statisticsWrapperLayout.setVisibility(View.INVISIBLE);

        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Monitor database updates if the fragment is displayed.
        final ContentResolver cr = getActivity().getContentResolver();
        cr.registerContentObserver(Events.CONTENT_URI, true, contentMonitor);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop monitoring database updates.
        getActivity().getContentResolver().unregisterContentObserver(contentMonitor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mobile_network_chart_fragment, container, false);
    }
    
	@Override
	public Loader<Statistics> onCreateLoader(int arg0, Bundle arg1) {
        return new StatisticsLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Statistics> loader, Statistics s) {
		Log.i(TAG, "Statistics loaded: " + s);

		onOrangeNetworkTextView.setText(s.orangeUsePercent + "%");
        final double[] orange2G3GUsePercents = {
                (double) s.orange2GUsePercent * s.orangeUsePercent / 100d,
                (double) s.orange3GUsePercent * s.orangeUsePercent / 100d
        };
        Statistics.roundPercentagesUpToN(orange2G3GUsePercents, s.orangeUsePercent);
	    onOrange2GnetworkTextView.setText((int) orange2G3GUsePercents[0] + "%");
	    onOrange3GnetworkTextView.setText((int) orange2G3GUsePercents[1] + "%");
	    onFreeMobileNetworkTextView.setText(s.freeMobileUsePercent + "%");
        final double[] freeMobile3G4GUsePercents = {
                (double) s.freeMobile3GUsePercent * s.freeMobileUsePercent / 100d,
                (double) s.freeMobileFemtocellUsePercent * s.freeMobileUsePercent / 100d,
                (double) s.freeMobile4GUsePercent * s.freeMobileUsePercent / 100d
        };
        Statistics.roundPercentagesUpToN(freeMobile3G4GUsePercents, s.freeMobileUsePercent);
	    onFreeMobile3GnetworkTextView.setText((int) freeMobile3G4GUsePercents[0] + "%");
	    onFreeMobileFemtocellTextView.setText((int) freeMobile3G4GUsePercents[1] + "%");
	    onFreeMobile4GnetworkTextView.setText((int) freeMobile3G4GUsePercents[2] + "%");
	    
	    mobileNetworkChart.clear();
        PieChartView.PieChartComponent orange =
        		mobileNetworkChart.new PieChartComponent(R.color.orange_network_color1, R.color.orange_network_color2,
        				s.orangeUsePercent);
        PieChartView.PieChartComponent freeMobile =
        		mobileNetworkChart.new PieChartComponent(R.color.free_mobile_network_color1, R.color.free_mobile_network_color2,
        				s.freeMobileUsePercent);
        mobileNetworkChart.new PieChartComponent(R.color.orange_2G_network_color1, R.color.orange_2G_network_color2,
        				s.orange2GUsePercent, orange);
        mobileNetworkChart.new PieChartComponent(R.color.orange_3G_network_color1, R.color.orange_3G_network_color2,
        				s.orange3GUsePercent, orange);
        mobileNetworkChart.new PieChartComponent(R.color.free_mobile_3G_network_color1, R.color.free_mobile_3G_network_color2,
        				s.freeMobile3GUsePercent, freeMobile);
        mobileNetworkChart.new PieChartComponent(
        		R.color.free_mobile_3G_femtocell_network_color1, R.color.free_mobile_3G_femtocell_network_color2,
				s.freeMobileFemtocellUsePercent, freeMobile);
        mobileNetworkChart.new PieChartComponent(R.color.free_mobile_4G_network_color1, R.color.free_mobile_4G_network_color2,
        				s.freeMobile4GUsePercent, freeMobile);
        
        mobileNetworkChart.addPieChartComponent(freeMobile);
        mobileNetworkChart.addPieChartComponent(orange);

        progressBar.setVisibility(View.INVISIBLE);
        statisticsWrapperLayout.setVisibility(View.VISIBLE);
        statisticsWrapperLayout.invalidate();
        mobileNetworkChart.invalidate();
	}

	@Override
	public void onLoaderReset(Loader<Statistics> loader) {
	}

    private void refresh() {
        if ((isDetached()) || (!isAdded())) { //handle case if fragment is not properly attached to the parent activity
            return;
        }
        if (getLoaderManager().hasRunningLoaders()) {
            if (DEBUG) {
                Log.d(TAG, "Skip statistics refresh: already running");
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "Refresh statistics");
        }
        getLoaderManager().restartLoader(0, null, this);
    }
}
