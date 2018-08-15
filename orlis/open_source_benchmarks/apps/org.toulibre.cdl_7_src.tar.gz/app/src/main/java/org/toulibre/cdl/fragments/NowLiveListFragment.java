package org.toulibre.cdl.fragments;

import org.toulibre.cdl.R;
import org.toulibre.cdl.db.DatabaseManager;
import org.toulibre.cdl.loaders.BaseLiveLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;

public class NowLiveListFragment extends BaseLiveListFragment {

	@Override
	protected String getEmptyText() {
		return getString(R.string.now_empty);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new NowLiveLoader(getActivity());
	}

	private static class NowLiveLoader extends BaseLiveLoader {

		public NowLiveLoader(Context context) {
			super(context);
		}

		@Override
		protected Cursor getCursor() {
			long now = System.currentTimeMillis();
			return DatabaseManager.getInstance().getEvents(-1L, now, now, false);
		}
	}
}
