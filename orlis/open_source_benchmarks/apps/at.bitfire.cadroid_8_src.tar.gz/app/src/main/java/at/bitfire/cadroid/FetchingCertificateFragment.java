package at.bitfire.cadroid;

import java.net.URL;

import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class FetchingCertificateFragment extends DialogFragment implements LoaderCallbacks<ConnectionInfo> {
	private final static String TAG = "cadroid.Fetch";
	final static String EXTRA_AUTHORITY = "authority";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
		setCancelable(false);
		
		Loader<ConnectionInfo> loader = getLoaderManager().initLoader(0, getArguments(), this);
		if (savedInstanceState == null)		// http://code.google.com/p/android/issues/detail?id=14944
			loader.forceLoad();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_query_server, container, false);
	}

	
	// LoaderCallbacks for LoaderManager

	@Override
	public Loader<ConnectionInfo> onCreateLoader(int id, Bundle args) {
		return new CertificateInfoLoader(getActivity(), args.getString(EXTRA_AUTHORITY));
	}

	@Override
	public void onLoadFinished(Loader<ConnectionInfo> loader, ConnectionInfo info) {
		if (info.getException() == null) {
			MainActivity main = (MainActivity)getActivity();
			main.setConnectionInfo(info);
			main.showFragment(SelectFragment.TAG, true);
		} else {
			Log.e(TAG, "Couldn't fetch certificate", info.getException());
			Toast.makeText(getActivity(), info.getException().getMessage(), Toast.LENGTH_LONG).show();
		}
		
		getDialog().dismiss();
	}
	
	@Override
	public void onLoaderReset(Loader<ConnectionInfo> loader) {
	}
	
	
	// Loader
	
	private static class CertificateInfoLoader extends AsyncTaskLoader<ConnectionInfo> {
		String authority;
		
		public CertificateInfoLoader(Context context, String authority) {
			super(context);
			this.authority = authority;
		}

		@Override
		public ConnectionInfo loadInBackground() {
			try {
				// either return result with certificate info …
				return ConnectionInfo.fetch(new URL("https://" + authority));
			} catch (Exception e) {
				// … or return the exception
				return new ConnectionInfo(e);
			}
		}
		
	}

}
