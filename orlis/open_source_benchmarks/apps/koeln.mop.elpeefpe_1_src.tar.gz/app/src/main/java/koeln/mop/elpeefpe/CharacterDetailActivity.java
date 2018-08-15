package koeln.mop.elpeefpe;

        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Bundle;
        import android.support.design.widget.FloatingActionButton;
        import android.support.design.widget.Snackbar;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.widget.Toolbar;
        import android.view.Menu;
        import android.view.View;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.app.ActionBar;
        import android.support.v4.app.NavUtils;
        import android.view.MenuItem;

/**
 * An activity representing a single Character detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CharacterListActivity}.
 */
public class CharacterDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(CharacterDetailFragment.ARG_CHARACTER_ID,
                    getIntent().getIntExtra(CharacterDetailFragment.ARG_CHARACTER_ID, 0));
            CharacterDetailFragment fragment = new CharacterDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.character_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, CharacterListActivity.class));
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, CharacterEditActivity.class);
            intent.putExtra(CharacterDetailFragment.ARG_CHARACTER_ID, getIntent().getIntExtra(CharacterDetailFragment.ARG_CHARACTER_ID, 0));

            this.startActivity(intent);
        } else if (id == R.id.action_delete) {
            DeleteClickListener deleteClickListener = new DeleteClickListener(this, getIntent().getIntExtra(CharacterDetailFragment.ARG_CHARACTER_ID, 0));

            new AlertDialog.Builder(this)
                    .setTitle("Wirklich löschen?")
                    .setPositiveButton(android.R.string.yes, deleteClickListener)
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }
}
