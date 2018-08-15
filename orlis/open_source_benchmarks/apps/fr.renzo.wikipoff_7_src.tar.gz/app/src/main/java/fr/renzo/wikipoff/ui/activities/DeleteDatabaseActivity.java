package fr.renzo.wikipoff.ui.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.StorageUtils;

public class DeleteDatabaseActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "DeleteDatabaseActivity";
	private ArrayList<String> dbtodelete;
	private int dbtodeleteposition;
	private SharedPreferences config;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		config = PreferenceManager.getDefaultSharedPreferences(this);

		Intent i =getIntent();
		if (i.getExtras()!= null) {
			this.dbtodelete = i.getStringArrayListExtra("dbtodelete");
			this.dbtodeleteposition = i.getIntExtra("dbtodeleteposition", -1);
		}

		setTitle(getString(R.string.message_warning));
		setContentView(R.layout.alert_dialog);
		TextView msg = (TextView) findViewById(R.id.message);
		String txtmessage = getString(R.string.message_delete_db,dbtodelete.get(0));
		msg.setText(txtmessage);
		setResult(-1); //default is "we did nothing"
		Button bno = (Button) findViewById(R.id.cancelbutton);
		bno.setText(getString(R.string.no));
		bno.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Button byes = (Button) findViewById(R.id.okbutton);
		byes.setText(getString(R.string.yes));
		byes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for(String db : dbtodelete)
					deleteDb(db);
				setResult(dbtodeleteposition);
				config.edit().putBoolean(getString(R.string.config_key_should_update_db), true).commit();
				finish();
			}
		});

	}

	public void deleteDb(String dbtodelete) {
		SharedPreferences config= PreferenceManager.getDefaultSharedPreferences(this);
		String storage = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage(this));
		File root=new File(storage,this.getString(R.string.DBDir));
		File db=new File(root,dbtodelete);
		if (db.exists()) {
			db.delete();
		}

	}
}
