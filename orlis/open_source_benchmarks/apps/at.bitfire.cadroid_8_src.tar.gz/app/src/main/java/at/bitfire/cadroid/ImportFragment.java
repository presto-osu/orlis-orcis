package at.bitfire.cadroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.cert.X509Certificate;

public class ImportFragment extends Fragment {
	public final static String TAG = "cadroid.ImportFragment";

	ConnectionInfo connectionInfo;
	CertificateInfo certificateInfo;
	TextView tvStatus;
	boolean imported;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_import, container, false);
		
		TextView textIntro = (TextView)v.findViewById(R.id.import_text);
		textIntro.setText(Html.fromHtml(getString(R.string.import_text)));
		textIntro.setMovementMethod(LinkMovementMethod.getInstance());
		
		tvStatus = (TextView)v.findViewById(R.id.import_status);
		
		setHasOptionsMenu(true);
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		MainActivity main = (MainActivity)activity;
		main.onShowFragment(TAG);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		MainActivity main = (MainActivity)getActivity();
		connectionInfo = main.getConnectionInfo();
		X509Certificate cert = connectionInfo.getCertificates()[main.getIdxSelectedCertificate()];
		certificateInfo = new CertificateInfo(cert);

		imported = false;
		try {
			imported = connectionInfo.isTrusted();
		} catch (Exception e) {
			Log.e(TAG, "Couldn't determine trust status", e);
			imported = false;
		}
		tvStatus.setText(getString(
				imported ? R.string.certificate_imported : R.string.certificate_not_yet_imported
		));
		if (Build.VERSION.SDK_INT >= 17)
			tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(imported ? R.drawable.ic_action_accept : R.drawable.ic_action_warning, 0, 0, 0);
		else
			tvStatus.setCompoundDrawablesWithIntrinsicBounds(imported ? R.drawable.ic_action_accept : R.drawable.ic_action_warning, 0, 0, 0);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.import_cert, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.open_settings:
			startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
