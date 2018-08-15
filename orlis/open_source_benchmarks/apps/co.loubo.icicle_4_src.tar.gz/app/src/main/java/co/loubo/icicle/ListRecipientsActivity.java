package co.loubo.icicle;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.pterodactylus.fcp.Peer;

public class ListRecipientsActivity extends ActionBarActivity {

    private GlobalState gs;
    private ListView list;
    private ListRecipientsFragment mListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recipients);

        this.gs = (GlobalState) getApplication();
        this.list = (ListView)findViewById(android.R.id.list);
        mListFragment = (ListRecipientsFragment) getSupportFragmentManager().findFragmentById(R.id.listFragment);
        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setResult(Activity.RESULT_OK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_recipients, menu);
        mListFragment.setMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_forward) {
            handleNext();
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleNext() {
        Intent intent = new Intent(this, ComposeMessageActivity.class);
        Peer p = mListFragment.getSelectedPeer();
        if (p != null){
            intent.putExtra(Constants.MSGRecipientName, p.getMyName());
            intent.putExtra(Constants.MSGRecipientIdentity,p.getIdentity());
            startActivity(intent);
            finish();
        }
    }
}
