package com.freezingwind.animereleasenotifier.ui.animelist;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.freezingwind.animereleasenotifier.R;
import com.freezingwind.animereleasenotifier.data.Anime;
import com.freezingwind.animereleasenotifier.updater.AnimeUpdater;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompletedListFragment extends AnimeListFragment {
	static final AnimeUpdater completedAnimeUpdater = new AnimeUpdater(true);

	public CompletedListFragment() {
		// Required empty public constructor
		super();
	}

	@Override
	protected AnimeUpdater getAnimeUpdater() {
		return completedAnimeUpdater;
	}

	@Override
	protected String getCacheKey() {
		return super.getCacheKey() + "Completed";
	}

	@Override
	protected void displayUserNameMissingNotification() {
		// Nothing.
	}
}
