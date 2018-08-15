package co.loubo.icicle;

import android.app.Activity;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import net.pterodactylus.fcp.SendTextFeed;

public class ComposeMessageActivity extends ActionBarActivity {


    private GlobalState gs;
    private EditText mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);
        this.gs = (GlobalState) getApplication();

        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setResult(Activity.RESULT_OK);

        mMessage = (EditText)findViewById(R.id.message_text);
        TextView recipientList = (TextView) findViewById(R.id.recipient_list);
        recipientList.setText(getResources().getString(R.string.to) + ": " + getIntent().getStringExtra(Constants.MSGRecipientName));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            handleSendMessage();
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleSendMessage(){
        SendTextFeed message = new SendTextFeed(this.gs.getIdentity(),getIntent().getStringExtra(Constants.MSGRecipientIdentity),mMessage.getText().toString());
        try {
            this.gs.getQueue().put(Message.obtain(null, 0, Constants.MsgSendTextFeed, 0, message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
    }
}
