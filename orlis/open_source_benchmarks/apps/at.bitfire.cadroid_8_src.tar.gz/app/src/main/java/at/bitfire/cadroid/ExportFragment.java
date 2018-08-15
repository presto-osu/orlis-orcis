package at.bitfire.cadroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import lombok.Cleanup;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ExportFragment extends Fragment {
	public final static String TAG = "cadroid.ExportFragment";
	
	X509Certificate certificate;
	boolean exported;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_export, container, false);
		setHasOptionsMenu(true);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		MainActivity main = (MainActivity)getActivity();
		main.onShowFragment(TAG);
		
		TextView tvExportStatus = (TextView)getView().findViewById(R.id.export_status),
				 tvExportResult = (TextView)getView().findViewById(R.id.export_result);
		
		try {
			X509Certificate cert = main.getConnectionInfo().getCertificates()[main.getIdxSelectedCertificate()];
			String outputFile = exportCertificate(cert);
			
			exported = true;
			tvExportStatus.setText(R.string.export_successful);
			tvExportResult.setText(outputFile);
		} catch(Exception e) {
			exported = false;
			tvExportStatus.setText(R.string.export_failed);
			tvExportResult.setText(e.getMessage());
		}
	}

	
	// options menu
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.simple_next, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.next).setEnabled(exported);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.next:
			((MainActivity)getActivity()).showFragment(ImportFragment.TAG, true);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	
	// private methods
	
	private String exportCertificate(X509Certificate certificate) throws CertificateEncodingException, IOException {
		File file = new File(Environment.getExternalStorageDirectory(), certificate.getSerialNumber().toString(16) + ".crt");
		@Cleanup FileOutputStream fos = new FileOutputStream(file);
		fos.write(certificate.getEncoded());
		return file.getAbsolutePath();
	}

}
