package at.bitfire.cadroid;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class FetchFragment extends Fragment implements TextWatcher {
	public static final String
		TAG = "cadroid.FetchFragment",
		KEY_AUTHORITY = "authority";
	
	EditText editAuthority;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.frag_fetch, container, false);
		
		editAuthority = (EditText)v.findViewById(R.id.fetch_host_edit);
		editAuthority.addTextChangedListener(this);
		
		setHasOptionsMenu(true);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((MainActivity)getActivity()).onShowFragment(TAG);
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fetch, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean ok = true;
		try {
			getHostURI();
		} catch(URISyntaxException e) {
			Log.d(TAG, "Invalid URL authority", e);
			ok = false;
		}
				
		MenuItem item = menu.findItem(R.id.fetch);
		item.setEnabled(ok);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.fetch:
			Bundle args = new Bundle(1);
			args.putString(FetchingCertificateFragment.EXTRA_AUTHORITY, editAuthority.getText().toString());
			DialogFragment dialog = new FetchingCertificateFragment();
			dialog.setArguments(args);
		    dialog.show(getFragmentManager(), FetchingCertificateFragment.class.getName());
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	
	private URI getHostURI() throws URISyntaxException {
		return new URI("https", editAuthority.getText().toString(), null, null, null);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.i(TAG, "invalidating options menu");
		getActivity().invalidateOptionsMenu();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
