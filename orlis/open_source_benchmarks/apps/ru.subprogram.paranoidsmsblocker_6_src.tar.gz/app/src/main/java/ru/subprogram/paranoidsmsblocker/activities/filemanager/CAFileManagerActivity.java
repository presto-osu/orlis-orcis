package ru.subprogram.paranoidsmsblocker.activities.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import ru.subprogram.paranoidsmsblocker.R;

public class CAFileManagerActivity extends AppCompatActivity
		implements IAFileManagerFragmentObserver {

	private static final String MANAGER_TAG = "MANAGER_TAG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_holder);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle args = getIntent().getExtras();
		if (args == null)
			args = new Bundle();
		args.putString(CAFileManagerFragment.ARG_ACTION,
			getIntent().getAction());
		args.putString(CAFileManagerFragment.ARG_TYPE,
			getIntent().getType());

		if (savedInstanceState == null) {
			CAFileManagerFragment fileManager = new CAFileManagerFragment();
			fileManager.setArguments(args);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_placeholder, fileManager, MANAGER_TAG);
			ft.commit();
		}
	}

	@Override
	public void finishActivity(int result, Intent resultIntent) {
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	@Override
	public void onBackPressed() {
		Fragment manager = getSupportFragmentManager().findFragmentByTag(MANAGER_TAG);
		if (manager != null && manager instanceof CAFileManagerFragment) {
			if (!((CAFileManagerFragment) manager).onBackPressed()) {
				super.onBackPressed();
			}
		}
	}

}
