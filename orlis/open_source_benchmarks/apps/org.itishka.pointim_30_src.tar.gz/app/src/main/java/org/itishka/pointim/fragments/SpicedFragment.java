package org.itishka.pointim.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.octo.android.robospice.SpiceManager;

import org.itishka.pointim.network.PointService;
import org.itishka.pointim.utils.ImageSearchHelper;

/**
 * Created by Tishka17 on 27.02.2015.
 */
public class SpicedFragment extends Fragment {
    private SpiceManager mSpiceManager = new SpiceManager(PointService.class);
    private boolean mAutoload = true;

    protected SpiceManager getSpiceManager() {
        return mSpiceManager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mAutoload = false;
    }

    protected boolean shouldAutoload() {
        return mAutoload;
    }

    @Override
    public void onStart() {
        super.onStart();
        getSpiceManager().start(getActivity());
    }

    @Override
    public void onStop() {
        if (getSpiceManager().isStarted()) {
            getSpiceManager().shouldStop();
        }
        ImageSearchHelper.saveCache(getActivity());
        super.onStop();
    }
}
