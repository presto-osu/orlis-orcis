package com.example.root.analyticaltranslator;

/**
 * Created by root on 25.05.15.
 */
        import android.app.Activity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.widget.Toast;

public class AndroidMenusActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.action_settings:
                // Single menu item is selected do something
                // Ex: launching new activity/screen or show alert message
                Toast.makeText(AndroidMenusActivity.this, "Bookmark is Selected", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.about:
                Toast.makeText(AndroidMenusActivity.this, "Save is Selected", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.help:
                Toast.makeText(AndroidMenusActivity.this, "Search is Selected", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}