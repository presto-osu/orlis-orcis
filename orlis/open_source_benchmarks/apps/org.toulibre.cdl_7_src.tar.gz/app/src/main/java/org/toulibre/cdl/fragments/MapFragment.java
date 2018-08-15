package org.toulibre.cdl.fragments;

import org.toulibre.cdl.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

public class MapFragment extends Fragment {

    private static final double DESTINATION_LATITUDE = 43.602458;

    private static final double DESTINATION_LONGITUDE = 1.4557992;

    private static final String DESTINATION_NAME = "ENSEEIHT";

    private static final String NATIVE_URI = "google.navigation:q=%1$f,%2$f";

    private static final String WEB_URI = "http://maps.google.com/maps?f=d&daddr=%1$f,%2$f(%3$s)&dirflg=r";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directions:
                launchDirections();
                return true;
        }
        return false;
    }

    private void launchDirections() {
        Uri uri;
        Intent intent;

        uri = Uri.parse(String.format(Locale.US, NATIVE_URI, DESTINATION_LATITUDE, DESTINATION_LONGITUDE));
        intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
            intent.setPackage(null);
            intent.setData(Uri.parse(String.format(Locale.US, WEB_URI, DESTINATION_LATITUDE,
                    DESTINATION_LONGITUDE, DESTINATION_NAME)));
        }

        startActivity(intent);
    }
}
