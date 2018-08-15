package at.bitfire.cadroid;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.security.cert.X509Certificate;

public class SelectFragment extends ListFragment {
	public static final String
			TAG = "cadroid.Select";

	private boolean mayContinue = false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity main = (MainActivity)getActivity();
		main.onShowFragment(TAG);

		setHasOptionsMenu(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ConnectionInfo connectionInfo = ((MainActivity)getActivity()).getConnectionInfo();

		// set header/footer views first
		mayContinue = true;

		// host name matching?
		if (!connectionInfo.isHostNameMatching()) {
			mayContinue = false;
			getListView().addFooterView(getActivity().getLayoutInflater().inflate(R.layout.select_invalid_hostname, null), null, false);
		} else {
			try {       // already trusted?
				if (connectionInfo.isTrusted()) {
					mayContinue = false;
					getListView().addFooterView(getActivity().getLayoutInflater().inflate(R.layout.select_already_trusted, null), null, false);
				}
			} catch (Exception e) {
				Log.e(TAG, "Couldn't determine trust status of certificate", e);
				mayContinue = false;
			}
		}

		if (mayContinue) {
			TextView tv = new TextView(view.getContext());
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			tv.setText(R.string.select_text);
			tv.setPadding(0, 0, 0, 10);
			getListView().addHeaderView(tv, null, false);
		}

		// set list adapter after adding header/footer views (required by Android <4.4)
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView v = (TextView)super.getView(position, convertView, parent);
				if (Build.VERSION.SDK_INT >= 17)
					v.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_certificate, 0, 0, 0);
				else
					v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_certificate, 0, 0, 0);
				v.setPadding(0, 10, 0, 10);
				v.setCompoundDrawablePadding(10);
				return v;
			}
		};
		for (X509Certificate cert : ((MainActivity)getActivity()).getConnectionInfo().getCertificates())
			adapter.add(new CertificateInfo(cert).getSubjectName());
		setListAdapter(adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity)activity).onShowFragment(TAG);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mayContinue) {
			MainActivity main = (MainActivity) getActivity();
			main.setIdxSelectedCertificate(position - 1);
			main.showFragment(VerifyFragment.TAG, true);
		}
	}

	@Override
	public void onDestroyView() {
		setListAdapter(null);
		super.onDestroyView();
	}
}
