package fr.renzo.wikipoff.ui.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import fr.renzo.wikipoff.DownloadUtils;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.Wiki;
import fr.renzo.wikipoff.WikiDBFile;

public class WikiAvailableActivity extends Activity{
	@SuppressWarnings("unused")
	private static final String TAG = "WikiInstalledActivity";
	public static final String DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS";
	private Wiki wiki;

	
	private String storage;
	private ArrayList<String> urls_to_dl = new ArrayList<String>() ;
	private Button downloadbutton;
	private Button stopdownloadbutton;
	private DownloadCompleteReceiver downloadComplete;
	public boolean installed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_available_wiki);
		Intent intent = getIntent();
		this.storage = intent.getStringExtra("storage");
		this.wiki = (Wiki) intent.getExtras().getSerializable("wiki");
		this.installed = intent.getBooleanExtra("installed", false);
		// WARNING Wiki needs a context, it was lost on serializing...
		wiki.setContext(this);
		
		setTitle(wiki.getType()+" - "+wiki.getLangcode());
		setViews();

	}

	@Override
	protected void onStart() {
		downloadComplete = new DownloadCompleteReceiver();
		registerReceiver(downloadComplete, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		unregisterReceiver(downloadComplete);
		super.onStop();
	}

	private void setViews() {
		setIcon();
		setLanguage();
		setGenDate();
		setSource();
		setAuthor();
		setType();
		setSize();
		setFiles();
		setDowload();
		setStopDowload();
		if (DownloadUtils.isInCurrentDownloads(wiki,this)) {
			stopdownloadbutton.setVisibility(View.VISIBLE);
			downloadbutton.setVisibility(View.GONE);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == WikiManagerActivity.REQUEST_DELETE_CODE) {
			setResult(resultCode);
			finish();
		} 
	}

	private void setDowload() {
		downloadbutton = (Button) findViewById(R.id.wikiDownloadButton);
		downloadbutton.setOnClickListener(new downloadOnClickListener());
	}

	private void setStopDowload() {
		stopdownloadbutton = (Button) findViewById(R.id.wikiStopDownloadButton);
		stopdownloadbutton.setOnClickListener(new stopDownloadOnClickListener());
	}

	private void setFiles() {
		TextView d = (TextView) findViewById(R.id.wikiFilesTextView);
		d.setText(wiki.getFilenamesAsString());
	}

	private void setSize() {
		TextView d = (TextView) findViewById(R.id.wikiSizeTextView);
		d.setText(wiki.getSizeReadable(true));
	}

	private void setType() {
		TextView d = (TextView) findViewById(R.id.wikiTypeTextView);
		d.setText(wiki.getType());
	}

	private void setLanguage() {
		TextView d = (TextView) findViewById(R.id.wikiLanguageTextView);
		d.setText(wiki.getLanglocal()+" / "+wiki.getLangcode());
	}

	private void setAuthor() {
		TextView d = (TextView) findViewById(R.id.wikiAuthorTextView);
		d.setText(wiki.getAuthor());
	}

	private void setSource() {
		TextView d = (TextView) findViewById(R.id.wikiSourceTextView);
		d.setText(wiki.getSource());
	}

	private void setGenDate() {
		TextView d = (TextView) findViewById(R.id.wikiGenDateTextView);
		d.setText(wiki.getGendateAsString());
	}

	private void setIcon() {
		ImageView iconview = (ImageView) findViewById(R.id.wikiIcon);
		if (wiki.hasIconURL()){
			// TODO
		} else {
			AssetManager am = getAssets();
			try {
				InputStream in = am.open("icons/wiki-default-icon.png");
				iconview.setImageBitmap(BitmapFactory.decodeStream(in));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	private void download() {
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		int nb_of_files= wiki.getFilenamesAsString().split("\\+").length;
		String msg = "";

		if (wifi.isConnected()) {
			if (nb_of_files>1) {
				msg = getString(R.string.message_validate_download_n,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true),
						nb_of_files);
			} else {
				msg = getString(R.string.message_validate_download,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true));
			} 
		}else {
			if (nb_of_files>1) {
				msg = getString(R.string.message_validate_download_n_nowifi,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true),
						nb_of_files);
			} else {
				msg = getString(R.string.message_validate_download_nowifi,
						wiki.getType(),
						wiki.getLanglocal(),
						wiki.getSizeReadable(true));
			} 

		}
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.message_warning))
		.setMessage(msg)
		.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				urls_to_dl.clear();
			}
		})
		.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				do_download();
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
	}

	private void do_download() { 
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		for(WikiDBFile wdbf : wiki.getDBFiles()) {
			Request request = new Request(
					Uri.parse(wdbf.getUrl()));
			String destinationPath = new File(
					new File(storage,getString(R.string.DBDir)),
					wdbf.getFilename()).getAbsolutePath();
			request.setDestinationUri(Uri.parse("file://"+destinationPath));
			request.setTitle(wdbf.getFilename());
			dm.enqueue(request);
		}
		downloadbutton.setVisibility(View.GONE);
		stopdownloadbutton.setVisibility(View.VISIBLE);

	}
	
	private void stop_download() { 
		DownloadUtils.delete(wiki,this);
		downloadbutton.setVisibility(View.VISIBLE);
		stopdownloadbutton.setVisibility(View.GONE);
	}

	public class DownloadCompleteReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
				long downloadId = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				Query query = new Query();

				query.setFilterById(downloadId);
				Cursor c = dm.query(query);
				while (c.moveToNext()) {
					int columnIndex = c
							.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c
							.getInt(columnIndex)) {
							// We've finished all downloads;
							stopdownloadbutton.setVisibility(View.GONE);					
					}
				}
			}
		}
	}

	public class downloadOnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {

			if (!installed) {
				// We can't file the files, so it's not there, we can d/l
				download();
			}
			// TODO check if newer wiki is installed to prevent overwriting
		}
	}

	public class stopDownloadOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {

			new AlertDialog.Builder(WikiAvailableActivity.this)
			.setTitle(getString(R.string.message_warning))
			.setMessage(getString(R.string.message_stop_download))
			.setNegativeButton(getString(R.string.no), null )
			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					stop_download();
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
		}

	};
}
