package co.loubo.icicle;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.pterodactylus.fcp.AddPeer;

public class FriendNodeManagerActivity extends ActionBarActivity implements FriendNodeManagerDialog.NodeManagerDialogListener,FriendNodeListFragment.OnItemSelectedListener {

	private GlobalState gs;
	private ListView list;
    private FriendNodeListFragment mListFragment;
    private Menu menu;
    private Builder discardDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_node_management);
		this.gs = (GlobalState) getApplication();


		this.list = (ListView)findViewById(android.R.id.list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListFragment = (FriendNodeListFragment) getSupportFragmentManager().findFragmentById(R.id.listFragment);

        discardDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.node_discard)
                .setMessage(R.string.node_discard_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int selected = list.getCheckedItemPosition();
                        if(selected == AdapterView.INVALID_POSITION){
                            return;
                        }
                        list.setItemChecked(selected,false);
                        mListFragment.getValues().remove(selected);
                        mListFragment.notifyDataSetChanged();
                        gs.savePreferences();
                        redrawFriendNodeManagement();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });


	}

    @Override
    protected void onResume() {
        super.onResume();
        mListFragment.notifyDataSetChanged();
        redrawFriendNodeManagement();
    }

    @Override
    protected void onStart() {
        this.gs.registerActivity(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        this.gs.unregisterActivity(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_node_manager_menu, menu);
        redrawFriendNodeManagement();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_upload:
                handleNodeUpload();
                return true;
            case R.id.action_edit:
                handleNodeEdit();
                return true;
            case R.id.action_delete:
                handleNodeDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void handleNodeUpload() {
        try {
            AddPeer aPeer = this.gs.processStringIntoNode(gs.getFriendNodes().get(list.getCheckedItemPosition()).getNodeReference());
            this.gs.getQueue().put(Message.obtain(null, 0, Constants.MsgAddNoderef, 0,aPeer));
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.addingFriendNodeRef), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleNodeEdit(){
        DialogFragment newFragment = FriendNodeManagerDialog.newInstance(R.string.node_edit, gs.getFriendNodes().get(list.getCheckedItemPosition()));
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void handleNodeDelete(){
        discardDialog.show();
    }

	public void redrawFriendNodeManagement(){
        if(this.menu == null) return;
		int selected = list.getCheckedItemPosition();
		if(selected == AdapterView.INVALID_POSITION){
            this.menu.findItem(R.id.action_upload).setVisible(false);
            this.menu.findItem(R.id.action_edit).setVisible(false);
            this.menu.findItem(R.id.action_delete).setVisible(false);
		}else{
            this.menu.findItem(R.id.action_upload).setVisible(true);
            this.menu.findItem(R.id.action_edit).setVisible(true);
            this.menu.findItem(R.id.action_delete).setVisible(true);
		}
	}


    @Override
    public void doPositiveClick(FriendNode friendNode) {
        int selected = list.getCheckedItemPosition();
        if(selected == AdapterView.INVALID_POSITION){
            return;
        }

        mListFragment.getValues().set(selected, friendNode);

        mListFragment.notifyDataSetChanged();
        gs.savePreferences();
    }

    @Override
    public void doNegativeClick() {

    }
}
