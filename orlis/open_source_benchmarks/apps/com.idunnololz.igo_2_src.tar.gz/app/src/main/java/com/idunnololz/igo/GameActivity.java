package com.idunnololz.igo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.idunnololz.utils.FileTypes;
import com.idunnololz.utils.LogUtils;

public class GameActivity extends ActionBarActivity {
	private static final String TAG = GameActivity.class.getSimpleName();
	
	public static final String ARGS_BOARD_SIZE = "board_size";
	public static final String ARGS_HANDICAP = "handicaps";
	public static final String ARGS_KOMI = "komi";
	
	private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
//        View view = findViewById(android.R.id.home);
//        view.setPadding(Utils.convertToPixels(this, 5), 0, 0, 0);
        
        Bundle fragArgs = new Bundle();
        if (getIntent().getExtras() != null) {
        	fragArgs.putAll(getIntent().getExtras());
        }

        String filePath = null;
        if (getIntent().getData() != null) {
        	filePath = getIntent().getData().getEncodedPath();
        }
        
        if (filePath != null && filePath.length() > 0) {
        	getIntent().setData(null);
        	
        	// load this file...
        	if (filePath.endsWith(FileTypes.EXTENTION_SGF)) {
        		LogUtils.d(TAG, "Attempting to open file " + filePath);
        		// this is a SGF file... load it!
        		fragArgs.putString(GameFragment.ARGS_SGF, filePath);
        	}
        }
        
        currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (savedInstanceState == null) {
        	currentFragment = new GameFragment();
        	currentFragment.setArguments(fragArgs);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, currentFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
        	return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
    	if (currentFragment instanceof OnBackPressListener) {
    		if (((OnBackPressListener) currentFragment).onBackPressed())
    			return;
    	}
    	
    	super.onBackPressed();
    }
    
    public static interface OnBackPressListener {
    	public boolean onBackPressed();
    }

}
