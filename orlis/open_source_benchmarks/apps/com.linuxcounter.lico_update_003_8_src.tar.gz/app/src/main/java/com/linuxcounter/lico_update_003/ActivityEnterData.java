package com.linuxcounter.lico_update_003;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews.ActionException;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ActivityEnterData extends Activity implements OnClickListener {

	final String TAG = "MyDebugOutput";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enterdata);
		Button button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(this);

		Log.i(TAG, "starting enterData...");

		String filename = ".linuxcounter";
		String filepath = Environment.getExternalStorageDirectory()
				+ "/data/com.linuxcounter.lico_update_003";
		File readFile = new File(filepath, filename);
		String load = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(readFile)), 1000);
			load = reader.readLine();
			reader.close();
			String[] toks = load.split(" ");
			EditText myText1 = (EditText) this.findViewById(R.id.editText1);
			myText1.setText(toks[0]);
			EditText myText2 = (EditText) this.findViewById(R.id.editText2);
			myText2.setText(toks[1]);
		} catch (Exception e1) {
			// Do nothing
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		EditText myText1 = (EditText) this.findViewById(R.id.editText1);
		String machine_id = myText1.getText().toString();
		EditText myText2 = (EditText) this.findViewById(R.id.editText2);
		String machine_updatekey = myText2.getText().toString();

		SaveToFile(machine_id + " " + machine_updatekey + "\n");

		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.isConnected();
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnected();

		Log.i(TAG, "Connection 3G: " + is3g + " | Connection wifi: " + isWifi);
		if (!is3g && !isWifi) {
			Toast.makeText(getApplicationContext(),
					"Please make sure, your network connection is ON ",
					Toast.LENGTH_LONG).show();
		} else {
			startActivity(new Intent(this, getSysInfo.class));
			finish();
		}
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	public void SaveToFile(String thisstring) {
		final String TAG = "MyDebugOutput";
		String filename = ".linuxcounter";
		String filepath = Environment.getExternalStorageDirectory()
				+ "/data/com.linuxcounter.lico_update_003";
		File file = new File(filepath);
		file.mkdirs();
		File writeFile = new File(filepath, filename);
		if (thisstring != "") {
			try {
				FileWriter filewriter = new FileWriter(writeFile);
				BufferedWriter out = new BufferedWriter(filewriter);
				out.write(thisstring + "\n");
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "Could not write file " + e.getMessage());
			}
		}
	}
}
