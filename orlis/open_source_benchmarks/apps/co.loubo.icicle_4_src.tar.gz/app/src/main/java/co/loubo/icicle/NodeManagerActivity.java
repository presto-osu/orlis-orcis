package co.loubo.icicle;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NodeManagerActivity extends ActionBarActivity implements NodeManagerDialog.NodeManagerDialogListener,MyNodeListFragment.OnItemSelectedListener {

	private GlobalState gs;
	private LinearLayout actionBar;
	private ListView list;
    MyNodeListFragment mListFragment;
	private Builder discardDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_node_management);
		this.gs = (GlobalState) getApplication();


		this.list = (ListView)findViewById(android.R.id.list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListFragment = (MyNodeListFragment) getSupportFragmentManager().findFragmentById(R.id.listFragment);
		
		this.actionBar = (LinearLayout)findViewById(R.id.node_management_action_bar);
		ImageButton addButton = (ImageButton)this.actionBar.findViewById(R.id.node_add);
		ImageButton acceptButton = (ImageButton)this.actionBar.findViewById(R.id.node_accept);
		ImageButton editButton = (ImageButton)this.actionBar.findViewById(R.id.node_edit);
		ImageButton shareButton = (ImageButton)this.actionBar.findViewById(R.id.node_share);
		ImageButton discardButton = (ImageButton)this.actionBar.findViewById(R.id.node_discard);
		
		addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	DialogFragment newFragment = NodeManagerDialog.newInstance(R.string.node_add, new LocalNode(),false);
				newFragment.show(getFragmentManager(), "dialog");
            }
        });
		
		acceptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	int selected = list.getCheckedItemPosition();
            	//System.out.println(">>>getCheckedItemPosition():"+selected);
    			if(selected == AdapterView.INVALID_POSITION){
    				return;
    			}
    			if(gs.getActiveLocalNodeIndex()!=selected){
    				gs.setActiveLocalNodeIndex(selected);
    			}
                mListFragment.notifyDataSetChanged();
            }
        });

		editButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int selected = list.getCheckedItemPosition();
				if(selected == AdapterView.INVALID_POSITION){
					return;
				}
				DialogFragment newFragment = NodeManagerDialog.newInstance(R.string.node_edit, gs.getLocalNodeList().get(list.getCheckedItemPosition()),true);
				newFragment.show(getFragmentManager(), "dialog");
			}
		});
		
		shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	int selected = list.getCheckedItemPosition();
                if(selected == AdapterView.INVALID_POSITION){
                    return;
                }
                Intent intent = new Intent(gs, OpenReferenceActivity.class);
                intent.putExtra(Constants.LOCAL_NODE_SELECTED,selected);
                startActivityForResult(intent, Constants.Activity_Reference);
            }
        });
		
		discardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int selected = list.getCheckedItemPosition();
				if(selected == AdapterView.INVALID_POSITION){
					return;
				}
				discardDialog.show();
			}
		});
		
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
				
		        int currentActive = gs.getActiveLocalNodeIndex();
		        if(currentActive == selected){
		        	//System.out.println(">>>deleted active node");
		        	gs.setActiveLocalNodeIndex(0);
		        }else if(currentActive > selected){
		        	//System.out.println(">>>deleted node less than active node");
		        	gs.setActiveLocalNodeIndex(currentActive-1);
		        }

                mListFragment.notifyDataSetChanged();
		        gs.savePreferences();
		        redrawNodeManagementActionBar();
	        }
	     })
	     .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        }
		     });
		
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

	public void doPositiveClick(LocalNode n, boolean edit) {
		if(edit){
		    int selected = list.getCheckedItemPosition();
			if(selected == AdapterView.INVALID_POSITION){
				return;
			}

            mListFragment.getValues().set(selected, n);
		}else{
            mListFragment.getValues().add(n);
		}
        mListFragment.notifyDataSetChanged();
        gs.savePreferences();
	}

	public void doNegativeClick() {
	    // Do nothing
	}

	public void redrawNodeManagementActionBar(){
		int selected = list.getCheckedItemPosition();
		if(selected == AdapterView.INVALID_POSITION){
			actionBar.findViewById(R.id.node_add).setVisibility(View.VISIBLE);
			actionBar.findViewById(R.id.node_accept).setVisibility(View.INVISIBLE);
			actionBar.findViewById(R.id.node_edit).setVisibility(View.INVISIBLE);
			actionBar.findViewById(R.id.node_discard).setVisibility(View.INVISIBLE);
			actionBar.findViewById(R.id.node_share).setVisibility(View.INVISIBLE);
		}else{
            String nodeRef = this.gs.getLocalNodeList().get(selected).getNodeReference();
            if(nodeRef == null || nodeRef.equals("")){
                actionBar.findViewById(R.id.node_share).setVisibility(View.INVISIBLE);
            }else{
                actionBar.findViewById(R.id.node_share).setVisibility(View.VISIBLE);
            }
			actionBar.findViewById(R.id.node_add).setVisibility(View.VISIBLE);
			actionBar.findViewById(R.id.node_accept).setVisibility(View.VISIBLE);
			actionBar.findViewById(R.id.node_edit).setVisibility(View.VISIBLE);
			actionBar.findViewById(R.id.node_discard).setVisibility(View.VISIBLE);
		}
	}
	



}
