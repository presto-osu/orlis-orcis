package co.loubo.icicle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainViewBroadcastReceiver extends BroadcastReceiver {
	
	private MainActivity activity;


    public MainViewBroadcastReceiver(){
        super();
    }

	public MainViewBroadcastReceiver(MainActivity mainActivity){
		super();
		this.activity = mainActivity;
	}
	
  @Override
  public void onReceive(Context context, Intent intent) {
    if(intent.getAction().equals(Constants.BROADCAST_UPDATE_STATUS)){
    	this.activity.updateStatusView();
    }else if(intent.getAction().equals(Constants.BROADCAST_UPDATE_DOWNLOADS)){
    	this.activity.updateDownloadsView();
    }else if(intent.getAction().equals(Constants.BROADCAST_UPDATE_UPLOADS)){
    	this.activity.updateUploadsView();
    }else if(intent.getAction().equals(Constants.BROADCAST_UPDATE_PEERS)){
    	this.activity.updatePeersView();
    }
  }

} 