package at.linuxtage.companion.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import at.linuxtage.companion.R;
import at.linuxtage.companion.activities.EventDetailsActivity;
import at.linuxtage.companion.adapters.EventsAdapter;
import at.linuxtage.companion.db.DatabaseManager;
import at.linuxtage.companion.loaders.SimpleCursorLoader;
import at.linuxtage.companion.model.Event;

public class SearchResultListFragment extends SmoothListFragment implements LoaderCallbacks<Cursor> {

	private static final int EVENTS_LOADER_ID = 1;
	private static final String ARG_QUERY = "query";

	private EventsAdapter adapter;

	public static SearchResultListFragment newInstance(String query) {
		SearchResultListFragment f = new SearchResultListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_QUERY, query);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EventsAdapter(getActivity());
		setListAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText(getString(R.string.no_search_result));
		setListShown(false);

		getLoaderManager().initLoader(EVENTS_LOADER_ID, null, this);
	}

	private static class TextSearchLoader extends SimpleCursorLoader {

		private final String query;

		public TextSearchLoader(Context context, String query) {
			super(context);
			this.query = query;
		}

		@Override
		protected Cursor getCursor() {
			return DatabaseManager.getInstance().getSearchResults(query);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String query = getArguments().getString(ARG_QUERY);
		return new TextSearchLoader(getActivity(), query);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null) {
			adapter.swapCursor(data);
		}

		setListShown(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Event event = adapter.getItem(position);
		Intent intent = new Intent(getActivity(), EventDetailsActivity.class).putExtra(EventDetailsActivity.EXTRA_EVENT, event);
		startActivity(intent);
	}
}
